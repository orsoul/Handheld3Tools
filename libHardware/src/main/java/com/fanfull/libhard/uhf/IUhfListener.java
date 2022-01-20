package com.fanfull.libhard.uhf;

import com.fanfull.libhard.IOperationListener;

import java.util.Arrays;

public interface IUhfListener extends IOperationListener {
  default void onReceiveData(byte[] data, int len) {
    onReceiveData(Arrays.copyOf(data, len));
  }
}
