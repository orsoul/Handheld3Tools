package com.fanfull.libhard.lock3.task;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.libhard.lock3.Lock3Operation;
import com.fanfull.libhard.rfid.RfidController;
import com.fanfull.libhard.uhf.UhfController;

import org.orsoul.baselib.lock3.Lock3Util;
import org.orsoul.baselib.lock3.bean.HandoverBean;
import org.orsoul.baselib.lock3.bean.Lock3Bean;
import org.orsoul.baselib.lock3.bean.Lock3InfoUnit;
import org.orsoul.baselib.util.AESCoder;
import org.orsoul.baselib.util.BytesUtil;

import java.util.Arrays;
import java.util.List;

/**
 * 各项业务检查-更新袋锁任务.
 */
public abstract class CoverBagTask extends ReadLockTask {
  public static final int CHECK_RES_FORMAT_WRONG = -5;
  public static final int CHECK_RES_BAG_ID_NOT_CONTAIN_UID = -7;
  public static final int CHECK_RES_EPC_NOT_EQUALS = -8;
  public static final int CHECK_RES_EPC_EQUALS = -6;
  public static final int CHECK_RES_STATUS_NOT_EQUALS_1 = -9;
  public static final int CHECK_RES_STATUS_NOT_EQUALS_2 = -10;
  public static final int CHECK_RES_STATUS_NOT_EQUALS_3 = -11;
  public static final int CHECK_RES_STATUS_NOT_CHECK_EMPTY = -12;
  public static final int CHECK_RES_V_LOW = -13;
  public static final int CHECK_RES_ENABLE_CODE_WRONG = -14;
  /** 业务 tid不匹配. */
  public static final int CHECK_RES_TID_NOT_EQUALS = -15;
  public static final int CHECK_RES_EVENT_CODE_WRONG = -16;
  /** nfc与uhf tid不匹配. */
  public static final int CHECK_RES_NFC_UHF_TID_NOT_EQUALS = -17;

  public static final int WRITE_RES_ARGS_WRONG = -101;
  public static final int WRITE_RES_WRITE_EPC_FAILED = -102;
  public static final int WRITE_RES_WRITE_NFC_FAILED = -103;
  public static final int WRITE_RES_WRITE_HANDOVER_FAILED = -104;

  /** 空包登记. */
  public static final int TASK_TYPE_REG = 1;
  /** 封袋. */
  public static final int TASK_TYPE_COVER = 2;
  /** 开袋. */
  public static final int TASK_TYPE_OPEN = 3;
  /** 出入库等其他业务. */
  public static final int TASK_TYPE_OTHER = 4;

  /** 任务类型. */
  protected int taskType = TASK_TYPE_OTHER;

  /** 验袋标志位，用于封袋，当值为空袋检测位时 将其写入 验袋标志位. */
  protected int statusCheck;
  /** 更新启用码，用于所有业务，当为true时，更新袋锁时 将写入 启用标志. */
  protected boolean isUpdateEnableCode;

  public int getTaskType() {
    return taskType;
  }

  public void setTaskType(int taskType) {
    this.taskType = taskType;
  }

  public int getStatusCheck() {
    return statusCheck;
  }

  public void setStatusCheck(int statusCheck) {
    this.statusCheck = statusCheck;
  }

  public boolean isUpdateEnableCode() {
    return isUpdateEnableCode;
  }

  public void setUpdateEnableCode(boolean updateEnableCode) {
    isUpdateEnableCode = updateEnableCode;
  }

