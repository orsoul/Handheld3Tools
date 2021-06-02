package com.fanfull.libjava.io.socketClient;

public class Options {
  public String serverIp;
  public int serverPort;

  /** 断线重连. */
  public boolean reconnectEnable = false;
  /** 重连 时间隔，单位毫秒. */
  public int reconnectInterval = 5000;

  /** 心跳. */
  public boolean heartBeatEnable = false;
  /** 心跳 时间隔，单位秒. */
  public int heartBeatInterval = 10;
  /** 心跳 失败次数，失败达到该次数后 认为连接已断开. */
  public int disconnectCount = 3;

  /** 连接超时，单位毫秒. */
  public int connectTimeout = 5000;
}