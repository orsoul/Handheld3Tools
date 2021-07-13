package com.fanfull.libjava.io.socketClient;

public interface OnceReceiveListener<T> {
  void onReceive(T recData);

  default Object getSendData() {return null;}

  default void onRecTimeout() {}

  default void onCatchException(Throwable throwable) {}
}
