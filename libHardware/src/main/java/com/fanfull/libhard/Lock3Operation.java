package com.fanfull.libhard;

import com.fanfull.libhard.nfc.RfidController;
import com.fanfull.libhard.uhf.UhfCmd;
import com.fanfull.libhard.uhf.UhfController;

/**
 * 锁3高频、超高频 操作类，使用前 RfidController 以及 UhfController 需要先初始化.
 */
public class Lock3Operation {
  private static final Lock3Operation ourInstance = new Lock3Operation();

  public static Lock3Operation getInstance() {
    return ourInstance;
  }

  private Lock3Operation() {
  }

  private RfidController rfidController = RfidController.getInstance();
  private UhfController uhfController = UhfController.getInstance();

  /**
   * 寻卡nfc-读epc-读tid.
   *
   * @param uid byte[7]
   * @param epc12 byte[12]
   * @param tid6 byte[6]
   * @return 全部读成功返回0；寻卡nfc失败 返回1，读epc失败 返回2，获取tid失败 返回3，其他异常 返回-1
   */
  public int readUidEpcTid(byte[] uid, byte[] epc12, byte[] tid6) {
    if (uid == null || epc12 == null || tid6 == null
        || uid.length != 7 || epc12.length != 12 || tid6.length != 6) {
      return -1;
    }

    /* 1、读Nfc uid */
    boolean readSuccess = rfidController.findCard(uid);
    if (!readSuccess) {
      return 1;
    }

    int reVal = -1;
    /* 2、读 epc */
    byte[] epc = uhfController.fastEpc(300);
    if (epc == null) {
      return 2;
    } else if (epc.length == 12) {
      /* 3、读 tid */
      byte[] tid = uhfController.read(UhfCmd.MB_TID, 0x03, 6, epc, UhfCmd.MB_EPC, 0x02);
      if (tid == null) {
        return 2;
      }
      System.arraycopy(epc, 0, epc12, 0, epc12.length);
      System.arraycopy(tid, 0, tid6, 0, tid6.length);
      reVal = 0;
    } else if (epc.length == 24) {
      System.arraycopy(epc, 0, epc12, 0, epc12.length);
      System.arraycopy(epc, 18, tid6, 0, tid6.length);
      reVal = 0;
    }
    return reVal;
  }
}
