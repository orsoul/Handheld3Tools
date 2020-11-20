package com.fanfull.libhard.lock3;

import androidx.annotation.Nullable;
import com.fanfull.libhard.rfid.RfidController;
import com.fanfull.libhard.uhf.UhfCmd;
import com.fanfull.libhard.uhf.UhfController;
import java.util.List;
import org.orsoul.baselib.lock3.bean.Lock3Bean;
import org.orsoul.baselib.lock3.bean.Lock3InfoUnit;

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
    byte[] epc = uhfController.readEpcWithTid(300);
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
   * 寻卡NFC后马上读tid.
   *
   * @param uid 7字节，必选
   * @param tid 12字节，可选
   * @param epc 12字节，可选，若读取会以tid作为过滤
   * @return 结果码<br />
   * 结果码=0 执行成功<br/>
   * 结果码=-1 参数错误<br/>
   * 结果码=-2 nfc寻卡失败<br/>
   * 结果码=-3 读tid失败<br/>
   * 结果码=-4 读epc失败<br/>
   */
  public int readUidAndTid(byte[] uid, @Nullable byte[] tid, @Nullable byte[] epc) {
    if (uid == null
        || uid.length != 7) {
      return -1;
    }

    /* 1、读Nfc uid */
    boolean readSuccess = rfidController.findCard(uid);
    if (!readSuccess) {
      return -2;
    }

    /* 2、读Uhf tid */
    if (tid != null) {
      readSuccess = uhfController.fastTid(0x00, tid);
      if (!readSuccess) {
        return -3;
      }
    }

    /* 3、读Uhf epc */
    if (epc != null) {
      readSuccess = uhfController.read(UhfCmd.MB_EPC, 0x02, epc, UhfCmd.MB_TID, 0x00, tid);
      if (!readSuccess) {
        return -4;
      }
    }
    return 0;
  }

  /**
   * 寻卡NFC后马上读tid.
   *
   * @param uid 7字节，必选
   * @param tid 12字节，必选
   * @return 结果码<br />
   * 结果码=0 执行成功<br/>
   * 结果码=-1 参数错误<br/>
   * 结果码=-2 nfc寻卡失败<br/>
   * 结果码=-3 读tid失败<br/>
   */
  public int readUidAndTid(byte[] uid, byte[] tid) {
    return readUidAndTid(uid, tid, null);
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
      if (uhfController.write(UhfCmd.MB_EPC, 0x02, epcBuff, UhfCmd.MB_TID, 0x03, filterBuff)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 读nfc.
   *
   * @param uid 7字节，传入null 不寻卡
   */
  public boolean readLockNfc(byte[] uid, Lock3InfoUnit... willReadList) {
    if (willReadList == null || willReadList.length == 0) {
      return false;
    }

    if (uid != null && !rfidController.findCard(uid)) {
      return false;
    }

    for (Lock3InfoUnit infoUnit : willReadList) {
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

  public boolean readLockNfc(Lock3InfoUnit... willReadList) {
    return readLockNfc(null, willReadList);
  }

  public boolean readLockNfc(Lock3Bean lock3Bean, boolean withFindCard) {
    if (lock3Bean == null) {
      return false;
    }
    List<Lock3InfoUnit> willReadList = lock3Bean.getWillDoList();
    if (willReadList == null || willReadList.isEmpty()) {
      return false;
    }

    willReadList.toArray(new Lock3InfoUnit[0]);

    if (withFindCard) {
      byte[] uid = rfidController.findNfc();
      if (uid == null) {
        return false;
      }
      lock3Bean.uidBuff = uid;
    }

    for (Lock3InfoUnit infoUnit : willReadList) {
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
    List<Lock3InfoUnit> willReadList = lock3Bean.getWillDoList();
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
    for (Lock3InfoUnit infoUnit : willReadList) {
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
