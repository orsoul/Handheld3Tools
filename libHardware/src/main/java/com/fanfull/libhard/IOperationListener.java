package com.fanfull.libhard;

public interface IOperationListener {

  void onOpen(boolean openSuccess);

  void onReceiveData(byte[] data);
}
