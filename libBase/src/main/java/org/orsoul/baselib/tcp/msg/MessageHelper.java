package org.orsoul.baselib.tcp.msg;

import com.apkfuns.logutils.LogUtils;

public final class MessageHelper {

  /**
   * 根据接收到消息new一个基本消息对象.
   */
  public static BaseSocketMessage4qz newInstance(String recString) {
    String[] split = MessageParser4qz.splitRecInfo(recString);
    if (split == null) {
      LogUtils.w("parse failed:%s", recString);
      return null;
    }
    return newInstance(split);
  }

  public static BaseSocketMessage4qz newInstance(String[] split) {
    if (!MessageParser4qz.checkSplit(split)) {
      return null;
    }
    String headNum = split[0];
    if (headNum.charAt(0) == '*' || headNum.charAt(0) == '$') {
      headNum = headNum.substring(1);
    }

    String endNum = split[split.length - 1];
    if (endNum.endsWith("#")) {
      endNum = endNum.substring(0, endNum.length() - 1);
    }
    int func = Integer.parseInt(headNum);
    int num = Integer.parseInt(endNum);
    return new BaseSocketMessage4qz(func, split, num) {
      @Override public String getMessage() {
        return "";
      }
    };
  }

  /** 解析 字符串. */
  public static BaseSocketMessage4qz parse(
      BaseSocketMessage4qz baseMsg) {
    if (baseMsg == null) {
      return null;
    }
    BaseSocketMessage4qz reVal = baseMsg;
    switch (baseMsg.getFunc()) {
      case BaseSocketMessage4qz.FUNC_SELECT_TASK_REC:
        reVal = TaskSelectMessage.parse(baseMsg);
        break;
      case BaseSocketMessage4qz.FUNC_PERSON_IN_OUT:
        reVal = PersonInOutMessage.parse(baseMsg);
        break;
      case BaseSocketMessage4qz.FUNC_QUERY_PLAIN_NUM:
        reVal = QueryBagNumMessage.parse(baseMsg);
        break;
      case BaseSocketMessage4qz.FUNC_SUBMIT_OFFLINE_DATA:
        //reVal = SubmitOfflineMessage.parse(baseMsg);
        break;
      case BaseSocketMessage4qz.FUNC_ACCESS:
        reVal = AccessMessage.parse(baseMsg);
        break;
      case BaseSocketMessage4qz.FUNC_CHANGE_PWD:
        reVal = ChangePasswordMessage.parse(baseMsg);
        break;
    }
    //LogUtils.d("%s", reVal);
    return reVal;
  }

  /** 解析 字符串. */
  public static BaseSocketMessage4qz parse(String recString) {
    BaseSocketMessage4qz baseMsg = null;
    try {
      baseMsg = newInstance(recString);
      return parse(baseMsg);
    } catch (Exception e) {
      return baseMsg;
    }
    //BaseSocketMessage4qz baseMsg = newInstance(recString);
    //return parse(baseMsg);
  }

  public static BaseSocketMessage4qz parse(String[] recString) {
    BaseSocketMessage4qz baseMsg = null;
    try {
      baseMsg = newInstance(recString);
      return parse(baseMsg);
    } catch (Exception e) {
      return baseMsg;
    }
    //BaseSocketMessage4qz baseMsg = newInstance(recString);
    //return parse(baseMsg);
  }
}
