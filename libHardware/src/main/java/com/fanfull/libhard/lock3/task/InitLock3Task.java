package com.fanfull.libhard.lock3.task;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.libhard.lock3.Lock3Operation;
import com.fanfull.libhard.rfid.RfidController;
import com.fanfull.libhard.uhf.UhfController;
import com.fanfull.libjava.util.BytesUtil;
import com.fanfull.libjava.util.ThreadUtil;
import java.util.Arrays;
import java.util.Random;
import org.orsoul.baselib.lock3.Lock3Util;
import org.orsoul.baselib.lock3.bean.BagIdParser;
import org.orsoul.baselib.lock3.bean.Lock3Bean;

/**
 * 锁3 初始化逻辑：.
 * 1、寻卡NFC、读超高频EPC及TID；
 * 2、生成袋id、密钥等信息，写入NFC；
 * 3、袋id写入EPC；
 * 4、将写入的信息从锁中读出，检查写入-读出的信息是否一样，确保写入成功；
 */
public class InitLock3Task extends ThreadUtil.TimeThreadRunnable {
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
  public byte[] tid = new byte[12];
  public byte[] bagIdBuff;

  /** 尝试读袋锁的 持续时间. */
  private long findLockTime = 5000;
  /** 尝试读写Epc的 次数. */
  private int readWriteEpcTimes = 5;
  /** 写入数据后是否需要读出数据检查. */
  private boolean isCheckData;
  /** 记录袋信息，用于生成袋id. */
  private BagIdParser bagIdParser = new BagIdParser();

  private boolean isInitUhf = true;

  public void setCheckData(boolean checkData) {
    isCheckData = checkData;
  }

  public void setBagIdParser(BagIdParser bagIdParser) {
    if (bagIdParser != null) {
      this.bagIdParser = bagIdParser;
    }
  }

  public void setBagIdBuff(byte[] bagIdBuff) {
    this.bagIdBuff = bagIdBuff;
  }

  public boolean isInitUhf() {
    return isInitUhf;
  }

  public void setInitUhf(boolean initUhf) {
    isInitUhf = initUhf;
  }

  public BagIdParser getBagIdParser() {
    return bagIdParser;
  }

  public InitLock3Task setCityCode(String cityCode) {
    bagIdParser.setCityCode(cityCode);
    return this;
  }

  public InitLock3Task setBagType(String bagType) {
    bagIdParser.setBagType(bagType);
    return this;
  }

  public InitLock3Task setMoneyType(String moneyType) {
    bagIdParser.setMoneyType(moneyType);
    return this;
  }

