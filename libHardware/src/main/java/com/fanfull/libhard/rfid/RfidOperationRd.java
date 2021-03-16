package com.fanfull.libhard.rfid;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.libhard.EnumErrCode;
import com.fanfull.libhard.gpio.impl.GpioController;
import com.fanfull.libhard.serialport.impl.SerialPortController;
import com.halio.Rfid;
import com.rd.io.Platform;

import org.orsoul.baselib.util.BytesUtil;

import java.util.concurrent.Executors;

/**
 * 高频卡 操作类，基于 雨滴开发板.
 */
public class RfidOperationRd extends AbsRfidOperation {
  //private final String SERIAL_PORT_PATH = "/dev/ttyMT2";
  //private final int BUADRATE = 115200;
  private SerialPortController serialPortController;
  //    private ExecutorService executor;

  public RfidOperationRd() {

  }

  @Override
  public boolean open() throws SecurityException {
    Rfid.closeCommPort();
    Rfid.openPort((byte) 2, 115200);
    Platform.initIO();
    Platform.SetGpioMode(6, 0);
    Platform.SetGpioOutput(6);
    Platform.SetGpioDataLow(6);

    try {
      Thread.sleep(50);
    } catch (java.lang.InterruptedException ie) {
    }
    Platform.SetGpioDataHigh(6);
    try {
      Thread.sleep(120);
    } catch (java.lang.InterruptedException ie) {
    }
    Rfid.notifyBootStart();
    try {
      Thread.sleep(200);
    } catch (java.lang.InterruptedException ie) {
    }

    boolean reVal;
    byte[] buff = new byte[16];
    int len = Rfid.getHwVersion(buff);
    LogUtils.tag(TAG).v("%s:%s", len, BytesUtil.bytes2HexString(buff, 0, len));
    if (len > 0) {
      LogUtils.tag(TAG).v("HwVersion:%s", new String(buff, 0, len).trim());
      isOpen = true;
      executor = Executors.newSingleThreadExecutor();
      reVal = true;
    } else {
      reVal = false;
    }
    if (nfcListener != null) {
      nfcListener.onOpen(reVal);
    }
    return reVal;
  }

  @Override public void release() {
    setListener(null);
    Rfid.closeCommPort();
    isOpen = false;
  }

  private boolean setGpioUhfMode() {

    boolean reVal = GpioController.getInstance().init()
        && GpioController.getInstance().setMode(64, 0)
        && GpioController.getInstance().setMode(62, 0)
        && GpioController.getInstance().setIO(64, false)
        && GpioController.getInstance().setIO(62, false)
        && GpioController.getInstance().set(64, true)
        && GpioController.getInstance().set(62, false);

    return reVal;
  }

  private boolean config() {
    if (!Rfid.PcdConfigISOType(Rfid.ISOTYPE_14443A)) {
      LogUtils.tag(TAG).w("PcdConfigISOType() failed ");
      return false;
    }

    byte[] tagType = new byte[2];
    boolean reVal = Rfid.PcdRequest(Rfid.CARD_ALL, tagType);
    LogUtils.tag(TAG).v("PcdRequest %s:%s", reVal, BytesUtil.bytes2HexString(tagType));
    return reVal;
  }

  private boolean authM1(byte[] uid, int block) {
    if (uid == null) {
      uid = findCard(false);
    }
    if (uid == null) {
      return false;
    }
    byte[] tagSize = new byte[1];
    if (!Rfid.PcdSelect(uid, tagSize)) {
      LogUtils.tag(TAG).v("PcdSelect failed");
      return false;
    }
    //LogUtils.tag(TAG).v("%X", tagSize[0]);
    boolean authSuccess = Rfid.PcdDoAuthen(Rfid.AUTH_KEY_A, (byte) block, Rfid.DEFAULT_KEY);
    if (!authSuccess) {
      LogUtils.tag(TAG).v("PcdDoAuthen failed");
      return false;
    }
    return true;
  }

  private boolean authM1(int block) {
    return authM1(null, block);
  }

  private byte[] findCard(boolean isFindNfc) {
    if (!config()) {
      return null;
    }

    byte[] uid;
    if (isFindNfc) {
      uid = new byte[7];
    } else {
      uid = new byte[4];
    }
    boolean findSuccess = findCard(uid);
    byte[] reVal = null;
    if (findSuccess) {
      reVal = uid;
    }
    return reVal;
  }

  @Override public byte[] findNfc() {
    return findCard(true);
  }

  @Override public byte[] findM1() {
    return findCard(false);
  }

  /**
   * M1、NFC寻卡.
   *
   * @param uidBuff 传入4byte寻卡M1，传入7byte寻卡NFC
   * @return 寻卡成功 返回true
   */
  @Override public boolean findCard(byte[] uidBuff) {
    return findCardRes(uidBuff) == EnumErrCode.SUCCESS;
  }

  @Override public EnumErrCode findCardRes(byte[] uidBuff) {
    if (uidBuff == null) {
      return EnumErrCode.ARGS_ERR;
    }
    if (!config()) {
      return EnumErrCode.FIND_CARD_NOTHING;
    }

    boolean findSuccess;
    if (uidBuff.length == 4) {
      findSuccess = Rfid.PcdAnticoll(uidBuff);
    } else if (uidBuff.length == 7) {
      findSuccess = Rfid.ULPcdAnticoll(uidBuff);
    } else {
      return EnumErrCode.ARGS_ERR;
    }
    LogUtils.tag(TAG).v("findSuccess %s:%s", findSuccess, BytesUtil.bytes2HexString(uidBuff));
    if (findSuccess) {
      return EnumErrCode.SUCCESS;
    } else {
      return EnumErrCode.FIND_CARD_FAILED;
    }
  }

