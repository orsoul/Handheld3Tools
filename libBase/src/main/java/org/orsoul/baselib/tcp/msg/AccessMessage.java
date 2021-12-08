package org.orsoul.baselib.tcp.msg;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.orsoul.baselib.lock3.bean.StoreIdBean;

import java.util.List;

/**
 * 手持接入前置。$99 devType 00 macAddress versionName taskcount#
 */
public class AccessMessage extends BaseSocketMessage4qz<AccessMessage> {

  public static String currOrgName;

  String macAddress;
  String versionName;
  String devType = "02";

  public AccessMessage(String macAddress, String versionName) {
    this.func = FUNC_ACCESS;
    this.macAddress = macAddress;
    this.versionName = versionName;
  }

  @Override public String getMessage() {
    message = genProtocol(func, devType, "00", macAddress, versionName, msgNum);
    return message;
  }

  public AccessMessage(BaseSocketMessage4qz msg) {
    super(msg.getFunc(), msg.getSplit(), msg.getMsgNum());
  }

  /* 以下 接收字段 */
  private String orgName;
  @SerializedName(value = "storeLabelList", alternate = {"StoreLabelList"})
  private List<StoreIdBean> storeLabelList;

  public String getOrgName() {
    return orgName;
  }

  public List<StoreIdBean> getStoreLabelList() {
    return storeLabelList;
  }

  public static AccessMessage parse(BaseSocketMessage4qz baseMsg) {
    AccessMessage reVal = new AccessMessage(baseMsg);
    if ("00".equals(reVal.split[1])) {
      reVal.jsonBean = new Gson().fromJson(baseMsg.getSplit()[2],
          AccessMessage.class);
      if (reVal.jsonBean != null) {
        reVal.orgName = reVal.jsonBean.orgName;
        reVal.storeLabelList = reVal.jsonBean.storeLabelList;
        currOrgName = reVal.jsonBean.orgName;
      }
    }
    return reVal;
  }
}
