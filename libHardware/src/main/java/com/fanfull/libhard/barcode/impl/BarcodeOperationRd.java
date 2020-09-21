package com.fanfull.libhard.barcode.impl;

import android.content.Context;
import android.os.SystemClock;
import com.apkfuns.logutils.LogUtils;
import com.fanfull.libhard.barcode.AbsBarcodeOperation;
import com.fanfull.libhard.barcode.IBarcodeListener;
import com.rd.barcodeScanTest.NewApiService;
import com.rd.barcodeScanTest.ScanApi;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import org.orsoul.baselib.util.ArrayUtils;

/**
 * 雨滴二维码扫描 实现类
 */
public class BarcodeOperationRd extends AbsBarcodeOperation {

  private Context context;
  private ScanApi scanApi;
  private ScanApiDecodeCallback callback;

  public BarcodeOperationRd(Context context) {
    this.context = context;
    // TODO: 2020/6/23 两种 API
    this.scanApi = new NewApiService();
    this.callback = new ScanApiDecodeCallback();
    //        this.scanApi = new NewApiBroadcast();
  }

  @Override
  public synchronized boolean open() {
    if (isOpen) {
      return true;
    }
    init(context);
    SystemClock.sleep(500);
    powerOn();
    SystemClock.sleep(5000);
    scanApi.setDecodeCallback(callback);
    if (barcodeListener != null) {
      barcodeListener.onOpen(true);
    }
    isOpen = true;
    return true;
  }

  @Override
  public void release() {
    cancelScan();
    //        powerOff();
    scanApi.setDecodeCallback(null);
    uninit();
  }

  @Override
  public void init(Context context) {
    scanApi.init(context);
  }

  @Override
  public void uninit() {
    if (isOpen) {
      scanApi.deInit();
      isOpen = false;
    }
  }

  private Timer scanTimer;

  private void cancelTimer() {
    if (scanTimer != null) {
      scanTimer.cancel();
      scanTimer = null;
    }
  }

  @Override
  public void scanAsync(long timeout) {
    if (isScanning) {
      return;
    }
    scanApi.doScan();
    isScanning = true;
    if (barcodeListener != null) {
      barcodeListener.onScan();
    }
    if (timeout < 500) {
      timeout = 500;
    } else if (10_000 < timeout) {
      timeout = 10_000;
    }
    scanTimer = new Timer();
    scanTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        cancelScan();
      }
    }, timeout);
  }

  @Override
  public synchronized void scanAsync() {
    scanAsync(5000);
  }

  @Override
  public synchronized void cancelScan() {
    scanApi.cancelScan();
    cancelTimer();
    if (isScanning && barcodeListener != null) {
      barcodeListener.onStopScan();
    }
    isScanning = false;
  }

  @Override
  public void setBarcodeListener(IBarcodeListener barcodeListener) {
    this.barcodeListener = barcodeListener;
    scanApi.setDecodeCallback(callback);
  }

  @Override
  public void powerOn() {
    scanApi.powerOn();
  }

  @Override
  public void powerOff() {
    scanApi.powerOff();
    cancelScan();
    isOpen = false;
  }

  private class ScanApiDecodeCallback implements ScanApi.DecodeCallback {

    @Override
    public void onDecodeComplete(int symbology, int length, byte[] data, ScanApi api) {
      LogUtils.v("symbology %s dataLen:%s  %s", symbology, data.length,
          ArrayUtils.bytes2HexString(data, 0, length));
      cancelTimer();
      isScanning = false;
      if (barcodeListener != null) {
        barcodeListener.onReceiveData(Arrays.copyOf(data, length));
      }
    }

    @Override
    public void onEvent(int event, int info, byte[] data, ScanApi api) {
      LogUtils.v("event:%s info:%s %s", event, info, ArrayUtils.bytes2HexString(data));
    }
  }
}
