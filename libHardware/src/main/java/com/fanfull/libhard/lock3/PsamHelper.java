package com.fanfull.libhard.lock3;

import com.fanfull.libhard.rfid.APDUParser;
import com.fanfull.libhard.rfid.PSamCmd;
import com.fanfull.libhard.rfid.RfidController;

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
}
