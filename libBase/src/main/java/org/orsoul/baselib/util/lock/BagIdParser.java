package org.orsoul.baselib.util.lock;

import com.apkfuns.logutils.LogUtils;
import org.orsoul.baselib.util.ArrayUtils;

public class BagIdParser {
  public static final int BAG_ID_LEN = 24;
  public static final String BAG_VERSION = "05";
  public static final String BAG_BUNDLES = "20";
  public static final String BAG_TIE = "0";

  private String bagId;

  private String version;
  private String cityCode;
  private String moneyType;
  private String bagType;

  private byte[] uid;

  public BagIdParser() {
    version = BAG_VERSION;
  }

  /** 05 027 1 03 0480613ED24B89 BE. */
  public BagIdParser(String bagId) {
    this.bagId = bagId;

    this.version = bagId.substring(0, 2);
    this.cityCode = bagId.substring(2, 5);
    this.moneyType = bagId.substring(5, 6);
    this.bagType = bagId.substring(6, 8);

    this.uid = ArrayUtils.hexString2Bytes(bagId.substring(8, 22));
  }

  public String getBagId() {
    return bagId;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getCityCode() {
    return cityCode;
  }

  public void setCityCode(String cityCode) {
    this.cityCode = cityCode;
  }

  public String getMoneyType() {
    return moneyType;
  }

  public void setMoneyType(String moneyType) {
    this.moneyType = moneyType;
  }

  public String getBagType() {
    return bagType;
  }

  public void setBagType(String bagType) {
    this.bagType = bagType;
  }

  public byte[] getUid() {
    return uid;
  }

  public void setUid(byte[] uid) {
    this.uid = uid;
  }

  public String genBagId() {
    return ArrayUtils.bytes2HexString(genBagIdBuff());
  }

  public byte[] genBagIdBuff() {
    if (cityCode == null || moneyType == null || bagType == null || uid == null
        || cityCode.length() != 3
        || moneyType.length() != 1
        || bagType.length() != 2
        || uid.length != 7
    ) {
      return null;
    }
    byte[] bagIdPre = ArrayUtils.hexString2Bytes(version + cityCode + moneyType + bagType);
    byte[] bagIdBuff = new byte[12];
    System.arraycopy(bagIdPre, 0, bagIdBuff, 0, bagIdPre.length);
    System.arraycopy(uid, 0, bagIdBuff, 4, uid.length);

    /* 设置 异或校验位 */
    int crc = 0;
    int checkIndex = bagIdBuff.length - 1;
    for (int i = 0; i < checkIndex; i++) {
      crc ^= bagIdBuff[i];
    }
    bagIdBuff[checkIndex] = (byte) crc;
    bagId = ArrayUtils.bytes2HexString(bagIdBuff);
    LogUtils.d("genBagIdBuff:%s", bagId);
    return bagIdBuff;
  }

  @Override public String toString() {
    return "BagIdParser{" +
        "bagId='" + bagId + '\'' +
        ", version='" + version + '\'' +
        ", cityCode='" + cityCode + '\'' +
        ", moneyType='" + moneyType + '\'' +
        ", bagType='" + bagType + '\'' +
        '}';
  }

  public static boolean isBagId(String bagId) {
    return (bagId != null) && (bagId.length() == BAG_ID_LEN);
  }

  public static String getBagType(String bagId) {
    if (!isBagId(bagId)) {
      return null;
    }
    return bagId.substring(6, 8);
  }

  public static String getCityCode(String bagId) {
    if (!isBagId(bagId)) {
      return null;
    }
    return bagId.substring(2, 5);
  }
}
