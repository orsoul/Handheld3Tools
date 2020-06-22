package com.fanfull.libhard.barcode;

import android.content.Context;

public interface IBarcodeOperation {
    int BARCODE_DATA_LEN = 23;

    boolean open(Context context);

    boolean isOpen();

    void release();

    void init(Context context);

    void uninit();

    void scan();

    boolean isScanning();

    void cancelScan();

    void powerOn();

    void powerOff();

    void setBarcodeListener(IBarcodeListener barcodeListener);
}
