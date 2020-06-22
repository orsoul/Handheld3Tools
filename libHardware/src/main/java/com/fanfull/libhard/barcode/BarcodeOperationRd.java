package com.fanfull.libhard.barcode;


import android.content.Context;
import android.os.SystemClock;

import com.apkfuns.logutils.LogUtils;
import com.hardware.Hardware;
import com.rd.barcodeScanTest.NewApiBroadcast;
import com.rd.barcodeScanTest.ScanApi;

import org.orsoul.baselib.util.ArrayUtils;

import java.util.Arrays;

public class BarcodeOperationRd implements IBarcodeOperation {

    private ScanApi scanApi;
    private IBarcodeListener barcodeListener;
    private boolean isOpen;
    private boolean isScanning;

    public BarcodeOperationRd() {
        //        this.scanApi = new NewApiService();
        this.scanApi = new NewApiBroadcast();
        scanApi.setDecodeCallback(new ScanApi.DecodeCallback() {
            @Override
            public void onDecodeComplete(int symbology, int length, byte[] data, ScanApi api) {
                LogUtils.d("symbology %s dataLen:%s  %s", symbology, data.length,
                           ArrayUtils.bytes2HexString(data, 0, length));
                //jQqgP/-Qt(<aq1UY'8d*
                //053101001000002010109031317002911101
                //mHKb`#l7r=<aq1UY'8d*
                //053101001000009010109031317002911101
                //+^JRwAk7#)<aq1UY'8d*
                //053101001000006010109031317002911101
                if (IBarcodeOperation.BARCODE_DATA_LEN == length) {// 读到正确的数据
                    byte[] barcodeBuff = new byte[38];
                    System.arraycopy(data, 0, barcodeBuff, 0, length);
                    Hardware.decodeBarcode(barcodeBuff);// 解码
                    LogUtils.d("barcode解码后hex:"
                                       + ArrayUtils.bytes2HexString(barcodeBuff));
                    LogUtils.d("barcode:%s", new String(barcodeBuff));
                }
                if (barcodeListener != null) {
                    barcodeListener.onReceiveData(Arrays.copyOf(data, length));
                }
            }

            @Override
            public void onEvent(int event, int info, byte[] data, ScanApi api) {
                LogUtils.d("event:%s info:%s %s", event, info, ArrayUtils.bytes2HexString(data));
            }
        });
    }

    @Override
    public void setBarcodeListener(final IBarcodeListener listener) {
        this.barcodeListener = listener;
    }

    @Override
    public synchronized boolean open(Context context) {
        if (isOpen) {
            return true;
        }
        init(context);
        SystemClock.sleep(500);
        powerOn();
        SystemClock.sleep(5000);
        if (barcodeListener != null) {
            barcodeListener.onOpen();
        }
        //        for (int i = 0; i < 3; i++) {
        //            scan();
        //            SystemClock.sleep(500);
        //            cancelScan();
        //        }
        //        SystemClock.sleep(300);
        //        scan();
        //        SystemClock.sleep(500);
        //        cancelScan();
        //        SystemClock.sleep(2000);

        isOpen = true;
        return true;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public void release() {
        cancelScan();
        powerOff();
        uninit();
    }

    @Override
    public void init(Context context) {
        scanApi.init(context);
    }

    @Override
    public void uninit() {
        scanApi.deInit();
    }

    @Override
    public void scan() {
        scanApi.doScan();
        isScanning = true;
        if (barcodeListener != null) {
            barcodeListener.onScan();
        }
    }

    @Override
    public boolean isScanning() {
        return isScanning;
    }

    @Override
    public void cancelScan() {
        scanApi.cancelScan();
        isScanning = false;
        if (barcodeListener != null) {
            barcodeListener.onStopScan();
        }
    }

    @Override
    public void powerOn() {
        scanApi.powerOn();
    }

    @Override
    public void powerOff() {
        scanApi.powerOff();
        isOpen = false;
    }
}
