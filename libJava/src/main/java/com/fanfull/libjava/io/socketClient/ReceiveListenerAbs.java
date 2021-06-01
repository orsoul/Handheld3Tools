package com.fanfull.libjava.io.socketClient;

import com.fanfull.libjava.io.socketClient.message1.BaseSocketMessage4qz;

/**
 * 实现了 RecieveListener 接口 的抽象类， 继承此类可避免实现所有的接口方法，而只根据需要重写相应的方法
 *
 * @author orsoul
 */
public abstract class ReceiveListenerAbs implements ReceiveListener {

  public static boolean checkRec(String rec) {
    return rec != null && rec.matches("^\\*\\d+ .+ \\d+#$");
  }

  public static String[] replaceSpChar(String... source) {
    if (source == null || source.length == 0) {
      return source;
    }
    String[] newStr = new String[source.length];
    for (int i = 0; i < source.length; i++) {
      newStr[i] = replaceSpChar(source[i]);
    }
    return newStr;
  }

  public static String replaceSpChar(String source) {
    if (source == null || source.equals("")) {
      return source;
    }
    if (source.contains(BaseSocketMessage4qz.SP_HEAD_SEND)) {
      source =
          source.replaceAll(BaseSocketMessage4qz.SP_HEAD_SEND, BaseSocketMessage4qz.CH_HEAD_SEND);
    }
    if (source.contains(BaseSocketMessage4qz.SP_SPACE)) {
      source = source.replaceAll(BaseSocketMessage4qz.SP_SPACE, BaseSocketMessage4qz.CH_SPLIT);
    }
    if (source.contains(BaseSocketMessage4qz.SP_END)) {
      source = source.replaceAll(BaseSocketMessage4qz.SP_END, BaseSocketMessage4qz.CH_END);
    }
    if (source.contains(BaseSocketMessage4qz.SP_HEAD_REC)) {
      source =
          source.replaceAll(BaseSocketMessage4qz.SP_HEAD_REC, BaseSocketMessage4qz.CH_HEAD_REC);
    }
    return source;
  }

  /** 格式错误返回null；否则返回分割后的字符串数组，去掉头尾的 *#. */
  public static String[] splitRec(String recString) {
    if (!checkRec(recString)) {
      return null;
    }
    String[] split =
        recString.substring(1, recString.length() - 1).split(BaseSocketMessage4qz.CH_SPLIT);
    return replaceSpChar(split);
  }

  @Override public void onReceive(String recString) {
    if (!checkRec(recString)) {
      onRecWrong(recString);
    } else {
      String[] split = recString.split(BaseSocketMessage4qz.CH_SPLIT);
      onReceive(replaceSpChar(split));
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
