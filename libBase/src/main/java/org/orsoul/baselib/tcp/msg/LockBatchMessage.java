package org.orsoul.baselib.tcp.msg;

/**
 * 锁定批。$27 5ADD00E4 00 0 0000000000000000000000 005#
 */
public class LockBatchMessage extends BaseSocketMessage4qz<LockBatchMessage> {

  String userId;

  public LockBatchMessage(String userId) {
    this.func = FUNC_LOCK_BATCH;
    this.userId = userId;
  }

  @Override public String getMessage() {
    message = genProtocol(func, userId, "00 0 0000000000000000000000", deviceMac, msgNum);
    return message;
  }

  public LockBatchMessage(BaseSocketMessage4qz msg) {
    super(msg.getFunc(), msg.getSplit(), msg.getMsgNum());
  }

  public static LockBatchMessage parse(BaseSocketMessage4qz baseMsg) {
    if (baseMsg.getFunc() != FUNC_LOCK_BATCH) {
      return null;
    }

    //*27 00 005#
    //00 当前批不存在
    //01 锁定批成功
    //02 当前批已锁定

    LockBatchMessage reVal = new LockBatchMessage(baseMsg);
    reVal.resCode = Integer.parseInt(reVal.split[1]);
    if ("01".equals(reVal.split[1])) {
      reVal.success = true;
    }
    return reVal;
  }
}
