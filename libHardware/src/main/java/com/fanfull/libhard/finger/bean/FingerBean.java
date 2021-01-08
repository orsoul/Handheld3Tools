package com.fanfull.libhard.finger.bean;

import com.fanfull.libhard.finger.db.FingerPrintSQLiteHelper;
import com.fanfull.libhard.finger.impl.FingerprintController;

import org.orsoul.baselib.util.BytesUtil;

import java.util.Arrays;

/**
 * 指纹实体类.
 */
public class FingerBean {

  /** 指纹特征码 在指纹库中的位置，范围0~127. */
  private int fingerIndex = -1;
  /** 指纹特征码 512字节. */
  private byte[] fingerFeature;
  private String feature;

  /** 指纹id，业务暂未需要. */
  private String fingerId;
  /** 指纹对应的用户id . */
  private String userId;
  /** 指纹版本，用于比较本地指纹与服务器的是否一致. */
  private int fingerVersion;
  /** 指纹编号，用于区分同一用户的不同指纹. */
  private int fingerNum;
  /** 指纹状态，0表示未上传到服务器，1表示已上传到服务器，2表示与服务器指纹不一致. */
  private int fingerStatus;
  /** 指纹名. */
  private String fingerName;
  /** 用户名. */
  private String userName;

  public FingerBean(int fingerIndex, byte[] fingerFeature, String fingerId, int fingerVersion) {
    this.fingerIndex = fingerIndex;
    this.fingerFeature = fingerFeature;
    this.fingerVersion = fingerVersion;
    this.fingerId = fingerId;
  }

  public FingerBean(int fingerIndex, String feature, String fingerId,
      int fingerVersion) {
    this.fingerIndex = fingerIndex;
    this.fingerVersion = fingerVersion;
    this.fingerId = fingerId;
    setFeature(feature);
  }

  public FingerBean(int fingerIndex, byte[] fingerFeature, String fingerId) {
    this(fingerIndex, fingerFeature, fingerId, 0);
  }

  public FingerBean(int fingerIndex, byte[] fingerFeature) {
    this(fingerIndex, fingerFeature, null);
  }

  public FingerBean(String userId, int fingerVersion) {
    this.userId = userId;
    this.fingerVersion = fingerVersion;
  }

  public FingerBean() {
  }

  public String getFeature() {
    if (feature == null && fingerFeature != null) {
      feature = BytesUtil.bytes2HexString(fingerFeature);
    }
    //return Base64.encodeToString(fingerFeature, Base64.NO_WRAP);
    return feature;
  }

  public byte[] setFeature(String featureString) {
    if (featureString != null) {
      //fingerFeature = Base64.decode(featureString, Base64.NO_WRAP);
      feature = featureString;
      fingerFeature = BytesUtil.hexString2Bytes(featureString);
    }
    return fingerFeature;
  }

  @Override public String toString() {
    return "FingerBean{" +
        "fingerIndex=" + fingerIndex +
        ", fingerId='" + fingerId + '\'' +
        ", userId='" + userId + '\'' +
        ", fingerVersion=" + fingerVersion +
        ", fingerNum='" + fingerNum + '\'' +
        ", fingerStatus='" + fingerStatus + '\'' +
        ", fingerName='" + fingerName + '\'' +
        ", userName='" + userName + '\'' +
        ", feature='" + getFeature() + '\'' +
        '}';
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FingerBean that = (FingerBean) o;

    if (fingerIndex != that.fingerIndex) return false;
    return Arrays.equals(fingerFeature, that.fingerFeature);
  }

  @Override public int hashCode() {
    int result = fingerIndex;
    result = 31 * result + Arrays.hashCode(fingerFeature);
    return result;
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

  public int getFingerStatus() {
    return fingerStatus;
  }

  public void setFingerStatus(int fingerStatus) {
    this.fingerStatus = fingerStatus;
  }

  public byte[] getFingerFeature() {
    return fingerFeature;
  }

  public void setFingerFeature(byte[] fingerFeature) {
    this.fingerFeature = fingerFeature;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public int getFingerNum() {
    return fingerNum;
  }

  public void setFingerNum(int fingerNum) {
    this.fingerNum = fingerNum;
  }

  public String getFingerName() {
    return fingerName;
  }

  public void setFingerName(String fingerName) {
    this.fingerName = fingerName;
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

  public long insertOrUpdate() {
    FingerPrintSQLiteHelper instance = FingerPrintSQLiteHelper.getInstance();
    if (instance == null) {
      return -1;
    }
    return instance.saveOrUpdate(this);
  }

  public int deleteDb() {
    FingerPrintSQLiteHelper instance = FingerPrintSQLiteHelper.getInstance();
    if (instance == null) {
      return -1;
    }
    return instance.delete(this);
  }

  public boolean delete(boolean deleteDb) {
    boolean delete = FingerprintController.getInstance().delete(this, deleteDb);
    return delete;
  }
}
