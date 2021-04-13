package com.fanfull.libjava.io.socketClient.netty;

import com.fanfull.libjava.io.socketClient.GeneralException;
import com.fanfull.libjava.io.socketClient.Options;
import com.fanfull.libjava.util.Logs;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

public class ClientNetty {
  private boolean isShutdown;
  private Options options;

  private NioEventLoopGroup group;
  private Bootstrap bootstrap;
  private Channel channel;

  public synchronized void init() {
    try {
      group = new NioEventLoopGroup();
      bootstrap = new Bootstrap();
      bootstrap.group(group)// 指定EventLoopGroup
          .channel(NioSocketChannel.class) // 指定channel类型
          .handler(new ChannelInitializer<SocketChannel>() { // 指定Handler
            @Override protected void initChannel(SocketChannel socketChannel) {
              ChannelPipeline pipeline = socketChannel.pipeline();
              // 心跳
              pipeline.addLast(new IdleStateHandler(options.heartBeatInterval + 5,
                  options.heartBeatInterval,
                  0));

              // 粘包处理器
              pipeline.addLast(new HeadEndDecoder());
              // 粘包处理之后的字节数据 转换为 字符串
              pipeline.addLast(new StringDecoder());

              // 编码器，为发送字符串数据 加上头尾标识
              pipeline.addLast(new HeadEndEncoder("$", "#"));
              pipeline.addLast(new EchoClientHandler());
              //pipeline.addLast(new HeartbeatServerHandler());
            }
          });
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
      } else {
      }

      //future1.channel().eventLoop().schedule(() -> {
      //  if (options.reconnectEnable) {
      //    connect();
      //  }
      //}, options.reconnectInterval, TimeUnit.MILLISECONDS);
    });

    //try {
    //future.sync();
    //future.channel().closeFuture().addListener((ChannelFutureListener) future12 -> shutdown());
    //}
    Logs.out("connect end");
  }

  public void send(ChannelHandlerContext ctx, String msg) {
    final ChannelFuture f = channel.writeAndFlush(msg);
    f.addListener((ChannelFutureListener) future -> {
      if (f == future) {
        ctx.close();
      } else {
        throw new GeneralException("f != future");
      }
    });
  }

  public boolean send(String msg) {
    if (channel != null) {
      return channel.writeAndFlush(msg).isSuccess();
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
    Options options = new Options();
    options.serverIp = "192.168.11.246";
    options.serverPort = 23579;
    options.reconnectEnable = true;
    options.heartBeatEnable = false;

    ClientNetty clientNetty = new ClientNetty();
    clientNetty.options = options;
    clientNetty.init();

    Logs.out("====== main end ======");
  }

  private void handle(String info) {
    switch (info) {
      case "886":
      case "*886":
        //options.reconnectEnable = false;
        //channel.close();
        //group.shutdownGracefully();
        shutdown();
        break;
      case "88":
        disconnect();
        break;
      case "heartBeat on":
        options.heartBeatEnable = true;
        break;
      case "heartBeat off":
        options.heartBeatEnable = false;
        break;
      default:
        //ByteBuf reply = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer(info, CharsetUtil.UTF_8));
        //ctx.channel().writeAndFlush(reply);
        //channel.writeAndFlush(info);
        send(info);
        break;
    }
  }

  public class EchoClientHandler extends SimpleChannelInboundHandler<String> {
    @Override public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
        throws Exception {
      if (options.heartBeatEnable && evt instanceof IdleStateEvent) {
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
            send("heartBeat");
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

      if (isShutdown || !options.reconnectEnable) {
        return;
      }

      ctx.channel().eventLoop().schedule(() -> {
        Logs.out("Reconnecting to: " + options.serverIp + ':' + options.serverPort);
        connect();
      }, options.reconnectInterval, TimeUnit.MILLISECONDS);
    }

    @Override protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
      Logs.out("rec:%s", msg);
      handle(msg);
    }

    @Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
        throws Exception {
      Logs.out("exceptionCaught cause: " + cause.getMessage());
    }
  }

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

  public class HeartbeatServerHandler extends ChannelInboundHandlerAdapter {
    private final ByteBuf HEARTBEAT_SEQUENCE =
        Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Heartbeat", CharsetUtil.UTF_8));  // 1

    @Override public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
        throws Exception {

      if (evt instanceof IdleStateEvent) {  // 2
        IdleStateEvent event = (IdleStateEvent) evt;
        String type = "";
        switch (event.state()) {
          case READER_IDLE:
            //Logs.out(ctx.channel().remoteAddress() + "超时 READER_IDLE");
            Logs.out(ctx.channel().remoteAddress() + "超时 READER_IDLE");
            //ctx.writeAndFlush(HEARTBEAT_SEQUENCE.duplicate())
            //    .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            break;
          case WRITER_IDLE:
            Logs.out(ctx.channel().remoteAddress() + "超时 WRITER_IDLE");
            ctx.writeAndFlush(HEARTBEAT_SEQUENCE.duplicate())
                .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
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
    }

    @Override public void channelInactive(ChannelHandlerContext ctx) throws Exception {
      Logs.out("通道关闭, %s --断开--> %s\n",
          ctx.channel().remoteAddress(),
          ctx.channel().localAddress());
      if (options.reconnectEnable) {
        Thread.sleep(options.reconnectInterval);
        connect();
      }
    }

    byte[] buff = new byte[1024];

    @Override public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      Logs.out("channelRead: %s\n", msg);
      ByteBuf bb = (ByteBuf) msg;
      int len = bb.readableBytes();
      Logs.out("readableBytes:" + len);
      //String info = null;
      //if (0 < len) {
      //  bb.readBytes(buff, 0, len);
      //  info = new String(buff, 0, len);
      //}
      //handle(info);
    }

    @Override public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
      Logs.out("channelReadComplete");
    }

    @Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
        throws Exception {
      Logs.out("exceptionCaught cause: " + cause.getMessage());
    }
  }
}
