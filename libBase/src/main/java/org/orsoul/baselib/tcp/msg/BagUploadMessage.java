package org.orsoul.baselib.tcp.msg;

import com.fanfull.libjava.util.DateFormatUtil;

/** 封袋、出入库等业务 提交袋id. */
public class BagUploadMessage extends BaseSocketMessage4qz {

  String userId;
  int uploadType;
  String bagId;
  String tid;
  String eventCode;
  String moneyType;
  String checkerId;
  String batchId;
  String macAddress;

  String orgId;
  /** 0：手持登记，1:曾经登记过. */
  int bagType = 1;

  //BusinessTaskBean.BusiInfoBean busiInfoBean;

  public BagUploadMessage(int type, String userId, String checkerId,
      String batchId, String macAddress) {
    this.func = BaseSocketMessage4qz.FUNC_UPLOAD_BAG;
    this.userId = userId;
    this.uploadType = type;
    this.checkerId = checkerId;
    this.batchId = batchId;
    this.macAddress = macAddress;
  }

  /**
   * 空袋登记：$21 userId bagType bagId orgId yyyyMMddHHmmss 004#.
   * 封袋：$22 userID 21 bagid tid moneyType checkID pinumber taskcount#
   * 入库：$22 userID 22 bagid tid eventCode checkID pinumber taskcount#
   */
  @Override public String getMessage() {
    if (func == BaseSocketMessage4qz.FUNC_UPLOAD_BAG_REG) {
      message = genProtocol(func, userId, bagType, bagId, orgId,
          DateFormatUtil.getStringTime("yyyyMMddHHmmss"), msgNum);
    } else {
      message = genProtocol(func, userId, uploadType, bagId,
          tid, eventCode, moneyType, checkerId, batchId, macAddress, msgNum);
    }
    return message;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public int getUploadType() {
    return uploadType;
  }

  public void setUploadType(int uploadType) {
    this.uploadType = uploadType;
  }

  public String getBagId() {
    return bagId;
  }

  public void setBagId(String bagId) {
    this.bagId = bagId;
  }

  public String getTid() {
    return tid;
  }

  public void setTid(String tid) {
    this.tid = tid;
  }

  public String getEventCode() {
    return eventCode;
  }

  public void setCoverCode(String eventCode) {
    this.eventCode = eventCode;
  }

  public String getCheckerId() {
    return checkerId;
  }

  public void setCheckerId(String checkerId) {
    this.checkerId = checkerId;
  }

  public String getBatchId() {
    return batchId;
  }

  public void setBatchId(String batchId) {
    this.batchId = batchId;
  }

  public String getMacAddress() {
    return macAddress;
  }

  public void setMacAddress(String macAddress) {
    this.macAddress = macAddress;
  }

  //public BusinessTaskBean.BusiInfoBean getBusiInfoBean() {
  //  return busiInfoBean;
  //}

  //public void setBusiInfoBean(BusinessTaskBean.BusiInfoBean busiInfoBean) {
  //  if (busiInfoBean == null) {
  //    return;
  //  }
  //  this.busiInfoBean = busiInfoBean;
  //  String paperTypeID = busiInfoBean.getPaperTypeID();
  //  if (paperTypeID == null) {
  //    // do nothing
  //  } else if (paperTypeID.length() == 1) {
  //    moneyType = String.format("0%s%s",
  //        busiInfoBean.getPaperTypeID(), busiInfoBean.getVoucherTypeID());
  //  } else {
  //    moneyType = String.format("%s%s",
  //        busiInfoBean.getPaperTypeID(), busiInfoBean.getVoucherTypeID());
  //  }
  //}

  public String getOrgId() {
    return orgId;
  }

  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  public int getBagType() {
    return bagType;
  }

  public void setBagType(int bagType) {
    this.bagType = bagType;
  }

  public String getMoneyType() {
    return moneyType;
  }

  public void setMoneyType(String moneyType) {
    this.moneyType = moneyType;
  }

  public static void main(String[] args) {
    String s1 = "4";
    String s2 = "123456";
    System.out.println(String.format("%s%s", s1, s2));
  }
}
