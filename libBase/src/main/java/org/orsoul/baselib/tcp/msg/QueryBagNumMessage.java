package org.orsoul.baselib.tcp.msg;

/**
 * 37 查询任务概览
 */
public class QueryBagNumMessage extends BaseSocketMessage4qz {

  String userId;

  int res;
  int totalNum;
  int scannedNum;

  boolean isSuccess;

  public QueryBagNumMessage(String userId) {
    this.func = FUNC_QUERY_PLAIN_NUM;
    this.userId = userId;
  }

  public QueryBagNumMessage(BaseSocketMessage4qz msg) {
    super(msg.getFunc(), msg.getSplit(), msg.getMsgNum());
  }

  @Override public String getMessage() {
    message = genProtocol(func, userId, msgNum);
    return message;
  }

  public static QueryBagNumMessage parse(BaseSocketMessage4qz baseMsg) {
    if (baseMsg.getFunc() != FUNC_QUERY_PLAIN_NUM) {
      return null;
    }
    QueryBagNumMessage reVal = new QueryBagNumMessage(baseMsg);
    reVal.res = Integer.parseInt(baseMsg.getSplit()[1]);
    if (reVal.res == 1) {
      reVal.isSuccess = true;
      reVal.totalNum = Integer.parseInt(baseMsg.getSplit()[2]);
      reVal.scannedNum = Integer.parseInt(baseMsg.getSplit()[3]);
    }
    return reVal;
  }

  public int getTotalNum() {
    return totalNum;
  }

  public int getScannedNum() {
    return scannedNum;
  }

  public boolean isSuccess() {
    return isSuccess;
  }
}
