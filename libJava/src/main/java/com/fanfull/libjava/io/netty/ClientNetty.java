package com.fanfull.libjava.io.netty;

import com.fanfull.libjava.io.netty.future.MsgFuture;
import com.fanfull.libjava.io.netty.future.SyncWriteMap;
import com.fanfull.libjava.io.netty.handler.HeadEndEncoder;
import com.fanfull.libjava.io.netty.handler.ReconnectBeatHandler;
import com.fanfull.libjava.io.socketClient.Options;
import com.fanfull.libjava.io.socketClient.ReceiveListener;
import com.fanfull.libjava.io.socketClient.interf.ISocketClient;
import com.fanfull.libjava.io.socketClient.interf.ISocketClientListener;
import com.fanfull.libjava.util.Logs;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class ClientNetty implements ISocketClient {
  private boolean isShutdown;
  private final Options options;
  private ISocketClientListener clientListener;
  /** 是否 主动断开连接，如果是主动断开连接，不再执行重连. */
  private boolean isActiveDisconnect;

  private NioEventLoopGroup group;
  private Bootstrap bootstrap;
  private Channel channel;
  private ChannelFuture f;

  public boolean isShutdown() {
    return isShutdown;
  }

  public void setShutdown(boolean shutdown) {
    isShutdown = shutdown;
  }

  public Options getOptions() {
    return options;
  }

  public void setIpPort(String ip, int port) {
    options.serverIp = ip;
    options.serverPort = port;
  }

  public void setIp(String ip) {
    options.serverIp = ip;
  }

  public String getIp() {
    return options.serverIp;
  }

  public int getPort() {
    return options.serverPort;
  }

  public void setPort(int port) {
    options.serverPort = port;
  }

  public boolean isHeartBeatEnable() {
    return options.heartBeatEnable;
  }

  public void setHeartBeatEnable(boolean heartBeatEnable) {
    this.options.heartBeatEnable = heartBeatEnable;
  }

  public boolean isReconnectEnable() {
    return options.reconnectEnable;
  }

  public void setReconnectEnable(boolean reconnectEnable) {
    this.options.reconnectEnable = reconnectEnable;
  }

  public boolean isActiveDisconnect() {
    return isActiveDisconnect;
  }

  public void setActiveDisconnect(boolean activeDisconnect) {
    isActiveDisconnect = activeDisconnect;
  }

  public ClientNetty(Options options) {
    this.options = options;
  }

  public synchronized void init(ChannelInitializer<SocketChannel> channelInitializer) {
    init(channelInitializer, true);
  }

  public synchronized void init(ChannelInitializer<SocketChannel> channelInitializer,
      boolean connect) {
    try {
      group = new NioEventLoopGroup();
      bootstrap = new Bootstrap();
      bootstrap.group(group)// 指定EventLoopGroup
          .channel(NioSocketChannel.class) // 指定channel类型
          .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, options.connectTimeout)
          .handler(channelInitializer);
      // 连接到服务端
      //ChannelFuture channelFuture = bootstrap.connect(ip, port);
      // 添加连接状态监听
      //channelFuture.addListener(new ConnectListener(this));
      //获取连接通道
      //channel = channelFuture.sync().channel();
      if (connect) {
        connectChannelFuture();
      }
    } catch (Exception e) {
      Logs.out("Exception \n", e.getMessage());
      e.printStackTrace();
    }
  }

  @Override public boolean removeSocketClientListener(ISocketClientListener listener) {
    return false;
  }

  @Override public boolean addSocketClientListener(ISocketClientListener listener) {
    return false;
  }

  @Override public boolean connect() {
    try {
      return connectChannelFuture().sync().isSuccess();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return false;
  }

  public ChannelFuture connectChannelFuture(ISocketClientListener listener) {
    if (channel != null && channel.isActive()) {
      //channel.disconnect();
      return f;
    }
    f = bootstrap.connect(options.serverIp, options.serverPort);
    f.addListener(future -> {
      Logs.out("connect cause: %s", future.cause());
      if (future.cause() == null) {
        setActiveDisconnect(false);
        channel = f.channel();
        if (listener != null) {
          listener.onConnect(options.serverIp, options.serverPort);
        }
        if (clientListener != null) {
          clientListener.onConnect(options.serverIp, options.serverPort);
        }
      } else {
        if (listener != null) {
          listener.onConnectFailed(future.cause());
        }
        if (clientListener != null) {
          clientListener.onConnectFailed(future.cause());
        }
      }
    });
    Logs.out("connect end");
    return f;
  }

  public ChannelFuture connectChannelFuture() {
    return connectChannelFuture(null);
  }

  private <E> E sendAndWait(Object msg, final MsgFuture<E> writeFuture, final long timeout)
      throws Exception {
    if (channel == null) {
      throw new Exception("未连接");
    }

    channel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
      public void operationComplete(ChannelFuture future) throws Exception {
        writeFuture.setSendSuccess(future.isSuccess());
        writeFuture.setCause(future.cause());
        //失败移除
        if (!writeFuture.isSendSuccess()) {
          SyncWriteMap.syncKey.remove(writeFuture.requestId());
        }
      }
    });

    E response = writeFuture.get(timeout, TimeUnit.MILLISECONDS);
    if (response == null) {
      if (writeFuture.isTimeout()) {
        return null;
      } else {
        // write exception
        throw new Exception(writeFuture.cause());
      }
    }
    return response;
  }

  public void send(Object msg, ReceiveListener receiveListener) {
    if (channel != null) {
      channel.writeAndFlush(msg).addListener(
          new GenericFutureListener<Future<? super Void>>() {
            @Override public void operationComplete(Future<? super Void> future) {
              if (receiveListener != null) {
                receiveListener.onSend(future.isSuccess(), msg);
              }
            }
          });
    }
  }

  public boolean send(Object msg) {
    boolean reVal = false;
    if (isConnected() && msg != null) {
      try {
        reVal = channel.writeAndFlush(msg).sync().isSuccess();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    Logs.out("send %s:%s", reVal, msg);
    return reVal;
  }

  //public boolean send(String msg) {
  //  if (channel != null) {
  //    return channel.writeAndFlush(msg).isSuccess();
  //  }
  //  return false;
  //}
  //public boolean send(byte[] data) {
  //  if (channel != null) {
  //    return channel.writeAndFlush(data).isSuccess();
  //  }
  //  return false;
  //}

  @Override public boolean isConnected() {
    return channel != null && channel.isActive();
  }

  @Override public void disconnect() {
    setActiveDisconnect(true);
    if (channel != null) {
      f = null;
      channel.close();
      channel = null;
    }
  }

  public void shutdown() {
    disconnect();
    if (group != null) {
      isShutdown = true;
      group.shutdownGracefully();
      group.terminationFuture();
    }
  }

  public static void main(String[] args) {
    Options sOptions = new Options();
    sOptions.serverIp = "192.168.11.246";
    sOptions.serverPort = 23456;
    sOptions.reconnectEnable = true;
    sOptions.heartBeatEnable = true;

    ClientNetty clientNetty = new ClientNetty(sOptions);
    clientNetty.init(new ChannelInitializer<SocketChannel>() { // 指定Handler
      @Override protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();
        // 心跳
        pipeline.addLast(new IdleStateHandler(0,
            sOptions.heartBeatInterval,
            0));

        //pipeline.addLast(new ReconnectBeatHandler(clientNetty) {
        //  @Override public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //    Logs.out("ReconnectHandler channelInactive");
        //  }
        //});
        pipeline.addLast(new ReconnectBeatHandler(clientNetty) {
          private int connectFailedCount;

          @Override public void channelActive(ChannelHandlerContext ctx) throws Exception {
            Logs.out("建立连接成功");
          }

          @Override public void channelUnregistered(ChannelHandlerContext ctx)
              throws Exception {
            Logs.out("connectFailedCount:%s", connectFailedCount);
            ++connectFailedCount;
            Logs.out("网络连接失败 connectFailedCount:%s", connectFailedCount);
            super.channelUnregistered(ctx);
          }

          @Override public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
          }
        });

        // 粘包处理器
        //pipeline.addLast(clientNetty.headEndDecoder);
        // 粘包处理之后的字节数据 转换为 字符串
        //pipeline.addLast(new StringDecoder(Charset.forName("utf-8")));
        pipeline.addLast(new StringDecoder(StandardCharsets.UTF_8));
        pipeline.addLast(new EchoClientHandler(clientNetty));
        pipeline.addLast(new SimpleChannelInboundHandler<byte[]>() {
          @Override protected void channelRead0(ChannelHandlerContext ctx, byte[] bb)
              throws Exception {
            //int len = bb.readableBytes();
            //Logs.out("readableBytes:" + len);
            //if (0 < len) {
            String info = new String(bb);
            Logs.out("channelRead0 byte[]:" + info);
            ClientDemo.handle(info, clientNetty);
            //}
          }
        });
        pipeline.addLast(new ChannelInboundHandlerAdapter() {
          @Override public void channelRead(ChannelHandlerContext ctx, Object msg)
              throws Exception {
            Logs.out("channelRead: %s\n", msg);
            String info = null;
            if (msg instanceof ByteBuf) {
              ByteBuf bb = (ByteBuf) msg;
              int len = bb.readableBytes();
              Logs.out("readableBytes:" + len);
              if (0 < len) {
                info = new String(bb.array(), bb.readerIndex(), len);
                Logs.out("info:" + info);
              }
            } else if (msg instanceof String) {
              info = (String) msg;
            }
            ClientDemo.handle(info, clientNetty);
          }
        });

        // 编码器，为发送字符串数据 加上头尾标识
        pipeline.addLast(new HeadEndEncoder("$", "#"));
        pipeline.addLast(new ByteArrayEncoder());
      }
    });

    Logs.out("====== main end ======");
  }

  static class EchoClientHandler extends SimpleChannelInboundHandler<String> {
    private ClientNetty clientNetty;

    public EchoClientHandler(ClientNetty clientNetty) {
      this.clientNetty = clientNetty;
    }

    @Override public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
        throws Exception {
      if (clientNetty.options.heartBeatEnable && evt instanceof IdleStateEvent) {
        IdleStateEvent event = (IdleStateEvent) evt;
        String type = "";
        switch (event.state()) {
          case READER_IDLE:
            //Logs.out(ctx.channel().remoteAddress() + "超时 READER_IDLE");
            Logs.out(ctx.channel().remoteAddress() + "超时 READER_IDLE");
            //disconnect();
            //ctx.writeAndFlush(HEARTBEAT_SEQUENCE.duplicate())
            //    .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            break;
          case WRITER_IDLE:
            Logs.out(ctx.channel().remoteAddress() + "超时 WRITER_IDLE");
            //ctx.writeAndFlush("heartBeat");
            clientNetty.send("heartBeat");
            break;
          case ALL_IDLE:
            Logs.out(ctx.channel().remoteAddress() + "超时 ALL_IDLE");
            break;
        }
      } else {
        super.userEventTriggered(ctx, evt);
      }
    }

    @Override public void channelActive(ChannelHandlerContext ctx) throws Exception {
      Logs.out("通道打开, %s --接入--> %s\n",
          ctx.channel().remoteAddress(),
          ctx.channel().localAddress());

      ctx.writeAndFlush("hello server!");
    }

    @Override public void channelInactive(ChannelHandlerContext ctx) throws Exception {
      Logs.out("通道关闭, %s --断开--> %s\n",
          ctx.channel().remoteAddress(),
          ctx.channel().localAddress());
      //if (options.reconnectEnable) {
      //  Thread.sleep(options.reconnectInterval);
      //  connect();
      //}
    }

    @Override public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception {
      Logs.out("channelUnregistered");

      if (clientNetty.isShutdown || !clientNetty.isReconnectEnable()) {
        return;
      }

      ctx.channel().eventLoop().schedule(() -> {
        Logs.out("Reconnecting to: "
            + clientNetty.options.serverIp
            + ':'
            + clientNetty.options.serverPort);
        clientNetty.connect();
      }, clientNetty.options.reconnectInterval, TimeUnit.MILLISECONDS);
    }

    @Override protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
      ClientDemo.handle(msg, clientNetty);
    }

    public void fireChannelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    }

    @Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
        throws Exception {
      Logs.out("exceptionCaught cause: " + cause.getMessage());
      cause.printStackTrace();
      ctx.close();
    }
  }
}
