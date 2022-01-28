package com.fanfull.libjava.io.socketClient.interf;

public interface IReceiveListener<T> extends ISocketClientListener {
  int REC_FAILED_TIMEOUT = 2;
  int REC_FAILED_CATCH_EXCEPTION = 3;

  @Override default void onReceive(byte[] data, int len) {
    onReceive(convert(data, len));
  }

  @Override default void onCatchException(Throwable throwable) {
    onRecFailed(REC_FAILED_CATCH_EXCEPTION,
        throwable != null ? throwable.getMessage() : "空异常");
  }

  T convert(byte[] data, int len);

  void onReceive(T rec);

  default void onRecTimeout(Object sendData) {
    onRecFailed(REC_FAILED_TIMEOUT, "回复超时");
  }

  default void onRecFailed(int failedCode, String msg) {
  }
}