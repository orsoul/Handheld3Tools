package com.fanfull.libhard.finger.bean;

import org.orsoul.baselib.util.BytesUtil;

/**
 * 指纹.
 */
public class FingerBean {

  private int fingerIndex;
  private byte[] fingerFeature;

  private String fingerId;
  private int fingerVersion;
  private String cardId;
  private String userName;

  public FingerBean(int fingerIndex, byte[] fingerFeature, String fingerId, int fingerVersion) {
    this.fingerIndex = fingerIndex;
    this.fingerFeature = fingerFeature;
    this.fingerVersion = fingerVersion;
    this.fingerId = fingerId;
  }

  public FingerBean(int fingerIndex, String fingerFeatureBase64, String fingerId,
      int fingerVersion) {
    this.fingerIndex = fingerIndex;
    this.fingerVersion = fingerVersion;
    this.fingerId = fingerId;
    setFeatureString(fingerFeatureBase64);
  }

  public FingerBean(int fingerIndex, byte[] fingerFeature, String fingerId) {
    this(fingerIndex, fingerFeature, fingerId, 0);
  }

  public FingerBean(int fingerIndex, byte[] fingerFeature) {
    this(fingerIndex, fingerFeature, null);
  }

  public String getFeatureString() {
    if (fingerFeature == null) {
      return null;
    }
    //return Base64.encodeToString(fingerFeature, Base64.NO_WRAP);
    return BytesUtil.bytes2HexString(fingerFeature);
  }

  public byte[] setFeatureString(String featureString) {
    if (featureString != null) {
      //fingerFeature = Base64.decode(featureString, Base64.NO_WRAP);
      fingerFeature = BytesUtil.hexString2Bytes(featureString);
    }
    return fingerFeature;
  }

  @Override public String toString() {
    return "FingerBean{" +
        "fingerIndex=" + fingerIndex +
        ", fingerId='" + fingerId + '\'' +
        ", fingerVersion=" + fingerVersion +
        ", cardId='" + cardId + '\'' +
        ", userName='" + userName + '\'' +
        '}';
  }

  public int getFingerIndex() {
    return fingerIndex;
  }

  public void setFingerIndex(int fingerIndex) {
    this.fingerIndex = fingerIndex;
  }

  public String getFingerId() {
    return fingerId;
  }

  public void setFingerId(String fingerId) {
    this.fingerId = fingerId;
  }

  public byte[] getFingerFeature() {
    return fingerFeature;
  }

  public void setFingerFeature(byte[] fingerFeature) {
    this.fingerFeature = fingerFeature;
  }

  public String getCardId() {
    return cardId;
  }

  public void setCardId(String cardId) {
    this.cardId = cardId;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public int getFingerVersion() {
    return fingerVersion;
  }

  public void setFingerVersion(int fingerVersion) {
    this.fingerVersion = fingerVersion;
  }
}
