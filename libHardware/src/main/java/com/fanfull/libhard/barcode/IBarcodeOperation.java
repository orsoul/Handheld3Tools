package com.fanfull.libhard.barcode;

import android.content.Context;

import com.fanfull.libhard.IOperation;

public interface IBarcodeOperation extends IOperation {

    void init(Context context);

    void uninit();

    void scanAsync(long timeout);

    void scanAsync();

    void cancelScan();

    void powerOn();

    void powerOff();

    void setBarcodeListener(IBarcodeListener barcodeListener);
}
