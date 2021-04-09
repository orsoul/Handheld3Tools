package com.fanfull.handheldtools;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.libhard.uhf.UhfCmd;
import com.fanfull.libhard.uhf.UhfController;
import com.fanfull.libjava.util.BytesUtil;
import com.fanfull.libjava.util.ThreadUtil;

import org.orsoul.baselib.util.SoundHelper;

public class LotScanTask extends ThreadUtil.TimeThreadRunnable {
  final int TIMES_READ_TID = 10;
  byte[] epcBuff = new byte[12];
  byte[] tidBuff = new byte[12];
  private UhfController uhfController;

  public LotScanTask(UhfController uhfController) {
    this.uhfController = uhfController;
  }

  @Override protected boolean handleOnce() {
    SoundHelper.playToneDiDa();
    boolean readSuccess = uhfController.readEpc(epcBuff);
    if (!readSuccess) {
      return false;
    }
    LogUtils.d("success epc:%s", BytesUtil.bytes2HexString(epcBuff));
    for (int i = 0; i < TIMES_READ_TID; i++) {
      readSuccess = uhfController.read(UhfCmd.MB_TID, 0x00, tidBuff, UhfCmd.MB_EPC, 0x02, epcBuff);
      if (readSuccess) {
        SoundHelper.playToneBiu();
        resetStartTime();
        LogUtils.d("success tid:%s - %s", BytesUtil.bytes2HexString(tidBuff), i + 1);
        break;
      }
    }
    return false;
  }

  @Override protected void onHandleOnce(long goingTime, int total) {
  }

  @Override protected void onStop() {
    LogUtils.d("onStop");
  }

  @Override protected void onTimeout(long runTime, int total) {
    LogUtils.d("onTimeout:%s", runTime);
  }
}