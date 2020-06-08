package com.fanfull.libhard.nfc;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.libhard.gpio.impl.GpioController;
import com.fanfull.libhard.serialport.impl.SerialPortController;
import com.halio.Rfid;
import com.rd.io.Platform;
import java.util.concurrent.Executors;
import org.orsoul.baselib.util.ArrayUtils;

public class RfidOperationRd extends AbsRfidOperation {
  private final String SERIAL_PORT_PATH = "/dev/ttyMT2";
  private final int BUADRATE = 115200;
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

    byte[] buff = new byte[16];
    int len = Rfid.getHwVersion(buff);
    LogUtils.v("%s:%s", len, ArrayUtils.bytes2HexString(buff, 0, len));
    if (len > 0) {
      LogUtils.d("HwVersion:%s", new String(buff, 0, len).trim());
      isOpen = true;
      if (nfcListener != null) {
        nfcListener.onOpen();
      }
      executor = Executors.newSingleThreadExecutor();
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void release() {
    Rfid.closeCommPort();
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
      LogUtils.w("PcdConfigISOType() failed ");
      return false;
    }

    byte[] tagType = new byte[2];
    boolean reVal = Rfid.PcdRequest(Rfid.CARD_ALL, tagType);
    LogUtils.d("PcdRequest %s:%s", reVal, ArrayUtils.bytes2HexString(tagType));
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
      LogUtils.d("PcdSelect failed");
      return false;
    }
    LogUtils.d("%X", tagSize[0]);
    boolean authSuccess = Rfid.PcdDoAuthen(Rfid.AUTH_KEY_A, (byte) block, Rfid.DEFAULT_KEY);
    if (!authSuccess) {
      LogUtils.d("PcdDoAuthen failed");
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
    boolean findSuccess;
    if (isFindNfc) {
      uid = new byte[7];
      findSuccess = Rfid.ULPcdAnticoll(uid);
    } else {
      uid = new byte[4];
      findSuccess = Rfid.PcdAnticoll(uid);
    }
    byte[] reVal = null;
    if (findSuccess) {
      reVal = uid;
    }
    LogUtils.d("isFindNfc:%s - %s", isFindNfc, ArrayUtils.bytes2HexString(reVal));
    return reVal;
  }

  @Override
  public byte[] findNfc() {
    //        if (!config()) {
    //            return null;
    //        }
    //
    //        byte[] cardNumber = new byte[7];
    //        if (!Rfid.ULPcdAnticoll(cardNumber)) {
    //            return null;
    //        }
    return findCard(true);
  }

  @Override
  public byte[] findM1() {
    //        if (isScanning()) {
    //            return null;
    //        } else {
    //            setScanning(true);
    //        }
    //
    //        if (!config()) {
    //            setScanning(false);
    //            return null;
    //        }
    //
    //        byte[] uid = new byte[4];
    //        if (Rfid.PcdAnticoll(uid)) {
    //            ArrayUtils.reverse(uid);
    //            setScanning(false);
    //            return uid;
    //        }
    //        setScanning(false);
    return findCard(false);
  }

  @Override
  public boolean readNfc4Byte(int sa, byte[] data4) {
    boolean readSuccess = Rfid.ULPcdRead((byte) sa, data4);
    LogUtils.d("%s:%s(%s)", readSuccess, sa, ArrayUtils.bytes2HexString(data4));
    return readSuccess;
  }

  @Override
  public boolean readNfc(int sa, byte[] buff, boolean withFindCard) {
    if (sa < 0 || buff == null || buff.length < 1) {
      LogUtils.w("check failed sa:%s buff:%s len:%s", sa, buff);
      return false;
    }

    if (withFindCard) {
      byte[] uid = findCard(true);
      if (uid == null) {
        LogUtils.w("findNfc failed");
        return false;
      }
    }

    byte[] oneWord = new byte[4];
    int len = buff.length;
    int wordDataLen = (len - 1) / 4 + 1;
    for (int i = 0; i < wordDataLen; i++) {
      byte newStart = (byte) (i + sa);
      if (!readNfc4Byte(newStart, oneWord)) {
        LogUtils.w("第%s次 读nfc addr[%s]失败", i, newStart);
        return false;
      }
      int destPos = i * 4;
      int copyLen = len - destPos;
      if (4 < copyLen) {
        copyLen = 4;
      }
      System.arraycopy(oneWord, 0, buff, destPos, copyLen);
    }
    return true;
  }

  @Override
  public boolean writeNfc4Byte(int sa, byte[] data4) {
    boolean writeSuccess = Rfid.ULPcdWrite((byte) sa, data4);
    LogUtils.d("%s:%s(%s)", writeSuccess, sa, ArrayUtils.bytes2HexString(data4));
    return writeSuccess;
  }

  @Override
  public boolean writeNfc(int sa, byte[] buff, boolean withFindCard) {
    if (sa < 0 || buff == null || buff.length < 1) {
      LogUtils.w("check failed sa:%s buff:%s", sa, buff);
      return false;
    }

    if (withFindCard) {
      byte[] uid = findCard(true);
      if (uid == null) {
        LogUtils.w("findNfc failed");
        return false;
      }
    }

    byte[] oneWord = new byte[4];
    int len = buff.length;
    int wordDataLen = (len - 1) / 4 + 1;
    for (int i = 0; i < wordDataLen; i++) {
      byte newStart = (byte) (i + sa);
      if (!writeNfc4Byte(newStart, oneWord)) {
        LogUtils.w("第%s次 写nfc addr[%s]失败", i, newStart);
        return false;
      }
      int destPos = i * 4;
      int copyLen = len - destPos;
      if (4 < copyLen) {
        copyLen = 4;
      }
      System.arraycopy(oneWord, 0, buff, destPos, copyLen);
    }
    return true;
  }

  @Override
  public byte[] readM1(int block) {
    if (!authM1(block)) {
      return null;
    }
    byte[] reVal = null;
    byte[] data = new byte[16];
    boolean readSuccess = Rfid.PcdRead((byte) block, data);
    if (readSuccess) {
      reVal = data;
    }
    LogUtils.wtf("%s 高频卡 %s：%s", readSuccess, block, ArrayUtils.bytes2HexString(data));
    return reVal;
  }

  @Override
  public boolean writeM1(int block, byte[] data16) {
    if (!authM1(block)) {
      return false;
    }
    boolean writeSuccess = Rfid.PcdWrite((byte) block, data16);
    LogUtils.d("%s:%s-%s", writeSuccess, block, ArrayUtils.bytes2HexString(data16));
    return writeSuccess;
  }
}
