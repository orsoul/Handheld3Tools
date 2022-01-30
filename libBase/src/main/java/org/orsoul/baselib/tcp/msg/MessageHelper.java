package org.orsoul.baselib.tcp.msg;

import com.apkfuns.logutils.LogUtils;

/**
 * 前置消息 解析成 bean.
 */
public final class MessageHelper {

  /**
   * 根据接收到消息new一个基本消息对象.
   */
  public static BaseSocketMessage4qz newInstance(String recString) {
    String[] split = MessageParser4qz.splitRecInfo(recString);
    if (!MessageParser4qz.checkSplit(split)) {
      LogUtils.wtf("parse failed:%s", recString);
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

    BaseSocketMessage4qz reVal;
    if (func == BaseSocketMessage4qz.FUNC_TC2) {
      func = Integer.parseInt(split[3]);
      reVal = new BaseSocketMessage4qz1002(split[1], func, split, num) {
        @Override public String getMessage() {
          return "";
        }
      };
    } else {
      reVal = new BaseSocketMessage4qz(func, split, num) {
        @Override public String getMessage() {
          return "";
        }
      };
    }
    reVal.recString = recString;
    return reVal;
  }

  /** 解析 字符串. */
  public static BaseSocketMessage4qz parse(
      BaseSocketMessage4qz baseMsg) {
    if (baseMsg == null) {
      return null;
    }
    BaseSocketMessage4qz reVal = baseMsg;
    try {

      switch (baseMsg.getFunc()) {
        case BaseSocketMessage4qz.FUNC_SELECT_TASK_REC:
          reVal = TaskSelectMessage.parse(baseMsg);
          break;
        case BaseSocketMessage4qz.FUNC_PERSON_IN_OUT:
          if (baseMsg.split[1].equals("5")) {
            reVal = AuthTaskMessage.parse(baseMsg);
          } else {
            reVal = PersonInOutMessage.parse(baseMsg);
          }
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
        case BaseSocketMessage4qz.FUNC_UPLOAD_BAG:
        case BaseSocketMessage4qz.FUNC_UPLOAD_BAG_REG:
          reVal = BagUploadMessage.parse(baseMsg);
          break;
        case BaseSocketMessage4qz.FUNC_PALLET_OVER:
          reVal = PalletOverMessage.parse(baseMsg);
          break;
      }
    } catch (Exception e) {
      LogUtils.wtf("parse Exception:%s", e.getMessage());
    }
    reVal.recString = baseMsg.recString;
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
  }

  //public static BaseSocketMessage4qz parse(String[] recString) {
  //  BaseSocketMessage4qz baseMsg = null;
  //  try {
  //    baseMsg = newInstance(recString);
  //    return parse(baseMsg);
  //  } catch (Exception e) {
  //    return baseMsg;
  //  }
  //}
}
