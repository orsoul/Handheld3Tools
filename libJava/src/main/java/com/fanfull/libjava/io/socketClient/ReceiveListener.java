package com.fanfull.libjava.io.socketClient;

import com.fanfull.libjava.io.socketClient.interf.ISocketClientListener;

public interface ReceiveListener extends ISocketClientListener {
  void onConnect(String serverIp, int serverPort);

  void onReceive(String recString);

  default void onDisconnect(String serverIp, int serverPort, boolean isActive) {
    onDisconnect();
  }

  void onDisconnect();

  void onTimeout();

  default void onConnectFailed(Throwable e) {
  }

  default void onConnectFailed(String serverIp, int serverPort) {
  }
}