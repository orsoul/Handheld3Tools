package com.fanfull.libjava.io.transfer;

public interface IoTransferListener {

  /** 发送数据结果 回调. */
  default void onSend(boolean isSuccess, byte[] data, int offset, int len) {
  }

  /**
   * 接收到数据 的回调方法.
   *
   * @return 如果接收到的数据符合预期返回 true；否则返回 false
   */
  boolean onReceive(byte[] data);

  /** 回复超时. */
  void onReceiveTimeout();

  /** 接收线程 开始运行. */
  default void onStartReceive() {
  }

  /** 接收线程 停止运行. */
  void onStopReceive();
}
