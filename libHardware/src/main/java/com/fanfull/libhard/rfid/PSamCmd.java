package com.fanfull.libhard.rfid;

import org.orsoul.baselib.util.BytesUtil;

public final class PSamCmd {
  public static final int COS_RES_CARD_LEN = 12;

  public static final byte[] COS_RES_0 = new byte[] { (byte) 0x90, 0x00 };
  public static final byte[] COS_RES_3 = new byte[] { (byte) 0x91, 0x00 };

  public static final byte[] CMD_COS_0 = new byte[] {
      (byte) 0x80, (byte) 0x20, (byte) 0x07, (byte) 0x00, (byte) 0x08, (byte) 0x49, (byte) 0x43,
      (byte) 0x46, (byte) 0x43, (byte) 0x43, (byte) 0x4B, (byte) 0x46, (byte) 0x33,
  };
  public static final byte[] CMD_COS_1 = new byte[] {
      (byte) 0x80, (byte) 0x20, (byte) 0x06, (byte) 0x00, (byte) 0x08, (byte) 0x31, (byte) 0x32,
      (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38
  };
  public static final byte[] CMD_COS_2 = new byte[] {
      (byte) 0x80, (byte) 0x20, (byte) 0x02, (byte) 0x00, (byte) 0x08, (byte) 0x43, (byte) 0x46,
      (byte) 0x43, (byte) 0x43, (byte) 0x41, (byte) 0x43, (byte) 0x30, (byte) 0x32
  };
  public static final byte[] CMD_COS_3 = new byte[] {
      (byte) 0x80, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0xAA, (byte) 0x01,
  };
  public static final byte[] CMD_COS_4 =
      new byte[] { (byte) 0x80, (byte) 0xB2, (byte) 0x00, (byte) 0x00, (byte) 0x0A, };

  /** 取随机数. */
  public static final byte[] CMD_PSAM_GET_CHALLENGE =
      { (byte) 0x00, (byte) 0x84, (byte) 0x00, (byte) 0x00, (byte) 0x04 };
  /** 用户验证. */
  public static final byte[] CMD_PSAM_VERIFY_USER =
      { (byte) 0x80, (byte) 0x20, (byte) 0x00, (byte) 0x00 };

  /**
   * 获取信息.
   *
   * @param p1 01 获取PSAM卡信息, 02 获取 PSAM 卡设备证书
   * @param p2 0 取得第一部分或所有的数据;1 取得下一部分数据
   */
  public static byte[] genCmdGetInfo(int p1, int p2) {
    /* P1 含义:‘01’ 获取 PSAM 卡信息 ‘02’ 获取 PSAM 卡设备证书 ‘XX’ RFU */
    return APDUParser.genCmd(0x80, 0x10, p1, p2, 0x15);
  }

  /** 用户验证. */
  public static byte[] getCmdVerifyUser(byte[] userId, byte[] pin) {
    byte[] data = BytesUtil.concatArray(userId, pin);
    return APDUParser.genCmd(0x80, 0x20, 0, 0, data);
  }

  /** 用户验证. */
  public static byte[] getCmdVerifyUser() {
    byte[] userId = new byte[8];
    userId[7] = 1;
    byte[] pin = BytesUtil.hexString2Bytes("4d494d49535f5053414d5f55534552");
    //byte[] pin = ArrayUtils.hexString2Bytes("4d494d49535f5053414d5f55530000");
    return getCmdVerifyUser(userId, pin);
  }

  /**
   * 生成电子签封交互指令.
   *
   * @param elsType 交互命令类型: <br/>
   * 01 签封激活指令 <br/>
   * 02 关锁指令 <br/>
   * 03 开锁指令 <br/>
   * 04 关锁写物流指令 <br/>
   * 05 开锁写物流指令 <br/>
   * 06 追溯指令文档编号—EDJK005 8 <br/>
   * 07 恢复指令 <br/>
   * 08 日志清除指令<br/>
   * @param epc EPC 数据
   * @param elsData 交互指令 data 区数据
   */
  public static byte[] getCmdGenElsCmd(int elsType, byte[] epc, byte[] elsData) {
    // 转为 LV格式 后拼接
    byte[] lvEpc = BytesUtil.bytes2LnVData(1, epc);
    byte[] data = lvEpc;
    if (elsData != null) {
      byte[] lvElsData = BytesUtil.bytes2LnVData(1, elsData);
      data = BytesUtil.concatArray(lvEpc, lvElsData);
    }

    return APDUParser.genCmd(0x80, 0x30, elsType, 0, data, 0);
  }
}
