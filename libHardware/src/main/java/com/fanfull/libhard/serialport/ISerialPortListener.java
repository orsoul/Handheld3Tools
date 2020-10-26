package com.fanfull.libhard.serialport;

import java.util.Arrays;

public interface ISerialPortListener {
  default void onReceiveData(byte[] data) {
  }

  default void onReceiveData(byte[] data, int len) {
    onReceiveData(Arrays.copyOf(data, len));
  }
}
