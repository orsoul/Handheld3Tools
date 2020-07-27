package com.fanfull.libhard.lock3;

import com.fanfull.libhard.nfc.RfidController;
import com.fanfull.libhard.uhf.UhfCmd;
import com.fanfull.libhard.uhf.UhfController;
import java.util.List;
import org.orsoul.baselib.util.lock.Lock3Bean;

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

  /**
   * 获取epc区12字节，tid过滤，过滤地址为0x03.
   *
   * @param filterBuff 6字节 tid
   * @param times 1~50
   */
  public byte[] readEpcFilterTid(byte[] filterBuff, int times) {
    if (times < 1) {
      times = 1;
    } else if (50 < times) {
      times = 50;
    }
    for (int i = 0; i < times; i++) {
      byte[] epc = uhfController.read(UhfCmd.MB_EPC, 0x02, 12, filterBuff, UhfCmd.MB_TID, 0x03);
      if (epc != null) {
        return epc;
      }
    }
    return null;
  }

  /**
   * 写袋id到epc，tid过滤，过滤地址为0x03.
   *
   * @param epcBuff 12字节 袋id
   * @param filterBuff 6字节 tid
   * @param times 1~50
   */
  public boolean writeEpcFilterTid(byte[] epcBuff, byte[] filterBuff, int times) {
    if (times < 1) {
      times = 1;
    } else if (50 < times) {
      times = 50;
    }
    for (int i = 0; i < times; i++) {
      if (uhfController.write(UhfCmd.MB_EPC, 0x02, epcBuff, filterBuff, UhfCmd.MB_TID, 0x03)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 读nfc.
   *
   * @param withFindCard true:读之前执行寻卡；false:不寻卡
   */
  public boolean readLockNfc(Lock3Bean lock3Bean, boolean withFindCard) {
    if (lock3Bean == null) {
      return false;
    }
    List<Lock3Bean.InfoUnit> willReadList = lock3Bean.getWillDoList();
    if (willReadList == null || willReadList.isEmpty()) {
      return false;
    }

    if (withFindCard) {
      byte[] uid = rfidController.findNfc();
      if (uid == null) {
        return false;
      }
      lock3Bean.uidBuff = uid;
    }

    for (Lock3Bean.InfoUnit infoUnit : willReadList) {
      byte[] data = new byte[infoUnit.len];
      if (rfidController.readNfc(infoUnit.sa, data, false)) {
        infoUnit.buff = data;
        infoUnit.setDoSuccess(true);
      } else {
        return false;
      }
    }
    return true;
  }

  /** 读nfc，读之前执行寻卡. */
  public boolean readLockNfc(Lock3Bean lock3Bean) {
    return readLockNfc(lock3Bean, true);
  }

  /**
   * 写nfc.
   *
   * @param withFindCard true:写之前执行寻卡；false:不寻卡
   */
  public boolean writeLockNfc(Lock3Bean lock3Bean, boolean withFindCard) {
    if (lock3Bean == null) {
      return false;
    }
    List<Lock3Bean.InfoUnit> willReadList = lock3Bean.getWillDoList();
    if (willReadList == null || willReadList.isEmpty()) {
      return false;
    }

    if (withFindCard) {
      byte[] uid = rfidController.findNfc();
      if (uid == null) {
        return false;
      }
      lock3Bean.uidBuff = uid;
    }
    for (Lock3Bean.InfoUnit infoUnit : willReadList) {
      if (!rfidController.writeNfc(infoUnit.sa, infoUnit.buff, false)) {
        return false;
      }
      infoUnit.setDoSuccess(true);
    }
    return true;
  }

  /** 写nfc，写之前执行寻卡. */
  public boolean writeLockNfc(Lock3Bean lock3Bean) {
    return writeLockNfc(lock3Bean, true);
  }
}
