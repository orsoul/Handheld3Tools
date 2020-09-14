package com.fanfull.libhard.rfid;

public interface IRfidPSamOperation extends IRfidOperation {

  /**
   * 与PSam卡交互.
   *
   * @param withReset true 发送数据前执行reset指令；false 发送数据前不执行reset指令，用于连续发送多条指令.
   */
  int send2PSam(byte[] cmd, byte[] responseBuff, boolean withReset);

  /** 与PSam卡交互. 发送数据前执行reset指令 */
  int send2PSam(byte[] cmd, byte[] responseBuff);

  /**
   * 与cpu用户卡交互.
   *
   * @param withReset true 发送数据前执行reset指令；false 发送数据前不执行reset指令，用于连续发送多条指令.
   */
  int send2Cpu(byte[] cmd, byte[] responseBuff, boolean withReset);

  /** 与cpu用户卡交互. 发送数据前执行reset指令 */
  int send2Cpu(byte[] cmd, byte[] responseBuff);
}
