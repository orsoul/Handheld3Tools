package com.fanfull.libjava.io.netty;

import com.fanfull.libjava.io.netty.handler.HeadEndDecoder;
import com.fanfull.libjava.io.netty.handler.ReconnectBeatHandler;
import com.fanfull.libjava.io.socketClient.Options;
import com.fanfull.libjava.io.socketClient.message1.BaseSocketMessage4qz;
import com.fanfull.libjava.util.Logs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

public class ClientDemo {
  private static Options sOptions = new Options();
  private static ClientNetty clientNetty;

  public static void main(String[] args) {
    sOptions.serverIp = "192.168.11.246";
    sOptions.serverPort = 23579;
    sOptions.reconnectEnable = true;
    sOptions.heartBeatEnable = false;

    clientNetty = new ClientNetty(sOptions);
    clientNetty.init(new MyChannelInitializer(clientNetty));

    message4qz = new BaseSocketMessage4qz(13) {
      @Override public String getMessage() {
        return genProtocol(func, "base message", msgNum);
      }

      @Override public boolean send() {
        return clientNetty.send(getMessage());
      }
    };
    message4qz.setReplyListener(new BaseSocketMessage4qz.ReplyListener() {
      @Override public void onReceive(BaseSocketMessage4qz recMsg) {
        Logs.out("timeout");
      }

      @Override public void onTimeout(BaseSocketMessage4qz sendMsg) {
        Logs.out("timeout");
      }
    });

    Logs.out("====== main end ======");
  }

  static BaseSocketMessage4qz message4qz;

  public static void handle(String info, ClientNetty clientNetty) {
    Logs.out("handle msg: %s", info);
    String[] s = info.split(" ");
    switch (s[0]) {
      case "886":
      case "*886":
        //options.reconnectEnable = false;
        //channel.close();
        //group.shutdownGracefully();
        clientNetty.shutdown();
        break;
      case "88":
        clientNetty.disconnect();
        break;
      case "beat": // beat on
        if (s[1].equals("on")) {
          clientNetty.setHeartBeatEnable(true);
        } else {
          clientNetty.setHeartBeatEnable(false);
        }
        break;
      case "reconnect":  // reconnect on
        if (s[1].equals("on")) {
          clientNetty.setReconnectEnable(true);
        } else {
          clientNetty.setReconnectEnable(false);
        }
        break;
      case "302": // 302
        sendFile(s[1], clientNetty);
        break;
      case "byte":
        clientNetty.send("send bytes".getBytes());
        break;
      case "sync":
        message4qz.sendSync();
        break;
      default:
        clientNetty.send(info);
        break;
    }
  }

  private static void sendFile(String msg, ClientNetty clientNetty) {
    File file = new File(msg);
    if (!file.exists()) {
      Logs.out("Not find file : " + file);
      return;
    }

    if (!file.isFile()) {
      //ctx.writeAndFlush("Not a file : " + file);
      Logs.out("Not a file : " + file);
      return;
    }

    RandomAccessFile randomAccessFile = null;
    try {
      randomAccessFile = new RandomAccessFile(msg, "r");
      FileRegion region =
          new DefaultFileRegion(randomAccessFile.getChannel(), 0, randomAccessFile.length());
      //ctx.write(region);
      //ctx.writeAndFlush(region);
      clientNetty.send(region);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        randomAccessFile.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  static class MyChannelInitializer extends ChannelInitializer<SocketChannel> {
    private ClientNetty clientNetty;

    public MyChannelInitializer(ClientNetty clientNetty) {
      this.clientNetty = clientNetty;
    }

    @Override protected void initChannel(SocketChannel ch) throws Exception {
      ChannelPipeline pipeline = ch.pipeline();

      pipeline.addLast(new IdleStateHandler(clientNetty.getOptions().heartBeatInterval, 0, 0));
      pipeline.addLast(new ReconnectBeatHandler(clientNetty));

      //pipeline.addLast(new MyDelimiterBasedFrameDecoder(1024, "#"));

      pipeline.addLast(new HeadEndDecoder());
      pipeline.addLast(new StringDecoder());
      //pipeline.addLast(new MyStringDecoder());
      pipeline.addLast(new StringSimpleChannelInboundHandler());

      pipeline.addLast(new ChannelInboundHandlerAdapter() {
        @Override public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
          Logs.out("ChannelInboundHandlerAdapter msg: %s", msg);
        }
      });

      pipeline.addLast(new ByteArrayEncoder());
      pipeline.addLast(new StringEncoder());
      //pipeline.addLast(new MyStringEncoder());
      //pipeline.addLast(new MessageToByteEncoder<String>() {
      //  @Override protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out)
      //      throws Exception {
      //    ctx.writeAndFlush(Unpooled.wrappedBuffer((msg).getBytes()));
      //  }
      //});

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

  static class MyStringEncoder extends StringEncoder {
    @Override protected void encode(ChannelHandlerContext ctx, CharSequence msg, List<Object> out)
        throws Exception {
      super.encode(ctx, msg, out);
      Logs.out("MyStringEncoder msg: %s", msg);
      Logs.out("MyStringEncoder list: %s - %s", out.size(), out);
    }
  }

  static class MyStringDecoder extends StringDecoder {
    @Override protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out)
        throws Exception {
      super.decode(ctx, msg, out);
      Logs.out("MyStringDecoder ByteBuf: %s", msg);
      Logs.out("MyStringDecoder list: %s - %s", out.size(), out);
    }
  }

  static class StringSimpleChannelInboundHandler extends SimpleChannelInboundHandler<String> {
    @Override protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
      //Logs.out("StringHandler msg: %s", msg);
      //clientNetty.send("send byte[]".getBytes());
      handle(msg, clientNetty);
    }
  }
}