  @Override protected void onSuccess(Lock3Bean lock3Bean) {
    /* 1.初始化检查：袋id格式、 uhf与nfc 袋id是否一致 */
    //Lock3InfoUnit unitBagId = lock3Bean.getInfoUnit(Lock3Bean.SA_BAG_ID);
    //String bagId = BytesUtil.bytes2HexString(unitBagId.buff);
    String uid = BytesUtil.bytes2HexString(lock3Bean.uidBuff);
    String epc = lock3Bean.getPieceEpc();
    String bagId = lock3Bean.getBagId();
    LogUtils.d("uid:%s\nepc:%s\nnfc:%s", uid, epc, bagId);

    boolean check;
    if (!bagId.startsWith("05")) {
      check = onCheckFailed(CHECK_RES_FORMAT_WRONG, lock3Bean);
      if (check) {
        return;
      }
    }

    if (!bagId.contains(uid)) {
      check = onCheckFailed(CHECK_RES_BAG_ID_NOT_CONTAIN_UID, lock3Bean);
      if (check) {
        return;
      }
    }

    if (taskType == TASK_TYPE_COVER && bagId.equals(epc)) {
      // 封袋业务，epc等于袋id
      check = onCheckFailed(CHECK_RES_EPC_EQUALS, lock3Bean);
      if (check) {
        return;
      }
    }
    if (taskType != TASK_TYPE_COVER && !bagId.equals(epc)) {
      // 非封袋业务，epc不等于袋id
      check = onCheckFailed(CHECK_RES_EPC_NOT_EQUALS, lock3Bean);
      if (check) {
        return;
      }
    }

    // 检查 uhf与nfc 是否配对
    Lock3InfoUnit unitTid = lock3Bean.getInfoUnit(Lock3Bean.SA_LOCK_TID);
    if (unitTid != null) {
      String lockTid = BytesUtil.bytes2HexString(unitTid.buff);
      //String lockTid = lock3Bean.getTidFromLock();
      String pieceTid = lock3Bean.getPieceTid();
      if (!pieceTid.equals(lockTid)) {
        check = onCheckFailed(CHECK_RES_NFC_UHF_TID_NOT_EQUALS, lock3Bean);
        if (check) {
          return;
        }
        LogUtils.i("uhf与nfc tid不一致\nuhfTid：%s\nlockTid：%s", pieceTid, lockTid);
      }
    }
    /* 2.标志位检查 */
    int status = lock3Bean.getStatus();
    if (taskType == TASK_TYPE_REG && status != 1) {
      // 空包登记，标志位不等于 1
      check = onCheckFailed(CHECK_RES_STATUS_NOT_EQUALS_1, lock3Bean);
      if (check) {
        return;
      }
    }
    if (taskType == TASK_TYPE_COVER && status != 2) {
      // 封袋业务，标志位不等于 2
      check = onCheckFailed(CHECK_RES_STATUS_NOT_EQUALS_2, lock3Bean);
      if (check) {
        return;
      }
    }
    if ((taskType == TASK_TYPE_OTHER || taskType == TASK_TYPE_OPEN)
        && status != 3) {
      // 其他业务，标志位不等于 3
      check = onCheckFailed(CHECK_RES_STATUS_NOT_EQUALS_3, lock3Bean);
      if (check) {
        return;
      }
    }

    Lock3InfoUnit unitStatusCheck = lock3Bean.getInfoUnit(Lock3Bean.SA_STATUS_CHECK);
    if (unitStatusCheck != null
        && lock3Bean.getStatusCheck() != Lock3Util.FLAG_CHECK_STATUS_REG) {
      // 空袋检测位
      check = onCheckFailed(CHECK_RES_STATUS_NOT_CHECK_EMPTY, lock3Bean);
      if (check) {
        return;
      }
    }

    /* 3.电压检查 */
    if (lock3Bean.getInfoUnit(Lock3Bean.SA_VOLTAGE) != null) {
      float voltage = lock3Bean.getVoltage();
      if (!Lock3Util.checkV(voltage)) {
        check = onCheckFailed(CHECK_RES_V_LOW, lock3Bean);
        if (check) {
          return;
        }
      }
    }
    /* 4.启用码检查 */
    Lock3InfoUnit unitEnable = lock3Bean.getInfoUnit(Lock3Bean.SA_ENABLE);
    if (unitEnable != null
        && !Lock3Util.checkEnableCode(unitEnable.buff)) {
      check = onCheckFailed(CHECK_RES_ENABLE_CODE_WRONG, lock3Bean);
      if (check) {
        return;
      }
    }
    //int enable = lock3Bean.getEnable();
    //if (enable != Lock3Util.ENABLE_STATUS_ENABLE) {
    //}

    // 非封袋 非空包登记 继续检查
    //if (taskType != TASK_TYPE_COVER && taskType != TASK_TYPE_REG) {

    /* 5.业务tid检查 */
    Lock3InfoUnit unitPieceTid = lock3Bean.getInfoUnit(Lock3Bean.SA_PIECE_TID);
    if (unitPieceTid != null) {
      String pieceTid = lock3Bean.getPieceTid();
      String pieceTid6 = pieceTid.substring(12);
      String businessTid = lock3Bean.getTidFromPiece();
      if (!pieceTid.equals(businessTid)) {
        LogUtils.i("\n锁片Tid：%s\n业务Tid：%s", pieceTid, businessTid);
        String businessTid6 = businessTid.substring(0, 12);
        if (!pieceTid6.equals(businessTid6)) {
          LogUtils.i("\n锁片Tid6：%s\n业务Tid6：%s", pieceTid6, businessTid6);
          check = onCheckFailed(CHECK_RES_TID_NOT_EQUALS, lock3Bean);
          if (check) {
            return;
          }
        }
      }
    }

    /* 6.封签事件码 解密、格式检查 */
    Lock3InfoUnit unitEvent = lock3Bean.getInfoUnit(Lock3Bean.SA_COVER_EVENT);
    if (unitEvent != null) {
      Lock3InfoUnit infoUnit = lock3Bean.getInfoUnit(Lock3Bean.SA_PIECE_TID);
      Lock3InfoUnit infoUnitCover = lock3Bean.getInfoUnit(Lock3Bean.SA_COVER_EVENT);
      byte[] key = Arrays.copyOf(infoUnit.buff, 6);
      boolean b = AESCoder.myEncrypt(infoUnitCover.buff, key, false);
      infoUnitCover.buff[3] = (byte) (infoUnitCover.buff[3] & 0x0F);
      String eventCode = BytesUtil.bytes2HexString(infoUnitCover.buff);
      LogUtils.d("%s-解密:%s", eventCode, b);
      if (!b || eventCode == null || !eventCode.startsWith(bagId)) {
        LogUtils.i("\nevent:%s\nbagId:%s", eventCode, bagId);
        check = onCheckFailed(CHECK_RES_EVENT_CODE_WRONG, lock3Bean);
        if (check) {
          return;
        }
      }
      lock3Bean.setCoverCode(eventCode);
    }

    boolean b = onCheckSuccess(lock3Bean);
    if (b) {
      return;
    }

    synchronized (this) {
      try {
        this.wait(5000);
        LogUtils.d("wait end timeout");
        onWaitWriteTimeout(lock3Bean);
      } catch (InterruptedException e) {
        LogUtils.d("wait Interrupted  write:" + (lock3BeanWrite != null));
        if (lock3BeanWrite != null) {
          writeLock(lock3BeanWrite);
        }
      }
    }
  }

