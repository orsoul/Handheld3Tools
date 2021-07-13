package org.orsoul.baselib.lock3.bean;

/**
 * 库门标识卡、库房编号.
 */
public class StoreIdBean {
  String cardId;
  String storeId;
  String storeName;

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
}
