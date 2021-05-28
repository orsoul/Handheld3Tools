package com.fanfull.libjava.io.netty.future;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SyncWriteFuture implements MsgFuture<String> {

  private CountDownLatch latch = new CountDownLatch(1);
  private final long begin = System.currentTimeMillis();
  private long timeout;
  private String response;
  private final String requestId;
  private boolean writeResult;
  private Throwable cause;
  private boolean isTimeout = false;

  public SyncWriteFuture(String requestId) {
    this.requestId = requestId;
  }

  public SyncWriteFuture(String requestId, long timeout) {
    this.requestId = requestId;
    this.timeout = timeout;
    writeResult = true;
    isTimeout = false;
  }

  public Throwable cause() {
    return cause;
  }

  public void setCause(Throwable cause) {
    this.cause = cause;
  }

  public boolean isSendSuccess() {
    return writeResult;
  }

  public void setSendSuccess(boolean result) {
    this.writeResult = result;
  }

  public String requestId() {
    return requestId;
  }

  public String response() {
    return response;
  }

  public void setResponse(String response) {
    this.response = response;
    latch.countDown();
  }

  public boolean cancel(boolean mayInterruptIfRunning) {
    return true;
  }

  public boolean isCancelled() {
    return false;
  }

  public boolean isDone() {
    return false;
  }

  public String get() throws InterruptedException, ExecutionException {
    latch.wait();
    return response;
  }

  public String get(long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    if (latch.await(timeout, unit)) {
      return response;
    }
    return null;
  }

  public boolean isTimeout() {
    if (isTimeout) {
      return isTimeout;
    }
    return System.currentTimeMillis() - begin > timeout;
  }
}
