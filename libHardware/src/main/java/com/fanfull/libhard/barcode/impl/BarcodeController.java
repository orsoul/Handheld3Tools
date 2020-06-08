package com.fanfull.libhard.barcode.impl;

import android.content.Context;
import com.fanfull.libhard.barcode.IBarcodeListener;
import com.fanfull.libhard.barcode.IBarcodeOperation;

public class BarcodeController implements IBarcodeOperation {
    private IBarcodeOperation operation;

    private BarcodeController(IBarcodeOperation barcodeOperation) {
        this.operation = barcodeOperation;
    }

    @Override
    public boolean open() {
        return operation.open();
    }

    @Override
    public boolean isOpen() {
        return operation.isOpen();
    }

    @Override
    public void release() {
        operation.release();
    }

    @Override
    public void init(Context context) {
        operation.init(context);
    }

    @Override
    public void uninit() {
        operation.uninit();
    }

    @Override
    public void scanAsync(long timeout) {
        operation.scanAsync(timeout);
    }

    @Override
    public void scanAsync() {
        operation.scanAsync();
    }

    @Override
    public boolean isScanning() {
        return operation.isScanning();
    }

    @Override
    public void cancelScan() {
        operation.cancelScan();
    }

    @Override
    public void powerOn() {
        operation.powerOn();
    }

    @Override
    public void powerOff() {
        operation.powerOff();
    }

    @Override
    public void setBarcodeListener(IBarcodeListener barcodeListener) {
        operation.setBarcodeListener(barcodeListener);
    }

    //    private static class SingletonHolder {
    //        private static final BarcodeController instance = new BarcodeController(new BarcodeOperationRd());
    //    }

    public static BarcodeController getInstance() {
        if (instance == null) {
          //            throw new RuntimeException("BarcodeController is not init");
        }
        return instance;
    }

    private static BarcodeController instance;

    public static synchronized void initBarcodeController(Context context) {
        if (instance == null) {
            instance = new BarcodeController(new BarcodeOperationRd(context));
        }
    }
}
