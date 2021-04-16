package com.fanfull.libjava.io.socketClient.netty;

import com.fanfull.libjava.io.socketClient.Options;
import com.fanfull.libjava.util.Logs;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.string.StringDecoder;

public class ClientDemo {
  static Options sOptions = new Options();
  static ClientNetty clientNetty;

  public static void main(String[] args) {
    sOptions.serverIp = "192.168.11.246";
    sOptions.serverPort = 23579;
    sOptions.reconnectEnable = true;
    sOptions.heartBeatEnable = false;

    clientNetty = new ClientNetty(sOptions);
    clientNetty.init(new MyChannelInitializer());

    Logs.out("====== main end ======");
  }

  static class MyChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override protected void initChannel(SocketChannel ch) throws Exception {
      ChannelPipeline pipeline = ch.pipeline();

      pipeline.addLast(new HeadEndDecoder());
      //pipeline.addLast(new MyDelimiterBasedFrameDecoder(1024, "#"));

      pipeline.addLast(new MyStringDecoder());
      pipeline.addLast(new StringSimpleChannelInboundHandler());

      pipeline.addLast(new ChannelInboundHandlerAdapter() {
        @Override public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
          Logs.out("ChannelInboundHandlerAdapter msg: %s", msg);
        }
      });

      //pipeline.addLast(new StringEncoder());
      pipeline.addLast(new MessageToByteEncoder<String>() {
        @Override protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out)
            throws Exception {
          ctx.writeAndFlush(Unpooled.wrappedBuffer((msg).getBytes()));
        }
      });

      //pipeline.addLast(new ChannelOutboundHandlerAdapter() {
      //  @Override public void read(ChannelHandlerContext ctx) throws Exception {
      //    super.read(ctx);
      //    Logs.out("ChannelOutboundHandlerAdapter read");
      //  }
      //
      //  @Override public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
      //      throws Exception {
      //    super.write(ctx, msg, promise);
      //    Logs.out("ChannelOutboundHandlerAdapter write: %s", msg);
      //  }
      //});
    }
  }

  static class MyDelimiterBasedFrameDecoder extends DelimiterBasedFrameDecoder {
    public MyDelimiterBasedFrameDecoder(int maxFrameLength, String msg) {
      super(maxFrameLength, Unpooled.wrappedBuffer(msg.getBytes()));
    }

    @Override protected Object decode(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
      Logs.out("FrameDecoder buffer: %s", buffer);
      Object decode = super.decode(ctx, buffer);
      Logs.out("FrameDecoder decode: %s", decode);
      if (decode == null) {
        return buffer;
      }
      return decode;
    }
  }

  static class MyStringDecoder extends StringDecoder {
    @Override protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out)
        throws Exception {
      super.decode(ctx, msg, out);
      Logs.out("MyStringDecoder list: %s, ByteBuf: %s", out.size(), msg);
    }
  }

  static class StringSimpleChannelInboundHandler extends SimpleChannelInboundHandler<String> {
    @Override protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
      Logs.out("StringHandler msg: %s", msg);
      File file = new File(msg);
      if (file.exists()) {
        if (!file.isFile()) {
          ctx.writeAndFlush("Not a file : " + file);
          Logs.out("Not a file : " + file);
          return;
        }
        //ctx.write(file + " " + file.length());
        RandomAccessFile randomAccessFile = new RandomAccessFile(msg, "r");
        FileRegion region =
            new DefaultFileRegion(randomAccessFile.getChannel(), 0, randomAccessFile.length());
        //ctx.write(region);
        ctx.writeAndFlush(region);
        randomAccessFile.close();
      } else {
        ctx.writeAndFlush("File not found :" + file);
        Logs.out("File not found : " + file);
      }
    }
  }
}
