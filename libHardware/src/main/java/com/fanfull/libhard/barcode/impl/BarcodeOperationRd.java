package com.fanfull.libhard.barcode.impl;


import android.content.Context;
import android.os.SystemClock;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.libhard.barcode.AbsBarcodeOperation;
import com.rd.barcodeScanTest.NewApiService;
import com.rd.barcodeScanTest.ScanApi;

import org.orsoul.baselib.util.ArrayUtils;

import java.util.Arrays;

/**
 * 雨滴二维码扫描 实现类
 */
public class BarcodeOperationRd extends AbsBarcodeOperation {

    private ScanApi scanApi;

    public BarcodeOperationRd() {
        // TODO: 2020/6/23 两种 API
        this.scanApi = new NewApiService();
        //        this.scanApi = new NewApiBroadcast();
        scanApi.setDecodeCallback(new ScanApi.DecodeCallback() {
            @Override
            public void onDecodeComplete(int symbology, int length, byte[] data, ScanApi api) {
                LogUtils.v("symbology %s dataLen:%s  %s", symbology, data.length,
                           ArrayUtils.bytes2HexString(data, 0, length));
                if (barcodeListener != null) {
                    barcodeListener.onReceiveData(Arrays.copyOf(data, length));
                }
            }

            @Override
            public void onEvent(int event, int info, byte[] data, ScanApi api) {
                LogUtils.v("event:%s info:%s %s", event, info, ArrayUtils.bytes2HexString(data));
            }
        });
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
        powerOff();
        isOpen = false;
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
        cancelScan();
        isOpen = false;
    }
}
