package com.fanfull.libjava.io.socketClient;

import com.fanfull.libjava.io.socketClient.interf.IReceiveListener;

/**
 * 将字节数组 转成字符串
 */
public interface ReceiveListener extends IReceiveListener<String> {

  //void onReceive(String recString);

  @Override default String convert(byte[] data, int len) {
    return new String(data, 0, len);
  }

  @Override default void onDisconnect(String serverIp, int serverPort, boolean isActive) {
    onDisconnect();
  }

  void onDisconnect();

  void onTimeout();

  @Override default void onConnectFailed(Throwable e) {
  }

  default void onConnectFailed(String serverIp, int serverPort) {
  }
}