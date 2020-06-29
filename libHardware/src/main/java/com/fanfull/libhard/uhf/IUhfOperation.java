package com.fanfull.libhard.uhf;

public interface IUhfOperation {

    boolean open();

    boolean isOpen();

    void release();

    boolean send(byte[] data);

    void setListener(IUhfListener listener);
}
