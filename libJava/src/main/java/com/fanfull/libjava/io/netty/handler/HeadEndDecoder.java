package com.fanfull.libjava.io.netty.handler;

import com.fanfull.libjava.util.Logs;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * 以特定字符为协议头、协议尾 的解析器.
 */
public class HeadEndDecoder extends ByteToMessageDecoder {
  static final int HEAD = 42;
  static final int END = 35;

  public boolean isContainHead;
  public boolean notDecode;

  public HeadEndDecoder(boolean isContainHead) {
    this.isContainHead = isContainHead;
  }

  public HeadEndDecoder() {
  }

  @Override protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
      throws Exception {
    //Logs.out("notDecode:%s, decode:%s", notDecode, in);

    if (notDecode) {
      int len = in.readableBytes();
      byte[] bytes = new byte[len];
      in.readBytes(bytes);
      out.add(bytes);
      return;
    }

    Object decoded = decode(in);
    if (decoded != null) {
      out.add(decoded);
    }
  }

  protected Object decode(ByteBuf in) {
    int slice = slice(in);
    if (0 < slice) {
      ByteBuf frame;
      if (isContainHead) {
        frame = in.readBytes(slice);
      } else {
        //Logs.out("slice0:%s - %s", in, slice);
        in.skipBytes(1);
        //Logs.out("slice1:%s", in);
        frame = in.readBytes(slice - 2);
        //Logs.out("slice2:%s", in);
        //Logs.out("frame:%s - %s", frame, new String(frame.array()));
        in.skipBytes(1);
        //Logs.out("slice3:%s", in);
      }
      return frame;
    }

    return null;
  }

  private int slice(ByteBuf buf) {
    if (buf == null || buf.writerIndex() == 0) {
      return -1;
    }

    /* 遍历数据，定位头、尾 位置 */
    int headP = -1;
    int endP = -1;
    for (int i = buf.readerIndex(); i < buf.writerIndex(); i++) {
      if (headP == -1 && buf.getByte(i) == HEAD) {
        headP = i;
      } else if (endP == -1 && buf.getByte(i) == END) {
        endP = i;
      }
    }


    Logs.out("headP:%s, endP:%s", headP, endP);
    //Logs.out("str:%s", new String(buf.array()));

    if (headP < 0) {
      // 1、无协议头，丢弃所有数据
      //buf.clear();
      //buf.release();
      return -1;
    } else if (endP < 1) {
      // 2、有协议头，无协议尾
      if (buf.readerIndex() == 0 && 0 < headP) {
        // 第一条信息，且协议之前有沉冗数据
        buf.skipBytes(headP);
      }
      return -1;
    } else {
      // 3、有协议头，有协议尾，发现完整数据
      if (buf.readerIndex() == 0 && 0 < headP) {
        // 第一条信息，且协议之前有沉冗数据
        buf.skipBytes(headP);
      }
      return endP - headP + 1;
    }
  }
}