package com.fanfull.libhard.nfc;

import com.fanfull.libhard.IOperation;

public interface IRfidOperation extends IOperation {

    void setListener(IRfidListener listener);

    byte[] findNfc();

    void findNfcAsync();

    byte[] findM1();

    void findM1Async();

    byte[] findNfcOrM1();

    void findNfcOrM1Async();

    boolean readNfc(int sa, byte[] buff, int len);

    boolean readNfc(int sa, byte[] buff);

    void readNfcAsync(int sa, int dataLen);
}
