package com.fanfull.libhard.serialport;

public interface ISerialPortListener {
    void onReceiveData(byte[] data);
}
