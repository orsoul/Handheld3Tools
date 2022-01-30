package org.orsoul.baselib.tcp.msg;

import com.google.gson.Gson;

import java.util.List;

/**
 * 纯物流 任务授权, 25 5
 */
public class AuthTaskMessage extends BaseSocketMessage4qz<AuthTaskMessage> {
  private String batchId;
  private List<String> cardIds;

  /* 回复字段 */
  private Integer rst;

  int func2;

  public AuthTaskMessage(String batchId, List<String> cardIds) {
    this.func = BaseSocketMessage4qz.FUNC_PERSON_IN_OUT;
    this.func2 = 5;
    this.batchId = batchId;
    this.cardIds = cardIds;
  }

  public AuthTaskMessage(BaseSocketMessage4qz msg) {
    super(msg.getFunc(), msg.getSplit(), msg.getMsgNum());
  }

  @Override public String getMessage() {
    String json = new Gson().toJson(this);
    message = genProtocol(func, func2, json, deviceMac, msgNum);
    return message;
  }

  public static AuthTaskMessage parse(BaseSocketMessage4qz baseMsg) {
    if (baseMsg.getFunc() != FUNC_PERSON_IN_OUT) {
      return null;
    }

    // *25 5 {"rst":"1,"batchId":"220121027010010002","cardIds":["B6C53D5C","1EEA0E444"]} 4#
    AuthTaskMessage reVal = new AuthTaskMessage(baseMsg);
    if (3 < baseMsg.getSplit().length) {
      AuthTaskMessage fromJson =
          new Gson().fromJson(baseMsg.getSplit()[2], AuthTaskMessage.class);

      reVal.func2 = Integer.parseInt(reVal.split[1]);

      reVal.jsonBean = fromJson;
      reVal.rst = fromJson.rst;
      reVal.batchId = fromJson.batchId;
      reVal.cardIds = fromJson.cardIds;

      reVal.success = reVal.rst != null && reVal.rst == 0;
    }
    return reVal;
  }
}
