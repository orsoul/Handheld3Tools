package org.orsoul.baselib.lock3.bean;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.libjava.data.BaseJsonBean;
import com.fanfull.libjava.util.DateFormatUtil;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

/**
 * 记录登录信息.
 */
public final class LoginInfo {
  /** 默认登录密码. */
  public static final String DEFAULT_LOGIN_PWD = "000000";
  public static LoginInfoBean LOGIN_INFO_BEAN_OFFLINE;
  public static LoginInfoBean LOGIN_INFO_BEAN = new LoginInfoBean();

  public static String getLoginInfoJson() {
    return new Gson().toJson(LOGIN_INFO_BEAN);
  }

  public static long currentTimeMillisFix() {
    return LOGIN_INFO_BEAN.currentTimeMillisFix();
  }

  public static byte[] businessInfo() {
    return LOGIN_INFO_BEAN.businessInfo();
  }

  public static String getFingerUserId() {
    return LOGIN_INFO_BEAN.fingerUserId;
  }

  public static void setFingerUserId(String fingerUserId) {
    LOGIN_INFO_BEAN.fingerUserId = fingerUserId;
  }

  public static int getIdentStatus() {
    return LOGIN_INFO_BEAN.identStatus;
  }

  public static void setIdentStatus(int identStatus) {
    LOGIN_INFO_BEAN.identStatus = identStatus;
  }

  public static void setOfflineLoginInfoBean(LoginInfoBean bean) {
    LOGIN_INFO_BEAN = bean;
  }

  /** 已登录 返回true. */
  public static boolean isLogin() {
    return LOGIN_INFO_BEAN.isLogin();
  }

  /** 已复核登录 返回true. */
  public static boolean isCheckLogin() {
    return LOGIN_INFO_BEAN.checkerId != null;
  }

  /** userId不等于checkerId 返回true. */
  public static boolean userIdEquals(String checkerId) {
    return LOGIN_INFO_BEAN.userIdEquals(checkerId);
  }

  public static void setUserId(String userId) {
    LOGIN_INFO_BEAN.userId = userId;
  }

  public static void setCheckerId(String checkerId) {
    LOGIN_INFO_BEAN.checkerId = checkerId;
  }

  public static void setCheckerUid(String checkerUid) {
    LOGIN_INFO_BEAN.checkerUid = checkerUid;
  }

  public static void setNeedLogin(boolean needLogin) {
    LOGIN_INFO_BEAN.needLogin = needLogin;
  }

  public static void setNeedUpdateOrgList(boolean needUpdateOrgList) {
    LOGIN_INFO_BEAN.needUpdateOrgList = needUpdateOrgList;
  }

  public static String getUserId() {
    return LOGIN_INFO_BEAN.userId;
  }

  public static String getPwd() {
    return LOGIN_INFO_BEAN.getPwd();
  }

  public static void setPwd(String pwd) {
    LOGIN_INFO_BEAN.setPwd(pwd);
  }

  public static String getCheckerId() {
    return LOGIN_INFO_BEAN.checkerId;
  }

  public static String getUserNum() {
    return LOGIN_INFO_BEAN.userNum;
  }

  public static long getLoginTime() {
    return LOGIN_INFO_BEAN.loginTime;
  }

  public static void setLoginTime(long loginTime) {
    LOGIN_INFO_BEAN.loginTimeD = System.currentTimeMillis() - loginTime;
    LOGIN_INFO_BEAN.loginTime = loginTime;
  }

  public static String getPermission() {
    return LOGIN_INFO_BEAN.permission;
  }

  public static String getOrgId() {
    return LOGIN_INFO_BEAN.orgId;
  }

  public static String getUserUid() {
    return LOGIN_INFO_BEAN.userUid;
  }

  public static String getCheckerUid() {
    return LOGIN_INFO_BEAN.checkerUid;
  }

  public static String getOrgListVersion() {
    return LOGIN_INFO_BEAN.orgListVersion;
  }

  public static String getUserName() {
    return LOGIN_INFO_BEAN.userName;
  }

  public static String getCheckerName() {
    return LOGIN_INFO_BEAN.checkerName;
  }

  public static String getOrgName() {
    return LOGIN_INFO_BEAN.orgName;
  }

  public static String getLoginTimeStr() {
    return LOGIN_INFO_BEAN.loginTimeStr;
  }

