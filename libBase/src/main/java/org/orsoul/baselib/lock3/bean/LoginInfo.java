package org.orsoul.baselib.lock3.bean;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.libjava.util.DateFormatUtil;

import java.util.Calendar;
import java.util.List;

/**
 * 记录登录信息.
 */
public final class LoginInfo {
  /** 默认登录密码. */
  public static final String DEFAULT_LOGIN_PWD = "000000";

  /** 操作员登录id. */
  private static String userId;
  /** 复核员登录id. */
  private static String checkerId;
  /** 操作员登录编号. 机构Id + 用户编号？ 002701001 + 004 */
  private static String userNum;
  /** 服务端的 机构号-机构名称 列表版本号. */
  private static String orgListVersion;

  /** 权限. */
  private static String permission;
  /** 机构号. 002701001 */
  private static String orgId;
  ///** 银行列表版本号. */
  //private static int bankListVersion;

  /** 操作员姓名. */
  private static String userName;
  /** 复核员姓名. */
  private static String checkerName;
  /** 机构名. */
  private static String orgName;

  /** 登陆时 服务端时间 yyMMddHHmmss. */
  private static String loginTimeStr;
  /** 登陆时 服务端时间. */
  private static long loginTime;
  /** 本地时间 与 服务端时间 的时间差. 即：currentTimeMillis - loginTime */
  private static long loginTimeD;

  public static long currentTimeMillisFix() {
    return System.currentTimeMillis() - loginTimeD;
  }

  public static byte[] businessInfo() {

    return null;
  }

  /** 操作员 uid. */
  private static String userUid;
  /** 复核员 uid. */
  private static String checkerUid;
  /** 库间id. */
  private static String storeId;
  /** 库间名. */
  private static String storeName;

  /** 是否 需要再次登录，黑屏过后需要再次登录. */
  private static boolean needLogin;
  /** 是否 需要更新 银行. */
  private static boolean needUpdateOrgList = true;

  /** 认证状态. */
  private static int identStatus = -1;

  private static String fingerUserId;
  public static boolean isFingerLogin;

  public static String getFingerUserId() {
    return fingerUserId;
  }

  public static void setFingerUserId(String fingerUserId) {
    LoginInfo.fingerUserId = fingerUserId;
  }

  public static int getIdentStatus() {
    return identStatus;
  }

  public static void setIdentStatus(int identStatus) {
    LoginInfo.identStatus = identStatus;
  }

  /** 已登录 返回true. */
  public static boolean isLogin() {
    return userId != null;
  }

  /** 已复核登录 返回true. */
  public static boolean isCheckLogin() {
    return checkerId != null;
  }

  /** userId不等于checkerId 返回true. */
  public static boolean userIdEquals(String checkerId) {
    return userId == null ? false : userId.equals(checkerId);
  }

  public static void setUserId(String userId) {
    LoginInfo.userId = userId;
    //if (userId != null) {
    //  String id = SPUtils.getString(MyContexts.KEY_LAST_USER_ID);
    //  SPUtils.putString(MyContexts.KEY_LAST_USER_ID, userId);
    //  if (!userId.equals(id)) {
    //    SPUtils.putString(MyContexts.KEY_LAST_CHECKER_ID, null);
    //  }
    //}
  }

  public static void setCheckerId(String checkerId) {
    LoginInfo.checkerId = checkerId;
    //if (checkerId != null) {
    //SPUtils.putString(MyContexts.KEY_LAST_CHECKER_ID, checkerId);
    //}
  }

  public static void setCheckerUid(String checkerUid) {
    LoginInfo.checkerUid = checkerUid;
  }

  public static void setNeedLogin(boolean needLogin) {
    LoginInfo.needLogin = needLogin;
  }

  public static void setNeedUpdateOrgList(boolean needUpdateOrgList) {
    LoginInfo.needUpdateOrgList = needUpdateOrgList;
  }

  public static String getUserId() {
    return userId;
  }

  public static String getCheckerId() {
    return checkerId;
  }

  public static String getUserNum() {
    return userNum;
  }

  public static long getLoginTime() {
    return loginTime;
  }

