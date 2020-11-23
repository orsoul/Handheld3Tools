package com.fanfull.libhard.lock3.task;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.libhard.lock3.Lock3Operation;
import com.fanfull.libhard.rfid.RfidController;
import org.orsoul.baselib.lock3.bean.Lock3Bean;
import org.orsoul.baselib.lock3.bean.Lock3InfoUnit;
import org.orsoul.baselib.util.BytesUtil;
import org.orsoul.baselib.util.ThreadUtil;

/**
 * 读袋锁任务，包括读NFC、可选读超高频的EPC、TID区.
 */
public class ReadLockTask extends ThreadUtil.ThreadRunnable {
  private byte[] uid = new byte[7];

  private byte[] tid = new byte[12];
  private byte[] epc = new byte[12];

  private boolean isReadUhf = true;
  private long runTime = 5000L;
  private Lock3Bean lock3Bean;

  public Lock3Bean getLock3Bean() {
    return lock3Bean;
  }

  public void setLock3Bean(Lock3Bean lock3Bean) {
    this.lock3Bean = lock3Bean;
  }

  public void setReadUhf(boolean readUhf) {
    isReadUhf = readUhf;
  }

  public void setRunTime(long runTime) {
    this.runTime = runTime;
  }

  @Override public void run() {
    Lock3Operation lock3Operation = Lock3Operation.getInstance();

    /* 在指定时间内 寻卡NFC 和 读超高频 */
    long start = System.currentTimeMillis();
    while (true) {
      int res;
      if (isReadUhf) {
        res = lock3Operation.readUidAndTid(uid, tid, epc);
      } else {
        boolean findCard = RfidController.getInstance().findCard(uid);
        res = findCard ? 0 : -2;
      }
      long goingTime = System.currentTimeMillis() - start;
      onProgress(res, goingTime, runTime);
      if (res == 0) {
        break;
      } else if (runTime < goingTime) {
        onFailed(res);
        return;
      }
    }

    if (lock3Bean == null) {
      lock3Bean = new Lock3Bean();
      lock3Bean.addAllSa();
    }

    // 暂时移除 数据长度不固定的区域（袋流转信息）
    Lock3InfoUnit circulationUnit = lock3Bean.getInfoUnit(Lock3Bean.SA_CIRCULATION);
    lock3Bean.removeSa(Lock3Bean.SA_CIRCULATION);

    /* 读nfc 长度固定的数据区 */
    boolean readLockNfc = lock3Operation.readLockNfc(lock3Bean, false);
    if (!readLockNfc) {
      onFailed(-5);
      return;
    }

    /* 读nfc 长度不固定的数据区（袋流转信息） */
    // TODO: 2020-11-16  读nfc 长度不固定的数据区
    Lock3InfoUnit unitStatus = lock3Bean.getInfoUnit(Lock3Bean.SA_CIRCULATION_INDEX);
    if (circulationUnit != null && unitStatus != null) {
      byte[] buff = unitStatus.buff;
      //int handOverNum = buff[2];
      int handOverNum = buff[0];
      if (1 <= handOverNum && handOverNum <= 5) {
        circulationUnit.len = handOverNum * 7 * 4;
        readLockNfc = lock3Operation.readLockNfc(circulationUnit);
      }
      LogUtils.d("handOverNum:%s, readSuccess:%s", handOverNum, readLockNfc);
      // 无论袋流转信息是否读取成功，都将其添加回列表
      lock3Bean.add(circulationUnit);
    }

    lock3Bean.uidBuff = uid;
    lock3Bean.parseInfo();
    if (isReadUhf) {
      lock3Bean.setPieceEpc(BytesUtil.bytes2HexString(epc));
      lock3Bean.setPieceTid(BytesUtil.bytes2HexString(tid));
    }
    onSuccess(lock3Bean);
  }

  protected void onProgress(int res, long progress, long total) {
  }

  protected void onSuccess(Lock3Bean lock3Bean) {
  }

  /**
   * 读nfc失败时 回调.
   *
   * @param errorCode 结果码<br />
   * * 结果码=0 执行成功<br/>
   * * 结果码=-1 参数错误<br/>
   * * 结果码=-2 nfc寻卡失败<br/>
   * * 结果码=-3 读tid失败<br/>
   * * 结果码=-4 读epc失败<br/>
   * * 结果码=-5 读nfc失败<br/>
   */
  protected void onFailed(int errorCode) {
    switch (errorCode) {
      case -1:
        LogUtils.d("onFailed:%s 参数错误", errorCode);
        break;
      case -2:
        LogUtils.d("onFailed:%s nfc寻卡失败", errorCode);
        break;
      case -3:
        LogUtils.d("onFailed:%s 读tid失败", errorCode);
        break;
      case -4:
        LogUtils.d("onFailed:%s 读epc失败", errorCode);
        break;
      case -5:
        LogUtils.d("onFailed:%s 读nfc失败", errorCode);
        break;
      default:
        LogUtils.d("onFailed:%s 未定义失败", errorCode);
    }
  }
}