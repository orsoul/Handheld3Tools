package com.fanfull.libhard.rfid;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.libjava.util.BytesUtil;
import com.fanfull.libjava.util.DesUtil;

public final class APDUParser {

  /**
   * 根据输入参数生成 APDU 命令. 命令可分为四种情况: <br/>
   * Case1: CLA INS P1 P2 <br/>
   * Case2: CLA INS P1 P2 Le <br/>
   * Case3: CLA INS P1 P2 Lc Data <br/>
   * Case4: CLA INS P1 P2 Lc Data Le
   *
   * @param cla 指令类别，必须
   * @param ins 指令码，必须
   * @param p1 参数，必须
   * @param p2 参数，必须
   * @param data 发送数据，可选, lc = data.length
   * @param le 响应的数据字节数，可选；0表示最大可能长度，负数表示无此字段
   */
  public static byte[] genCmd(int cla, int ins, int p1, int p2, byte[] data, int le) {

    // 数据段长度：lc + data
    int haveData = data == null ? 0 : 1 + data.length;

    // le段长度
    int haveLe = 0 <= le ? 1 : 0;

    byte[] cmd = new byte[4 + haveData + haveLe];

    /* 命令头 */
    cmd[0] = (byte) cla;
    cmd[1] = (byte) ins;
    cmd[2] = (byte) p1;
    cmd[3] = (byte) p2;

    /* 命令体 */
    if (0 < haveData) {
      // 有 data段
      cmd[4] = (byte) data.length; // lc
      for (int i = 0; i < data.length; i++) {
        cmd[i + 5] = data[i];
      }
    }

    if (haveLe == 1) {
      // 有 le段
      cmd[cmd.length - 1] = (byte) le;
    }

    //LogUtils.d("genCmd:%s", cmd2String(cmd));
    //LogUtils.d("genCmd:%s", ArrayUtils.bytes2HexString(cmd));
    return cmd;
  }

  //public static byte[] genCmd(int cla, int ins, int p1, int p2, int lc, byte[] data, int le) {
  //  return genCmd(cla, ins, p1, p2, data, le);
  //}

  /**
   * 根据输入参数生成 APDU 命令.无data字段，命令可分为四种情况: <br/>
   * Case1: CLA INS P1 P2 <br/>
   * Case2: CLA INS P1 P2 Le <br/>
   * Case3: CLA INS P1 P2 Lc Data <br/>
   * Case4: CLA INS P1 P2 Lc Data Le
   *
   * @param cla 指令类别
   * @param ins 指令码
   * @param p1 参数
   * @param p2 参数
   * @param le 响应的数据字节数，可选；0表示最大可能长度，负数表示无此字段
   */
  public static byte[] genCmd(int cla, int ins, int p1, int p2, int le) {
    return genCmd(cla, ins, p1, p2, null, le);
  }

  /**
   * 根据输入参数生成 APDU 命令.无le字段，命令可分为四种情况: <br/>
   * Case1: CLA INS P1 P2 <br/>
   * Case2: CLA INS P1 P2 Le <br/>
   * Case3: CLA INS P1 P2 Lc Data <br/>
   * Case4: CLA INS P1 P2 Lc Data Le
   *
   * @param cla 指令类别
   * @param ins 指令码
   * @param p1 参数
   * @param p2 参数
   * @param data 发送数据，可选
   */
  public static byte[] genCmd(int cla, int ins, int p1, int p2, byte[] data) {
    return genCmd(cla, ins, p1, p2, data, -1);
  }

  /**
   * 根据输入参数生成 APDU 命令.无命令体，命令可分为四种情况: <br/>
   * Case1: CLA INS P1 P2 <br/>
   * Case2: CLA INS P1 P2 Le <br/>
   * Case3: CLA INS P1 P2 Lc Data <br/>
   * Case4: CLA INS P1 P2 Lc Data Le
   *
   * @param cla 指令类别
   * @param ins 指令码
   * @param p1 参数
   * @param p2 参数
   */
  public static byte[] genCmd(int cla, int ins, int p1, int p2) {
    return genCmd(cla, ins, p1, p2, null, -1);
  }

  public static String cmd2String(byte[] apduCmd) {
    if (apduCmd == null || apduCmd.length < 4) {
      return "apdu指令长度小于4：" + BytesUtil.bytes2HexString(apduCmd);
    }
    StringBuilder sb = new StringBuilder();
    // 命令头 cla ins p1 p2
    //sb.append(BytesUtil.bytes2HexString(apduCmd, 4));
    sb.append(
        String.format("cla=%02X, ins=%02X, p1=%02X, p2=%02X", apduCmd[0], apduCmd[1], apduCmd[2],
            apduCmd[3]));
    if (5 == apduCmd.length) {
      // 有le段，无data
      sb.append(String.format(",le=%02X", apduCmd[apduCmd.length - 1]));
    } else if (5 < apduCmd.length) {
      // 有lc和data段
      int lc = (apduCmd[4] & 0xFF);
      int d = apduCmd.length - lc;
      if (5 == d || 6 == d) {
        // 数据段 lc-data
        sb.append(",lc=").append(lc)
            .append('-')
            .append(BytesUtil.bytes2HexString(apduCmd, 5, 5 + lc));
        if (6 == d) {
          // le段
          sb.append(String.format(",le=%02X", apduCmd[apduCmd.length - 1]));
        }
      } else {
        return String.format("apdu指令lc长度错误, lc=%s(0x02X), allLen=%s, allLen - lc = %s",
            lc, lc, apduCmd.length, d);
      }
    }

    return sb.toString();
  }

