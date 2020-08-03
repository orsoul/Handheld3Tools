package com.fanfull.operation;

import com.fanfull.libhard.uhf.UhfController;

public class UHFOperation {

  public byte[] mEPC = new byte[12];
  public int count = 0;// 记录读卡数

  public UhfController uhfController;
  private static UHFOperation mUhf = null;

  private UHFOperation() {
    // 启动模块
    uhfController = UhfController.getInstance();
  }

  public static UHFOperation getInstance() {
    if (null == mUhf) {
      mUhf = new UHFOperation();
    }
    mUhf.open();
    return mUhf;
  }

  private void open() {
    uhfController.open();
  }

  /**
   * @param @return 设定文件
   * @return Boolean TRUE 扫描成功；FALSE没有扫描
   * @throws
   * @Title: findOne
   * @Description: 一次扫描EPC
   */
  public boolean findOne() {

    byte[] bytes = uhfController.fastEpc(500);
    if (bytes != null) {
      System.arraycopy(bytes, 0, mEPC, 0, mEPC.length);
      return true;
    }
    return false;
  }

  /**
   * @param EPC 过滤 数据,如果不需要过滤,请传入 长度为0的数组
   * @param data 需要写入的 数据
   * @param mmb 为启动过滤操作的 bank号， 0x01 表示 EPC， 0x02 表示 TID， 0x03 表示USR，其他值为非法值
   * //01
   * @param msa MSA启动过滤操作的起始地址, 单位为 bit // 0x20
   * @param mb memory bank，用户需要写入的数据的 bank号 // 代号与 mmb 一致
   * @param sa 写入起始位置, 单位为 字 // 0x02
   */
  //if (mUHFOp.writeUHF(mUHFOp.mEPC, data, 1, 0x20, 3, 0x02)) {
  public boolean writeUHF(byte[] EPC, byte[] data, int mmb, int msa, int mb,
      int sa) {
    return uhfController.write(mb, sa, data, EPC, mmb, msa);
  }

  /**
   * @param EPC 过滤数据
   * @param mmb 过滤的区
   * @param msa 过滤的起始地址
   * @param mb 读取数据 的 区,0x01 表示 EPC， 0x02 表示 TID， 0x03 表示user
   * @param sa 读取 数据的 起始地址, 单位 字
   * @param readDataLen 读取 数据 的长度, 单位 字节
   */
  //if (mUHFOp.writeUHF(mUHFOp.mEPC, data, 1, 0x20, 3, 0x02)) {
  //        public boolean writeUHF(byte[] EPC, byte[] data, int mmb, int msa, int mb,
  //                int sa) {
  public byte[] readUHF(byte[] EPC, int mmb, int msa, int mb, int sa,
      int readDataLen) {
    return uhfController.read(mb, sa, readDataLen, EPC, mmb, msa);
  }

  public byte[] readTIDNogl() {
    //    byte[] tid = readUHF(new byte[0], 1, 0x20, 2, 0x00, 12);
    // return readUHF(new byte[0], 1, 0x20, 2, 0, 12);
    return readUHF(new byte[0], 1, 0x20, 2, 0x03, 6);
  }

  /**
   * @param readPower 读 功率.范围:5-30
   * @param writePower 写 功率.范围:5-30
   * @param id 天线号
   * @param save 本次设置是否保存 , 0 = 不保存
   * @param open 是否闭环, 非0 = 闭环, 默认0
   * @return 设置成功 返回 true
   */
  public boolean setPower(int readPower, int writePower, int id, int save,
      int open) {
    return false;
  }

  /**
   * @param readPower 读 功率.范围:5-30
   * @param writePower 写 功率.范围:5-30
   * @return 设置成功 返回 true
   */
  public boolean setPower(int readPower, int writePower) {
    return uhfController.setPower(readPower, writePower, 1, true, false);
    //return setPower(readPower, writePower, 1, 1, 0);
  }

  /**
   * @param dwPower 第0个元素存 读功率, 第1个元素存 写功率
   * @description 获取 超高频 读写 功率, 保存在 dw_power 中;
   */
  public boolean getPower(int[] dwPower) {
    byte[] power = uhfController.getPower();
    if (power != null && 2 <= power.length && dwPower != null && 2 <= dwPower.length) {
      dwPower[0] = power[0];
      dwPower[1] = power[1];
      return true;
    }
    return false;
  }

  public void close() {
    uhfController.release();
  }
}
