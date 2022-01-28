package com.fanfull.libjava.io.socketClient.interf;

public interface IReceiveListener<T> extends ISocketClientListener {
  int REC_FAILED_TIMEOUT = 2;
  int REC_FAILED_CATCH_EXCEPTION = 3;

  @Override default void onReceive(byte[] data, int len) {
    onReceive(convert(data, len));
  }

  T convert(byte[] data, int len);

  void onReceive(T rec);

  default void onRecTimeout(Object sendData) {
    onRecFailed(REC_FAILED_TIMEOUT, "回复超时");
  }

  default void onRecFailed(int failedCode, String msg) {
  }

  @Override default void onDisconnect(String serverIp, int serverPort, boolean isActive) {
  }

  @Override default void onConnectFailed(Throwable e) {
  }

  @Override default void onCatchException(Throwable throwable) {
    onRecFailed(REC_FAILED_CATCH_EXCEPTION,
        throwable != null ? throwable.getMessage() : "空异常");
  }
}