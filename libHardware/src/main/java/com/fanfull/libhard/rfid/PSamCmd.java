package com.fanfull.libhard.rfid;

import com.fanfull.libjava.util.BytesUtil;

/**
 * 中钞锁，与psam交互的 apdu指令，参看文档《接口规范-二代货发 PSAM 卡命令集》
 */
public final class PSamCmd {
  public static final int COS_RES_CARD_LEN = 12;

  public static final byte[] COS_RES_0 = new byte[]{(byte) 0x90, 0x00};
  public static final byte[] COS_RES_3 = new byte[]{(byte) 0x91, 0x00};

  public static final byte[] CMD_COS_0 = new byte[]{
      (byte) 0x80, (byte) 0x20, (byte) 0x07, (byte) 0x00, (byte) 0x08, (byte) 0x49, (byte) 0x43,
      (byte) 0x46, (byte) 0x43, (byte) 0x43, (byte) 0x4B, (byte) 0x46, (byte) 0x33,
  };
  public static final byte[] CMD_COS_1 = new byte[]{
      (byte) 0x80, (byte) 0x20, (byte) 0x06, (byte) 0x00, (byte) 0x08, (byte) 0x31, (byte) 0x32,
      (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38
  };
  public static final byte[] CMD_COS_2 = new byte[]{
      (byte) 0x80, (byte) 0x20, (byte) 0x02, (byte) 0x00, (byte) 0x08, (byte) 0x43, (byte) 0x46,
      (byte) 0x43, (byte) 0x43, (byte) 0x41, (byte) 0x43, (byte) 0x30, (byte) 0x32
  };
  public static final byte[] CMD_COS_3 = new byte[]{
      (byte) 0x80, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0xAA, (byte) 0x01,
  };
  public static final byte[] CMD_COS_4 =
      new byte[]{(byte) 0x80, (byte) 0xB2, (byte) 0x00, (byte) 0x00, (byte) 0x0A,};

  /* ===================== 以上读取cup用户卡号相关指令，以下为Psam卡相关命令 ===================== */

  /** 取随机数. */
  public static final byte[] CMD_PSAM_GET_CHALLENGE =
      {(byte) 0x00, (byte) 0x84, (byte) 0x00, (byte) 0x00, (byte) 0x04};
  /** 用户验证. */
  public static final byte[] CMD_PSAM_VERIFY_USER =
      {(byte) 0x80, (byte) 0x20, (byte) 0x00, (byte) 0x00};

  public static final int CMD_CLA = 0x80;
  /** ins:用户验证. */
  public static final int CMD_INS_VERIFY = 0x20;
  /** ins:生成电子签封交互指令. */
  public static final int CMD_INS_GEN_ELS_CMD = 0x30;
  /** ins:解密业务数据。目前只支持追溯 的响应数据解密. */
  public static final int CMD_INS_DECRYPT = 0x34;
  /** ins:获取标签访问密码. */
  public static final int CMD_INS_GET_PWD = 0x36;
  /** ins:. 验证 EPC 校验码（VERIFY_EPC_DATA）. */
  public static final int CMD_INS_VERIFY_EPC = 0x38;

  /** 交互指令类型:签封激活指令. */
  public static final int CMD_ELS_TYPE_ACTIVE = 1;
  /** 交互指令类型: 关锁. */
  public static final int CMD_ELS_TYPE_CLOSE = 2;
  /** 交互指令类型: 开锁. */
  public static final int CMD_ELS_TYPE_OPEN = 3;
  /** 交互指令类型: 关锁. */
  public static final int CMD_ELS_TYPE_CLOSE_WRITE = 4;
  /** 交互指令类型: 开锁. */
  public static final int CMD_ELS_TYPE_OPEN_WRITE = 5;
  /** 交互指令类型: 追溯指令. */
  public static final int CMD_ELS_TYPE_READ_LOG = 6;
  /** 交互指令类型: 恢复. */
  public static final int CMD_ELS_TYPE_RECOVERY = 7;
  /** 交互指令类型: 清除. */
  public static final int CMD_ELS_TYPE_CLEAR = 8;

  /**
   * 获取信息.
   *
   * @param p1 01 获取PSAM卡信息, 02 获取 PSAM 卡设备证书
   * @param p2 0 取得第一部分或所有的数据;1 取得下一部分数据
   */
  public static byte[] genCmdGetInfo(int p1, int p2) {
    /* P1 含义:‘01’ 获取 PSAM 卡信息 ‘02’ 获取 PSAM 卡设备证书 ‘XX’ RFU */
    return APDUParser.genCmd(CMD_CLA, 0x10, p1, p2, 0x15);
  }

  /** 用户验证. */
  public static byte[] getCmdVerifyUser(byte[] userId, byte[] pin) {
    byte[] data = BytesUtil.concatArray(userId, pin);
    return APDUParser.genCmd(CMD_CLA, CMD_INS_VERIFY, 0, 0, data);
  }

  /** 用户验证.用户ID未做明确定义，8字节0也可以，用户PIN码目前是固定值：4d494d49535f5053414d5f55534552 */
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
   * @param epc 12字节
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

    return APDUParser.genCmd(CMD_CLA, CMD_INS_GEN_ELS_CMD, elsType, 0, data, 0);
  }

  /**
   * 获取 标签 写密码.
   *
   * @param epc 12字节
   */
  public static byte[] getPwdCmd(byte[] epc) {
    byte[] cmd = APDUParser.genCmd(CMD_CLA, CMD_INS_GET_PWD, 0, 0, epc, 0);
    return cmd;
  }

  /**
   * 解密电子签封返回的业务数据,目前只支持追溯 的响应数据解密。
   *
   * @param epc 12字节
   * @param encryptEls 封签响应的 加密数据
   */
  public static byte[] getDecryptCmd(byte[] epc, byte[] encryptEls) {
    byte[] lvEpc = BytesUtil.bytes2LnVData(1, epc);
    byte[] data = lvEpc;
    if (encryptEls != null) {
      byte[] lvData = BytesUtil.bytes2LnVData(1, encryptEls);
      data = BytesUtil.concatArray(lvEpc, lvData);
    }

    return APDUParser.genCmd(CMD_CLA, CMD_INS_DECRYPT, 0x06, 0x00, data, 0);
  }

  /**
   * 2.7. 验证 EPC 校验码（VERIFY_EPC_DATA）
   *
   * @param epc 12字节
   * @param tid 12字节
   */
  public static byte[] getVerifyEpcCmd(byte[] epc, byte[] tid) {
    byte[] lvEpc = BytesUtil.bytes2LnVData(1, epc);
    byte[] lvTid = BytesUtil.bytes2LnVData(1, tid);
    byte[] data = BytesUtil.concatArray(lvEpc, lvTid);
    return APDUParser.genCmd(CMD_CLA, CMD_INS_VERIFY_EPC, 0x00, 0x00, data, 0);
  }
}
