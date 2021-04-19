package com.fanfull.libjava.io.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class HeadEndEncoder extends MessageToByteEncoder<String> {

  private String delimiterHead;
  private String delimiterEnd;

  public HeadEndEncoder(String delimiterHead, String delimiterEnd) {
    this.delimiterHead = delimiterHead;
    this.delimiterEnd = delimiterEnd;
  }

  @Override protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) {
    // 在响应的数据后面添加分隔符
    ctx.writeAndFlush(Unpooled.wrappedBuffer((delimiterHead + msg + delimiterEnd).getBytes()));
  }
}