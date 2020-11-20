package org.orsoul.baselib.lock3.bean;

import org.orsoul.baselib.util.BytesUtil;

public class Lock3InfoUnit {
  public int sa;
  public int len;
  public byte[] buff;
  private boolean doSuccess;

  private Lock3InfoUnit(int sa, int len) {
    this.sa = sa;
    this.len = len;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Lock3InfoUnit infoUnit = (Lock3InfoUnit) o;

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
        "sa=0x" + Integer.toHexString(sa).toUpperCase() +
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

  public static Lock3InfoUnit newInstance(int sa, int len) {
    return new Lock3InfoUnit(sa, len);
  }

  public static Lock3InfoUnit newInstance(int sa) {
    switch (sa) {
      case Lock3Bean.SA_BAG_ID:
      case Lock3Bean.SA_PIECE_TID:
      case Lock3Bean.SA_LOCK_TID:
        return new Lock3InfoUnit(sa, 12);
      case Lock3Bean.SA_COVER_EVENT:
        return new Lock3InfoUnit(sa, 30);
      case Lock3Bean.SA_COVER_SERIAL:
        return new Lock3InfoUnit(sa, 11);
      case Lock3Bean.SA_CIRCULATION:
        return new Lock3InfoUnit(sa, 0);
      case Lock3Bean.SA_STATUS:
      case Lock3Bean.SA_KEY_NUM:
      case Lock3Bean.SA_VOLTAGE:
      case Lock3Bean.SA_CIRCULATION_INDEX:
      default:
        return new Lock3InfoUnit(sa, 4);
    }
  }
}