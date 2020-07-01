package com.fanfull.libhard;

public interface IOperation {
    boolean open();

    boolean isOpen();

    boolean isScanning();

    void release();
}
