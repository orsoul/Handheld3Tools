package com.fanfull.libjava.io.netty.future;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class SyncWrite {

  public String writeAndSync(final Channel channel, final String request, final long timeout)
      throws Exception {

    if (channel == null) {
      throw new NullPointerException("channel");
    }
    if (request == null) {
      throw new NullPointerException("request");
    }
    if (timeout <= 0) {
      throw new IllegalArgumentException("timeout <= 0");
    }

    //String requestId = UUID.randomUUID().toString();
    //request.setRequestId(requestId);

    //WriteFuture<String> future = new SyncWriteFuture(request.getRequestId());
    //SyncWriteMap.syncKey.put(request.getRequestId(), future);

    MsgFuture<String> future = new SyncWriteFuture(request);
    SyncWriteMap.syncKey.put(future.requestId(), future);

    String response = doWriteAndSync(channel, request, timeout, future);

    SyncWriteMap.syncKey.remove(future.requestId());
    return response;
  }

  private String doWriteAndSync(final Channel channel, final String request, final long timeout,
      final MsgFuture<String> writeFuture) throws Exception {

    channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
      public void operationComplete(ChannelFuture future) throws Exception {
        writeFuture.setSendSuccess(future.isSuccess());
        writeFuture.setCause(future.cause());
        //失败移除
        if (!writeFuture.isSendSuccess()) {
          SyncWriteMap.syncKey.remove(writeFuture.requestId());
        }
      }
    });

    String response = writeFuture.get(timeout, TimeUnit.MILLISECONDS);
    if (response == null) {
      if (writeFuture.isTimeout()) {
        throw new TimeoutException();
      } else {
        // write exception
        throw new Exception(writeFuture.cause());
      }
    }
    return response;
  }
}
