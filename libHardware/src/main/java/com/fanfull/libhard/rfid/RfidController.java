package com.fanfull.libhard.rfid;

import com.fanfull.libhard.EnumErrCode;
import com.fanfull.libhard.barcode.BarcodeUtil;
import java.util.Arrays;
import java.util.List;
import org.orsoul.baselib.lock3.Lock3Util;
import org.orsoul.baselib.lock3.bean.Lock3Bean;
import org.orsoul.baselib.lock3.bean.Lock3InfoUnit;
import org.orsoul.baselib.util.BytesUtil;

public class RfidController implements IRfidPSamOperation {
  private static final RfidController ourInstance = new RfidController(new RfidPSamOperationRd());

  public static RfidController getInstance() {
    return ourInstance;
  }

  private RfidController(IRfidPSamOperation rfidPSamOperation) {
    this.operation = rfidPSamOperation;
  }

  private IRfidPSamOperation operation;

  @Override
  public boolean open() {
    return operation.open();
  }

  @Override
  public boolean isOpen() {
    return operation.isOpen();
  }

  @Override
  public boolean isScanning() {
    return operation.isScanning();
  }

  @Override
  public void release() {
    operation.release();
  }

  @Override
  public void setListener(IRfidListener listener) {
    operation.setListener(listener);
  }

  @Override
  public void findNfcAsync() {
    operation.findNfcAsync();
  }

  @Override
  public byte[] findM1() {
    byte[] uid = operation.findM1();
    BytesUtil.reverse(uid);
    return uid;
  }

  @Override
  public void findM1Async() {
    operation.findM1Async();
  }

  @Override public boolean findCard(byte[] uidBuff) {
    return operation.findCard(uidBuff);
  }

  @Override public EnumErrCode findCardRes(byte[] uidBuff) {
    return operation.findCardRes(uidBuff);
  }

  public byte[] findNfcOrM1() {
    //if (isScanning()) {
    //  return null;
    //}
    //        setScanning(true);
    byte[] uid = findNfc();
    if (uid == null) {
      uid = findM1();
    }
    //        setScanning(false);
    return uid;
  }

  @Override public boolean readNfc4Byte(int sa, byte[] buff) {
    return operation.readNfc4Byte(sa, buff);
  }

  @Override public boolean readNfc(int sa, byte[] buff, boolean withFindCard) {
    return operation.readNfc(sa, buff, withFindCard);
  }

  @Override public EnumErrCode readNfc(int sa, byte[] data, byte[] uid) {
    return operation.readNfc(sa, data, uid);
  }

  /** 读nfc，不寻卡. */
  public EnumErrCode readNfc(int sa, byte[] data) {
    return readNfc(sa, data, null);
  }

  @Override public void readNfcAsync(int sa, int dataLen, boolean withFindCard) {
    operation.readNfcAsync(sa, dataLen, withFindCard);
  }

  @Override public boolean writeNfc4Byte(int sa, byte[] buff) {
    return operation.writeNfc4Byte(sa, buff);
  }

  @Override public boolean writeNfc(int sa, byte[] buff, boolean withFindCard) {
    return operation.writeNfc(sa, buff, withFindCard);
  }

  @Override
  public byte[] findNfc() {
    return operation.findNfc();
  }

  @Override public boolean readM1(int block, byte[] dataBuff) {
    return operation.readM1(block, dataBuff);
  }

  public byte[] readM1(int block) {
    byte[] data = new byte[16];
    boolean readSuccess = readM1(block, data);
    if (readSuccess) {
      return data;
    }
    return null;
  }

  @Override public boolean writeM1(int block, byte[] data16) {
    return operation.writeM1(block, data16);
  }

  @Override public int resetPSam(byte[] responseBuff) {
    return operation.resetPSam(responseBuff);
  }

  @Override public int send2PSam(byte[] cmd, byte[] responseBuff, boolean withReset) {
    return operation.send2PSam(cmd, responseBuff, withReset);
  }

  @Override public int send2PSam(byte[] cmd, byte[] responseBuff) {
    return operation.send2PSam(cmd, responseBuff);
  }

  @Override public int resetCpu(byte[] responseBuff) {
    return operation.resetCpu(responseBuff);
  }

  @Override public int send2Cpu(byte[] cmd, byte[] responseBuff, boolean withReset) {
    return operation.send2Cpu(cmd, responseBuff, withReset);
  }

  @Override public int send2Cpu(byte[] cmd, byte[] responseBuff) {
    return operation.send2Cpu(cmd, responseBuff);
  }

  public byte[] send2PSam(byte[] cosCmd) {
    byte[] responseBuff = new byte[255];
    int responseLen = send2PSam(cosCmd, responseBuff);
    if (0 < responseLen) {
      return Arrays.copyOf(responseBuff, responseLen);
    }
    return null;
  }

