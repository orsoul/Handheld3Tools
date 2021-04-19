package com.fanfull.libjava.io.netty;

import com.fanfull.libjava.io.netty.handler.HeadEndDecoder;
import com.fanfull.libjava.io.netty.handler.HeadEndEncoder;
import com.fanfull.libjava.io.socketClient.Options;
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
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

public class ClientNetty {
  private boolean isShutdown;
  private Options options;

  private NioEventLoopGroup group;
  private Bootstrap bootstrap;
  private Channel channel;
  private HeadEndDecoder headEndDecoder = new HeadEndDecoder();

  public boolean isShutdown() {
    return isShutdown;
  }

  public void setShutdown(boolean shutdown) {
    isShutdown = shutdown;
  }

  public Options getOptions() {
    return options;
  }

  public void setOptions(Options options) {
    this.options = options;
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

  public ClientNetty(Options options) {
    this.options = options;
  }

  public synchronized void init(ChannelInitializer<SocketChannel> channelInitializer) {
    try {
      group = new NioEventLoopGroup();
      bootstrap = new Bootstrap();
      bootstrap.group(group)// 指定EventLoopGroup
          .channel(NioSocketChannel.class) // 指定channel类型
          .handler(channelInitializer);
      // 连接到服务端
      //ChannelFuture channelFuture = bootstrap.connect(ip, port);
      // 添加连接状态监听
      //channelFuture.addListener(new ConnectListener(this));
      //获取连接通道
      //channel = channelFuture.sync().channel();
      connect();
    } catch (Exception e) {
      Logs.out("Exception \n", e.getMessage());
      e.printStackTrace();
    }
  }

  public void connect() {
    if (channel != null && channel.isActive()) {
      return;
    }
    ChannelFuture f = bootstrap.connect(options.serverIp, options.serverPort);
    f.addListener((ChannelFutureListener) future1 -> {
      Logs.out("connect cause: %s", future1.cause());
      if (future1.cause() == null) {
        channel = f.channel();
        return;
      }
    });

    //try {
    //future.sync();
    //future.channel().closeFuture().addListener((ChannelFutureListener) future12 -> shutdown());
    //}
    Logs.out("connect end");
  }

  public boolean send(Object msg) {
    if (channel != null) {
      return channel.writeAndFlush(msg).isSuccess();
    }
    return false;
  }

  public boolean send(String msg) {
    if (channel != null) {
      return channel.writeAndFlush(msg).isSuccess();
    }
    return false;
  }

  public boolean send(byte[] data) {
    if (channel != null) {
      return channel.writeAndFlush(data).isSuccess();
    }
    return false;
  }

  public boolean isConnected() {
    return channel != null && channel.isActive();
  }

  public void disconnect() {
    if (channel != null) {
      channel.close();
    }
  }

  public void shutdown() {
    if (group != null) {
      isShutdown = true;
      group.shutdownGracefully();
      group.terminationFuture();
    }
  }

  public static void main(String[] args) {
    Options sOptions = new Options();
    sOptions.serverIp = "192.168.11.246";
    sOptions.serverPort = 23579;
    sOptions.reconnectEnable = true;
    sOptions.heartBeatEnable = false;

    ClientNetty clientNetty = new ClientNetty(sOptions);
    clientNetty.init(new ChannelInitializer<SocketChannel>() { // 指定Handler
      @Override protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();
        // 心跳
        pipeline.addLast(new IdleStateHandler(sOptions.heartBeatInterval + 5,
            sOptions.heartBeatInterval,
            0));

        // 粘包处理器
        pipeline.addLast(clientNetty.headEndDecoder);
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
        //pipeline.addLast(new HeartbeatServerHandler());
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
