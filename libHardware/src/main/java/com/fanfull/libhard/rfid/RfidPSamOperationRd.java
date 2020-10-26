package com.fanfull.libhard.rfid;

import com.apkfuns.logutils.LogUtils;
import com.halio.Rfid;
import org.orsoul.baselib.util.BytesUtil;

/**
 * 高频卡 操作类，添加对PSam卡、CPU卡，基于 雨滴开发板.
 */
public class RfidPSamOperationRd extends RfidOperationRd implements IRfidPSamOperation {
  /** PSam编号 */
  private final byte pSamNum = 1;

  @Override public int resetPSam(byte[] responseBuff) {
    int[] responseLenBuff = new int[1];
    boolean res = Rfid.samReset(pSamNum, Rfid.PSAM_MODE_9600, responseBuff, responseLenBuff);
    if (!res) {
      LogUtils.tag(TAG).d("samReset failed");
      return -1;
    }
    LogUtils.tag(TAG)
        .d("samReset:%s", BytesUtil.bytes2HexString(responseBuff, responseLenBuff[0]));
    return responseLenBuff[0];
  }

  @Override public int send2PSam(byte[] cmd, byte[] responseBuff, boolean withReset) {
    if (cmd == null || responseBuff == null) {
      return 0;
    }

    if (withReset && resetPSam(responseBuff) < 1) {
      return -1;
    }

    int[] responseLenBuff = new int[1];
    boolean res;
    res = Rfid.samCos(pSamNum, cmd, cmd.length, responseBuff, responseLenBuff);
    if (!res) {
      LogUtils.tag(TAG).d("samCos failed");
      return -2;
    }
    LogUtils.tag(TAG).d("samCos res:%s cmd:%s",
        BytesUtil.bytes2HexString(responseBuff, responseLenBuff[0]),
        BytesUtil.bytes2HexString(cmd));
    return responseLenBuff[0];
  }

  @Override public int send2PSam(byte[] cmd, byte[] responseBuff) {
    return send2PSam(cmd, responseBuff, true);
  }

  @Override public int resetCpu(byte[] responseBuff) {
    byte[] responseLenBuff = new byte[1];
    boolean res = Rfid.iso14443aReset(Rfid.CARD_ALL, responseBuff, responseLenBuff);
    if (!res) {
      LogUtils.tag(TAG).d("iso14443aReset failed");
      return -1;
    }
    LogUtils.tag(TAG)
        .d("iso14443aReset:%s", BytesUtil.bytes2HexString(responseBuff, responseLenBuff[0]));
    return responseLenBuff[0];
  }

  @Override public int send2Cpu(byte[] cmd, byte[] responseBuff, boolean withReset) {
    if (cmd == null || responseBuff == null) {
      return 0;
    }

    if (withReset && resetCpu(responseBuff) < 1) {
      return -1;
    }

    boolean res;
    int[] responseLenBuff = new int[1];
    res = Rfid.iso14443aCos(cmd, cmd.length, responseBuff, responseLenBuff);
    if (!res) {
      LogUtils.tag(TAG).d("iso14443aCos failed");
      return -2;
    }
    LogUtils.tag(TAG).d("iso14443aCos res:%s cmd:%s",
        BytesUtil.bytes2HexString(responseBuff, responseLenBuff[0]),
        BytesUtil.bytes2HexString(cmd));
    return responseLenBuff[0];
  }

  @Override public int send2Cpu(byte[] cmd, byte[] responseBuff) {
    return send2Cpu(cmd, responseBuff, true);
  }
}
