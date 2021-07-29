package org.orsoul.baselib.lock3.bean;

import com.google.gson.annotations.SerializedName;

/**
 * 库门标识卡、库房编号.
 */
public class StoreIdBean {
  String cardId;
  String storeId;
  String storeName;
  /** 是否为库外卡 */
  @SerializedName(value = "isOutDoor", alternate = {"outdoor"})
  boolean isOutDoor = true;
  /** 开库门 需要验证的人数 */
  int needNumber = 3;

  public StoreIdBean(String cardId, String storeId, String storeName) {
    this.cardId = cardId;
    this.storeId = storeId;
    this.storeName = storeName;
  }

  @Override public String toString() {
    return "StoreIdBean{" +
        "cardId='" + cardId + '\'' +
        ", storeId='" + storeId + '\'' +
        ", storeName='" + storeName + '\'' +
        ", isOutDoor=" + isOutDoor +
        ", needNumber=" + needNumber +
        '}';
  }

  public String getStoreId() {
    return storeId;
  }

  public void setStoreId(String storeId) {
    this.storeId = storeId;
  }

  public String getCardId() {
    return cardId;
  }

  public String getStoreName() {
    return storeName;
  }

  public boolean isOutDoor() {
    return isOutDoor;
  }

  public int getNeedNumber() {
    return needNumber;
  }

  public void setOutDoor(boolean outDoor) {
    this.isOutDoor = outDoor;
  }

  public void setNeedNumber(int needNumber) {
    this.needNumber = needNumber;
  }
}
