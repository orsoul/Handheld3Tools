package com.fanfull.libhard.barcode;

public abstract class AbsBarcodeOperation implements IBarcodeOperation {

    protected IBarcodeListener barcodeListener;
    protected boolean isOpen;
    protected boolean isScanning;

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public boolean isScanning() {
        return isScanning;
    }

    @Override
    public void setBarcodeListener(IBarcodeListener barcodeListener) {
        this.barcodeListener = barcodeListener;
    }
}
