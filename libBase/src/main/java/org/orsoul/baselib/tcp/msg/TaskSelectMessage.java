package org.orsoul.baselib.tcp.msg;

import com.google.gson.Gson;

import org.orsoul.baselib.lock3.bean.StoreIdBean;

import java.util.List;

/** 选择任务消息、及回复解析. */
public class TaskSelectMessage extends BaseSocketMessage4qz {

  String userId;
  int taskType;

  String batchId;
  /* ============= 二代物流 接收json段 =============== */
  Integer identStatus;
  Integer mimisId;
  List<StoreIdBean> identList;

  /* ============= 纯物流 接收json段 =============== */
  Boolean needAuth;
  Integer needNum;

  public TaskSelectMessage(int taskType, String userId, String batchId) {
    this.func = BaseSocketMessage4qz.FUNC_SELECT_TASK;
    this.userId = userId;
    this.taskType = taskType;
    this.batchId = batchId;
  }

  public TaskSelectMessage(int taskType, String userId) {
    this(taskType, userId, "00000000000000000");
  }

  public TaskSelectMessage(BaseSocketMessage4qz msg) {
    super(msg.getFunc(), msg.getSplit(), msg.getMsgNum());
  }

  @Override public String getMessage() {
    //if (message == null) {
    message = genProtocol(func, userId, taskType, batchId, msgNum);
    //}
    return message;
  }

  public boolean isAuth() {
    return identStatus != null && identStatus == 1;
  }

  public static TaskSelectMessage parse(BaseSocketMessage4qz baseMsg) {
    if (baseMsg.getFunc() != FUNC_SELECT_TASK_REC) {
      return null;
    }

    TaskSelectMessage reVal = new TaskSelectMessage(baseMsg);
    if (4 < baseMsg.getSplit().length) {
      // 携带 json数据
      TaskSelectMessage fromJson =
          new Gson().fromJson(baseMsg.getSplit()[3], TaskSelectMessage.class);
      reVal.batchId = fromJson.batchId;
      reVal.identStatus = fromJson.identStatus;
      reVal.mimisId = fromJson.mimisId;
      reVal.identList = fromJson.identList;

      /* ============= 纯物流 接收json段 =============== */
      reVal.needAuth = fromJson.needAuth;
      reVal.needNum = fromJson.needNum;
    } else {
      reVal.batchId = baseMsg.getSplit()[1];
    }
    reVal.success = true;
    //StaticString.pinumber = baseMsg.split[1];
    return reVal;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public int getTaskType() {
    return taskType;
  }

  public void setTaskType(int taskType) {
    this.taskType = taskType;
  }

  public String getBatchId() {
    return batchId;
  }

  public void setBatchId(String batchId) {
    this.batchId = batchId;
  }

  public Integer getMimisId() {
    return mimisId;
  }

  public void setMimisId(Integer mimisId) {
    this.mimisId = mimisId;
  }

  public List<StoreIdBean> getIdentList() {
    return identList;
  }
}
