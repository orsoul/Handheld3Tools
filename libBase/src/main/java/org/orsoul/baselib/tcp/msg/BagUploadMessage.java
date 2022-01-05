package org.orsoul.baselib.tcp.msg;

import com.fanfull.libjava.data.BaseJsonBean;

/** 封袋、出入库等业务 提交袋id. */
public class BagUploadMessage extends BaseSocketMessage4qz<BagUploadMessage.BagUploadBean> {

  String userId;
  int uploadType;
  String bagId;
  String tid;
  String eventCode;
  String moneyType;
  String checkerId;
  String batchId;

  String orgId;
  /** 1：登记-验袋，2:转移，3：减损 */
  int emptyType = 1;

  //BusinessTaskBean.BusiInfoBean busiInfoBean;
  public BagUploadMessage(BaseSocketMessage4qz msg) {
    super(msg.getFunc(), msg.getSplit(), msg.getMsgNum());
  }

  public BagUploadMessage(int type, String userId, String checkerId, String batchId) {
    this.func = BaseSocketMessage4qz.FUNC_UPLOAD_BAG;
    this.userId = userId;
    this.uploadType = type;
    this.checkerId = checkerId;
    this.batchId = batchId;
    //this.macAddress = macAddress;
  }

  /**
   * 空袋管理业务，提交袋信息指令。
   *
   * @param emptyType 1：验袋，2:转移，3：减损,4：登记
   */
  public BagUploadMessage(int emptyType, String userId, String batchId) {
    this.func = BaseSocketMessage4qz.FUNC_UPLOAD_BAG_REG;
    this.userId = userId;
    this.emptyType = emptyType;
    this.batchId = batchId;
    //this.macAddress = macAddress;
  }

  /**
   * 空袋登记：$21 userId bagType bagId orgId yyyyMMddHHmmss 004#.
   * 封袋：$22 userID 21 bagid tid moneyType checkID pinumber taskcount#
   * 入库：$22 userID 22 bagid tid eventCode checkID pinumber taskcount#
   */
  @Override public String getMessage() {
    if (func == BaseSocketMessage4qz.FUNC_UPLOAD_BAG_REG) {
      message = genProtocol(func, userId, emptyType, bagId, batchId, deviceMac, msgNum);
    } else {
      message = genProtocol(func, userId, uploadType, bagId,
          tid, eventCode, moneyType, checkerId, batchId, deviceMac, msgNum);
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

  public String getOrgId() {
    return orgId;
  }

  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  public int getEmptyType() {
    return emptyType;
  }

  public void setEmptyType(int emptyType) {
    this.emptyType = emptyType;
  }

  public String getMoneyType() {
    return moneyType;
  }

  public void setMoneyType(String moneyType) {
    this.moneyType = moneyType;
  }

  public int personNum;
  public int totalNum;
  public int planNum;

  /**
   * *22 2200010001999912 087101001005210401151128 05532103047A47D23E618040 009#
   * *21 00 009#
   */
  public static BagUploadMessage parse(BaseSocketMessage4qz baseMsg) {
    BagUploadMessage reVal = new BagUploadMessage(baseMsg);
    String[] split = reVal.split;
    if (baseMsg.getFunc() == FUNC_UPLOAD_BAG) {
      if (14 <= split[1].length()) {
        String strPersonNum = split[1].substring(2, 6);// 个人完成数量
        String strTotalNum = split[1].substring(6, 10);// 总完成数量
        String strPlanNum = split[1].substring(10, 14);// 计划数量
        reVal.personNum = Integer.parseInt(strPersonNum);
        reVal.totalNum = Integer.parseInt(strTotalNum);
        reVal.planNum = Integer.parseInt(strPlanNum);
        reVal.success = true;
      }
    } else if (baseMsg.getFunc() == FUNC_UPLOAD_BAG_REG) {
      reVal.resCode = Integer.parseInt(split[1]);
      reVal.success = reVal.resCode == 0;
    } else {
      return null;
    }

    return reVal;
  }

  public static void main(String[] args) {
    String s1 = "4";
    String s2 = "123456";
    System.out.println(String.format("%s%s", s1, s2));
  }

  public class BagUploadBean extends BaseJsonBean {

  }
}
