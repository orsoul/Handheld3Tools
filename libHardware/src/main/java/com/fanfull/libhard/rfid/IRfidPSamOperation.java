package com.fanfull.libhard.rfid;

/**
 * PSam卡、cpu卡 操作接口.
 */
public interface IRfidPSamOperation extends IRfidOperation {

  /** resetPSam指令.执行成功返回大于1的整数 */
  int resetPSam(byte[] responseBuff);

  /**
   * 与PSam卡交互.执行成功返回大于1的整数
   *
   * @param withReset true 发送数据前执行reset指令；false 发送数据前不执行resetPSam指令，用于连续发送多条指令.
   */
  int send2PSam(byte[] cmd, byte[] responseBuff, boolean withReset);

  /** 与PSam卡交互.执行成功返回大于1的整数，发送数据前执行reset指令 */
  int send2PSam(byte[] cmd, byte[] responseBuff);

  /** resetCpu指令.执行成功返回大于1的整数 */
  int resetCpu(byte[] responseBuff);

  /**
   * 与cpu用户卡交互.执行成功返回大于1的整数
   *
   * @param withReset true 发送数据前执行reset指令；false 发送数据前不执行reset指令，用于连续发送多条指令.
   */
  int send2Cpu(byte[] cmd, byte[] responseBuff, boolean withReset);

  /** 与cpu用户卡交互.执行成功返回大于1的整数，发送数据前执行resetCpu指令 */
  int send2Cpu(byte[] cmd, byte[] responseBuff);
}
