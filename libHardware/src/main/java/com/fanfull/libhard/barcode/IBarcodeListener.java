package com.fanfull.libhard.barcode;

public interface IBarcodeListener {
    void onOpen();

    void onScan();

    void onStopScan();
    void onReceiveData(byte[] data);
}
