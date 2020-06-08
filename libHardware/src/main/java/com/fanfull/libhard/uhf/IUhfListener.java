package com.fanfull.libhard.uhf;

public interface IUhfListener {
    void onOpen();

    void onScan();

    void onStopScan();

    void onReceiveData(byte[] data);
}
