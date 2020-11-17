package com.fanfull.libhard;

public interface IOperation {
  int SUCCESS = 0;
  int ERR_ARGS = -1;
  int ERR_FAILED = -2;
  int ERR_TIMEOUT = -3;

  int ERR_NOT_FIND = -4;
  int ERR_FIND_CARD_FAILED = -5;

  boolean open();

  boolean isOpen();

  boolean isScanning();

  void release();
}
