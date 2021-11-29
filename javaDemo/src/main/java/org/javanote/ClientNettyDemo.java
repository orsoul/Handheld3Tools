package org.javanote;

import com.fanfull.libjava.io.netty.ClientNetty;
import com.fanfull.libjava.io.netty.handler.ReconnectBeatHandler;
import com.fanfull.libjava.io.socketClient.Options;
import com.fanfull.libjava.util.BytesUtil;
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
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

public class ClientNettyDemo {
  private static Options sOptions = new Options();
  private static ClientNetty clientNetty;

  public static void main(String[] args) {
    sOptions.serverIp = "192.168.11.246";
    sOptions.serverPort = 23456;
    sOptions.reconnectEnable = true;
    sOptions.heartBeatEnable = false;

    clientNetty = new ClientNetty(sOptions);
    clientNetty.init(new MyChannelInitializer(clientNetty), false);

    clientNetty.connect();
    Logs.out("====== main end ======");
  }

  public static void handle(Object obj, ClientNetty clientNetty) {
    String info;
    if (obj instanceof String) {
      info = obj.toString();
    } else if (obj instanceof byte[]) {
      info = new String((byte[]) obj);
    } else {
      info = null;
    }
    Logs.out("handle msg: %s", info);
    if (info == null) {
      return;
    }
    String[] s = info.split(" ");
    switch (s[0]) {
      case "886":
        //options.reconnectEnable = false;
        //channel.close();
        //group.shutdownGracefully();
        clientNetty.shutdown();
        break;
      case "88":
        clientNetty.disconnect();
        break;
      case "11":
        clientNetty.setReconnectEnable(true);
        clientNetty.setHeartBeatEnable(true);
        break;
      case "00":
        clientNetty.setReconnectEnable(false);
        clientNetty.setHeartBeatEnable(false);
        break;
      case "10":
        clientNetty.setReconnectEnable(true);
        clientNetty.setHeartBeatEnable(false);
        break;
      case "01":
        clientNetty.setReconnectEnable(false);
        clientNetty.setHeartBeatEnable(true);
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
        //sendFile(s[1], clientNetty);
        //break;
      case "byte":
        clientNetty.send("send bytes".getBytes());
        break;
      case "error":
        int t = 1 / 0;
        break;
      case "nothing":
        //break;
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
    private int connectFailedCount = 0;

    public MyChannelInitializer(ClientNetty clientNetty) {
      this.clientNetty = clientNetty;
    }

    @Override protected void initChannel(SocketChannel ch) throws Exception {
      ChannelPipeline pipeline = ch.pipeline();

      pipeline.addLast(new IdleStateHandler(clientNetty.getOptions().heartBeatInterval, 0, 0));
      pipeline.addLast(new ReconnectBeatHandler(clientNetty) {
        private int sendFailedNum = 0;
        private int beatNum = 0;

        @Override public void sendHeartBeat() {
          boolean send = clientNetty.send("beat-" + ++beatNum);
          if (send) {
            sendFailedNum = 0;
          } else {
            sendFailedNum++;
            if (3 <= sendFailedNum) {
              clientNetty.disconnect();
              Logs.out("心跳发送失败 %s, 已断开连接", sendFailedNum);
            } else {
              Logs.out("心跳发送失败 %s", sendFailedNum);
            }
          }
        }

        @Override public void onHeartBeatOut() {
          //  心跳回复
          Logs.out("心跳回复超时 onHeartBeatOut");
        }

        @Override public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
          Logs.out("channelRegistered");
          super.channelRegistered(ctx);
        }

        @Override public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
          Options opt = clientNetty.getOptions();
          ++connectFailedCount;
          Logs.out("channelUnregistered ==连接失败==:%s, %s:%s",
              connectFailedCount, opt.serverIp, opt.serverPort);
          //if (!clientNetty.isActiveDisconnect() && 1 < connectFailedCount) {
          //  Logs.out("重连失败 %s:%s", opt.serverIp, opt.serverPort);
          //}

          if (!clientNetty.isReconnectEnable()) {
            Logs.out("== shutdown ==");
            clientNetty.shutdown();
            return;
          }
          super.channelUnregistered(ctx);
        }

        @Override public void channelActive(ChannelHandlerContext ctx) throws Exception {
          clientNetty.setActiveDisconnect(false);
          Options options = clientNetty.getOptions();
          Logs.out("已连接 channelActive %s:%s", options.serverIp, options.serverPort);
        }

        @Override public void channelInactive(ChannelHandlerContext ctx) throws Exception {
          super.channelInactive(ctx);
          //final Options opt = clientNetty.getOptions();
          //ToastUtils.showLong("网络连接断开 %s:%s", opt.serverIp, opt.serverPort);
          if (!clientNetty.isActiveDisconnect()) {
            Logs.out("网络连接断开");
          } else {
            Logs.out("主动断开网络");
          }
          connectFailedCount = -1;
        }

        @Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
          Logs.out("出现异常 传递 %s: %s", this.getClass().getSimpleName(), cause.getMessage());
          super.exceptionCaught(ctx, cause);
        }
      });

      pipeline.addLast(new ByteArrayDecoder());
      //pipeline.addLast(new StringDecoder());
      //pipeline.addLast(new MyStringDecoder());
      pipeline.addLast(new SimpleChannelInboundHandler<byte[]>() {
        @Override protected void channelRead0(ChannelHandlerContext ctx, byte[] msg)
            throws Exception {
          Logs.out("recBytes %s:%s", msg.length, BytesUtil.bytes2HexString(msg));
          //clientNetty.send(msg);
          //clientNetty.send("string");
          handle(msg, clientNetty);
        }

        @Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
          Logs.out("出现异常 处理 %s: %s", this.getClass().getSimpleName(), cause.getMessage());
        }
      });

      pipeline.addLast(new SimpleChannelInboundHandler<String>() {
        @Override protected void channelRead0(ChannelHandlerContext ctx, String msg)
            throws Exception {
          Logs.out("recStr %s:%s", msg.length(), msg);
        }
      });

      pipeline.addLast(new ChannelInboundHandlerAdapter() {
        @Override public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
          Logs.out("ChannelInboundHandlerAdapter msg: %s", msg);
        }
      });

      pipeline.addLast(new ByteArrayEncoder());
      pipeline.addLast(new StringEncoder());
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