  @Override public boolean readNfc4Byte(int sa, byte[] data4) {
    boolean readSuccess = Rfid.ULPcdRead((byte) sa, data4);
    //LogUtils.tag(TAG).v("%s:%02X(%s)", readSuccess, sa, BytesUtil.bytes2HexString(data4));
    return readSuccess;
  }

  @Override public boolean readNfc(int sa, byte[] buff, boolean withFindCard) {
    if (sa < 0 || buff == null || buff.length < 1) {
      LogUtils.tag(TAG).w("check failed sa:%s buff:%s", sa, buff);
      return false;
    }

    if (withFindCard) {
      byte[] uid = findCard(true);
      if (uid == null) {
        LogUtils.tag(TAG).w("findNfc failed");
        return false;
      }
    }

    byte[] oneWord = new byte[4];
    int len = buff.length;
    int wordDataLen = (len - 1) / 4 + 1;
    for (int i = 0; i < wordDataLen; i++) {
      byte newStart = (byte) (i + sa);
      if (!readNfc4Byte(newStart, oneWord)) {
        LogUtils.tag(TAG).w("第%s次 读nfc addr[%s]失败", i, newStart);
        return false;
      }
      int destPos = i * 4;
      int copyLen = len - destPos;
      if (4 < copyLen) {
        copyLen = 4;
      }
      System.arraycopy(oneWord, 0, buff, destPos, copyLen);
    }
    LogUtils.tag(TAG).d("read sa:%02X-%s", sa, BytesUtil.bytes2HexString(buff));
    return true;
  }

  @Override public EnumErrCode readNfc(int sa, byte[] data, byte[] uid) {
    if (sa < 0 || data == null || data.length < 1) {
      LogUtils.tag(TAG).w("check failed sa:%s buff:%s", sa, data);
      return EnumErrCode.ARGS_ERR;
    }

    EnumErrCode res;
    if (uid != null && uid.length == 7) {
      res = findCardRes(uid);
      if (res != EnumErrCode.SUCCESS) {
        LogUtils.tag(TAG).w("findNfc failed");
        return res;
      }
    }

    byte[] oneWord = new byte[4];
    int len = data.length;
    int wordDataLen = (len - 1) / 4 + 1;
    for (int i = 0; i < wordDataLen; i++) {
      byte newStart = (byte) (i + sa);
      if (!readNfc4Byte(newStart, oneWord)) {
        LogUtils.tag(TAG).w("第%s次 读nfc addr[%s]失败", i, newStart);
        return EnumErrCode.FAILED;
      }
      int destPos = i * 4;
      int copyLen = len - destPos;
      if (4 < copyLen) {
        copyLen = 4;
      }
      System.arraycopy(oneWord, 0, data, destPos, copyLen);
    }
    return EnumErrCode.SUCCESS;
  }

  @Override
  public boolean writeNfc4Byte(int sa, byte[] data4) {
    boolean writeSuccess = Rfid.ULPcdWrite((byte) sa, data4);
    LogUtils.tag(TAG).v("%s:%02X(%s)", writeSuccess, sa, BytesUtil.bytes2HexString(data4));
    return writeSuccess;
  }

  @Override
  public boolean writeNfc(int sa, byte[] buff, boolean withFindCard) {
    if (sa < 0 || buff == null || buff.length < 1) {
      LogUtils.tag(TAG).w("check failed sa:%s buff:%s", sa, buff);
      return false;
    }

    if (withFindCard) {
      byte[] uid = findCard(true);
      if (uid == null) {
        LogUtils.tag(TAG).w("findNfc failed");
        return false;
      }
    }

    byte[] oneWord = new byte[4];
    int len = buff.length;
    int wordDataLen = (len - 1) / 4 + 1;
    for (int i = 0; i < wordDataLen; i++) {
      int destPos = i * 4;
      int copyLen = len - destPos;
      if (4 < copyLen) {
        copyLen = 4;
      }
      System.arraycopy(buff, destPos, oneWord, 0, copyLen);

      byte newStart = (byte) (i + sa);
      if (!writeNfc4Byte(newStart, oneWord)) {
        LogUtils.tag(TAG).w("第%s次 写nfc addr[%02X]失败", i, newStart);
        return false;
      }
    }
    LogUtils.tag(TAG).d("write sa:%02X-%s", sa, BytesUtil.bytes2HexString(buff));
    return true;
  }

  @Override public boolean readM1(int block, byte[] dataBuff) {
    if (dataBuff == null || dataBuff.length != 16 || !authM1(block)) {
      return false;
    }
    boolean readSuccess = Rfid.PcdRead((byte) block, dataBuff);
    LogUtils.tag(TAG).v("%s:%s-%s", readSuccess, block, BytesUtil.bytes2HexString(dataBuff));
    return readSuccess;
  }

  @Override
  public boolean writeM1(int block, byte[] data16) {
    if (!authM1(block)) {
      return false;
    }
    boolean writeSuccess = Rfid.PcdWrite((byte) block, data16);
    LogUtils.tag(TAG).v("%s:%s-%s", writeSuccess, block, BytesUtil.bytes2HexString(data16));
    return writeSuccess;
  }
}
