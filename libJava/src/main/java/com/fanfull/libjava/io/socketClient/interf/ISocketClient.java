package com.fanfull.libjava.io.socketClient.interf;

/**
 * TCP客户端接口。
 *
 * @author orsoul
 */
public interface ISocketClient {

  boolean removeSocketClientListener(ISocketClientListener listener);

  boolean addSocketClientListener(ISocketClientListener listener);

  /** 连接服务器，连接成功或已经连接返回true. */
  boolean connect();

  /** 断开和服务器的连接 */
  void disconnect();

  /** 查询当前的连接状态. */
  boolean isConnected();
}
