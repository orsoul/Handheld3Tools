package org.orsoul.baselib.util.lock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.orsoul.baselib.util.BytesUtil;

public class Lock3Bean {
  /** 锁内袋id. */
  public static final int SA_BAG_ID = 0x04;
  /** 锁片tid 0x07~0x09. */
  public static final int SA_PIECE_TID = 0x07;
  /** 锁内UHF的tid 0x0A~0x0C. */
  public static final int SA_LOCK_TID = 0x0A;
  /** 标志位 0x10. */
  public static final int SA_STATUS = 0x10;
  /** 启用码 0x11. 启用码  FFDDFFEE已启用，EEEEEEEE注销 000000尚未启用 */
  public static final int SA_ENABLE = 0x11;
  /** 单片机工作模式 0x12. */
  public static final int SA_WORK_MODE = 0x12;
  /** 上电次数 0x13. */
  public static final int SA_TIMES = 0x13;
  /** 密钥编号 0x14. */
  public static final int SA_KEY_NUM = 0x14;
  /** 电压 0x17. */
  public static final int SA_VOLTAGE = 0x17;
  /** 封签事件码30字节  0x30—0x37. */
  public static final int SA_COVER_EVENT = 0x30;
  /** 封签流水号11字节  0x90—0x92. */
  public static final int SA_COVER_SERIAL = 0x90;
  /** 袋流转信息，35个字长，每条记录7个字长(28byte) 0x93—0xB6. */
  public static final int SA_CIRCULATION = 0x93;

  public byte[] uidBuff;

  private String pieceEpc;
  private String pieceTid;

  private String bagId;
  /** 锁片的tid. */
  private String tidFromPiece;
  /** 锁内超高频的tid. */
  private String tidFromLock;
  /** 封签事件码. */
  private String coverCode;
  /** 封袋流水号. */
  private String coverSerial;

  /** 标志位. */
  private int status;
  /** 标志位加解密选用的算法. */
  private int keyNum;
  /** 交接信息索引. */
  private int handoverIndex;
  /** 袋流转信息 索引. */
  private int circulationIndex;

  /** 基金代启用状态. 1:已启用，0:未启用，-1:已注销 */
  private int enable;

  /** 单片机工作模式. */
  private boolean isTestMode;

  /** 电压. */
  private float voltage;

  public String getTidFromPiece() {
    return tidFromPiece;
  }

  public void setTidFromPiece(String tidFromPiece) {
    this.tidFromPiece = tidFromPiece;
  }

  public String getTidFromLock() {
    return tidFromLock;
  }

