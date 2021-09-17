package com.fanfull.libhard.lock_zc;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.libhard.rfid.APDUParser;
import com.fanfull.libhard.rfid.PSamCmd;
import com.fanfull.libhard.rfid.RfidController;
import com.fanfull.libjava.util.BytesUtil;

import java.util.Arrays;

public final class PsamHelper {
  /**
   * iso14443a 读取用户卡号（cup卡号)
   *
   * @param cpuCardBuff 12字节
   * @return 读取成功返回字节数12，否则返回 负数
   */
  public static int readCPUCard(byte[] cpuCardBuff) {
    if (cpuCardBuff == null || cpuCardBuff.length < PSamCmd.COS_RES_CARD_LEN) {
      return -1;
    }
    RfidController nfcController = RfidController.getInstance();
    //byte[] cpuCardBuff = new byte[12];
    int len = nfcController.send2Cpu(PSamCmd.CMD_COS_0, cpuCardBuff);
    if (!APDUParser.checkReply(cpuCardBuff, len)) {
      return -2;
    }

    len = nfcController.send2Cpu(PSamCmd.CMD_COS_1, cpuCardBuff, false);
    if (!APDUParser.checkReply(cpuCardBuff, len)) {
      return -3;
    }
    len = nfcController.send2Cpu(PSamCmd.CMD_COS_2, cpuCardBuff, false);
    if (!APDUParser.checkReply(cpuCardBuff, len)) {
      return -4;
    }
    len = nfcController.send2Cpu(PSamCmd.CMD_COS_3, cpuCardBuff, false);
    if (!APDUParser.checkReply(cpuCardBuff, len, 0x9100)) {
      return -5;
    }
    len = nfcController.send2Cpu(PSamCmd.CMD_COS_4, cpuCardBuff, false);
    if (!APDUParser.checkReply(cpuCardBuff, len) || len != PSamCmd.COS_RES_CARD_LEN) {
      return -6;
    }

    //String cpuCard = new String(cpuCardBuff, 0, len - 2);
    //System.arraycopy(cpuCardBuff, 0, cpuCardBuff, 0, len);
    return len;
  }

  public static String readCpuCard() {
    byte[] cardBuff = new byte[12];
    int len = readCPUCard(cardBuff);
    if (12 == len) {
      return new String(cardBuff, 0, len - 2);
    }
    return null;
  }

  /** 向 Psam卡 发送 验证用户指令. */
  public static boolean sendVerifyUser() {
    byte[] cmd = PSamCmd.getCmdVerifyUser();
    byte[] recBuff = new byte[4];
    int len = RfidController.getInstance().send2PSam(cmd, recBuff);
    boolean b = APDUParser.checkReply(recBuff, len);
    return b;
  }

  /**
   * 向 Psam卡 发送:验证 EPC 校验码（VERIFY_EPC_DATA）.
   *
   * @param epc 12字节
   * @param tid 12字节
   */
  public static boolean sendVerifyEpc(byte[] epc, byte[] tid) {
    byte[] cmd = PSamCmd.getVerifyEpcCmd(epc, tid);
    byte[] recBuff = new byte[4];
    int len = RfidController.getInstance().send2PSam(cmd, recBuff, false);
    boolean b = APDUParser.checkReply(recBuff, len);
    return b;
  }

  /**
   * 向 Psam卡 发送  获取 标签密码.
   *
   * @param epc 12字节
   */
  public static byte[] sendGetPwd(byte[] epc) {
    byte[] cmd = PSamCmd.getPwdCmd(epc);
    byte[] recBuff = new byte[8];
    int len = RfidController.getInstance().send2PSam(cmd, recBuff, false);
    boolean b = APDUParser.checkReply(recBuff, len);
    if (!b) {
      return null;
    }
    return Arrays.copyOf(recBuff, len - 2);
  }

  /**
   * 向 Psam卡 发送  生成电子签封交互指令.
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
  public static byte[] sendGenElsCmd(int elsType, byte[] epc, byte[] elsData) {
    byte[] cmd = PSamCmd.getCmdGenElsCmd(elsType, epc, elsData);
    LogUtils.d("eldCmd:%s", BytesUtil.bytes2HexString(cmd));
    byte[] recBuff = new byte[112]; // 指令区 112 字节
    int len = RfidController.getInstance().send2PSam(cmd, recBuff, false);
    boolean b = APDUParser.checkReply(recBuff, len);
    if (!b) {
      return null;
    }
    return Arrays.copyOf(recBuff, len - 2);
  }

  /**
   * 向 Psam卡 发送  追溯指令
   *
   * @param sa 日志序号 2 byte
   * @param n 日志项数 1 byte
   * @param isFinish 结束标识 1 byte 0- 结束 非 0 为继续
   */
  public static byte[] sendReadLog(byte[] epc, int sa, int n, boolean isFinish) {
    //日志序号 2 byte
    //日志项数 1 byte
    //结束标识 1 byte 0- 结束 非 0 为继续
    byte[] els = new byte[4];
    els[0] = (byte) ((sa >> 4) & 0x0F); // 日志序号
    els[1] = (byte) (sa & 0x0F); // 日志序号
    els[2] = (byte) n; // 日志项数
    els[3] = (byte) (isFinish ? 0 : 1); // 0- 结束 非 0 为继续
    return sendGenElsCmd(PSamCmd.CMD_ELS_TYPE_READ_LOG, epc, els);
  }

  /**
   * 向 Psam卡 发送
   * 解密电子签封返回的业务数据,目前只支持追溯 的响应数据解密。
   *
   * @param epc 12字节
   * @param encryptEls 封签响应的 加密数据
   */
  public static byte[] sendDecryptEls(byte[] epc, byte[] encryptEls) {
    byte[] cmd = PSamCmd.getDecryptCmd(epc, encryptEls);
    byte[] recBuff = new byte[encryptEls.length + 2];
    int len = RfidController.getInstance().send2PSam(cmd, recBuff, false);
    boolean b = APDUParser.checkReply(recBuff, len);
    if (!b) {
      return null;
    }
    return Arrays.copyOf(recBuff, len - 2);
  }
}