  public static String getStoreId() {
    return LOGIN_INFO_BEAN.storeId;
  }

  public static String getStoreName() {
    return LOGIN_INFO_BEAN.storeName;
  }

  public static boolean isNeedLogin() {
    return LOGIN_INFO_BEAN.needLogin;
  }

  /** 当前机构是否为人行. */
  public static boolean isRh(String orgId) {
    // 002701001
    if (orgId == null || orgId.length() != 9) {
      return false;
    }
    String substring = orgId.substring(4, 6);
    return "01".equals(substring);
  }

  public static boolean isRh() {
    return LOGIN_INFO_BEAN.isRh;
  }

  public static boolean isNeedUpdateOrgList() {
    return LOGIN_INFO_BEAN.needUpdateOrgList;
  }

  /**
   * 解析登录回复信息. 20200928例：<br/>
   *
   * ` 0  1      2           3          4      5   6        7             8         9     10  11
   * *01 01 1111111113 200928105902 071501001 000 16 74212001010101 中心支库-b库间 70080 登录员 {} 001#
   * *14 03 00 1111111113 200928105902 071501001 000 16 74212001010101 中心支库-b库间 70080 登录员 cardId 001#
   *
   * @return 返回0：解析成功，1：参数错误，2：密码错误
   */
  public static int parseLoginInfo(String[] split, String cardId) {
    return LOGIN_INFO_BEAN.parseLoginInfo(split, cardId);
  }

  public static int parseLoginInfo(String[] split) {
    return LOGIN_INFO_BEAN.parseLoginInfo(split);
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
    return LOGIN_INFO_BEAN.parseCheckLogin(checkerId, info);
  }

  /**
   * $14 06 002701001004 010#
   * *14 06 00 1111111111 73201001010102 上库 31853 XINCHENGDU 1000000001 010#
   *
   * @return 返回0：解析成功，1：参数错误，2：密码错误
   */
  public static int parseCheckLogin(String[] split, String checkerId) {
    return LOGIN_INFO_BEAN.parseCheckLogin(split, checkerId);
  }

  public static void reset() {
    LOGIN_INFO_BEAN.reset();
  }

  public static String getInfoString() {
    return LOGIN_INFO_BEAN.getInfoString();
  }

  private static List<StoreIdBean> storeIdBeanList;

  public static List<StoreIdBean> getStoreIdBeanList() {
    return storeIdBeanList;
  }

