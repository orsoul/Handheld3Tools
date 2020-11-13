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
  public static final int SA_HAND_OVER = 0x93;

  public Lock3Bean(int... saArr) {
    if (saArr == null) {
      throw new RuntimeException("袋锁地址不能为null");
    }
    addSa(saArr);
  }

  public Lock3Bean() {
  }

  List<InfoUnit> willReadList = new ArrayList<>();

  public byte[] uidBuff;

  String bagId;
  byte[] bagIdBuff;

  int status;
  byte[] statusBuff;

  float voltage;
  byte[] voltageBuff;

  public boolean addOneSa(int sa, int len) {
    InfoUnit infoUnit = InfoUnit.newInstance(sa, len);
    if (willReadList.contains(infoUnit)) {
      return false;
    }
    return willReadList.add(infoUnit);
  }

  public void addSa(int... saArr) {
    for (int i = 0; i < saArr.length; i++) {
      InfoUnit infoUnit = InfoUnit.newInstance(saArr[i]);
      if (willReadList.contains(infoUnit)) {
        willReadList.add(infoUnit);
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

  public InfoUnit getInfoUnit(int sa) {
    for (InfoUnit infoUnit : willReadList) {
      if (infoUnit.sa == sa) {
        return infoUnit;
      }
    }
    return null;
  }

  public void parseInfo() {
    for (InfoUnit infoUnit : willReadList) {
      if (!infoUnit.haveData()) {
        return;
      }
    }
  }

  public List<InfoUnit> getWillDoList() {
    return willReadList;
  }

  public boolean dataEquals(Lock3Bean lock3Bean) {
    if (lock3Bean == null) {
      return false;
    }

    List<InfoUnit> willDoList = getWillDoList();
    if (lock3Bean.getWillDoList().size() != willDoList.size()) {
      return false;
    }

    for (InfoUnit infoUnit : willDoList) {
      InfoUnit infoUnit2 = lock3Bean.getInfoUnit(infoUnit.sa);
      if (infoUnit2 == null || Arrays.equals(infoUnit.buff, infoUnit2.buff)) {
        return false;
      }
    }
    return true;
  }

  public static class InfoUnit {
    public int sa;
    public int len;
    public byte[] buff;
    private boolean doSuccess;

    private InfoUnit(int sa, int len) {
      this.sa = sa;
      this.len = len;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      InfoUnit infoUnit = (InfoUnit) o;

      if (sa != infoUnit.sa) return false;
      return len == infoUnit.len;
    }

    @Override
    public int hashCode() {
      int result = sa;
      result = 31 * result + len;
      return result;
    }

    @Override
    public String toString() {
      return "InfoUnit{" +
          "sa=" + sa +
          ", len=" + len +
          ", buff=" + BytesUtil.bytes2HexString(buff) +
          '}';
    }

    public boolean isDoSuccess() {
      return doSuccess;
    }

    public void setDoSuccess(boolean doSuccess) {
      this.doSuccess = doSuccess;
    }

    public boolean haveData() {
      return buff != null;
    }

    public static InfoUnit newInstance(int sa, int len) {
      return new InfoUnit(sa, len);
    }

    public static InfoUnit newInstance(int sa) {
      switch (sa) {
        case SA_BAG_ID:
        case SA_PIECE_TID:
        case SA_LOCK_TID:
          return new InfoUnit(sa, 12);
        case SA_COVER_EVENT:
          return new InfoUnit(sa, 30);
        case SA_COVER_SERIAL:
          return new InfoUnit(sa, 11);
        case SA_STATUS:
        case SA_KEY_NUM:
        case SA_VOLTAGE:
        default:
          return new InfoUnit(sa, 4);
      }
    }
  }
}
