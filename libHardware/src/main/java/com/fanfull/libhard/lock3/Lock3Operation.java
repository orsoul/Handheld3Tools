package com.fanfull.libhard.lock3;

import androidx.annotation.Nullable;

import com.fanfull.libhard.rfid.IRfidOperation;
import com.fanfull.libhard.rfid.RfidController;
import com.fanfull.libhard.uhf.UhfCmd;
import com.fanfull.libhard.uhf.UhfController;

import org.orsoul.baselib.lock3.Lock3Util;
import org.orsoul.baselib.lock3.bean.HandoverBean;
import org.orsoul.baselib.lock3.bean.Lock3Bean;
import org.orsoul.baselib.lock3.bean.Lock3InfoUnit;

import java.util.List;

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

    int sa = 0x00;
    /* 2、读Uhf tid */
    if (tid != null) {
      if (tid.length == 6) {
        sa = 0x03;
      }
      readSuccess = uhfController.readTid(sa, tid);
      if (!readSuccess) {
        return -3;
      }
    }

    /* 3、读Uhf epc */
    if (epc != null) {
      readSuccess = uhfController.read(UhfCmd.MB_EPC, 0x02, epc, UhfCmd.MB_TID, sa, tid);
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
   * @param tid 12字节，可选
   * @param epc 12字节，可选，若读取会以tid作为过滤
   * @return 结果码<br />
   * 结果码=0 执行成功<br/>
   * 结果码=-1 参数错误<br/>
   * 结果码=-2 nfc寻卡失败<br/>
   * 结果码=-3 读tid失败<br/>
   * 结果码=-4 读epc失败<br/>
   */
  public int readUidEpcFilterTid(byte[] uid, @Nullable byte[] tid, @Nullable byte[] epc) {
    if (uid == null
        || uid.length != 7) {
      return -1;
    }

    /* 1、读Nfc uid */
    boolean readSuccess = rfidController.findCard(uid);
    if (!readSuccess) {
      return -2;
    }

    int sa = 0x00;
    /* 2、读Uhf tid */
    if (tid != null) {
      if (tid.length == 6) {
        sa = 0x03;
      }
      readSuccess = uhfController.readEpcFilterTid(epc, tid);
      if (!readSuccess) {
        return -3;
      }
    }

    /* 3、读Uhf epc */
    if (epc != null) {
      readSuccess = uhfController.read(UhfCmd.MB_EPC, 0x02, epc, UhfCmd.MB_TID, sa, tid);
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
   * 获取epc区12字节，tid过滤，过滤地址为0x00.
   *
   * @param filterBuff 12字节 tid
   * @param times 1~50
   */
  public byte[] readEpcFilterTid(byte[] filterBuff, int times) {
    if (times < 1) {
      times = 1;
    } else if (50 < times) {
      times = 50;
    }
    for (int i = 0; i < times; i++) {
      byte[] epc = uhfController.read(UhfCmd.MB_EPC, 0x02, 12, filterBuff, UhfCmd.MB_TID, 0x00);
      if (epc != null) {
        return epc;
      }
    }
    return null;
  }

  /**
   * 写袋id到epc，tid过滤，过滤地址为0x12.
   *
   * @param epcBuff 12字节 袋id
   * @param filterBuff 12字节 tid
   * @param times 1~50
   */
  public boolean writeEpcFilterTid(byte[] epcBuff, byte[] filterBuff, int times) {
    if (times < 1) {
      times = 1;
    } else if (50 < times) {
      times = 50;
    }
    for (int i = 0; i < times; i++) {
      if (uhfController.write(UhfCmd.MB_EPC, 0x02, epcBuff, UhfCmd.MB_TID, 0x00, filterBuff)) {
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
   * 写锁3，可写epc、nfc、袋流转信息.
   *
   * @return 所有信息写入成功返回true.
   */
  public boolean writeLock(Lock3Bean lock3Bean) {
    if (lock3Bean == null) {
      return false;
    }

    boolean reVal = false;
    if (lock3Bean.pieceEpcBuff != null) {
      reVal = uhfController.writeEpcFilterTid(lock3Bean.pieceEpcBuff, lock3Bean.pieceTidBuff);
      if (!reVal) {
        return false;
      }
    }

    byte[] uid = rfidController.findNfc();
    if (uid == null) {
      return false;
    }
    lock3Bean.uidBuff = uid;

    if (lock3Bean.handoverBean != null) {
      reVal = 0 < writeHandoverInfo(lock3Bean.handoverBean, false);
      if (!reVal) {
        return false;
      }
    }

    List<Lock3InfoUnit> willReadList = lock3Bean.getWillDoList();
    if (willReadList == null || willReadList.isEmpty()) {
      return reVal;
    }

    for (Lock3InfoUnit infoUnit : willReadList) {
      if (!rfidController.writeNfc(infoUnit.sa, infoUnit.buff, false)) {
        return false;
      }
      infoUnit.setDoSuccess(true);
    }
    return true;
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

  /**
   * 设置标志位.1~5 对应 F1~F5
   *
   * @param status 1~5
   * @param uid 为null，执行寻卡操作，否则后续的读写均不再寻卡.
   */
  public boolean setLockStatus(int status, byte[] uid) {
    if (5 < status || status < 1) {
      return false;
    }

    if (uid == null) {
      uid = rfidController.findNfc();
      if (uid == null) {
        return false;
      }
    }

    Lock3Bean lock3Bean = new Lock3Bean();
    Lock3InfoUnit unitKey = Lock3InfoUnit.newInstance(Lock3Bean.SA_KEY_NUM);
    unitKey.buff = new byte[4];
    unitKey.buff[0] = (byte) 0xA0;
    lock3Bean.add(unitKey);

    Lock3InfoUnit unitFlag = Lock3InfoUnit.newInstance(Lock3Bean.SA_STATUS);
    int statusEncrypt = Lock3Util.getStatus(status, 0, uid, true);
    unitFlag.buff = new byte[4];
    unitFlag.buff[0] = (byte) statusEncrypt;
    lock3Bean.add(unitFlag);

    return Lock3Operation.getInstance().writeLockNfc(lock3Bean, false);
  }

  public boolean setLockStatus(int status) {
    return setLockStatus(status, null);
  }

  /**
   * 往NFC写袋流转信息.
   *
   * @return 写入成功 返回 写入后袋流转信息记录的数目，error：<br/>
   * 0:参数错误<br/>
   * -1:获取记录数目失败<br/>
   * -2:获取记录数目 超出范围<br/>
   * -3:写记录失败<br/>
   */
  public static int writeHandoverInfo(HandoverBean handoverBean, boolean withFindCard) {
    if (handoverBean == null || handoverBean.getFunction() < HandoverBean.FUN_COVER_BAG) {
      return 0;
    }
    byte[] data = handoverBean.toBytes();
    if (data == null || data.length != HandoverBean.ONE_ITEM_LEN) {
      return 0;
    }

    byte[] numBuff = new byte[1];
    boolean writeRes;

    // 流转记录写入的位置，封袋业务作为第一条记录
    int saData = HandoverBean.SA_DATA;
    // 已保存记录的数量
    int savedNum = 0;

    IRfidOperation rfidController = RfidController.getInstance();
    if (HandoverBean.FUN_COVER_BAG < handoverBean.getFunction()) {
      // 其他业务 读取已保存的记录数目
      writeRes = rfidController.readNfc(HandoverBean.SA_INDEX, numBuff, withFindCard);
      if (!writeRes) {
        return -1;  // 获取记录数目失败
      } else if (9 < numBuff[0] || numBuff[0] < 1) {
        return -2; // 记录数目超出范围
      }
      savedNum = numBuff[0];
      saData += savedNum * 7; // 一条记录7个字长，28byte
      withFindCard = false;
    }
    //handoverBean.setTimeSecond(LoginInfo.currentTimeMillisFix());
    writeRes = rfidController.writeNfc(saData, data, withFindCard);
    if (writeRes) {
      numBuff[0] = (byte) (savedNum + 1);
      writeRes = rfidController.writeNfc(
          HandoverBean.SA_INDEX, numBuff, false);
    }
    if (!writeRes) {
      return -3;  // 写记录失败
    }
    return savedNum + 1;
  }
}
