package com.fanfull.libhard.uhf;

import com.fanfull.libhard.IOperation;

public interface IUhfOperation extends IOperation {

    boolean send(byte[] data);

    void setListener(IUhfListener listener);
}