  private Lock3Bean lock3BeanWrite;

  /**
   * 中断线程等待，继续进行写袋锁.
   *
   * @param lock3BeanWrite 为null时，线程唤醒后不写袋锁，然后结束
   */
  public synchronized void continueWriteLock(Lock3Bean lock3BeanWrite) {
    this.lock3BeanWrite = lock3BeanWrite;
    interrupt();
  }

  public Lock3Bean genLock3Bean2Write(
      Lock3Bean lock3Bean, HandoverBean handoverBean, Lock3InfoUnit... infoUnits) {
    Lock3Bean lock3BeanWrite = new Lock3Bean();
    lock3BeanWrite.uidBuff = lock3Bean.uidBuff;

    if (isUpdateEnableCode) {
      Lock3InfoUnit enable = Lock3InfoUnit.newInstance(Lock3Bean.SA_ENABLE);
      enable.buff = Lock3Util.ENABLE_CODE_ENABLE;
      lock3BeanWrite.add(enable);
      isUpdateEnableCode = false;
    }

    if (taskType == CoverBagTask.TASK_TYPE_COVER) {
      //  ====== 封袋业务 ====== 1. 写epc
      lock3BeanWrite.pieceEpcBuff = lock3Bean.bagIdBuff;
      lock3BeanWrite.pieceTidBuff = lock3Bean.pieceTidBuff;

      // 2. 写封签事件码
      //Lock3InfoUnit unitEvent = Lock3InfoUnit.newInstance(Lock3Bean.SA_COVER_EVENT);
      //byte[] bagIdBuff = lock3Bean.getInfoUnit(Lock3Bean.SA_BAG_ID).buff;
      //byte[] tid1 = Arrays.copyOf(lock3Bean.pieceTidBuff, 6);
      //byte[] tid2 = Arrays.copyOfRange(lock3Bean.pieceTidBuff, 6, 12);
      //unitEvent.buff = BytesUtil.concatArray(bagIdBuff, tid2, bagIdBuff);
      //AESCoder.myEncrypt(unitEvent.buff, tid2, true);
      //lock3BeanWrite.add(unitEvent);
      lock3BeanWrite.getWillDoList().addAll(Arrays.asList(infoUnits));

      // 3. 写入锁片tid
      byte[] tid1 = Arrays.copyOf(lock3Bean.pieceTidBuff, 6);
      byte[] tid2 = Arrays.copyOfRange(lock3Bean.pieceTidBuff, 6, 12);
      Lock3InfoUnit unitPiece = Lock3InfoUnit.newInstance(Lock3Bean.SA_PIECE_TID);
      unitPiece.buff = BytesUtil.concatArray(tid2, tid1);
      lock3BeanWrite.add(unitPiece);

      if (statusCheck == Lock3Util.FLAG_CHECK_STATUS_REG
          || statusCheck == Lock3Util.FLAG_CHECK_STATUS_CHECKED
          || statusCheck == Lock3Util.FLAG_CHECK_STATUS_FAILED) {
        Lock3InfoUnit unitStatusCheck = Lock3InfoUnit.newInstance(Lock3Bean.SA_STATUS_CHECK);
        unitStatusCheck.buff = lock3Bean.getInfoUnit(Lock3Bean.SA_STATUS_CHECK).buff;
        unitStatusCheck.buff[0] = (byte) statusCheck;
        lock3BeanWrite.add(unitStatusCheck);
        statusCheck = -1;
      }

      // 4. 更新 标志位为F3
      int keyNum = lock3Bean.getKeyNum();
      int status = Lock3Util.getStatus(3, keyNum, lock3Bean.uidBuff, true);
      Lock3InfoUnit unitStatus = Lock3InfoUnit.newInstance(Lock3Bean.SA_STATUS);
      unitStatus.buff = lock3Bean.getInfoUnit(Lock3Bean.SA_STATUS).buff;
      unitStatus.buff[0] = (byte) status;
      lock3BeanWrite.add(unitStatus);
    } else if (taskType == CoverBagTask.TASK_TYPE_OPEN) {
      // ====== 开袋业务 ====== 更新 标志位为F4
      int keyNum = lock3Bean.getKeyNum();
      int status = Lock3Util.getStatus(4, keyNum, lock3Bean.uidBuff, true);
      Lock3InfoUnit unitStatus = Lock3InfoUnit.newInstance(Lock3Bean.SA_STATUS);
      unitStatus.buff = lock3Bean.getInfoUnit(Lock3Bean.SA_STATUS).buff;
      unitStatus.buff[0] = (byte) status;
      lock3BeanWrite.add(unitStatus);
    }

    lock3BeanWrite.handoverBean = handoverBean;

    //boolean b = Lock3Operation.getInstance().writeLock(lock3BeanWrite);
    //LogUtils.d("writeLock:%s", b);
    return lock3BeanWrite;
  }

