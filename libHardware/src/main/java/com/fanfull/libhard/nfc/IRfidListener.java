package com.fanfull.libhard.nfc;

public interface IRfidListener {
    void onOpen();

    void onScan();

    void onStopScan();

    void onReceiveData(byte[] data);
}
