package org.orsoul.baselib.io.socketClient;

public class Options {
  public String serverIp;
  public int serverPort;

  /** 断线重连 */
  public boolean autoReconnect = false;
  /** 重连 时间隔 */
  public int reconnectInterval = 3000;
  /** 连接超时 */
  public int connectTimeout = 5000;
}