  public static void setLoginTime(long loginTime) {
    LoginInfo.loginTimeD = System.currentTimeMillis() - loginTime;
    LoginInfo.loginTime = loginTime;
  }

  public static String getPermission() {
    return permission;
  }

  public static String getOrgId() {
    return orgId;
  }

  public static String getUserUid() {
    return userUid;
  }

  public static String getCheckerUid() {
    return checkerUid;
  }

  public static String getOrgListVersion() {
    return orgListVersion;
  }

  public static String getUserName() {
    return userName;
  }

  public static String getCheckerName() {
    return checkerName;
  }

  public static String getOrgName() {
    return orgName;
  }

  public static String getLoginTimeStr() {
    return loginTimeStr;
  }

  public static String getStoreId() {
    return storeId;
  }

  public static String getStoreName() {
    return storeName;
  }

  public static boolean isNeedLogin() {
    return needLogin;
  }

  public static boolean isNeedUpdateOrgList() {
    return needUpdateOrgList;
  }

  /**
   * 解析登录回复信息. 20200928例：<br/>
   *
   * ` 0  1      2           3          4      5   6        7             8         9     10
   * *01 01 1111111113 200928105902 071501001 000 16 74212001010101 中心支库-b库间 70080 登录员 001#
   *
   * @return 返回0：解析成功，1：参数错误，2：密码错误
   */
  public static int parseLoginInfo(String[] split, String cardId) {
    if (split == null || split.length < 4) {
      return 1;
    }

    if (("*01".equals(split[0]) || "00".equals(split[0])) && "01".equals(split[1])) {
      // 刷卡登录，do nothing
      isFingerLogin = false;
    } else if (("*14".equals(split[0]) || "14".equals(split[0])) &&
        "03".equals(split[1]) &&
        "00".equals(split[2])) {
      isFingerLogin = true;
      cardId = split[12];
      setFingerUserId(split[5]);
      // 指纹登录
      for (int i = 3; i < split.length; i++) {
        split[i - 1] = split[i];
      }
    } else {
      return 2;
    }

    permission = split[2];

    // 3-6
    if (6 <= split.length) {
      // 新手持添加 字段
      loginTimeStr = split[3];
      loginTime = DateFormatUtil.parseString2Long(loginTimeStr, "yyMMddHHmmss");
      setLoginTime(loginTime);
      orgId = split[4];
      userNum = split[5];
      orgListVersion = split[6];
      //String localVersion = SPUtils.getString(MyContexts.KEY_ORG_LIST_VERSION, "");
      //LogUtils.d("org local Version：%s，service：%s", localVersion, orgListVersion);
      //needUpdateOrgList = !localVersion.equals(orgListVersion);
    }

    // 7-9
    if (11 <= split.length) {
      // 对接中钞 版本添加字段
      storeId = split[7];
      storeName = split[8].replace("-b", " ");
      long uid = Long.parseLong(split[9]);
      userUid = String.format("%08X", uid).substring(0, 8);
    }

    // 10 登录员
    if (12 <= split.length) {
      userName = split[10];
    }
    LoginInfo.setUserId(cardId);
    return 0;
  }

  public static int parseLoginInfo(String[] split) {
    return parseLoginInfo(split, null);
  }

  /**
   * 解析复核登录回复信息. 20200928例：<br/>
   *
   * ` 0  1      2           3               4      5   6
   * *00 01 1111111114 74212001010101 中心支库-b库间 3 复核员 001#
   *
   * @return 返回0：解析成功，1：参数错误，2：密码错误
   */
  public static boolean parseCheckLogin(String checkerId, String info) {
    if (info == null) {
      return false;
    }

    String[] split = info.split(" ");
    if (split == null || split.length < 7) {
      return false;
    }

    if (!"*00".equals(split[0]) || !"01".equals(split[1])) {
      return false;
    }

    try {
      long uid;
      if (5 < split.length) {
        uid = Long.parseLong(split[5]);
        String uidStr = String.format("%08X", uid).substring(0, 8);
        LoginInfo.setCheckerUid(uidStr);
      }
      //} else {
      //uid = Long.parseLong(split[2]);
      //}
    } catch (Exception e) {
      LogUtils.w("%s", e.getMessage());
      LoginInfo.setCheckerUid(split[5]);
    }
    LoginInfo.setCheckerId(checkerId);

    if (8 <= split.length) {
      LoginInfo.checkerName = split[6];
    }

    return true;
  }