  public void setTidFromLock(String tidFromLock) {
    this.tidFromLock = tidFromLock;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public float getVoltage() {
    return voltage;
  }

  public void setVoltage(float voltage) {
    this.voltage = voltage;
  }

  public String getPieceEpc() {
    return pieceEpc;
  }

  public void setPieceEpc(String pieceEpc) {
    this.pieceEpc = pieceEpc;
  }

  public String getPieceTid() {
    return pieceTid;
  }

  public void setPieceTid(String pieceTid) {
    this.pieceTid = pieceTid;
  }

  public String getBagId() {
    return bagId;
  }

  public void setBagId(String bagId) {
    this.bagId = bagId;
  }

  public String getCoverCode() {
    return coverCode;
  }

  public void setCoverCode(String coverCode) {
    this.coverCode = coverCode;
  }

  public String getCoverSerial() {
    return coverSerial;
  }

  public void setCoverSerial(String coverSerial) {
    this.coverSerial = coverSerial;
  }

  public int getKeyNum() {
    return keyNum;
  }

  public void setKeyNum(int keyNum) {
    this.keyNum = keyNum;
  }

  public int getHandoverIndex() {
    return handoverIndex;
  }

  public void setHandoverIndex(int handoverIndex) {
    this.handoverIndex = handoverIndex;
  }

  public int getCirculationIndex() {
    return circulationIndex;
  }

  public void setCirculationIndex(int circulationIndex) {
    this.circulationIndex = circulationIndex;
  }

  public int getEnable() {
    return enable;
  }

  public void setEnable(int enable) {
    this.enable = enable;
  }

  public boolean isTestMode() {
    return isTestMode;
  }

  public void setTestMode(boolean testMode) {
    isTestMode = testMode;
  }

  @Override public String toString() {
    return "Lock3Bean{" +
        "pieceEpc='" + pieceEpc + '\'' +
        ", pieceTid='" + pieceTid + '\'' +
        ", bagId='" + bagId + '\'' +
        ", tidFromPiece='" + tidFromPiece + '\'' +
        ", tidFromLock='" + tidFromLock + '\'' +
        ", coverCode='" + coverCode + '\'' +
        ", coverSerial='" + coverSerial + '\'' +
        ", status=" + status +
        ", keyNum=" + keyNum +
        ", handoverIndex=" + handoverIndex +
        ", circulationIndex=" + circulationIndex +
        ", enable=" + enable +
        ", isTestMode=" + isTestMode +
        ", voltage=" + voltage +
        '}';
  }

  // =======================================================

  public Lock3Bean(int... saArr) {
    if (saArr == null) {
      throw new RuntimeException("袋锁地址不能为null");
    }
    addSa(saArr);
  }

  public Lock3Bean() {
  }

  List<Lock3InfoUnit> willReadList = new ArrayList<>();

  public boolean addOneSa(int sa, int len) {
    Lock3InfoUnit infoUnit = Lock3InfoUnit.newInstance(sa, len);
    if (willReadList.contains(infoUnit)) {
      return false;
    }
    return willReadList.add(infoUnit);
  }

  public void addSa(int... saArr) {
    for (int i = 0; i < saArr.length; i++) {
      Lock3InfoUnit infoUnit = Lock3InfoUnit.newInstance(saArr[i]);
      if (!willReadList.contains(infoUnit)) {
        willReadList.add(infoUnit);
      }
    }
  }

  public void removeSa(int... saArr) {
    if (saArr == null || saArr.length < 1) {
      return;
    }
    for (int i = 0; i < saArr.length; i++) {
      Lock3InfoUnit unit = getInfoUnit(saArr[i]);
      if (unit != null) {
        willReadList.remove(unit);
      }
    }
  }

  public void addBaseSa() {
    addSa(SA_BAG_ID, SA_STATUS, SA_KEY_NUM, SA_VOLTAGE);
  }

  public void addInitBagSa() {
    addSa(SA_BAG_ID, SA_STATUS, SA_KEY_NUM);
  }

  /** 添加固定长度的 区域，不包括交接信息、袋流转信息等非固定长度的数据区域. */
  public void addAllSa() {
    addSa(SA_BAG_ID,
        SA_PIECE_TID,
        SA_LOCK_TID,
        SA_STATUS,
        SA_ENABLE,
        SA_WORK_MODE,
        SA_COVER_EVENT,
        SA_COVER_SERIAL,
        SA_KEY_NUM);
  }

  public Lock3InfoUnit getInfoUnit(int sa) {
    for (Lock3InfoUnit infoUnit : willReadList) {
      if (infoUnit.sa == sa) {
        return infoUnit;
      }
    }
    return null;
  }

  public void parseInfo() {
    for (Lock3InfoUnit unit : willReadList) {
      if (!unit.haveData()) {
        continue;
      }
      switch (unit.sa) {
        case Lock3Bean.SA_BAG_ID:
          this.bagId = BytesUtil.bytes2HexString(unit.buff);
          break;
        case Lock3Bean.SA_PIECE_TID:
          this.tidFromPiece = BytesUtil.bytes2HexString(unit.buff);
          break;
        case Lock3Bean.SA_LOCK_TID:
          this.tidFromLock = BytesUtil.bytes2HexString(unit.buff);
          break;
        case Lock3Bean.SA_STATUS:
          this.status = Lock3Util.getStatus(unit.buff[0], this.keyNum, this.uidBuff, false);
          this.handoverIndex = unit.buff[1];
          this.circulationIndex = unit.buff[2];
          break;
        case Lock3Bean.SA_ENABLE:
          if (Arrays.equals(Lock3Util.ENABLE_CODE_ENABLE, unit.buff)) {
            this.enable = 1;
          } else if (Arrays.equals(Lock3Util.ENABLE_CODE_UN_REG, unit.buff)) {
            this.enable = -1;
          } else {
            this.enable = 0;
          }
          break;
        case Lock3Bean.SA_WORK_MODE:
          this.isTestMode = Lock3Util.MODE_DEBUG == unit.buff[0];
          break;
        case Lock3Bean.SA_KEY_NUM:
          this.keyNum = Lock3Util.parseKeyNum(unit.buff[0]);
          break;
        case Lock3Bean.SA_VOLTAGE:
          this.voltage = Lock3Util.parseV(unit.buff[0]);
          break;
        case Lock3Bean.SA_COVER_EVENT:
          this.coverCode = BytesUtil.bytes2HexString(unit.buff);
          break;
        case Lock3Bean.SA_COVER_SERIAL:
          this.coverSerial = BytesUtil.bytes2HexString(unit.buff);
          break;
        default:
      }
    }
  }

  public List<Lock3InfoUnit> getWillDoList() {
    return willReadList;
  }

  public boolean dataEquals(Lock3Bean lock3Bean) {
    if (lock3Bean == null) {
      return false;
    }

    List<Lock3InfoUnit> willDoList = getWillDoList();
    if (lock3Bean.getWillDoList().size() != willDoList.size()) {
      return false;
    }

    for (Lock3InfoUnit infoUnit : willDoList) {
      Lock3InfoUnit infoUnit2 = lock3Bean.getInfoUnit(infoUnit.sa);
      if (infoUnit2 == null || Arrays.equals(infoUnit.buff, infoUnit2.buff)) {
        return false;
      }
    }
    return true;
  }
}
