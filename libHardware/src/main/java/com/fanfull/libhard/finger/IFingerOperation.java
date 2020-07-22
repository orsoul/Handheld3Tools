package com.fanfull.libhard.finger;

import com.fanfull.libhard.IOperation;

public interface IFingerOperation extends IOperation {

  boolean send(byte[] data);

  void setListener(IFingerListener listener);
}
