package com.fanfull.libhard.finger;

import com.fanfull.libhard.IOperation;

public interface IFingerOperation extends IOperation {

  boolean send(byte[] data);

  int addFinger(int[] fingerIdBuff);

  int searchFinger(int[] fingerIdBuff);

  int getFingerNum(int[] fingerNumBuff);

  boolean clearFinger();

  void setListener(IFingerListener listener);
}
