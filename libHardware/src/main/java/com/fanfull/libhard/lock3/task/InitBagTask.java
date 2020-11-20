package com.fanfull.libhard.lock3.task;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.libhard.lock3.Lock3Operation;
import com.fanfull.libhard.rfid.RfidController;
import com.fanfull.libhard.uhf.UhfController;
import java.util.Arrays;
import java.util.Random;
import org.orsoul.baselib.lock3.Lock3Util;
import org.orsoul.baselib.lock3.bean.BagIdParser;
import org.orsoul.baselib.lock3.bean.Lock3Bean;
import org.orsoul.baselib.util.ClockUtil;

/**
 * 锁3 初始化逻辑：.
 * 1、寻卡NFC、读超高频EPC及TID；
 * 2、生成袋id、密钥等信息，写入NFC；
 * 3、袋id写入EPC；
 * 3、将写入的信息从锁中读出，检查写入-读出的信息是否一样，确保写入成功；
 */
public class InitBagTask implements Runnable {
  public static final int RES_FIND_NFC = 1;
  public static final int RES_READ_TID = 2;
  public static final int RES_GEN_DATA = 3;

  public static final int RES_WRITE_NFC = 4;
  public static final int RES_WRITE_EPC = 5;
  public static final int RES_READ_NFC = 6;
  public static final int RES_NFC_DATA_NOT_EQUALS = 7;
  public static final int RES_READ_EPC = 8;
  public static final int RES_EPC_DATA_NOT_EQUALS = 9;

  public byte[] uid = new byte[7];
  public byte[] tid = new byte[6];

  private boolean stopped = true;
  /** 尝试读袋锁的 持续时间. */
  private long findLockTime = 5000;
  /** 尝试读写Epc的 次数. */
  private int readWriteEpcTimes = 5;
  /** 写入数据后是否需要读出数据检查. */
  private boolean isCheckData = true;
  /** 记录袋信息，用于生成袋id. */
  private BagIdParser bagIdParser = new BagIdParser();

  public void setCheckData(boolean checkData) {
    isCheckData = checkData;
  }

  public void setBagIdParser(BagIdParser bagIdParser) {
    if (bagIdParser != null) {
      this.bagIdParser = bagIdParser;
    }
  }

  public BagIdParser getBagIdParser() {
    return bagIdParser;
  }

  public InitBagTask setCityCode(String cityCode) {
    bagIdParser.setCityCode(cityCode);
    return this;
  }

  public InitBagTask setBagType(String bagType) {
    bagIdParser.setBagType(bagType);
    return this;
  }

  public InitBagTask setMoneyType(String moneyType) {
    bagIdParser.setMoneyType(moneyType);
    return this;
  }

  public InitBagTask setUid(byte[] uid) {
    bagIdParser.setUidBuff(uid);
    return this;
  }

  public void setFindLockTime(long findLockTime) {
    this.findLockTime = findLockTime;
  }

  public void setReadWriteEpcTimes(int readWriteEpcTimes) {
    this.readWriteEpcTimes = readWriteEpcTimes;
  }

  public boolean isStopped() {
    return stopped;
  }

  public synchronized void stop() {
    this.stopped = true;
  }

  public int findUidTid(byte[] uid, byte[] tid6) {
    if (uid == null || tid6 == null
        || uid.length != 7 || tid6.length != 6) {
      return -1;
    }

    /* 1、读Nfc uid */
    boolean readSuccess = RfidController.getInstance().findCard(uid);
    if (!readSuccess) {
      return RES_FIND_NFC;
    }
    /* 2、读 tid */
    if (!UhfController.getInstance().fastTid(0x03, tid6)) {
      return RES_READ_TID;
    }
    return 0;
  }