  public byte[] send2Cpu(byte[] cosCmd) {
    byte[] responseBuff = new byte[255];
    int responseLen = send2Cpu(cosCmd, responseBuff);
    if (0 < responseLen) {
      return Arrays.copyOf(responseBuff, responseLen);
    }
    return null;
  }

  public boolean write456Block(byte[] barcodeBuf, int WRITE_TIMES) {
    byte[][] datas = BarcodeUtil.get3Data(barcodeBuf);
    if (null == datas || 3 != datas.length) {
      return false;
    }
    boolean[] wasWrite = new boolean[3];
    boolean allWrite = false;
    int count = 0;
    while (!allWrite) {
      if (!wasWrite[0]) {
        wasWrite[0] = writeM1(4, datas[0]);
      }
      if (!wasWrite[1]) {
        wasWrite[1] = writeM1(5, datas[1]);
      }
      if (!wasWrite[2]) {
        wasWrite[2] = writeM1(6, datas[2]);
      }
      allWrite = wasWrite[0] && wasWrite[1] && wasWrite[2];
      if (WRITE_TIMES <= ++count) {
        break;
      }
    }
    return allWrite;
  }

  public boolean check456Block(byte[] barcodeBuf) {
    byte[] read456Block = read456Block();
    return Arrays.equals(barcodeBuf, read456Block);
  }

  public byte[] read456Block() {

    byte[][] datas = new byte[3][];
    boolean allRead = false;
    datas[0] = readM1(4);
    datas[1] = readM1(5);
    datas[2] = readM1(6);
    allRead = datas[0] != null
        && datas[1] != null
        && datas[2] != null;
    if (!allRead) {
      return null;
    }
    // 合并3个数组
    byte[] threeInOne = new byte[38];
    System.arraycopy(datas[0], 0, threeInOne, 0, datas[0].length);
    System.arraycopy(datas[1], 0, threeInOne, 16, datas[1].length);
    System.arraycopy(datas[2], 0, threeInOne, 32, threeInOne.length - 32);

    return threeInOne;
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
    List<Lock3InfoUnit> willReadList = lock3Bean.getWillDoList();
    if (willReadList == null || willReadList.isEmpty()) {
      return false;
    }

    if (withFindCard) {
      byte[] uid = findNfc();
      if (uid == null) {
        return false;
      }
      lock3Bean.uidBuff = uid;
    }

    for (Lock3InfoUnit infoUnit : willReadList) {
      byte[] data = new byte[infoUnit.len];
      if (readNfc(infoUnit.sa, data, false)) {
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
      byte[] uid = findNfc();
      if (uid == null) {
        return false;
      }
      lock3Bean.uidBuff = uid;
    }
    for (Lock3InfoUnit infoUnit : willReadList) {
      if (!writeNfc(infoUnit.sa, infoUnit.buff, false)) {
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

  public boolean writeStatus(int status) {
    byte[] uid = findNfc();
    if (uid == null) {
      return false;
    }

    byte[] buff4 = new byte[4];
    boolean readSuccess = readNfc4Byte(Lock3Bean.SA_KEY_NUM, buff4);
    if (!readSuccess) {
      return false;
    }

    int keyNum = Lock3Util.parseKeyNum(buff4[0]);
    if (keyNum < 0) {
      /* 密钥编号 不在合法范围，重设密钥编号为A0 */
      buff4[0] = (byte) 0xA0;
      boolean writeKeyNumSuccess = writeNfc4Byte(Lock3Bean.SA_KEY_NUM, buff4);
      if (!writeKeyNumSuccess) {
        return false;
      }
      keyNum = 0;
    }

    readSuccess = readNfc4Byte(Lock3Bean.SA_STATUS, buff4);
    if (!readSuccess) {
      return false;
    }
    int statusEncode = Lock3Util.getStatus(status, keyNum, uid, false);
    buff4[0] = (byte) statusEncode;
    boolean writeSuccess = writeNfc4Byte(Lock3Bean.SA_STATUS, buff4);
    return writeSuccess;
  }

  public boolean writeWorkMode(boolean isTestMode) {
    byte[] uid = findNfc();
    if (uid == null) {
      return false;
    }

    byte[] buff4 = new byte[4];
    if (isTestMode) {
      buff4[0] = (byte) Lock3Util.MODE_DEBUG;
    } else {
      buff4[0] = (byte) Lock3Util.MODE_NORMAL;
    }
    boolean writeSuccess = writeNfc4Byte(Lock3Bean.SA_WORK_MODE, buff4);
    return writeSuccess;
  }
}
