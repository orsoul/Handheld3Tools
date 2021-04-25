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

  public void onBeat(IdleStateEvent event) {
  }

  public void onBeatTimeout(IdleStateEvent event) {

  }

  @Override public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
      throws Exception {
    //Logs.out("ReconnectHandler userEventTriggered: %s", evt);
    if (clientNetty.isHeartBeatEnable() && evt instanceof IdleStateEvent) {
      IdleStateEvent event = (IdleStateEvent) evt;
      String type = "";
      switch (event.state()) {
        case READER_IDLE:
          beatTimeoutCount++;
          Logs.out(ctx.channel().remoteAddress() + "READER_IDLE 超时次数：" + beatTimeoutCount);
          if (3 <= beatTimeoutCount) {
            clientNetty.disconnect();
            return;
          }
          clientNetty.send("heartBeat");
          break;
        case WRITER_IDLE:
          Logs.out(ctx.channel().remoteAddress() + "超时 WRITER_IDLE");
          //ctx.writeAndFlush("heartBeat");
          break;
        case ALL_IDLE:
          Logs.out(ctx.channel().remoteAddress() + "超时 ALL_IDLE");
          break;
      }
    } else {
      super.userEventTriggered(ctx, evt);
    }
  }

  @Override public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception {
    Logs.out("ReconnectHandler channelUnregistered");

    if (clientNetty.isShutdown() || !clientNetty.isReconnectEnable()) {
      return;
    }

    ctx.channel().eventLoop().schedule(() -> {
      Logs.out("Reconnecting to %s:%s",
          clientNetty.getOptions().serverIp,
          clientNetty.getOptions().serverPort);
      clientNetty.connect();
    }, clientNetty.getOptions().reconnectInterval, TimeUnit.MILLISECONDS);
  }
}