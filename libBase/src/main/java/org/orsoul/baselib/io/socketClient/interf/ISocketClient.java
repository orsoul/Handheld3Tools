package org.orsoul.baselib.io.socketClient.interf;

/**
 * TCP客户端接口。
 *
 * @author orsoul
 */
public interface ISocketClient {

  boolean removeSocketClientListener(ISocketClientListener listener);

  boolean addSocketClientListener(ISocketClientListener listener);

  /** 连接服务器 */
  boolean connect();

  /** 断开和服务器的连接 */
  void disconnect();

  /** 查询当前的连接状态. */
  boolean isConnected();
}
