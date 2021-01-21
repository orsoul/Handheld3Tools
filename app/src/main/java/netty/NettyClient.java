package netty;

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
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

public class NettyClient {
  static String ip = "192.168.11.246";
  static int port = 23579;
  static int timeout = 5;

  static NioEventLoopGroup group;
  static Bootstrap bootstrap;
  static Channel channel;

  static boolean autoConnect;

  public static void main(String[] args) {
    autoConnect = true;
    init(ip, port);
    System.out.println("main end");
  }

  public static void connect() {
    if (channel != null && channel.isActive()) {
      return;
    }
    ChannelFuture future = bootstrap.connect(ip, port);
    future.addListener((ChannelFutureListener) future1 -> {
      System.out.println("Connect Listener");
      if (future1.isSuccess()) {
        channel = future.channel();
        return;
      }
      future1.channel().eventLoop().schedule(() -> {
        if (autoConnect) connect();
      }, timeout, TimeUnit.SECONDS);
    });
    try {
      future.sync();
      future.channel().closeFuture().sync();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println("connect end");
  }

  public static void init(String host, int port) {
    //ip = "192.168.11.246";
    //port = 23579;
    group = new NioEventLoopGroup();
    bootstrap = new Bootstrap();
    try {
      bootstrap
          .group(group)
          .channel(NioSocketChannel.class)
          .option(ChannelOption.TCP_NODELAY, true)
          .handler(new ChannelInitializer<NioSocketChannel>() {
            @Override protected void initChannel(NioSocketChannel ch) {
              ChannelPipeline pipeline = ch.pipeline();
              pipeline.addLast(new IdleStateHandler(
                  timeout, timeout, timeout, TimeUnit.SECONDS));
              pipeline.addLast(new LengthFieldBasedFrameDecoder(
                  Integer.MAX_VALUE, // 解码的帧的最大长度
                  0, // head1长度,属性的起始位（偏移位），包中存放有整个大数据包长度的字节，这段字节的其实位置
                  1, // length长度,属性的长度，即存放整个大数据包长度的字节所占的长度
                  0, // head2长度,调节值，在总长被定义为包含包头长度时，修正信息长度
                  1));
              //pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
              pipeline.addLast(new HeartbeatServerHandler());
            }
          });
      connect();
    } catch (Exception e) {
      //e.printStackTrace();
    } finally {
      //group.shutdownGracefully();
    }
  }

  public static class HeartbeatServerHandler extends ChannelInboundHandlerAdapter {
    private final ByteBuf HEARTBEAT_SEQUENCE =
        Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Heartbeat", CharsetUtil.UTF_8));  // 1

    @Override public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
        throws Exception {

      if (evt instanceof IdleStateEvent) {  // 2
        IdleStateEvent event = (IdleStateEvent) evt;
        String type = "";
        switch (event.state()) {
          case READER_IDLE:
            System.out.println(ctx.channel().remoteAddress() + "超时 READER_IDLE");
            ctx.writeAndFlush(HEARTBEAT_SEQUENCE.duplicate())
                .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            break;
          case WRITER_IDLE:
            System.out.println(ctx.channel().remoteAddress() + "超时 WRITER_IDLE");
            break;
          case ALL_IDLE:
            System.out.println(ctx.channel().remoteAddress() + "超时 ALL_IDLE");
            break;
        }
      } else {
        super.userEventTriggered(ctx, evt);
      }
    }

    @Override public void channelActive(ChannelHandlerContext ctx) throws Exception {
      System.out.printf("通道打开, %s --接入--> %s\n",
          ctx.channel().remoteAddress(),
          ctx.channel().localAddress());
    }

    @Override public void channelInactive(ChannelHandlerContext ctx) throws Exception {
      System.out.printf("通道关闭, %s --断开--> %s\n",
          ctx.channel().remoteAddress(),
          ctx.channel().localAddress());
      if (autoConnect) {
        Thread.sleep(timeout * 1000);
        connect();
      }
    }

    byte[] buff = new byte[1024];

    @Override public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      System.out.printf("channelRead: %s\n", msg);
      ByteBuf bb = (ByteBuf) msg;
      int len = bb.readableBytes();
      System.out.println("readableBytes:" + len);
      String info = null;
      if (0 < len) {
        bb.readBytes(buff, 0, len);
        info = new String(buff, 0, len);
      }
      switch (info) {
        case "886":
          autoConnect = false;
          channel.close();
          group.shutdownGracefully();
          break;
        case "88":
          channel.close();
          break;
        default:
          ByteBuf reply =
              Unpooled.unreleasableBuffer(Unpooled.copiedBuffer(info, CharsetUtil.UTF_8));
          //ctx.channel().writeAndFlush(reply);
          channel.writeAndFlush(reply);
          break;
      }
    }

    @Override public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
      System.out.println("channelReadComplete");
    }

    @Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
        throws Exception {
      System.out.println("exceptionCaught cause: " + cause.getMessage());
    }
  }
}