  public InitLock3Task setUid(byte[] uid) {
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

  private int findUidTid(byte[] uid, byte[] tid6) {
    if (uid == null || uid.length != 7) {
      return -1;
    }

    /* 1、读Nfc uid */
    boolean readSuccess = RfidController.getInstance().findCard(uid);
    if (!readSuccess) {
      return RES_FIND_NFC;
    }
    /* 2、读 tid */
    if (tid6 != null && !UhfController.getInstance().readTid(0x00, tid6)) {
      return RES_READ_TID;
    }
    return 0;
  }

  @Override protected boolean handleOnce() {
    boolean initUhf = isInitUhf;
    Lock3Operation lock3Operation = Lock3Operation.getInstance();
    /* 1、 读uid、epc、tid */
    boolean readSuccess;
    int failedCause = findUidTid(uid, initUhf ? tid : null);
    readSuccess = failedCause == 0;

    String info;
    if (readSuccess) {
      onProgress(RES_READ_TID);
    } else {
      if (failedCause == RES_READ_TID) {
        info = "读TID失败";
      } else {
        info = "寻卡NFC失败";
      }
      LogUtils.wtf("findUidTid failed:%s - %s", failedCause, info);
      onFailed(failedCause, info);
      return false;
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

    byte[] sa4 = null;
    if (bagIdBuff != null) {
      sa4 = bagIdBuff;
      bagIdParser.setBagId(BytesUtil.bytes2HexString(bagIdBuff));
    } else {
      bagIdParser.setUidBuff(uid);
      sa4 = bagIdParser.genBagIdBuff();
    }
    if (sa4 == null) {
      info = "生成袋Id 失败";
      LogUtils.wtf("%s:%s", info, bagIdParser);
      onFailed(RES_GEN_DATA, info);
      return true;
    } else {
      onProgress(RES_GEN_DATA);
    }

    initLock3Bean.getInfoUnit(Lock3Bean.SA_BAG_ID).buff = sa4;
    initLock3Bean.getInfoUnit(Lock3Bean.SA_STATUS).buff = sa10;
    initLock3Bean.getInfoUnit(Lock3Bean.SA_KEY_NUM).buff = sa14;

    if (!initUhf) {
      Arrays.fill(tid, (byte) 0x7B);
    }
    initLock3Bean.getInfoUnit(Lock3Bean.SA_LOCK_TID).buff = tid;

    boolean writeLockNfc = lock3Operation.writeLockNfc(initLock3Bean, false);
    if (!writeLockNfc) {
      info = "写NFC 失败";
      LogUtils.wtf(info);
      onFailed(RES_WRITE_NFC, info);
      return true;
    } else {
      onProgress(RES_WRITE_NFC);
    }

    /* 3、 袋id 写入超高频卡epc区 */
    boolean writeEpd = true;
    if (initUhf) {
      writeEpd = lock3Operation.writeEpcFilterTid(sa4, tid, readWriteEpcTimes);
    }
    if (!writeEpd) {
      info = "写EPC 失败";
      LogUtils.wtf(info);
      onFailed(RES_WRITE_EPC, info);
      return true;
    } else {
      onProgress(RES_WRITE_EPC);
    }

    if (!isCheckData) {
      onSuccess(bagIdParser);
      return true;
    }

    Lock3Bean readLock3Bean = new Lock3Bean();
    readLock3Bean.addInitBagSa();
    boolean checkNfcWrite = lock3Operation.readLockNfc(initLock3Bean, false);
    if (!checkNfcWrite) {
      info = "重读NFC 失败";
      LogUtils.wtf(info);
      onFailed(RES_READ_NFC, info);
      return true;
    } else {
      onProgress(RES_READ_NFC);
    }
    boolean dataEquals = readLock3Bean.dataEquals(initLock3Bean);
    if (!dataEquals) {
      info = "写入-读出NFC数据 不一致";
      LogUtils.wtf(info);
      onFailed(RES_NFC_DATA_NOT_EQUALS, info);
      return true;
    }
    byte[] readEpc = null;
    if (initUhf) {
      readEpc = lock3Operation.readEpcFilterTid(tid, readWriteEpcTimes);
    }
    if (initUhf && readEpc == null) {
      info = "重读EPC 失败";
      LogUtils.wtf(info);
      onFailed(RES_READ_EPC, info);
      return true;
    } else {
      onProgress(RES_READ_EPC);
    }
    boolean epcEquals = !initUhf || Arrays.equals(sa4, readEpc);
    if (!epcEquals) {
      info = "写入-读出EPC数据 不一致";
      LogUtils.wtf(info);
      onFailed(RES_EPC_DATA_NOT_EQUALS, info);
      return true;
    }

    onSuccess(bagIdParser);
    return true;
  }

  protected void onSuccess(BagIdParser bagIdParser) {
    bagIdBuff = null;
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
        LogUtils.d("写EPC成功" + isInitUhf);
        break;
      case RES_READ_NFC:
        LogUtils.d("读NFC成功");
        break;
      case RES_READ_EPC:
        LogUtils.d("读EPC成功" + isInitUhf);
        break;
    }
  }

  protected void onFailed(int readRes, String info) {
    LogUtils.i("onFailed:%s %s", readRes, info);
  }
}