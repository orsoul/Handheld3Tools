package com.fanfull.libjava.io.netty.future;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class BaseMsgFuture<T> implements MsgFuture<T> {

  private CountDownLatch latch = new CountDownLatch(1);
  private long begin;

  private final Object requestId;
  private long timeout;
  private T response;

  private boolean isSendSuccess;
  private boolean isTimeout = false;
  private Throwable cause;

  public BaseMsgFuture(Object requestId) {
    this.requestId = requestId;
  }

  public BaseMsgFuture(Object requestId, long timeout) {
    this.requestId = requestId;
    this.timeout = timeout;
    //isSendSuccess = false;
    //isTimeout = false;
  }

  @Override public Throwable cause() {
    return cause;
  }

  @Override public void setCause(Throwable cause) {
    this.cause = cause;
  }

  @Override public boolean isSendSuccess() {
    return isSendSuccess;
  }

  @Override public void setSendSuccess(boolean result) {
    this.isSendSuccess = result;
  }

  @Override public Object requestId() {
    return requestId;
  }

  @Override public T response() {
    return response;
  }

  @Override public void setResponse(T response) {
    this.response = response;
    latch.countDown();
  }

  @Override public boolean cancel(boolean mayInterruptIfRunning) {
    return true;
  }

  @Override public boolean isCancelled() {
    return false;
  }

  @Override public boolean isDone() {
    return false;
  }

  @Override public T get(long timeout, TimeUnit unit)
      throws InterruptedException {
    begin = System.currentTimeMillis();
    isTimeout = false;

    if (latch.await(timeout, unit)) {
      return response;
    }

    isTimeout = System.currentTimeMillis() - begin > timeout;
    return null;
  }

  public T get(long timeout) throws InterruptedException {
    return get(timeout, TimeUnit.MILLISECONDS);
  }

  public T getAndWaitTimeout() throws InterruptedException {
    return get(timeout);
  }

  @Override public T get() throws InterruptedException {
    latch.wait();
    return response;
    //return get(timeout);
  }

  @Override public boolean isTimeout() {
    if (isTimeout) {
      return true;
    }
    return System.currentTimeMillis() - begin > timeout;
  }
}
