package com.fanfull.libhard.barcode;

import android.content.Context;

import com.fanfull.libhard.barcode.impl.BarcodeOperationRd;

public class BarcodeController implements IBarcodeOperation {
    private IBarcodeOperation barcodeOperation;

    private BarcodeController(IBarcodeOperation barcodeOperation) {
        this.barcodeOperation = barcodeOperation;
    }

    @Override
    public boolean open(Context context) {
        return barcodeOperation.open(context);
    }

    @Override
    public boolean isOpen() {
        return barcodeOperation.isOpen();
    }

    @Override
    public void release() {
        barcodeOperation.release();
    }

    @Override
    public void init(Context context) {
        barcodeOperation.init(context);
    }

    @Override
    public void uninit() {
        barcodeOperation.uninit();
    }

    @Override
    public void scan() {
        barcodeOperation.scan();
    }

    @Override
    public boolean isScanning() {
        return barcodeOperation.isScanning();
    }

    @Override
    public void cancelScan() {
        barcodeOperation.cancelScan();
    }

    @Override
    public void powerOn() {
        barcodeOperation.powerOn();
    }

    @Override
    public void powerOff() {
        barcodeOperation.powerOff();
    }

    @Override
    public void setBarcodeListener(IBarcodeListener barcodeListener) {
        barcodeOperation.setBarcodeListener(barcodeListener);
    }

    private static class SingletonHolder {
        private static final BarcodeController instance = new BarcodeController(new BarcodeOperationRd());
    }

    public static BarcodeController getInstance() {
        return SingletonHolder.instance;
    }
}