  @Override public void run() {
    stopped = false;
    onStart(bagIdParser);
    Lock3Operation lock3Operation = Lock3Operation.getInstance();
    /* 1、 读uid、epc、tid */
    boolean readSuccess = false;
    int failedCause = -1;
    ClockUtil.runTime(true);
    while (!stopped && ClockUtil.runTime() < findLockTime) {
      //if (0 == (failedCause = lock3Operation.readUidEpcTid(uid, epc, tid))) {
      if (0 == (failedCause = findUidTid(uid, tid))) {
        readSuccess = true;
        break;
      } else {
        LogUtils.d("readUidEpcTid failed:%s", failedCause);
        //SystemClock.sleep(50);
      }
    } // end while()

    String info;
    if (!readSuccess) {
      if (failedCause == RES_READ_TID) {
        info = "读TID失败";
      } else {
        info = "寻卡NFC失败";
      }
      LogUtils.i(info);
      onFailed(failedCause, info);
      stopped = true;
      return;
    } else {
      onProgress(RES_READ_TID);
    }

    /* 2、 生成袋id、密钥编号，写入nfc */
    Lock3Bean initLock3Bean = new Lock3Bean();
    initLock3Bean.addInitBagSa();

    int keyNum = new Random().nextInt(10); // 加密编号
    int statusEncrypt = Lock3Util.getStatus(1, keyNum, uid, true);
    int statusPlain = Lock3Util.getStatus(statusEncrypt, keyNum, uid, false);
    LogUtils.d("Encrypt=Plain: %s : %s == 1", statusEncrypt, statusPlain);
    byte[] sa10 = new byte[12];
    sa10[0] = (byte) statusEncrypt;
    byte[] sa14 = new byte[4];
    sa14[0] = (byte) (keyNum | 0xA0);
    sa10[3] = sa14[0]; //

    bagIdParser.setUidBuff(uid);
    byte[] sa4 = bagIdParser.genBagIdBuff();
    if (sa4 == null) {
      info = "生成袋Id 失败";
      LogUtils.i("%s:%s", info, bagIdParser);
      onFailed(RES_GEN_DATA, info);
      stopped = true;
      return;
    } else {
      onProgress(RES_GEN_DATA);
    }

    initLock3Bean.getInfoUnit(Lock3Bean.SA_BAG_ID).buff = sa4;
    initLock3Bean.getInfoUnit(Lock3Bean.SA_STATUS).buff = sa10;
    initLock3Bean.getInfoUnit(Lock3Bean.SA_KEY_NUM).buff = sa14;

    boolean writeLockNfc = lock3Operation.writeLockNfc(initLock3Bean, false);
    if (!writeLockNfc) {
      info = "写NFC 失败";
      LogUtils.i(info);
      onFailed(RES_WRITE_NFC, info);
      stopped = true;
      return;
    } else {
      onProgress(RES_WRITE_NFC);
    }

    /* 3、 袋id 写入超高频卡epc区 */
    boolean writeEpd = lock3Operation.writeEpcFilterTid(sa4, tid, readWriteEpcTimes);
    if (!writeEpd) {
      info = "写EPC 失败";
      LogUtils.i(info);
      onFailed(RES_WRITE_EPC, info);
      stopped = true;
      return;
    } else {
      onProgress(RES_WRITE_EPC);
    }

    if (!isCheckData) {
      onSuccess(bagIdParser);
      stopped = true;
      return;
    }

    Lock3Bean readLock3Bean = new Lock3Bean();
    readLock3Bean.addInitBagSa();
    boolean checkNfcWrite = lock3Operation.readLockNfc(initLock3Bean, false);
    if (!checkNfcWrite) {
      info = "重读NFC 失败";
      LogUtils.i(info);
      onFailed(RES_READ_NFC, info);
      stopped = true;
      return;
    } else {
      onProgress(RES_READ_NFC);
    }
    boolean dataEquals = readLock3Bean.dataEquals(initLock3Bean);
    if (!dataEquals) {
      info = "写入-读出NFC数据 不一致";
      LogUtils.i(info);
      onFailed(RES_NFC_DATA_NOT_EQUALS, info);
      stopped = true;
      return;
    }
    byte[] readEpc = lock3Operation.readEpcFilterTid(tid, readWriteEpcTimes);
    if (readEpc == null) {
      info = "重读EPC 失败";
      LogUtils.i(info);
      onFailed(RES_READ_EPC, info);
      stopped = true;
      return;
    } else {
      onProgress(RES_READ_EPC);
    }
    boolean epcEquals = Arrays.equals(sa4, readEpc);
    if (!epcEquals) {
      info = "写入-读出EPC数据 不一致";
      LogUtils.i(info);
      onFailed(RES_EPC_DATA_NOT_EQUALS, info);
      stopped = true;
      return;
    }

    onSuccess(bagIdParser);
    stopped = true;
  } // end run()

  protected void onStart(BagIdParser bagIdParser) {
    LogUtils.i("onSuccess:%s", bagIdParser);
  }

  protected void onSuccess(BagIdParser bagIdParser) {
    LogUtils.i("onSuccess:%s", bagIdParser);
  }

  protected void onProgress(int progress) {
    //LogUtils.i("onProgress:%s", progress);
    switch (progress) {
      case RES_READ_TID:
        LogUtils.d("读锁成功");
        break;
      case RES_WRITE_NFC:
        LogUtils.d("写NFC成功");
        break;
      case RES_WRITE_EPC:
        LogUtils.d("写EPC成功");
        break;
      case RES_READ_NFC:
        LogUtils.d("读NFC成功");
        break;
      case RES_READ_EPC:
        LogUtils.d("读EPC成功");
        break;
    }
  }

  protected void onFailed(int readRes, String info) {
    LogUtils.i("onFailed:%s %s", readRes, info);
  }
}