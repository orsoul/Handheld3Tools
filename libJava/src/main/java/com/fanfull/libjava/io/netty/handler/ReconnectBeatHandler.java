package com.fanfull.libjava.io.netty.handler;

import com.fanfull.libjava.io.netty.ClientNetty;
import com.fanfull.libjava.util.Logs;

import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 断线重连、心跳处理类.
 */
public class ReconnectBeatHandler extends ChannelInboundHandlerAdapter {
  private ClientNetty clientNetty;
  private int beatTimeoutCount;

  public ReconnectBeatHandler(ClientNetty clientNetty) {
    this.clientNetty = clientNetty;
  }

  public Object getHeartBeatMsg() {
    return "heartBeat";
  }

  public void sendHeartBeat() {
    final Object beatMsg = getHeartBeatMsg();
    if (beatMsg != null) {
      clientNetty.send(beatMsg);
    }
  }

  /** 心跳超时. */
  public void onHeartBeatOut() {
    clientNetty.disconnect();
  }

  public void onHeartBeatTriggered(IdleStateEvent event) {
    beatTimeoutCount++;
    if (clientNetty.getOptions().disconnectCount <= beatTimeoutCount) {
      onHeartBeatOut();
      beatTimeoutCount = 0;
      return;
    }
    sendHeartBeat();
  }

  @Override public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
      throws Exception {
    if (!(evt instanceof IdleStateEvent)) {
      Logs.out("ReconnectHandler userEventTriggered: %s", evt);
      return;
    }

    IdleStateEvent event = (IdleStateEvent) evt;
    Logs.out("userEventTriggered: %s - BeatEnable:%s",
        event.state(), clientNetty.isHeartBeatEnable());

    if (clientNetty.isHeartBeatEnable()) {
      String type = "";
      switch (event.state()) {
        case READER_IDLE:
          //Logs.out(ctx.channel().remoteAddress() + "READER_IDLE 超时次数：");
          onHeartBeatTriggered(event);
          break;
        case WRITER_IDLE:
          //Logs.out(ctx.channel().remoteAddress() + "超时 WRITER_IDLE");
          onHeartBeatTriggered(event);
          //ctx.writeAndFlush("heartBeat");
          break;
        case ALL_IDLE:
          //Logs.out(ctx.channel().remoteAddress() + "超时 ALL_IDLE");
          onHeartBeatTriggered(event);
          break;
      }
    } else {
      super.userEventTriggered(ctx, evt);
    }
  }

  /** 建立连接失败、断开连接 回调. */
  @Override public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception {
    //Logs.out("ReconnectHandler channelUnregistered " + this);

    if (clientNetty.isShutdown()
        || !clientNetty.isReconnectEnable()
        || clientNetty.isActiveDisconnect()) {
      return;
    }

    ctx.channel().eventLoop().schedule(() -> {
      Logs.out("Reconnecting to %s:%s",
          clientNetty.getOptions().serverIp,
          clientNetty.getOptions().serverPort);
      clientNetty.connect();
    }, clientNetty.getOptions().reconnectInterval, TimeUnit.MILLISECONDS);
  }

  /** 连接断开 回调. */
  @Override public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    Logs.out("ReconnectHandler channelInactive");
    beatTimeoutCount = 0;
    super.channelInactive(ctx);
  }

  @Override public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    beatTimeoutCount = 0;
    Logs.out("ReconnectHandler channelRead");
    super.channelRead(ctx, msg);
  }
}