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
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

public class NettyClientDemo {
  static String ip = "192.168.11.246";
  static int port = 23579;
  static int timeout = 5;

  static NioEventLoopGroup group;
  static Bootstrap bootstrap;
  static Channel channel;

  public static void main(String[] args) {
    init(ip, port);
    System.out.println("connect end");
  }

  public static void connect() {
    if (channel != null && channel.isActive()) {
      return;
    }
    ChannelFuture future = bootstrap.connect(ip, port);
    future.addListener((ChannelFutureListener) future1 -> {
      if (!future1.isSuccess()) {
        connect();
      }
    });
    Channel channel = future.channel();
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
              pipeline.addLast(new HeartbeatServerHandler());
            }
          });
      ChannelFuture future = bootstrap.connect(host, port);
      future.addListener((ChannelFutureListener) future1 -> {
        if (!future1.isSuccess()) {
          connect();
        }
      });
      future.channel().closeFuture().sync();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      group.shutdownGracefully();
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

        if (event.state() == IdleState.READER_IDLE) {

          type = "read idle";
        } else if (event.state() == IdleState.WRITER_IDLE) {
          type = "write idle";
          ctx.writeAndFlush(HEARTBEAT_SEQUENCE.duplicate())
              .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        } else if (event.state() == IdleState.ALL_IDLE) {
          type = "all idle";
        }
        System.out.println(ctx.channel().remoteAddress() + "超时类型：" + type);
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
      connect();
    }

    @Override public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      System.out.printf("channelRead: %s\n", msg);
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
