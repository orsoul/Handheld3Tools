package com.fanfull.handheldtools.bean;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.List;
import org.orsoul.baselib.util.BytesUtil;

public class UserInfoBean extends BaseJsonBean {

  private List<UserInfoBean> userInfoList;
  /**
   * uid : 70080
   * cid : 0023200002
   * name : a01
   * privileges : ["audit","isolate","operate"]
   */

  private int uid;
  private String cid;
  private String name;
  private List<String> privileges;

  @Override public String toString() {
    if (userInfoList != null) {
      return "UserInfoBean{" +
          "code=" + code +
          ", userInfoList=" + userInfoList +
          '}';
    }

    return "{" +
        "uid=" + uid +
        ", cid='" + cid + '\'' +
        ", name='" + name + '\'' +
        ", privileges=" + privileges +
        '}';
  }

  public static UserInfoBean parseJsonString(String jsonString) {
    UserInfoBean mainUserInfoBean = null;
    //UserInfoBean userInfoBean = new Gson().fromJson(jsonString, UserInfoBean.class);
    if (jsonString.startsWith("{")) {
      mainUserInfoBean = new Gson().fromJson(jsonString, UserInfoBean.class);
    } else {
      List<UserInfoBean> o = new Gson().fromJson(jsonString, new TypeToken<List<UserInfoBean>>() {
      }.getType());
      if (o != null && !o.isEmpty()) {
        mainUserInfoBean = o.get(0);
        if (mainUserInfoBean.getUserInfoList() != null) {
          for (UserInfoBean infoBean : mainUserInfoBean.getUserInfoList()) {
            infoBean.setCode(mainUserInfoBean.getCode());
          }
        }
      }
    }
    return mainUserInfoBean;
  }

  public static byte[] obtainReplyMsg(long sequence, int transport,
      byte[] payloads, boolean isPackage, byte packageMode) {
    byte[] sequenceBuff = BytesUtil.long2Bytes(sequence, 8);
    byte[] transportBuff = BytesUtil.long2Bytes(transport, 2);

    byte[] payloadsBuff;
    if (isPackage) {
      //String s = DesUtil.encodPayloads(BytesUtil.bytes2HexString(payloads));
      //payloadsBuff = BytesUtil.hexString2Bytes(s);
      payloadsBuff = payloads;
    } else {
      payloadsBuff = payloads;
    }

    /* 处理length段，32bit */
    byte[] length = BytesUtil.long2Bytes(payloadsBuff.length, 4);
    //length[0] |= 0B1000_0000; // 最高位、32位
    //length[0] |= 0B0100_0000; // 31位
    length[0] |= (isPackage ? 0B1100_0000 : 0B1000_0000);
    length[0] |= (packageMode << 4);

    byte[] allData =
        BytesUtil.concatArray(length, sequenceBuff, transportBuff, payloadsBuff);
    return allData;
  }

  public static void main(String[] args) {
    String jsonArr =
        "[{\"code\":1,\"userInfoList\":[{\"uid\":70080,\"cid\":\"0023200002\",\"name\":\"a01\",\"privileges\":[\"audit\",\"isolate\",\"operate\"]},{\"uid\":67562,\"cid\":\"0023200001\",\"name\":\"a02\",\"privileges\":[\"audit\",\"isolate\",\"operate\"]},{\"uid\":70080,\"cid\":\"1000000002\",\"name\":\"a04\",\"privileges\":[\"audit\",\"isolate\",\"operate\"]}]}]";
    String jsonObj =
        "{\"code\":1,\"userInfoList\":[{\"uid\":70080,\"cid\":\"0023200002\",\"name\":\"a01\",\"privileges\":[\"audit\",\"isolate\",\"operate\"]},{\"uid\":67562,\"cid\":\"0023200001\",\"name\":\"a02\",\"privileges\":[\"audit\",\"isolate\",\"operate\"]},{\"uid\":70080,\"cid\":\"1000000002\",\"name\":\"a04\",\"privileges\":[\"audit\",\"isolate\",\"operate\"]}]}";

    jsonObj =
        "{\"code\":1,\"data\":{\"uid\":70080,\"cid\":\"0023200002\",\"name\":\"a01\",\"privileges\":[\"audit\",\"isolate\",\"operate\"]}}";
    jsonArr =
        "{\"code\":1,\"data\":[{\"uid\":70080,\"cid\":\"0023200002\",\"name\":\"a01\",\"privileges\":[\"audit\",\"isolate\",\"operate\"]},{\"uid\":67562,\"cid\":\"0023200001\",\"name\":\"a02\",\"privileges\":[\"audit\",\"isolate\",\"operate\"]},{\"uid\":70080,\"cid\":\"1000000002\",\"name\":\"a04\",\"privileges\":[\"audit\",\"isolate\",\"operate\"]}]}";
    BaseJsonBean<UserInfoBean> jsonBeanObj =
        BaseJsonBean.jsonString2Obj(jsonObj, UserInfoBean.class);
    System.out.println(jsonBeanObj);
    System.out.println(jsonBeanObj.getData());
    BaseJsonBean<List<UserInfoBean>> jsonBeanList =
        BaseJsonBean.jsonString2List(jsonArr, UserInfoBean.class);
    System.out.println(jsonBeanList);
    System.out.println(jsonBeanList.getData());

    //UserInfoBean userInfoBean = parseJsonString(jsonArr);
    //System.out.println(userInfoBean);

    //byte[] bytes = obtainReplyMsg(0, 0x4003, new byte[4], true, (byte) 2);
    //System.out.println(BytesUtil.bytes2HexString(bytes));
  }

  public List<UserInfoBean> getUserInfoList() {
    return userInfoList;
  }

  public void setUserInfoList(List<UserInfoBean> userInfoList) {
    this.userInfoList = userInfoList;
  }

  public int getUid() {
    return uid;
  }

  public void setUid(int uid) {
    this.uid = uid;
  }

  public String getCid() {
    return cid;
  }

  public void setCid(String cid) {
    this.cid = cid;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getPrivileges() {
    return privileges;
  }

  public void setPrivileges(List<String> privileges) {
    this.privileges = privileges;
  }
}
