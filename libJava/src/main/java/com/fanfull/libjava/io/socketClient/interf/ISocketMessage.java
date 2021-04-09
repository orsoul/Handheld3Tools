package com.fanfull.libjava.io.socketClient.interf;

/**
 * TCP通讯消息 接口
 */
public interface ISocketMessage {
  /**
   * @return 返回要发送的 数据
   */
  byte[] getData();
}
