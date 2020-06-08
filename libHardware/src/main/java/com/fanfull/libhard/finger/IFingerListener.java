package com.fanfull.libhard.finger;

public interface IFingerListener {
  void onOpen();

  void onScan();

  void onStopScan();

  void onReceiveData(byte[] data);
}
