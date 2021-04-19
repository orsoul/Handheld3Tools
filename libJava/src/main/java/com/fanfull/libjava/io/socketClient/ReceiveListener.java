package com.fanfull.libjava.io.socketClient;

public interface ReceiveListener {
  void onConnect(String serverIp, int serverPort);

  default void onConnectFailed(String serverIp, int serverPort) {
  }

  void onReceive(byte[] data, int len);

  void onReceive(String recString);

  void onDisconnect();

  void onTimeout();
}