  /**
   * $14 06 002701001004 010#
   * *14 06 00 1111111111 73201001010102 上库 31853 XINCHENGDU 1000000001 010#
   *
   * @return 返回0：解析成功，1：参数错误，2：密码错误
   */
  public static int parseCheckLogin(String[] split, String checkerId) {
    if (split == null || split.length < 3) {
      return 1;
    }

    if (("*00".equals(split[0]) || "00".equals(split[0])) && "01".equals(split[1])) {
      // 刷卡复核，do nothing
    } else if (("*14".equals(split[0]) || "14".equals(split[0]))
        && "06".equals(split[1])
        && "00".equals(split[2])) {
      checkerId = split[8]; // 指纹登录 无卡号
      // 指纹复核
      for (int i = 3; i < split.length; i++) {
        split[i - 1] = split[i];
      }
    } else {
      return 2;
    }

    try {
      long uid = Long.parseLong(split[5]);
      String uidStr = String.format("%08X", uid).substring(0, 8);
      LoginInfo.setCheckerUid(uidStr);
    } catch (Exception e) {
      LogUtils.w("%s", e.getMessage());
      //LoginInfo.setCheckerUid(split[5]);
    }

    if (8 <= split.length) {
      LoginInfo.checkerName = split[6];
    }

    // 复核id 转 10进制
    //StaticString.userIdcheck = Long.toString(Long.parseLong(checkerId, 16));

    LoginInfo.setCheckerId(checkerId);
    return 0;
  }

  public static void reset() {
    userId = null;
    checkerId = null;
    userNum = null;

    permission = null;
    orgId = null;
    orgListVersion = null;

    storeId = null;
    storeName = null;
    userUid = null;

    needLogin = false;
    needUpdateOrgList = true;
  }

  public static String getInfoString() {
    return "LoginInfo{" +
        "userId='" + userId + '\'' +
        ", userName='" + userName + '\'' +
        ", orgId='" + orgId + '\'' +
        ", orgName='" + orgName + '\'' +
        ", storeId='" + storeId + '\'' +
        ", storeName='" + storeName + '\'' +
        ", loginTimeStr='" + loginTimeStr + '\'' +
        ", userNum='" + userNum + '\'' +
        ", userUid='" + userUid + '\'' +
        ", needLogin=" + needLogin +
        ", orgListVersion=" + orgListVersion +
        ", needUpdateOrgList=" + needUpdateOrgList +
        ", checkerId='" + checkerId + '\'' +
        ", userName2='" + checkerName + '\'' +
        ", permission='" + permission + '\'' +
        '}';
  }

  private static List<StoreIdBean> storeIdBeanList;

  public static List<StoreIdBean> getStoreIdBeanList() {
    return storeIdBeanList;
  }

  public static void setStoreIdBeanList(List<StoreIdBean> storeIdBeanList) {
    LoginInfo.storeIdBeanList = storeIdBeanList;
  }

  public static StoreIdBean getStoreIdBean(String storeLabelId) {
    if (storeIdBeanList != null && storeLabelId != null) {
      for (StoreIdBean storeIdBean : storeIdBeanList) {
        if (storeLabelId.equals(storeIdBean.getCardId())) {
          return storeIdBean;
        }
      }
    }
    return null;
  }

  public static boolean isStoreLabelId(String storeLabelId) {
    return getStoreIdBean(storeLabelId) != null;
  }

  public static void main(String[] args) {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, 2020);
    cal.set(Calendar.MONTH, 0);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    System.out.printf("2020:", DateFormatUtil.getStringTime(cal.getTimeInMillis()));

    System.out.println(DateFormatUtil.getStringTime("yyyyMMdd_HH_mm"));
  }
}
