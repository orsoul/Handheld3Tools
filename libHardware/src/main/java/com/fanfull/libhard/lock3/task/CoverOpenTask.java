package com.fanfull.libhard.lock3.task;

import com.apkfuns.logutils.LogUtils;

import org.orsoul.baselib.lock3.Lock3Util;
import org.orsoul.baselib.lock3.bean.Lock3Bean;
import org.orsoul.baselib.lock3.bean.Lock3InfoUnit;
import org.orsoul.baselib.util.AESCoder;
import org.orsoul.baselib.util.BytesUtil;

import java.util.Arrays;

/**
 * 各项业务检查袋锁任务.
 */
public abstract class CoverOpenTask extends ReadLockTask {
  public static final int CHECK_RES_FORMAT_WRONG = -5;
  public static final int CHECK_RES_BAG_ID_NOT_CONTAIN_UID = -7;
  public static final int CHECK_RES_EPC_NOT_EQUALS = -8;
  public static final int CHECK_RES_EPC_EQUALS = -6;
  public static final int CHECK_RES_STATUS_NOT_EQUALS_1 = -9;
  public static final int CHECK_RES_STATUS_NOT_EQUALS_2 = -10;
  public static final int CHECK_RES_STATUS_NOT_EQUALS_3 = -11;
  public static final int CHECK_RES_V_LOW = -12;
  public static final int CHECK_RES_ENABLE_CODE_WRONG = -13;
  public static final int CHECK_RES_TID_NOT_EQUALS = -14;
  public static final int CHECK_RES_EVENT_CODE_WRONG = -15;
  /** 空包登记. */
  public static final int TASK_TYPE_REG = 1;
  /** 封袋. */
  public static final int TASK_TYPE_COVER = 2;
  /** 开袋. */
  public static final int TASK_TYPE_OPEN = 3;
  /** 出入库等其他业务. */
  public static final int TASK_TYPE_OTHER = 4;

  /** 任务类型. */
  private int taskType = TASK_TYPE_OTHER;

  public int getTaskType() {
    return taskType;
  }

  public void setTaskType(int taskType) {
    this.taskType = taskType;
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
    //Lock3InfoUnit unitTid = lock3Bean.getInfoUnit(Lock3Bean.SA_LOCK_TID);
    //String nfcTid = BytesUtil.bytes2HexString(unitTid.buff);
    String lockTid = lock3Bean.getTidFromLock();
    String pieceTid = lock3Bean.getPieceTid();
    if (!pieceTid.equals(lockTid)) {
      LogUtils.i("uhf与nfc tid不一致\nuhfTid：%s\nlockTid：%s", pieceTid, lockTid);
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

    /* 3.电压检查 */
    float voltage = lock3Bean.getVoltage();
    if (!Lock3Util.checkV(voltage)) {
      check = onCheckFailed(CHECK_RES_V_LOW, lock3Bean);
      if (check) {
        return;
      }
    }

    /* 4.启用码检查 */
    int enable = lock3Bean.getEnable();
    if (enable != Lock3Util.ENABLE_STATUS_ENABLE) {
      check = onCheckFailed(CHECK_RES_ENABLE_CODE_WRONG, lock3Bean);
      if (check) {
        return;
      }
    }

    if (taskType == TASK_TYPE_COVER || taskType == TASK_TYPE_REG) {
      // 封袋 或 空包登记 检查到此结束
      onCheckSuccess(lock3Bean);
      return;
    }

    /* 5.业务tid检查 */
    String pieceTid6 = pieceTid.substring(6);
    String businessTid = lock3Bean.getTidFromPiece();
    if (!pieceTid.equals(businessTid)) {
      String businessTid6 = businessTid.substring(0, 6);
      if (!pieceTid6.equals(businessTid6)) {
        check = onCheckFailed(CHECK_RES_TID_NOT_EQUALS, lock3Bean);
        if (check) {
          return;
        }
      }
    }

    /* 6.封签事件码 解密、格式检查 */
    //byte[] key = BytesUtil.hexString2Bytes(pieceTid6);
    Lock3InfoUnit infoUnit = lock3Bean.getInfoUnit(Lock3Bean.SA_PIECE_TID);
    Lock3InfoUnit infoUnitCover = lock3Bean.getInfoUnit(Lock3Bean.SA_COVER_EVENT);
    byte[] key = Arrays.copyOf(infoUnit.buff, 6);
    boolean b = AESCoder.myEncrypt(infoUnitCover.buff, key, false);
    String eventCode = BytesUtil.bytes2HexString(infoUnitCover.buff);
    LogUtils.d("%s-解密:%s", eventCode, b);
    if (!b || eventCode == null || eventCode.startsWith(bagId)) {
      check = onCheckFailed(CHECK_RES_EVENT_CODE_WRONG, lock3Bean);
      if (check) {
        return;
      }
    }
    lock3Bean.setCoverCode(eventCode);
    onCheckSuccess(lock3Bean);
  }

  /** @return 返回true，终止任务； */
  public boolean onCheckFailed(int res, Lock3Bean lock3Bean) {
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

  public abstract boolean onCheckSuccess(Lock3Bean lock3Bean);
}
