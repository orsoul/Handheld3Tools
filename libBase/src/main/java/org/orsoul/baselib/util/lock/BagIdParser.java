package org.orsoul.baselib.util.lock;

import androidx.annotation.Nullable;
import com.apkfuns.logutils.LogUtils;
import java.util.Objects;
import org.orsoul.baselib.util.ArrayUtils;

/** 锁3袋id解析工具. */
public class BagIdParser {
  public static final int BAG_ID_LEN = 24;
  public static final int BAG_ID_BYTE_LEN = 12;
  public static final String BAG_VERSION_5 = "05";

  public static final String BAG_BUNDLES = "20";
  public static final String BAG_TIE = "0";

  private String bagId;

  private String version = BAG_VERSION_5;
  private String cityCode;
  private String moneyType;
  private String bagType;
  private String uid;
  private String checkByte;

  private byte[] uidBuff;

  /** 05 027 1 03 0480613ED24B89 BE. */
  public static BagIdParser parseBagId(String bagId) {
    if (!isBagId(bagId)) {
      return null;
    }
    BagIdParser bagIdParser = new BagIdParser();
    bagIdParser.bagId = bagId;
    bagIdParser.version = bagId.substring(0, 2);
    bagIdParser.cityCode = bagId.substring(2, 5);
    bagIdParser.moneyType = bagId.substring(5, 6);
    bagIdParser.bagType = bagId.substring(6, 8);
    bagIdParser.uid = bagId.substring(8, 22);
    bagIdParser.checkByte = bagId.substring(22);
    //bagIdParser.uidBuff = ArrayUtils.hexString2Bytes(bagIdParser.uid);
    return bagIdParser;
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

  public String getUid() {
    if (uid == null) {
      uid = ArrayUtils.bytes2HexString(uidBuff);
    }
    return uid;
  }

  public byte[] getUidBuff() {
    if (uidBuff == null) {
      uidBuff = ArrayUtils.hexString2Bytes(uid);
    }
    return uidBuff;
  }

  public void setUidBuff(byte[] uidBuff) {
    this.uidBuff = uidBuff;
    this.uid = null;
  }

  public String genBagId() {
    return ArrayUtils.bytes2HexString(genBagIdBuff());
  }

  public byte[] genBagIdBuff() {
    if (cityCode == null || moneyType == null || bagType == null || uidBuff == null
        || cityCode.length() != 3
        || moneyType.length() != 1
        || bagType.length() != 2
        || uidBuff.length != 7
    ) {
      LogUtils.i("genBagIdBuff failed:%s", this);
      return null;
    }
    byte[] bagIdPre = ArrayUtils.hexString2Bytes(version + cityCode + moneyType + bagType);
    byte[] bagIdBuff = new byte[BAG_ID_BYTE_LEN];
    System.arraycopy(bagIdPre, 0, bagIdBuff, 0, bagIdPre.length);
    System.arraycopy(uidBuff, 0, bagIdBuff, 4, uidBuff.length);

    /* 设置 异或校验位 */
    int crc = 0;
    int checkIndex = bagIdBuff.length - 1;
    for (int i = 0; i < checkIndex; i++) {
      crc ^= bagIdBuff[i];
    }
    bagIdBuff[checkIndex] = (byte) crc;
    bagId = ArrayUtils.bytes2HexString(bagIdBuff);
    checkByte = bagId.substring(22);
    LogUtils.v("genBagIdBuff:%s", bagId);
    return bagIdBuff;
  }

  public boolean typeEquals(@Nullable BagIdParser parser) {
    if (parser == null) {
      return false;
    }

    return Objects.equals(this.version, parser.version)
        && Objects.equals(this.cityCode, parser.cityCode)
        && Objects.equals(this.moneyType, parser.getMoneyType())
        && Objects.equals(this.bagType, parser.bagType);
  }

  public boolean typeEquals(@Nullable String bagId) {
    return typeEquals(parseBagId(bagId));
  }

  public static boolean typeEquals(@Nullable String bagId, @Nullable String bagId2) {
    if (!isBagId(bagId) || !isBagId(bagId2)) {
      return false;
    }
    return bagId.substring(0, 8).equals(bagId.substring(0, 8));
  }

  @Override public String toString() {
    return "BagIdParser{" +
        "bagId='" + bagId + '\'' +
        ", version='" + version + '\'' +
        ", cityCode='" + cityCode + '\'' +
        ", moneyType='" + moneyType + '\'' +
        ", bagType='" + bagType + '\'' +
        ", uid='" + uid + '\'' +
        ", checkByte='" + checkByte + '\'' +
        ", uidBuff=" + ArrayUtils.bytes2HexString(uidBuff) +
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

  /** 格式化袋Id：05_027_1_03_0480613ED24B89_BE. */
  public String getFormatBagId() {
    return String.format("%s_%s_%s_%s_%s_%s",
        this.version,
        this.cityCode,
        this.moneyType,
        this.bagType,
        this.getUid(),
        this.checkByte
    );
  }

  /** 格式化袋Id：05_027_1_03_0480613ED24B89_BE. */
  public static String format(String bagId) {
    BagIdParser bagIdParser = parseBagId(bagId);
    if (bagIdParser == null) {
      return null;
    }
    return bagIdParser.getFormatBagId();
  }
}
