package com.fanfull.libhard.uhf;

import com.fanfull.libhard.IOperation;

public interface IUhfOperation extends IOperation {

  boolean send(byte[] data);

  void setListener(IUhfListener listener);

  /**
   * 读 超高频.
   *
   * @param mb 读取数据 的 区, 1 表示 EPC， 2 表示 TID， 3 表示user
   * @param sa 读取 数据的 起始地址, 单位：字（字长: 2byte）
   * @param dataBuff 接收数据,单位：byte；长度必须为偶数
   * @param timeout 超时
   * @param mmb 过滤的区
   * @param msa 过滤的起始地址, 单位 字
   * @param filter 过滤数据
   */
  boolean read(int mb, int sa, byte[] dataBuff, int timeout, int mmb, int msa, byte[] filter);

  /**
   * 读 超高频.
   *
   * @param mb 读取数据 的 区, 1 表示 EPC， 2 表示 TID， 3 表示user
   * @param sa 读取 数据的 起始地址, 单位：字（字长: 2byte）
   * @param readLen 读取数据,单位：byte；应设为偶数，如为奇数等同于（len + 1）
   * @param filter 过滤数据
   * @param mmb 过滤的区
   * @param msa 过滤的起始地址, 单位 字
   */
  byte[] read(int mb, int sa, int readLen, byte[] filter, int mmb, int msa);

  /**
   * 读 超高频.
   * 数据通过回调方法返回
   *
   * @param mb 读取数据 的 区, 1 表示 EPC， 2 表示 TID， 3 表示user
   * @param sa 读取 数据的 起始地址, 单位：字（字长: 2byte）
   * @param readLen 读取数据,单位：byte；应设为偶数，如为奇数等同于（len + 1）
   * @param filter 过滤数据
   * @param mmb 过滤的区
   * @param msa 过滤的起始地址, 单位 字
   */
  void readAsync(int mb, int sa, int readLen, byte[] filter, int mmb, int msa);

  /**
   * 获取12byte epc，对支持的工作模式，额外获得12byte的tid数据
   *
   * @param timeout 尝试读取数据的时间
   * @return 12byte 的epc 或者 byte[24]的 epc + tid
   */
  byte[] readEpcWithTid(int timeout);

  boolean fastTid(int sa, byte[] buff);

  boolean readEpc(int sa, byte[] buff);

  boolean readTid(int sa, byte[] buff);

  boolean readUse(int sa, byte[] buff);

  /**
   * 写 超高频.
   *
   * @param mb 读取数据 的 区, 1 表示 EPC， 2 表示 TID， 3 表示user
   * @param sa 读取 数据的 起始地址, 单位 字（字长: 2byte）
   * @param data 待写入数据, 长度应为 偶数
   * @param filter 过滤数据
   * @param mmb 过滤的区
   * @param msa 过滤的起始地址, 单位 字
   */
  boolean write(int mb, int sa, byte[] data, int timeout, int mmb, int msa, byte[] filter);

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

  /**
   * 获取读写功率.
   *
   * @return 获取失败返回null；否则[0]为读功率，[1]为写功率，[2]天线id，[3]是否闭环：1是、0否
   */
  byte[] getPower();
}
