package com.fanfull.libjava.io.socketClient;

public interface OnceReceiveListener<T> {
  //int REC_FAILED_SEND_FAILED = 1;
  int REC_FAILED_TIMEOUT = 2;
  int REC_FAILED_CATCH_EXCEPTION = 3;

  void onReceive(T recData);

  default void onRecTimeout() {
    onRecFailed(OnceReceiveListener.REC_FAILED_TIMEOUT,
        "回复超时");
  }

  default void onCatchException(Throwable throwable) {
    onRecFailed(OnceReceiveListener.REC_FAILED_CATCH_EXCEPTION,
        throwable != null ? throwable.getMessage() : "空异常");
  }

  default void onRecFailed(int failedCode, String msg) {}
}
