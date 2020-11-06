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
  /** 锁内tid 0x0A~0x0C. */
  public static final int SA_LOCK_TID = 0x0A;
  public static final int SA_STATUS = 0x10;
  public static final int SA_WORK_MODE = 0x12;
  public static final int SA_KEY_NUM = 0x14;
  /** 电压. */
  public static final int SA_VOLTAGE = 0x17;

  public Lock3Bean(int sa, int... saArr) {
    if (saArr == null) {
      throw new RuntimeException("袋锁地址不能为null");
    }
    addSa(sa, saArr);
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

  public boolean addSa(int sa) {
    InfoUnit infoUnit = InfoUnit.newInstance(sa);
    if (willReadList.contains(infoUnit)) {
      return false;
    }
    return willReadList.add(infoUnit);
  }

  public void addSa(int sa, int... saArr) {
    addSa(sa);
    for (int i = 0; i < saArr.length; i++) {
      addSa(saArr[i]);
    }
  }

  public void addBaseSa() {
    addSa(SA_BAG_ID, SA_STATUS, SA_KEY_NUM, SA_VOLTAGE);
  }

  public void addInitBagSa() {
    addSa(SA_BAG_ID, SA_STATUS, SA_KEY_NUM);
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

    public static InfoUnit newInstance(int sa) {
      switch (sa) {
        case SA_KEY_NUM:
        case SA_VOLTAGE:
          return new InfoUnit(sa, 4);
        default:
        case SA_STATUS:
        case SA_BAG_ID:
        case SA_PIECE_TID:
        case SA_LOCK_TID:
          return new InfoUnit(sa, 12);
      }
    }
  }
}
