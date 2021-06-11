package org.orsoul.baselib.data.preference;

/** 全局配置变量. */
public final class ConfigGlobal {
  // TODO: 2020/10/29 全局配置
  /** 测试模式. */
  public static boolean isTestMode;
  /** 是否使用老袋. */
  public static boolean isUseOldBag = false;
  /** 是否需要复核登录. */
  public static boolean needCheckLogin = true;
  /** 与前置通讯是否启用校验位. */
  public static boolean isUseCheckByte = false;

  /** 是否有PSam卡. */
  public static boolean havePSam = false;
  /** 是否为人行. */
  public static boolean isRh = false;

  /** 是否启用指纹登录. */
  public static boolean fingerEnable = false;
  /** 服务器指纹库 版本. */
  public static int fingerLibVersion;

  /** 是否使用串口与前置通讯. */
  public static boolean useSerialPort = false;

  /** 封袋业务 单独读锁片环节 是否启用. */
  public static boolean readLockLabelEnable = false;
  /** 单独扫描锁片 第1阶段 等待时间，单位秒. */
  public static int timesWait = 8;
  /** 单独扫描锁片 第2阶段 扫描高频卡，单位秒. */
  public static int timesFindNfc = 8;

  public static int bagIdVersion = 5;
  /** 自动登录. */
  public static boolean autoLogin;

  /** 心跳. */
  public static boolean heartBeatEnable;
  /** 断线重连. */
  public static boolean reconnectEnable;

  /** 心跳 时间隔，单位秒. */
  public int heartBeatInterval = 10;
  /** 心跳 失败次数，失败达到该次数后 认为连接已断开. */
  public int disconnectCount = 3;

  /** 连接超时，单位毫秒. */
  public int connectTimeout = 5000;
}