  protected void writeLock(Lock3Bean lock3BeanWrite) {
    if (lock3BeanWrite == null) {
      LogUtils.w("lock3BeanWrite == null");
      onWriteFailed(WRITE_RES_ARGS_WRONG, null);
      return;
    }

    boolean res = false;

    /* 1、封袋任务 或 开袋任务 写 nfc */
    if (taskType == TASK_TYPE_COVER || taskType == TASK_TYPE_OPEN) {
      List<Lock3InfoUnit> willReadList = lock3BeanWrite.getWillDoList();
      if (willReadList == null || willReadList.isEmpty()) {
        onWriteFailed(WRITE_RES_ARGS_WRONG, lock3BeanWrite);
        return;
      }

      for (Lock3InfoUnit infoUnit : willReadList) {
        if (RfidController.getInstance().writeNfc(infoUnit.sa, infoUnit.buff, false)) {
          infoUnit.setDoSuccess(true);
          continue;
        }
        //if (taskType == TASK_TYPE_COVER) {
        //  // 封袋业务 更新nfc失败，覆盖epc
        //  byte[] epc = new byte[12];
        //  Arrays.fill(epc, Lock3Util.REWRITE_EPC);
        //  res = UhfController.getInstance().writeEpcFilterTid(
        //      epc, lock3BeanWrite.pieceTidBuff);
        //  LogUtils.wtf("复写epc:%s", res);
        //}
        onWriteFailed(WRITE_RES_WRITE_NFC_FAILED, lock3BeanWrite);
        return;
      }
    }

    /* 2、封袋任务 写epc */
    if (taskType == TASK_TYPE_COVER) {
      if (lock3BeanWrite.pieceEpcBuff == null || lock3BeanWrite.pieceTidBuff == null) {
        LogUtils.w("epc == %s, tid == %s",
            lock3BeanWrite.pieceEpcBuff, lock3BeanWrite.pieceTidBuff);
        onWriteFailed(WRITE_RES_ARGS_WRONG, lock3BeanWrite);
        return;
      }
      res = UhfController.getInstance().writeEpcFilterTid(
          lock3BeanWrite.pieceEpcBuff, lock3BeanWrite.pieceTidBuff);
      if (!res) {
        onWriteFailed(WRITE_RES_WRITE_EPC_FAILED, lock3BeanWrite);
        return;
      }
    }

    /* 3、所有业务都尝试写入袋流转信息，非必需 */
    if (lock3BeanWrite.handoverBean != null) {
      res = 0 < Lock3Operation.writeHandoverInfo(lock3BeanWrite.handoverBean, false);
      if (!res) {
        onWriteFailed(WRITE_RES_WRITE_HANDOVER_FAILED, lock3BeanWrite);
      }
    }
    onWriteSuccess(lock3BeanWrite);
  }

