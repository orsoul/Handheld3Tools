package com.fanfull.libhard.nfc;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.libhard.barcode.BarcodeUtil;
import com.halio.Rfid;
import java.util.Arrays;
import java.util.List;
import org.orsoul.baselib.util.ArrayUtils;
import org.orsoul.baselib.util.lock.Lock3Bean;
import org.orsoul.baselib.util.lock.Lock3Util;

public class RfidController implements IRfidOperation {
    private static final RfidController ourInstance = new RfidController(new RfidOperationRd());

    public static RfidController getInstance() {
        return ourInstance;
    }

    private RfidController(IRfidOperation uhfOperation) {
        this.operation = uhfOperation;
    }

    private IRfidOperation operation;

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
        return operation.findM1();
    }

    @Override
    public void findM1Async() {
        operation.findM1Async();
    }

    @Override
    public byte[] findNfcOrM1() {
        return operation.findNfcOrM1();
    }

    @Override
    public void findNfcOrM1Async() {
        operation.findNfcOrM1Async();
    }

    @Override
    public byte[] findNfc() {
        return operation.findNfc();
    }

    @Override
    public void readNfcAsync(int sa, int dataLen) {
        operation.readNfcAsync(sa, dataLen);
    }

  @Override
  public byte[] readM1(int block) {
    return operation.readM1(block);
  }

  @Override
  public boolean writeM1(int block, byte[] data16) {
    return operation.writeM1(block, data16);
  }

    @Override
    public boolean readNfc(int sa, byte[] buff, int len) {
        return operation.readNfc(sa, buff, len);
    }

    @Override
    public boolean readNfc(int sa, byte[] buff) {
        return operation.readNfc(sa, buff);
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
    return allWrite;// 写入数据失败
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

  private boolean readNfcOnly(int sa, byte[] data4) {
    boolean readSuccess = Rfid.ULPcdRead((byte) sa, data4);
    LogUtils.d("%s:%s(%s)", readSuccess, sa, ArrayUtils.bytes2HexString(data4));
    return readSuccess;
  }

  private byte[] readNfc(int sa, int len) {

    byte[] buff = new byte[len];
    byte[] oneWord = new byte[4];
    int wordDataLen = (len - 1) / 4 + 1;
    for (int i = 0; i < wordDataLen; i++) {
      byte newStart = (byte) (i + sa);
      if (!readNfcOnly(newStart, oneWord)) {
        LogUtils.w("第%s次 读nfc addr[%s]失败", i, newStart);
        return null;
      }
      int destPos = i * 4;
      int copyLen = len - destPos;
      if (4 < copyLen) {
        copyLen = 4;
      }
      System.arraycopy(oneWord, 0, buff, destPos, copyLen);
    }
    return buff;
  }

  public boolean readLockNfc(Lock3Bean lock3Bean) {
    if (lock3Bean == null) {
      return false;
    }
    List<Lock3Bean.InfoUnit> willReadList = lock3Bean.getWillReadList();
    if (willReadList == null || willReadList.isEmpty()) {
      return false;
    }

    byte[] uid = findNfc();
    if (uid == null) {
      return false;
    }

    lock3Bean.uidBuff = uid;
    for (Lock3Bean.InfoUnit infoUnit : willReadList) {
      infoUnit.buff = readNfc(infoUnit.sa, infoUnit.len);
    }

    return true;
  }

  private boolean writeNfcOnly(int sa, byte[] data) {
    boolean writeSuccess = Rfid.ULPcdWrite((byte) sa, data);
    LogUtils.d("%s:%s(%s)", writeSuccess, sa, ArrayUtils.bytes2HexString(data));
    return writeSuccess;
  }

  public boolean writeStatus(int status) {
    byte[] uid = findNfc();
    if (uid == null) {
      return false;
    }

    byte[] buff4 = new byte[4];
    boolean readSuccess = readNfcOnly(Lock3Bean.SA_KEY_NUM, buff4);
    if (!readSuccess) {
      return false;
    }

    int keyNum = Lock3Util.parseKeyNum(buff4[0]);
    if (keyNum < 0) {
      /* 密钥编号 不在合法范围，重设密钥编号为A0 */
      buff4[0] = (byte) 0xA0;
      boolean writeKeyNumSuccess = writeNfcOnly(Lock3Bean.SA_KEY_NUM, buff4);
      if (!writeKeyNumSuccess) {
        return false;
      }
      keyNum = 0;
    }

    readSuccess = readNfcOnly(Lock3Bean.SA_STATUS, buff4);
    if (!readSuccess) {
      return false;
    }
    int statusEncode = Lock3Util.getStatus(status, keyNum, uid, false);
    buff4[0] = (byte) statusEncode;
    boolean writeSuccess = writeNfcOnly(Lock3Bean.SA_STATUS, buff4);
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
    boolean writeSuccess = writeNfcOnly(Lock3Bean.SA_WORK_MODE, buff4);
    return writeSuccess;
  }
}
