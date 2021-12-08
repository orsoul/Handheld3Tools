package org.orsoul.baselib.tcp.msg;

import com.fanfull.libjava.io.socketClient.ReceiveListener;

/**
 * 实现了 RecieveListener 接口 的抽象类， 继承此类可避免实现所有的接口方法，而只根据需要重写相应的方法
 *
 * @author orsoul
 */
public abstract class ReceiveListenerAbs implements ReceiveListener {

  @Override public void onReceive(String recString) {
    if (!MessageParser4qz.checkRec(recString)) {
      onRecWrong(recString);
    } else {
      String[] split = recString.split(BaseSocketMessage4qz.CH_SPLIT);
      onReceive(MessageParser4qz.replaceSpChar(split));
    }
  }

  /**
   * @param split 保留头尾的*#.
   */
  protected void onReceive(String[] split) {
  }

  protected void onRecWrong(String info) {
  }

  @Override public void onReceive(byte[] data) {
  }

  @Override public void onConnect(String serverIp, int serverPort) {
  }

  @Override public void onConnectFailed(String serverIp, int serverPort) {
  }

  @Override public void onDisconnect() {
  }

  @Override public void onTimeout() {
  }
}
