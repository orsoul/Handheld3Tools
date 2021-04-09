package com.fanfull.libjava.io.socketClient.interf;

/**
 * Tcp Client 回调应用层的接口
 * 应用层必须实现此接口，从而可以接收事件通知。
 * 从服务器接收到的数据也是通过这个接口递交应用层的。
 */
public interface ISocketClientListener {
  /**
   * Socket组件通知应用层，Socket 连接已经成功建立
   *
   * @param serverIp 服务器的IP地址
   * @param serverPort 服务器的端口号
   */
  void onConnect(String serverIp, int serverPort);

  /**
   * Socket组件通知应用层，Socket 连接失败
   */
  void onConnectFailed(Throwable e);

  /**
   * Socket组件通知应用层，Socket 连接已经断开 应用层主动调用disconnect方法断开连接，或者服务器断开连接，或者网络原因导致连接断开，
   * 都会导致onDisconnect 被回调。但反复尝试重连时，此方法不会被重复调用。
   */
  void onDisconnect(String serverIp, int serverPort);

  /** Socket组件回调此方法，通知应用层收到了数据. */
  boolean onReceive(byte[] data);

  /** 发送数据结果 回调. */
  default void onSend(boolean isSuccess, byte[] data, int offset, int len) {
  }
}