  /**
   * 检查响应数据的状态字.状态字为响应数据的末2位
   *
   * @param recData 响应数据
   * @param recDataLen 响应数据的长度
   * @param sw 预期的状态字,2byte,例：9000，sw = sw1_sw2
   */
  public static boolean checkReply(byte[] recData, int recDataLen, int sw) {
    if (recData == null || recData.length < 2 || recDataLen < 2 || recData.length < recDataLen) {
      return false;
    }
    byte sw1 = (byte) ((sw >> 8) & 0xFF);
    byte sw2 = (byte) (sw & 0xFF);
    boolean res = recData[recDataLen - 2] == sw1 && recData[recDataLen - 1] == sw2;
    LogUtils.d("res:%s, data:%s, sw:%X", res, BytesUtil.bytes2HexString(recData, recDataLen), sw);
    return res;
  }

  /**
   * 检查响应数据的状态字是否为0x9000.状态字为响应数据的末2位
   *
   * @param recData 响应数据
   * @param recDataLen 响应数据的长度
   */
  public static boolean checkReply(byte[] recData, int recDataLen) {
    return checkReply(recData, recDataLen, 0x9000);
  }

  public static boolean checkRecData(byte[] successBuff, byte[] recData, int recLen) {
    if (recData == null
        || successBuff == null
        || recLen != successBuff.length
        || recData.length < recLen) {
      return false;
    }
    for (int i = 0; i < successBuff.length; i++) {
      if (successBuff[i] != recData[i]) {
        return false;
      }
    }
    return true;
  }

  static byte[] regData(byte[] data) {
    if (data == null) {
      return null;
    }
    for (int i = 0; i < data.length; i++) {
      data[i] ^= 0xFF;
    }
    return data;
  }

  static byte[] tripleDes(byte[] text, byte[] key) {
    DesUtil tDes = new DesUtil("DESede/ECB/NoPadding");
    DesUtil des = new DesUtil("DES/ECB/NoPadding");
    //System.out.println(ArrayUtils.bytes2HexString(text));
    byte[] encrypt = tDes.encrypt(text, key);
    //System.out.println(ArrayUtils.bytes2HexString(encrypt));

    byte[] decrypt = tDes.decrypt(encrypt, key);
    //System.out.println(ArrayUtils.bytes2HexString(decrypt));
    return encrypt;
  }

  /**
   * 传输保护密钥 ：1233db4e05758ac9dcee55b702a9fb4b
   * 随机因子： 000000000F466880
   * 传输保护子密钥：
   */
  static byte[] genDiversify(byte[] mk, byte[] randomData) {
    if (mk == null || randomData == null
        || mk.length != 16 || randomData.length != 8) {
      return null;
    }

    byte[] key = new byte[24];
    System.arraycopy(mk, 0, key, 0, 16);
    System.arraycopy(mk, 0, key, 16, 8);

    byte[] dkL = tripleDes(randomData, key);
    //System.out.println(ArrayUtils.bytes2HexString(randomData));
    //System.out.println(ArrayUtils.bytes2HexString(dkL));
    byte[] dkR = tripleDes(regData(randomData), key);
    return BytesUtil.concatArray(dkR, dkL);
  }

  static void test() {
    byte[] mk = BytesUtil.hexString2Bytes("1233db4e05758ac9dcee55b702a9fb4b");
    byte[] randomData = BytesUtil.hexString2Bytes("000000000F466880");
    byte[] diversify = genDiversify(mk, randomData);
    System.out.println(String.format("diversify:%s", BytesUtil.bytes2HexString(diversify)));
  }

  public static void main(String[] args) {
    test();
  }

  static void test1() {
    byte[] cmd = genCmd(0x00, 0x00, 0x00, 0x00);
    System.out.println(cmd2String(cmd));

    System.out.println(cmd2String(genCmd(1, 2, 3, 4, 5)));
    System.out.println(cmd2String(genCmd(1, 2, 3, 4, cmd)));
    System.out.println(cmd2String(genCmd(1, 2, 3, 4, cmd, 6)));

    System.out.println(cmd2String(PSamCmd.getCmdVerifyUser()));

    byte[] epc = BytesUtil.hexString2Bytes("000F46311000000000000000");
    byte[] elsData = BytesUtil.hexString2Bytes("0100000000000000000000000000");
    System.out.println(cmd2String(PSamCmd.getCmdGenElsCmd(1, epc, null)));
  }
}
