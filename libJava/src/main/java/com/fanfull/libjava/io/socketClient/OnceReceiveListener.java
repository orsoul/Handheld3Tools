package com.fanfull.libjava.io.socketClient;

public interface OnceReceiveListener<T> extends ReceiveListener {
  //int REC_FAILED_SEND_FAILED = 1;
  int REC_FAILED_TIMEOUT = 2;
  int REC_FAILED_CATCH_EXCEPTION = 3;

  //default void onSendFailed(Object sendData) {
  //}

  //void onReceive(T recData);

  default void onRecTimeout(Object sendData) {
    onTimeout();
    onRecFailed(REC_FAILED_TIMEOUT,
        "回复超时");
  }

  default void onCatchException(Throwable throwable) {
    onRecFailed(REC_FAILED_CATCH_EXCEPTION,
        throwable != null ? throwable.getMessage() : "空异常");
  }

  default void onRecFailed(int failedCode, String msg) {}

  @Override default void onConnect(String serverIp, int serverPort) {

  }

  @Override default void onReceive(String recString) {

  }

  @Override default void onDisconnect() {

  }

  @Override default void onTimeout() {

  }

  //@Override default void onReceive(byte[] data) {
  //
  //}
}
