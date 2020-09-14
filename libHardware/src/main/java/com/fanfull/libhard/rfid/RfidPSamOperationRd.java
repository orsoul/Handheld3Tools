package com.fanfull.libhard.rfid;

import com.apkfuns.logutils.LogUtils;
import com.halio.Rfid;

/**
 * 高频卡 操作类，添加对PSam卡、CPU卡，基于 雨滴开发板.
 */
public class RfidPSamOperationRd extends RfidOperationRd implements IRfidPSamOperation {

  @Override public int send2PSam(byte[] cmd, byte[] responseBuff, boolean withReset) {
    if (cmd == null || responseBuff == null) {
      return 0;
    }

    byte samNum = 1;
    int[] responseLenBuff = new int[1];
    boolean res;

    if (withReset) {
      res = Rfid.samReset(samNum, Rfid.PSAM_MODE_9600, responseBuff, responseLenBuff);
      if (!res) {
        return -1;
      }
    }

    res = Rfid.samCos(samNum, cmd, cmd.length, responseBuff, responseLenBuff);
    if (!res) {
      return -2;
    }

    return responseLenBuff[0];
  }

  @Override public int send2PSam(byte[] cmd, byte[] responseBuff) {
    return send2PSam(cmd, responseBuff, true);
  }

  @Override public int send2Cpu(byte[] cmd, byte[] responseBuff, boolean withReset) {
    if (cmd == null || responseBuff == null) {
      return 0;
    }

    boolean res;
    if (withReset) {
      byte[] responseLenBuff = new byte[1];
      res = Rfid.iso14443aReset(Rfid.CARD_ALL, responseBuff, responseLenBuff);
      if (!res) {
        LogUtils.i("iso14443aReset failed");
        return -1;
      }
    }

    int[] lenBuff = new int[1];
    res = Rfid.iso14443aCos(cmd, cmd.length, responseBuff, lenBuff);
    if (!res) {
      LogUtils.i("iso14443aCos failed");
      return -2;
    }

    return lenBuff[0];
  }

  @Override public int send2Cpu(byte[] cmd, byte[] responseBuff) {
    return send2Cpu(cmd, responseBuff, true);
  }
}
