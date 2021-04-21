package com.fanfull.libjava.io.netty.future;

import java.util.concurrent.Future;

public interface MsgFuture<T> extends Future<T> {

  Throwable cause();

  void setCause(Throwable cause);

  boolean isSendSuccess();

  void setSendSuccess(boolean result);

  Object requestId();

  T response();

  void setResponse(T response);

  boolean isTimeout();

  //default void onReceive(T recMsg) {}
  //default void onTimeout(T sendMsg) {}
}