  /** @return 返回true，终止任务； */
  protected boolean onCheckFailed(int res, Lock3Bean lock3Bean) {
    String msg;
    switch (res) {
      case CHECK_RES_FORMAT_WRONG:
        msg = "袋id格式错误";
        break;
      case CHECK_RES_BAG_ID_NOT_CONTAIN_UID:
        msg = "袋id与袋锁不匹配";
        break;
      case CHECK_RES_EPC_NOT_EQUALS:
        msg = "袋id与epc不一致，请检查锁片是否被更换";
        break;
      case CHECK_RES_EPC_EQUALS:
        msg = "袋id与epc一样，请检查锁片是否已插好";
        break;
      case CHECK_RES_STATUS_NOT_EQUALS_1:
        msg = "标志位不是F1(空袋)，flag=" + lock3Bean.getStatus();
        break;
      case CHECK_RES_STATUS_NOT_EQUALS_2:
        msg = "标志位不是F2(上锁)，flag=" + lock3Bean.getStatus();
        break;
      case CHECK_RES_STATUS_NOT_EQUALS_3:
        msg = "标志位不是F3(封袋)，flag=" + lock3Bean.getStatus();
        break;
      case CHECK_RES_STATUS_NOT_CHECK_EMPTY:
        msg = "未进行空袋登记，flag=" + lock3Bean.getStatusCheck();
        break;
      case CHECK_RES_V_LOW:
        msg = "电压不在允许范围，V=" + lock3Bean.getVoltage();
        break;
      case CHECK_RES_ENABLE_CODE_WRONG:
        msg = "启用码错误，enableCode=" + lock3Bean.getEnable();
        break;
      case CHECK_RES_TID_NOT_EQUALS:
        msg = "袋锁与锁片tid不匹配";
        break;
      case CHECK_RES_EVENT_CODE_WRONG:
        msg = "封签事件码解密失败";
        break;
      default:
        msg = "未定义错误";
    }
    LogUtils.d("onCheckFailed：%s，%s", res, msg);
    return false;
  }

  protected boolean onWriteFailed(int res, Lock3Bean lock3Bean) {
    String msg;
    switch (res) {
      case WRITE_RES_ARGS_WRONG:
        msg = "袋id格式错误";
        break;
      case WRITE_RES_WRITE_EPC_FAILED:
        msg = "更新epc失败";
        break;
      case WRITE_RES_WRITE_NFC_FAILED:
        msg = "更新nfc失败";
        break;
      case WRITE_RES_WRITE_HANDOVER_FAILED:
        msg = "写入袋流转信息失败";
        break;
      default:
        msg = "未定义错误";
    }
    LogUtils.d("onUpdateFailed：%s，%s", res, msg);
    return false;
  }

  /** 线程等待超时 回调. */
  protected boolean onWaitWriteTimeout(Lock3Bean lock3Bean) {
    return false;
  }

  /**
   * 检测通过的回调.
   *
   * @return 返回true结束任务，返回false wait线程等待唤醒进行 更新袋锁.
   */
  protected abstract boolean onCheckSuccess(Lock3Bean lock3Bean);

  /** 写袋锁成功 回调. */
  protected abstract boolean onWriteSuccess(Lock3Bean lock3Bean);
}
