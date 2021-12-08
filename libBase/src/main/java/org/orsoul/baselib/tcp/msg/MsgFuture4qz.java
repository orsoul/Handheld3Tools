package org.orsoul.baselib.tcp.msg;

import com.fanfull.libjava.io.netty.future.MsgFuture;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MsgFuture4qz implements MsgFuture<BaseSocketMessage4qz> {

  private CountDownLatch latch = new CountDownLatch(1);
  private long begin;

  private final Object requestId;
  private long timeout;
  private BaseSocketMessage4qz response;

  private boolean isSendSuccess;
  private boolean isTimeout = false;
  private Throwable cause;

  public MsgFuture4qz(Object requestId) {
    this.requestId = requestId;
  }

  public MsgFuture4qz(Object requestId, long timeout) {
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

  @Override public BaseSocketMessage4qz response() {
    return response;
  }

  @Override public void setResponse(BaseSocketMessage4qz response) {
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

  @Override public BaseSocketMessage4qz get(long timeout, TimeUnit unit)
      throws InterruptedException {
    begin = System.currentTimeMillis();
    isTimeout = false;

    if (latch.await(timeout, unit)) {
      return response;
    }

    isTimeout = System.currentTimeMillis() - begin > timeout;
    return null;
  }

  public BaseSocketMessage4qz get(long timeout) throws InterruptedException {
    return get(timeout, TimeUnit.MILLISECONDS);
  }

  public BaseSocketMessage4qz getAndWaitTimeout() throws InterruptedException {
    return get(timeout);
  }

  @Override public BaseSocketMessage4qz get() throws InterruptedException {
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