  public static void setStoreIdBeanList(List<StoreIdBean> storeIdBeanList) {
    LOGIN_INFO_BEAN.storeIdBeanList = storeIdBeanList;
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

  public static boolean needUpdatePwd() {
    return LOGIN_INFO_BEAN.needUpdatePwd;
  }

  public static boolean isFingerLogin() {
    return LOGIN_INFO_BEAN.isFingerLogin;
  }

  public static class LoginInfoBean extends BaseJsonBean {
    /** 操作员 登录id，16进制. */
    private String userId;
    /** 操作员 登录id，10进制. */
    private String userId10;
    private String pwd;
    /** 复核员登录id，16进制. */
    private String checkerId;
    /** 复核员登录id，10进制. */
    private String checkerId10;
    /** 操作员登录编号. 用户所属机构Id + 用户编号： 002701001 + 004 */
    private String userNum;
    /** 服务端的 机构号-机构名称 列表版本号. */
    private String orgListVersion;

    /** 权限. */
    private String permission;
    /** 登录机构号. 002701001 */
    private String orgId;
    /** 是否为人行. */
    private Boolean isRh;

    /** 操作员姓名. */
    private String userName;
    /** 复核员姓名. */
    private String checkerName;
    /** 机构名. */
    private String orgName;
    private Boolean needUpdatePwd;

    /** 登陆时 服务端时间 yyMMddHHmmss. */
    private String loginTimeStr;
    /** 登陆时 服务端时间. */
    private Long loginTime;
    /** 本地时间 与 服务端时间 的时间差. 即：currentTimeMillis - loginTime */
    private Long loginTimeD;

    public long currentTimeMillisFix() {
      return System.currentTimeMillis() - loginTimeD;
    }

    public byte[] businessInfo() {
      return null;
    }

    /** 操作员 uid. */
    private String userUid;
    /** 复核员 uid. */
    private String checkerUid;
    /** 库间id. */
    private String storeId;
    /** 库间名. */
    private String storeName;

    /** 是否 需要再次登录，黑屏过后需要再次登录. */
    private Boolean needLogin;
    /** 是否 需要更新 银行. */
    private Boolean needUpdateOrgList = true;

    /** 认证状态. */
    private Integer identStatus = -1;

    private String fingerUserId;
    private Boolean isFingerLogin;

    public String getFingerUserId() {
      return fingerUserId;
    }

    public void setFingerUserId(String fingerUserId) {
      this.fingerUserId = fingerUserId;
    }

    public int getIdentStatus() {
      return identStatus;
    }

    public void setIdentStatus(int identStatus) {
      this.identStatus = identStatus;
    }

    /** 离线登录 是否有效. */
    public boolean isOfflineValid() {
      long t = loginTime + loginTimeD; // 上次登录时的 本机时间t
      t = (System.currentTimeMillis() - t) / (60 * 60 * 1000);
      boolean offlineValid = 0 < t && t < 24;
      return offlineValid;
    }

    /** 已登录 返回true. */
    public boolean isLogin() {
      return userId != null;
    }

    /** 已复核登录 返回true. */
    public boolean isCheckLogin() {
      return checkerId != null;
    }

    /** userId不等于checkerId 返回true. */
    public boolean userIdEquals(String checkerId) {
      return userId == null ? false : userId.equals(checkerId);
    }

    public void setUserId(String userId) {
      this.userId = userId;
    }

    public void setCheckerId(String checkerId) {
      this.checkerId = checkerId;
    }

    public void setCheckerUid(String checkerUid) {
      this.checkerUid = checkerUid;
    }

    public void setNeedLogin(boolean needLogin) {
      this.needLogin = needLogin;
    }

    public void setNeedUpdateOrgList(boolean needUpdateOrgList) {
      this.needUpdateOrgList = needUpdateOrgList;
    }

    public String getUserId() {
      return userId;
    }

    public String getUserId10() {
      return userId10;
    }

    public String getCheckerId10() {
      return checkerId10;
    }

    public String getPwd() {
      return pwd;
    }

    public void setPwd(String pwd) {
      this.pwd = pwd;
    }

    public String getCheckerId() {
      return checkerId;
    }

    public String getUserNum() {
      return userNum;
    }

    public long getLoginTime() {
      return loginTime;
    }

    public void setLoginTime(long loginTime) {
      this.loginTimeD = System.currentTimeMillis() - loginTime;
      this.loginTime = loginTime;
    }

    public String getPermission() {
      return permission;
    }

    public String getOrgId() {
      return orgId;
    }

    public String getUserUid() {
      return userUid;
    }

    public String getCheckerUid() {
      return checkerUid;
    }

    public String getOrgListVersion() {
      return orgListVersion;
    }

    public String getUserName() {
      return userName;
    }

    public String getCheckerName() {
      return checkerName;
    }

    public String getOrgName() {
      return orgName;
    }

    public String getLoginTimeStr() {
      return loginTimeStr;
    }

    public String getStoreId() {
      return storeId;
    }

    public String getStoreName() {
      return storeName;
    }

    public boolean isNeedLogin() {
      return needLogin;
    }

    /** 当前机构是否为人行. */
    public boolean isRh(String orgId) {
      // 002701001
      if (orgId == null || orgId.length() != 9) {
        return false;
      }
      String substring = orgId.substring(4, 6);
      return "01".equals(substring);
    }

    public boolean isRh() {
      return isRh;
    }

    public boolean isNeedUpdateOrgList() {
      return needUpdateOrgList;
    }

    /**
     * 解析登录回复信息. 20200928例：<br/>
     *
     * ` 0  1      2           3          4      5   6        7             8         9     10  11
     * *01 01 1111111113 200928105902 071501001 000 16 74212001010101 中心支库-b库间 70080 登录员 {} 001#
     * *14 03 00 1111111113 200928105902 071501001 007 16 74212001010101 中心支库-b库间 70080 登录员 cardId 001#
     *
     * @param cardId 刷卡登录为16进制，指纹登录为null
     * @return 返回0：解析成功，1：参数错误，2：密码错误
     */
    public int parseLoginInfo(String[] split, String cardId) {
      if (split == null || split.length < 4) {
        return 1;
      }

      if (("*01".equals(split[0]) || "01".equals(split[0])) && "01".equals(split[1])) {
        // 刷卡登录，do nothing
        isFingerLogin = false;
      } else if (("*14".equals(split[0]) || "14".equals(split[0])) &&
          "03".equals(split[1]) &&
          "00".equals(split[2])) {
        isFingerLogin = true;
        userId10 = split[12];
        cardId = String.format("%08X", Long.parseLong(userId10));
        setFingerUserId(split[6]);
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
        long time = DateFormatUtil.parseString2Long(loginTimeStr, "yyMMddHHmmss");
        setLoginTime(time);
        orgId = split[4];
        userNum = split[5];
        orgListVersion = split[6];
        isRh = isRh(orgId);
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

      // 11 用户名+密码登录，检查是否需改密码
      if (13 <= split.length && !isFingerLogin) {
        String json = split[11];
        try {
          JSONObject jsonObject = new JSONObject(json);
          needUpdatePwd = jsonObject.getBoolean("needUpdatePwd");
        } catch (JSONException e) {
          LogUtils.wtf("%s", e);
        }
      }

      this.setUserId(cardId);
      userId10 = String.format("%08d", Long.parseLong(cardId, 16));
      return 0;
    }

    public int parseLoginInfo(String[] split) {
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
    public boolean parseCheckLogin(String checkerId, String info) {
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
          this.setCheckerUid(uidStr);
        }
        //} else {
        //uid = Long.parseLong(split[2]);
        //}
      } catch (Exception e) {
        LogUtils.w("%s", e.getMessage());
        this.setCheckerUid(split[5]);
      }
      this.setCheckerId(checkerId);

      if (8 <= split.length) {
        this.checkerName = split[6];
      }

      return true;
    }

    /**
     * $14 06 002701001004 010#
     * *14 06 00 1111111111 73201001010102 上库 31853 XINCHENGDU 1000000001 010#
     *
     * @return 返回0：解析成功，1：参数错误，2：密码错误
     */
    public int parseCheckLogin(String[] split, String checkerId) {
      if (split == null || split.length < 3) {
        return 1;
      }

      if (("*00".equals(split[0]) || "00".equals(split[0])) && "01".equals(split[1])) {
        // 刷卡复核，do nothing
      } else if (("*14".equals(split[0]) || "14".equals(split[0]))
          && "06".equals(split[1])
          && "00".equals(split[2])) {
        checkerId10 = split[8];
        checkerId = String.format("%08X", Long.parseLong(userId10));
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
        this.setCheckerUid(uidStr);
      } catch (Exception e) {
        LogUtils.w("%s", e.getMessage());
        //loginInfoBean.setCheckerUid(split[5]);
      }

      if (8 <= split.length) {
        this.checkerName = split[6];
      }

      // 复核id 转 10进制
      //StaticString.userIdcheck = Long.toString(Long.parseLong(checkerId, 16));

      this.setCheckerId(checkerId);
      checkerId10 = String.format("%08d", Long.parseLong(checkerId, 16));
      return 0;
    }

    public void reset() {
      userId = null;
      userId10 = null;
      checkerId = null;
      checkerId10 = null;
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

    public String getInfoString() {
      return "LoginInfo{" +
          "userId='" + userId + '\'' +
          "userId10='" + userId10 + '\'' +
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
          ", checkerId10='" + checkerId10 + '\'' +
          ", userName2='" + checkerName + '\'' +
          ", permission='" + permission + '\'' +
          '}';
    }

    private List<StoreIdBean> storeIdBeanList;

    public List<StoreIdBean> getStoreIdBeanList() {
      return storeIdBeanList;
    }

    public void setStoreIdBeanList(List<StoreIdBean> storeIdBeanList) {
      this.storeIdBeanList = storeIdBeanList;
    }

    public StoreIdBean getStoreIdBean(String storeLabelId) {
      if (storeIdBeanList != null && storeLabelId != null) {
        for (StoreIdBean storeIdBean : storeIdBeanList) {
          if (storeLabelId.equals(storeIdBean.getCardId())) {
            return storeIdBean;
          }
        }
      }
      return null;
    }

    public boolean isStoreLabelId(String storeLabelId) {
      return getStoreIdBean(storeLabelId) != null;
    }
  }
}
