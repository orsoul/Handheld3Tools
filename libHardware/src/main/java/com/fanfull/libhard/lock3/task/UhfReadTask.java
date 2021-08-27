package com.fanfull.libhard.lock3.task;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.libhard.uhf.UhfCmd;
import com.fanfull.libhard.uhf.UhfController;
import com.fanfull.libjava.util.BytesUtil;
import com.fanfull.libjava.util.ThreadUtil;

/**
 * 读超高频 tid、epc、use区任务.
 */
public abstract class UhfReadTask extends ThreadUtil.TimeThreadRunnable {
  protected int filterReadTimes = 3;
  private byte[] tidBuff = new byte[12];
  //private byte[] epcBuff = new byte[12];

  boolean isReadTid = true;
  boolean isReadEpc = true;
  boolean isReadUse = false;
  private int useSa;
  private int useLen = 12;

  public void setReadTid(boolean readTid) {
    isReadTid = readTid;
  }

  public void setReadEpc(boolean readEpc) {
    isReadEpc = readEpc;
  }

  public void setReadUse(boolean readUse) {
    isReadUse = readUse;
  }

  public void setUseSa(int useSa) {
    this.useSa = useSa;
  }

  public void setUseLen(int useLen) {
    this.useLen = useLen;
  }

  private UhfController uhfController;

  public UhfReadTask(UhfController uhfController) {
    this.uhfController = uhfController;
  }

  /** 返回true结束扫描，否则继续扫描. */
  protected abstract boolean onScanSuccess(UhfReadBean data);

  /** 返回true结束扫描，否则继续扫描. */
  protected abstract boolean onScanFailed(int errorCode);

  @Override protected boolean handleOnce() {
    boolean readSuccess = false;
    /* ==== 1、 读tid ==== */
    byte[] readTid = null;
    if (isReadTid) {
      readSuccess = uhfController.readTid(tidBuff);
      if (!readSuccess) {
        return onScanFailed(1);
      }
      readTid = tidBuff;
    }

    /* ==== 2、 读epc，tid过滤 ==== */
    byte[] readEpc = null;
    if (isReadEpc) {
      for (int i = 0; i < filterReadTimes; i++) {
        readEpc = uhfController.read(UhfCmd.MB_EPC, 0x02, 12, readTid, UhfCmd.MB_TID, 0x00);
        if (readEpc != null) {
          break;
        }
        LogUtils.d("epc failed:%s / %s", i, filterReadTimes);
      }
      if (readEpc == null) {
        return onScanFailed(2);
      }
    }

    /* 3、 ==== 读use，tid过滤 ==== */
    byte[] readUse = null;
    if (isReadUse && 0 <= useSa && 0 < useLen) {
      for (int i = 0; i < filterReadTimes; i++) {
        readUse = uhfController.read(UhfCmd.MB_USE, useSa, useLen, readTid, UhfCmd.MB_TID, 0x00);
        if (readUse != null) {
          break;
        }
        LogUtils.d("use failed:%s / %s", i, filterReadTimes);
      }
      if (readUse == null) {
        return onScanFailed(3);
      }
    }

    UhfReadBean uhfReadBean = new UhfReadBean(readTid, readEpc, readUse);
    return onScanSuccess(uhfReadBean);
  }

  protected UhfReadBean scanOnceUhf() {
    boolean readSuccess = false;
    /* ==== 1、 读tid ==== */
    readSuccess = uhfController.readTid(tidBuff);
    if (!readSuccess) {
      return null;
    }

    /* ==== 2、 读epc，tid过滤 ==== */
    byte[] readEpc = null;
    for (int i = 0; i < filterReadTimes; i++) {
      readEpc = uhfController.read(UhfCmd.MB_EPC, useSa, useLen, tidBuff, UhfCmd.MB_TID, 0x00);
      if (readEpc != null) {
        break;
      }
      LogUtils.d("epc failed:%s / %s", i, filterReadTimes);
      //readSuccess = uhfController.readEpcFilterTid(epcBuff, tidBuff);
      //if (readSuccess) {
      //  scanLotBean = new ReadWriteBean(tidBuff, Arrays.copyOf(epcBuff, epcBuff.length));
      //  break;
      //}
    }
    if (readEpc == null) {
      return null;
    }

    /* 3、 ==== 读use，tid过滤 ==== */
    byte[] readUse = null;
    if (0 <= useSa && 0 < useLen) {
      for (int i = 0; i < filterReadTimes; i++) {
        readUse = uhfController.read(UhfCmd.MB_USE, useSa, useLen, tidBuff, UhfCmd.MB_TID, 0x00);
        if (readUse != null) {
          break;
        }
        LogUtils.d("use failed:%s / %s", i, filterReadTimes);
      }
      if (readUse == null) {
        return null;
      }
    }

    UhfReadBean uhfReadBean = new UhfReadBean(tidBuff, readEpc, readUse);
    return uhfReadBean;
  }

  public static class UhfReadBean {
    byte[] tidBuff;
    byte[] epcBuff;
    byte[] useBuff;

    @Override public String toString() {
      return "UhfReadBean{" +
          "epc=" + BytesUtil.bytes2HexString(epcBuff) +
          ", tid=" + BytesUtil.bytes2HexString(tidBuff) +
          ", use=" + BytesUtil.bytes2HexString(useBuff) +
          '}';
    }

    public UhfReadBean(byte[] tidBuff, byte[] epcBuff, byte[] useBuff) {
      this.tidBuff = tidBuff;
      this.epcBuff = epcBuff;
      this.useBuff = useBuff;
    }

    public byte[] getTidBuff() {
      return tidBuff;
    }

    public byte[] getEpcBuff() {
      return epcBuff;
    }

    public byte[] getUseBuff() {
      return useBuff;
    }
  }
}
