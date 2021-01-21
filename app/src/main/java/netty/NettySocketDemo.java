package netty;

import org.orsoul.baselib.util.BytesUtil;

import java.util.UUID;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class NettySocketDemo {
  public static void main(String[] args) throws Exception {
    //负责接收客户端连接
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    //负责处理连接
    EventLoopGroup workerGroup = new NioEventLoopGroup();

    try {
      ServerBootstrap serverBootstrap = new ServerBootstrap();
      // 在服务器端的handler()方法表示对bossGroup起作用
      // childHandler表示对wokerGroup起作用
      serverBootstrap.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .localAddress(54321)
          .childHandler(new ChannelInitializer<SocketChannel>() { // 绑定客户端连接时候触发操作

            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
              System.out.printf("客户端 %s:%s 接入-初始化\n",
                  ch.localAddress().getAddress().getHostAddress(),
                  ch.localAddress().getPort());

              // 这里将LengthFieldBasedFrameDecoder添加到pipeline的首位，因为其需要对接收到的数据
              // 进行长度字段解码，这里也会对数据进行粘包和拆包处理

              //maxFrameLength：指定了每个包所能传递的最大数据包大小；
              //lengthFieldOffset：指定了长度字段在字节码中的偏移量；
              //lengthFieldLength：指定了长度字段所占用的字节长度；
              //lengthAdjustment：对一些不仅包含有消息头和消息体的数据进行消息头的长度的调整，
              // 这样就可以只得到消息体的数据，这里的lengthAdjustment指定的就是消息头的长度；
              //initialBytesToStrip：对于长度字段在消息头中间的情况，可以通过initialBytesToStrip忽略掉消息头以及长度字段占用的字节。
              ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(
                  Integer.MAX_VALUE, // 解码的帧的最大长度
                  2, // head1长度,属性的起始位（偏移位），包中存放有整个大数据包长度的字节，这段字节的其实位置
                  2, // length长度,属性的长度，即存放整个大数据包长度的字节所占的长度
                  1, // head2长度,调节值，在总长被定义为包含包头长度时，修正信息长度
                  4)); // 跳过的字节数，根据需要我们跳过lengthFieldLength个字节，以便接收端直接接受到不含“长度属性”的内容

              // LengthFieldPrepender是一个编码器，主要是在响应字节数据前面添加字节长度字段
              //ch.pipeline().addLast(new LengthFieldPrepender(2));

              //ch.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8)); // 字符串解码
              //ch.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8)); // 字符串编码

              //ch.pipeline().addLast(new MyServerHandler());
              ch.pipeline().addLast(new ClientDataHandler());
            }

            @Override public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
              super.channelUnregistered(ctx);
            }
          });

      // 启动服务
      ChannelFuture channelFuture = serverBootstrap.bind(54321).sync();
      System.out.printf("================ server %s 已启动 ================\n",
          channelFuture.channel().localAddress());

      channelFuture.channel().closeFuture().sync(); // 关闭服务器通道
    } finally {
      // 释放线程池资源
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }

  static class MyServerInitializer extends ChannelInitializer<SocketChannel> {

    /**
     * 当服务器端与客户端进行建立连接的时候会触发，如果没有触发读写操作，则客户端和客户端之间不会进行数据通信，也就是channelRead0不会执行，
     * 当通道连接的时候，触发channelActive方法向服务端发送数据触发服务器端的handler的channelRead0回调，然后
     * 服务端向客户端发送数据触发客户端的channelRead0，依次触发。
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
      ctx.writeAndFlush("来自与客户端的问题!");
      System.out.println("connect");
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
      ChannelPipeline pipeline = ch.pipeline();

      pipeline.addLast(new LengthFieldBasedFrameDecoder(
          Integer.MAX_VALUE, // 解码的帧的最大长度
          1, // 长度属性的起始位（偏移位），包中存放有整个大数据包长度的字节，这段字节的其实位置
          4, // 长度属性的长度，即存放整个大数据包长度的字节所占的长度
          0, // 长度调节值，在总长被定义为包含包头长度时，修正信息长度
          0)); // 跳过的字节数，根据需要我们跳过lengthFieldLength个字节，以便接收端直接接受到不含“长度属性”的内容
      //pipeline.addLast(new LengthFieldPrepender(4));
      //pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8)); // 字符串解码
      //pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8)); // 字符串编码

      //自己定义的处理器
      pipeline.addLast(new MyServerHandler());
    }
  }

  static class ClientDataHandler extends ChannelInboundHandlerAdapter {
    @Override public void channelActive(ChannelHandlerContext ctx) throws Exception {
      System.out.printf("通道打开, %s --接入--> %s\n",
          ctx.channel().remoteAddress(),
          ctx.channel().localAddress());
    }

    @Override public void channelInactive(ChannelHandlerContext ctx) throws Exception {
      System.out.printf("通道关闭, %s --断开--> %s\n",
          ctx.channel().remoteAddress(),
          ctx.channel().localAddress());
    }

    @Override public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      System.out.printf("channelRead123: %s", msg);
      if (msg instanceof ByteBuf) {
        ByteBuf bb = (ByteBuf) msg;

        int readableBytes = bb.readableBytes();
        int type = bb.getUnsignedByte(0);
        System.out.printf("readableBytes:%s,type:%s\n", readableBytes, type);

        byte[] data = new byte[readableBytes];
        byte[] data2 = new byte[readableBytes - 1];
        bb.getBytes(1, data2);
        ByteBuf byteBuf = bb.readBytes(data);
        System.out.println("data:" + BytesUtil.bytes2HexString(data));
        System.out.println("data2:" + BytesUtil.bytes2HexString(data2));
        //System.out.println("byteBuf:" + byteBuf);
        System.out.println("json:" + new String(data2));
        //System.out.println("byteBuf:" + byteBuf.to);

        //byte b1 = bb.readByte();
        //byte b2 = bb.readByte();
        //System.out.println(b1 + "   " + b2);
      }
      super.channelRead(ctx, msg);
    }

    @Override public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
      System.out.println("channelReadComplete");
      super.channelReadComplete(ctx);
    }

    @Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
        throws Exception {
      System.out.println("exceptionCaught cause: " + cause.getMessage());
      super.exceptionCaught(ctx, cause);
    }
  }

  static class MyServerHandler extends SimpleChannelInboundHandler<String> {

    @Override public void channelActive(ChannelHandlerContext ctx) throws Exception {
      System.out.printf("通道打开, %s --接入--> %s\n",
          ctx.channel().remoteAddress(),
          ctx.channel().localAddress());
    }

    @Override public void channelInactive(ChannelHandlerContext ctx) throws Exception {
      System.out.printf("通道关闭, %s --断开--> %s\n",
          ctx.channel().remoteAddress(),
          ctx.channel().localAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
      //打印出客户端地址
      System.out.println(ctx.channel().remoteAddress() + "：" + msg);
      ctx.channel().writeAndFlush("form server: " + UUID.randomUUID());
    }

    @Override public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
      System.out.println("channelReadComplete");
      super.channelReadComplete(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
      cause.printStackTrace();
      ctx.close();
    }
  }

  //public class SimpleProtocolDecoder extends ByteToMessageDecoder {

  public class MyProtocolDecoder extends LengthFieldBasedFrameDecoder {

    private static final int HEADER_SIZE = 8;

    /**
     * @param maxFrameLength 帧的最大长度
     * @param lengthFieldOffset length字段偏移的地址
     * @param lengthFieldLength length字段所占的字节长
     * @param lengthAdjustment 修改帧数据长度字段中定义的值，可以为负数 因为有时候我们习惯把头部记入长度,若为负数,则说明要推后多少个字段
     * @param initialBytesToStrip 解析时候跳过多少个长度
     * @param failFast 为true，当frame长度超过maxFrameLength时立即报TooLongFrameException异常，为false，读取完整个帧再报异
     */

    public MyProtocolDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
        int lengthAdjustment, int initialBytesToStrip, boolean failFast) {
      super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment,
          initialBytesToStrip, failFast);
    }

    public MyProtocolDecoder() {
      super(Integer.MAX_VALUE,
          2,
          4,
          2,
          8);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
      //在这里调用父类的方法,实现指得到想要的部分,我在这里全部都要,也可以只要body部分
      in = (ByteBuf) super.decode(ctx, in);

      if (in == null) {
        return null;
      }
      if (in.readableBytes() < HEADER_SIZE) {
        throw new Exception("字节数不足");
      }
      //读取type字段
      byte type = in.readByte();
      //读取flag字段
      byte flag = in.readByte();
      //读取length字段
      int length = in.readInt();

      if (in.readableBytes() != length) {
        throw new Exception("标记的长度不符合实际长度");
      }
      //读取body
      byte[] bytes = new byte[in.readableBytes()];
      in.readBytes(bytes);

      return null;
    }
  }
}