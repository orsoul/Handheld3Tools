package com.fanfull.libhard.uhf;

import com.fanfull.libhard.IOperation;

public interface IUhfOperation extends IOperation {

  boolean send(byte[] data);

  void setListener(IUhfListener listener);

  byte[] read(int mb, int sa, int readLen, byte[] filter, int mmb, int msa);

  void readAsync(int mb, int sa, int readLen, byte[] filter, int mmb, int msa);

  /**
   * 获取12byte epc，对支持的工作模式，额外获得12byte的tid数据
   *
   * @param timeout 尝试读取数据的时间
   * @return 12byte 的epc 或者 byte[24]的 epc + tid
   */
  byte[] fastEpc(int timeout);

  byte[] fastTid(int sa, int len);

  byte[] readEpc(int sa, int len);

  byte[] readTid(int sa, int len);

  byte[] readUse(int sa, int len);

  boolean write(int mb, int sa, byte[] data, byte[] filter, int mmb, int msa);

  void writeAsync(int mb, int sa, byte[] data, byte[] filter, int mmb, int msa);

  boolean writeEpc(int sa, byte[] data);

  boolean writeUse(int sa, byte[] data);

  /**
   * @param readPower 读 功率.范围:5-30
   * @param writePower 写 功率.范围:5-30
   * @param id 天线号
   * @param isSave 本次设置是否保存
   * @param isClosed 是否闭环, 非0 = 闭环, 默认0
   */
  boolean setPower(int readPower, int writePower, int id, boolean isSave,
      boolean isClosed);

  byte[] getPower();
}
