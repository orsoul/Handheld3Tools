package org.orsoul.baselib.tcp.msg;

import com.fanfull.libjava.data.BaseJsonBean;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.orsoul.baselib.lock3.bean.StoreIdBean;

import java.util.Arrays;
import java.util.List;

/**
 * 出入库授权 - 门控。
 */
public class PersonInOutMessage
    extends BaseSocketMessage4qz<PersonInOutMessage.PersonInOutRecBean> {
  public static final int FUNC2_CHECK_CARD = 1;
  public static final int FUNC2_AUTH_SUCCESS = 2;
  public static final int FUNC2_OPEN_DOOR = 3;

  int func2;

  public PersonInOutMessage(int func2, PersonInOutRecBean jsonBean) {
    this.func = FUNC_PERSON_IN_OUT;
    this.func2 = func2;
    this.jsonBean = jsonBean;
  }

  public PersonInOutMessage(BaseSocketMessage4qz msg) {
    super(msg.getFunc(), msg.getSplit(), msg.getMsgNum());
    try {
      this.func2 = Integer.parseInt(split[1]);
    } catch (Exception e) {
    }
  }

  @Override public String getMessage() {
    message = genProtocol(func, func2, jsonBean, msgNum);
    return message;
  }

  @Override public String toString() {
    return "PersonInOutMessage{" +
        "func=" + func +
        ", msgNum=" + msgNum +
        ", jsonBean=" + jsonBean +
        ", split=" + Arrays.toString(split) +
        '}';
  }

  public int getFunc2() {
    return func2;
  }

  public static PersonInOutMessage parse(BaseSocketMessage4qz baseMsg) {
    PersonInOutMessage reVal = new PersonInOutMessage(baseMsg);
    reVal.jsonBean = new Gson().fromJson(baseMsg.getSplit()[2],
        PersonInOutRecBean.class);
    return reVal;
  }

  public static class PersonInOutRecBean extends BaseJsonBean {

    // ==== 以下 发送字段 ====
    String[] cardIds;
    String batchId;
    String storeId;
    String cardId;
    Boolean isOpenDoor;
    Boolean isCheckTask;
    Boolean isOutDoor;

    public void setCardIds(String[] cardIds) {
      this.cardIds = cardIds;
    }

    public void setBatchId(String batchId) {
      this.batchId = batchId;
    }

    public void setStoreId(String storeId) {
      this.storeId = storeId;
    }

    public void setCardId(String cardId) {
      this.cardId = cardId;
    }

    public void setOpenDoor(Boolean openDoor) {
      isOpenDoor = openDoor;
    }

    public void setCheckTask(Boolean checkTask) {
      this.isCheckTask = checkTask;
    }

    public void setOutDoor(Boolean outDoor) {
      isOutDoor = outDoor;
    }

    public PersonInOutRecBean() {
    }

    // ==== 以下 接收字段 ====
    /** 0=身份合法，1=查无此人，2=权限不够 */
    Integer identResult;
    @SerializedName(value = "cids", alternate = {"ids"})
    List<String> cids;
    //List<String> ids;
    Boolean isAuth;
    List<StoreIdBean> identList;
    /** 二代任务号 */
    Integer mimisId;
    Integer errorCode;

    public Integer getIdentResult() {
      return identResult;
    }

    public List<String> getCids() {
      return cids;
    }

    public Boolean isAuth() {
      return isAuth;
    }

    public List<StoreIdBean> getIdentList() {
      return identList;
    }

    public Integer getMimisId() {
      return mimisId;
    }

    public Integer getErrorCode() {
      return errorCode;
    }
  }

  public static void main(String[] args) {
  }
}
