package org.orsoul.baselib.tcp.msg;

/**
 * 修改密码。
 * 手持发：`$04 userID oldPwd newPwd 001#`
 * 手持收：`*04 res 004#`
 * 0：修改成功
 * 1：操作员未注册
 * 2：原密码错误
 * 3：修改密码失败
 * 4：新密码格式不符合要求
 */
public class ChangePasswordMessage extends BaseSocketMessage4qz {

  String userId;
  String pwd;
  String pwdNew;

  public ChangePasswordMessage(String userId, String pwd, String pwdNew) {
    this.func = FUNC_CHANGE_PWD;
    this.userId = userId;
    this.pwd = pwd;
    this.pwdNew = pwdNew;
  }

  public ChangePasswordMessage(BaseSocketMessage4qz msg) {
    super(msg.getFunc(), msg.getSplit(), msg.getMsgNum());
  }

  @Override public String getMessage() {
    message = genProtocol(func, userId, pwd, pwdNew, msgNum);
    return message;
  }

  /** 更改成功时，值为 null */
  public String failedInfo;

  /**
   * *04 res 004#
   * 0：修改成功
   * 1：操作员未注册
   * 2：原密码错误
   * 3：修改密码失败
   * 4：新密码格式不符合要求
   */
  public static ChangePasswordMessage parse(BaseSocketMessage4qz baseMsg) {
    if (baseMsg.getFunc() != FUNC_CHANGE_PWD) {
      return null;
    }
    ChangePasswordMessage reVal = new ChangePasswordMessage(baseMsg);
    reVal.resCode = Integer.parseInt(baseMsg.getSplit()[1]);
    switch (reVal.resCode) {
      case 0:
        break;
      case 1:
        reVal.failedInfo = "操作员未注册";
        break;
      case 2:
        reVal.failedInfo = "原密码错误";
        break;
      case 3:
        reVal.failedInfo = "修改密码失败";
        break;
      case 4:
        reVal.failedInfo = "新密码格式不符合要求";
        break;
      default:
        reVal.failedInfo = "修改密码失败，未知原因";
        break;
    }
    return reVal;
  }
}
