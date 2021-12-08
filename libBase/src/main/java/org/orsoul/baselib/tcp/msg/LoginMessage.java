package org.orsoul.baselib.tcp.msg;

/**
 * 登录、复核登录
 */
public class LoginMessage extends BaseSocketMessage4qz {

  String userId;
  String pwd;

  int res;

  public LoginMessage(String userId, String pwd) {
    this.func = FUNC_LOGIN;
    this.userId = userId;
    this.pwd = pwd;
  }

  public LoginMessage(BaseSocketMessage4qz msg) {
    super(msg.getFunc(), msg.getSplit(), msg.getMsgNum());
  }

  @Override public String getMessage() {
    message = genProtocol(func, userId, pwd, "666666", "00000000000000000",
        macAddress, msgNum);
    return message;
  }

  public static LoginMessage parse(BaseSocketMessage4qz baseMsg) {
    if (baseMsg.getFunc() != FUNC_LOGIN) {
      return null;
    }

    //LoginInfo.parseLoginInfo()

    LoginMessage reVal = new LoginMessage(baseMsg);
    reVal.res = Integer.parseInt(baseMsg.getSplit()[1]);
    if (reVal.res == 1) {
      //reVal.isSuccess = true;
      //reVal.totalNum = Integer.parseInt(baseMsg.getSplit()[2]);
      //reVal.scannedNum = Integer.parseInt(baseMsg.getSplit()[3]);
    }
    return reVal;
  }

  boolean needUpdatePwd;
}
