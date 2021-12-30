package org.orsoul.baselib.tcp.msg;

import java.util.List;

/** 57 查询 库存列表、任务列表. */
public class QueryStockBagMessage extends BaseSocketMessage4qz<List<String>> {

  String userId;

  public QueryStockBagMessage(String userId) {
    this.func = FUNC_QUERY_STOCK;
    this.userId = userId;
  }

  public QueryStockBagMessage(BaseSocketMessage4qz msg) {
    super(msg.getFunc(), msg.getSplit(), msg.getMsgNum());
  }

  @Override public String getMessage() {
    message = genProtocol(func, userId, msgNum);
    return message;
  }

  public static QueryStockBagMessage parse(BaseSocketMessage4qz baseMsg) {
    if (baseMsg.getFunc() != FUNC_QUERY_STOCK) {
      return null;
    }
    QueryStockBagMessage reVal = new QueryStockBagMessage(baseMsg);
    reVal.jsonBean = null;
    if (reVal.jsonBean != null) {
      reVal.success = true;
    }
    return reVal;
  }
}
