package com.fanfull.libhard.lock_zc;

import com.fanfull.libjava.util.BytesUtil;
import com.fanfull.libjava.util.Logs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 中钞锁 实体类，解析锁内 epc区、use区的指令区数据
 */
public class LockZcBean {

  int bagId;
  int version;
  int voucher;
  int sheet;
  int randomNum;
  int operateNum;
  int checksum;
  int statusEpc;

  /* =================== epc上，use指令区 下 ===================== */
  int logNum;
  int statusUse;
  int label;
  int cmdLen;
  byte[] cmd;

  @Override public String toString() {
    return "LockZcBean{" +
        "bagId=" + bagId +
        ", version=" + version +
        ", voucher=" + voucher +
        ", sheet=" + sheet +
        ", randomNum=" + randomNum +
        ", operateNum=" + operateNum +
        ", checksum=" + checksum +
        ", statusEpc=" + statusEpc +
        ", logNum=" + logNum +
        ", statusUse=" + statusUse +
        ", label=" + label +
        ", cmdLen=" + cmdLen +
        ", cmd=" + BytesUtil.bytes2HexString(cmd) +
        '}';
  }

  public int getBagId() {
    return bagId;
  }

  public int getVersion() {
    return version;
  }

  public int getVoucher() {
    return voucher;
  }

  public int getSheet() {
    return sheet;
  }

  public int getRandomNum() {
    return randomNum;
  }

  public int getOperateNum() {
    return operateNum;
  }

  public int getChecksum() {
    return checksum;
  }

  public int getStatusEpc() {
    return statusEpc;
  }

  public int getLogNum() {
    return logNum;
  }

  public int getStatusUse() {
    return statusUse;
  }

  public int getLabel() {
    return label;
  }

  public int getCmdLen() {
    return cmdLen;
  }

  public byte[] getCmd() {
    return cmd;
  }

  /**
   * @param epc 中钞锁epc区， 12 字节
   * @param useCmd 中钞锁指令区 至少 9 字节
   * @return 完全解析 返回true
   */
  public static LockZcBean parse(byte[] epc, byte[] useCmd) {
    LockZcBean lockZcBean = null;
    try {
      if (epc != null && 12 <= epc.length) {
        lockZcBean = new LockZcBean();
        lockZcBean.bagId = BytesUtil.bytes2Long(epc, 0, 4).intValue();
        lockZcBean.version = BytesUtil.bytes2Long(epc, 4, 1).intValue();
        lockZcBean.voucher = lockZcBean.version & 0x0F;
        lockZcBean.version = lockZcBean.version >> 4;
        lockZcBean.sheet = BytesUtil.bytes2Long(epc, 5, 2).intValue();
        lockZcBean.randomNum = BytesUtil.bytes2Long(epc, 7, 1).intValue();
        lockZcBean.statusEpc = epc[11];

        int count = (epc[9] >> 4) & 0x0F;
        Logs.out("9:%X", count);
        count = (0xFF & epc[8] << 4) | count;
        lockZcBean.operateNum = count;

        count = ((0x0F & epc[9]) << 8) | epc[10];
        lockZcBean.checksum = count;
      }

      if (lockZcBean != null && useCmd != null && 9 <= useCmd.length) {
        lockZcBean.logNum = BytesUtil.bytes2Long(useCmd, 2, 2).intValue();
        lockZcBean.statusUse = BytesUtil.bytes2Long(useCmd, 4, 4).intValue();
        lockZcBean.label = BytesUtil.bytes2Long(useCmd, 8, 1).intValue();

        if (10 <= useCmd.length && useCmd[9] + 10 <= useCmd.length) {
          lockZcBean.cmdLen = useCmd[9] & 0xFF;
          lockZcBean.cmd = Arrays.copyOfRange(useCmd, 10, lockZcBean.cmdLen);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }

    return lockZcBean;
  }

  public static class CmdBean {
    //1 指令头 8 bit Hex
    //2 张数 16 bit Int
    //2 操作员 ID1 32 bit Int
    //3 操作员 ID2 32 bit Int
    //4 操作时间 32 bit Time
    //5 手持机 ID 20 bit Int
    //6 位置序号 4 bit Int
    //7 随机数 32 bit Hex
    //8 保留 8 bit Hex 填充 0
    //9 操作结果 8 Bit Hex
    public int head;
    public int sheet;
    public int operator1;
    public int operator2;
    public int time;
    public int psamId;
    public int index;
    public int randomNum;
    public int res;

    @Override public String toString() {
      return "CmdBean{" +
          "head=" + head +
          ", sheet=" + sheet +
          ", operator1=" + operator1 +
          ", operator2=" + operator2 +
          ", time=" + time +
          ", psamId=" + psamId +
          ", index=" + index +
          ", randomNum=" + randomNum +
          ", res=" + res +
          '}';
    }
  }

  public static List<CmdBean> parse(byte[] logBuff) {
    if (logBuff == null || logBuff.length < 24) {
      return Collections.emptyList();
    }
    List<CmdBean> list = new ArrayList<>(4);
    for (int i = 0; i <= logBuff.length - 24; i += 24) {
      CmdBean cmdBean = new CmdBean();
      cmdBean.head = logBuff[i];
      cmdBean.sheet = BytesUtil.bytes2Long(logBuff, i + 1, 2).intValue();
      cmdBean.operator1 = BytesUtil.bytes2Long(logBuff, i + 3, 4).intValue();
      cmdBean.operator2 = BytesUtil.bytes2Long(logBuff, i + 7, 4).intValue();
      cmdBean.time = BytesUtil.bytes2Long(logBuff, i + 11, 4).intValue();
      cmdBean.psamId = BytesUtil.bytes2Long(logBuff, i + 15, 3).intValue();
      cmdBean.index = cmdBean.psamId & 0x0F;
      cmdBean.psamId >>= 4;
      cmdBean.randomNum = BytesUtil.bytes2Long(logBuff, i + 18, 4).intValue();
      cmdBean.res = logBuff[i + 23];
      list.add(cmdBean);
    }
    return list;
  }
}
