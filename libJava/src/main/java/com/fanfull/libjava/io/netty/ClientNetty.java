package com.fanfull.libjava.io.netty;

import com.fanfull.libjava.io.netty.future.MsgFuture;
import com.fanfull.libjava.io.netty.future.SyncWriteMap;
import com.fanfull.libjava.io.socketClient.Options;
import com.fanfull.libjava.io.socketClient.ReceiveStringListener;
import com.fanfull.libjava.io.socketClient.interf.ISocketClient;
import com.fanfull.libjava.io.socketClient.interf.ISocketClientListener;
import com.fanfull.libjava.util.Logs;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class ClientNetty implements ISocketClient {
  private boolean isShutdown;
  private final Options options;
  /** 是否 主动断开连接，如果是主动断开连接，不再执行重连. */
  private boolean isActiveDisconnect;

  /** 监听者 容器 */
  private final Set<ISocketClientListener> clientListenerHashSet = new HashSet<>();

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

  //RxtxChannel
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
      Logs.out("Exception %s", e.getMessage());
      e.printStackTrace();
    }
  }

  private synchronized void init(boolean connect) {
    try {
      group = new NioEventLoopGroup();
      bootstrap = new Bootstrap();
      bootstrap.group(group)// 指定EventLoopGroup
          .channel(NioSocketChannel.class) // 指定channel类型
          .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, options.connectTimeout)
          .handler(new ChannelInitializer<SocketChannel>() {
            @Override protected void initChannel(SocketChannel ch) throws Exception {
              ch.pipeline().addLast(new IdleStateHandler(
                  options.readerIdleTimeSeconds,
                  options.writerIdleTimeSeconds,
                  options.allIdleTimeSeconds));
            }
          });
      if (connect) {
        connectChannelFuture();
      }
    } catch (Exception e) {
      Logs.out("Exception %s", e.getMessage());
      e.printStackTrace();
    }
  }

  @Override public boolean addSocketClientListener(ISocketClientListener listener) {
    return this.clientListenerHashSet.add(listener);
  }

  @Override public boolean removeSocketClientListener(ISocketClientListener listener) {
    return this.clientListenerHashSet.remove(listener);
  }

  /** 同步建立连接 */
  @Override public boolean connect() {
    try {
      //Logs.out("connect start");
      //boolean success = connectChannelFuture().sync().isSuccess();
      //Logs.out("connect end:%s", success);
      //return success;
      return connectChannelFuture().sync().isSuccess();
    } catch (Exception e) {
      //e.printStackTrace();
      //Logs.out("connect end Exception:%s", e.getMessage());
    }
    return false;
  }

  /** 异步建立连接 */
  public ChannelFuture connectChannelFuture(ISocketClientListener listener) {
    if (channel != null && channel.isActive()) {
      //channel.disconnect();
      return f;
    }
    //Logs.out("connectChannelFuture start");
    f = bootstrap.connect(options.serverIp, options.serverPort);
    f.addListener(future -> {
      Logs.out("connect cause: %s", future.cause());
      if (future.cause() == null) {
        setActiveDisconnect(false);
        channel = f.channel();
        if (listener != null) {
          listener.onConnect(options.serverIp, options.serverPort);
        }
        for (ISocketClientListener clientListener : clientListenerHashSet) {
          clientListener.onConnect(options.serverIp, options.serverPort);
        }
      } else {
        if (listener != null) {
          listener.onConnectFailed(future.cause());
        }
        for (ISocketClientListener clientListener : clientListenerHashSet) {
          clientListener.onConnectFailed(future.cause());
        }
      }
    });
    //Logs.out("connectChannelFuture end");
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

  public void send(Object msg, ReceiveStringListener receiveListener) {
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
    //setActiveDisconnect(true);
    if (channel != null) {
      f = null;
      channel.close();
      channel = null;
    }
  }

  public void disconnect(boolean activeDisconnect) {
    setActiveDisconnect(activeDisconnect);
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
    Logs.out("====== main end ======");
  }
}
