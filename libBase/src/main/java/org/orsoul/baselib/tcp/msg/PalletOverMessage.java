package org.orsoul.baselib.tcp.msg;

import com.fanfull.libjava.data.BaseJsonBean;

import java.util.Objects;

/**
 * 结束托盘、预设托盘袋数量.
 * $48 userId 02 json deviceMac count#
 * {"palletNum":40,"dutyId":"123456"}
 * palletNum=0，结束当前托盘
 * 02=入库, 03=出库
 */
public class PalletOverMessage extends BaseSocketMessage4qz1002<PalletOverMessage.PalletJsonBean> {

  String userId;
  String taskType;

  public PalletOverMessage(String userId, boolean isInStore, int palletNum, String dutyId) {
    this.func = BaseSocketMessage4qz.FUNC_PALLET_OVER;
    this.userId = userId;
    this.taskType = isInStore ? "02" : "03";

    jsonBean = new PalletJsonBean();
    jsonBean.dutyId = dutyId;
    jsonBean.palletNum = palletNum;
  }

  public PalletOverMessage(BaseSocketMessage4qz msg) {
    super(msg);
  }

  @Override public String getMessage() {
    //if (message == null) {
    message = genProtocol(FRAME, DEVICE_TYPE, IP, func, userId, taskType, jsonBean.toJsonString(),
        deviceMac, msgNum);
    //}
    return message;
  }

  /**
   * *48 xx count#
   * xx  00 表正常回复
   * 01 异常回复
   */
  public static PalletOverMessage parse(BaseSocketMessage4qz baseMsg) {
    if (baseMsg.getFunc() != FUNC_PALLET_OVER) {
      return null;
    }

    PalletOverMessage reVal = new PalletOverMessage(baseMsg);
    reVal.success = Objects.equals(baseMsg.getSplit()[1], "00");
    return reVal;
  }

  public boolean isSuccess() {
    return success;
  }

  public class PalletJsonBean extends BaseJsonBean {
    String dutyId;
    int palletNum;
  }
}
