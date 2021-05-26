package org.orsoul.baselib.data.preference;

/** 全局配置变量. */
public abstract class ConfigGlobal {
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

  /** 是否使用串口与前置通讯. */
  public static boolean useSerialPort = false;

  /** 封袋业务 单独读锁片环节 是否启用. */
  public static boolean readLockLabelEnable = false;
  /** 单独扫描锁片 第1阶段 等待时间，单位秒. */
  public static int timesWait = 8;
  /** 单独扫描锁片 第2阶段 扫描高频卡，单位秒. */
  public static int timesFindNfc = 8;

  public static int bagIdVersion = 5;
}
