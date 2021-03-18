package org.orsoul.baselib.lock3.bean;

import android.util.SparseArray;

import com.apkfuns.logutils.LogUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;
import org.orsoul.baselib.util.BytesUtil;
import org.orsoul.baselib.util.DateFormatUtil;
import org.orsoul.baselib.util.RegularUtil;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 交接信息实体类.
 */
public class HandoverBean {
  /** 封袋. */
  public static final int FUN_COVER_BAG = 1;
  /** 入库. */
  public static final int FUN_IN_STORE = 2;
  /** 出库. */
  public static final int FUN_OUT_STORE = 3;
  /** 验封. */
  public static final int FUN_CHECK_COVER = 4;
  /** 未知. */
  public static final int FUN_UNDEFINE = 5;
  /** 开袋. */
  public static final int FUN_OPEN_BAG = 6;
  /** 封入. */
  public static final int FUN_COVER_IN = 7;
  /** 出开. */
  public static final int FUN_OUT_OPEN = 8;
  /** 接收. */
  public static final int FUN_RECEIVE = 9;

  /** 已保存记录数量的存储位置. */
  public static final int SA_INDEX = 0x93;
  /** 首条记录的存储位置. */
  public static final int SA_DATA = 0x94;

  /** 2020-01-01 00:00:00 的时刻 */
  public static final long TIME_20200101 = 1577808000000L;
  public static final SparseArray<String> funMap;

  static {
    funMap = new SparseArray<>();
    funMap.put(FUN_COVER_BAG, "封袋");
    funMap.put(FUN_IN_STORE, "入库");
    funMap.put(FUN_OUT_STORE, "出库");
    funMap.put(FUN_CHECK_COVER, "验封");
    funMap.put(FUN_UNDEFINE, "未知");
    funMap.put(FUN_OPEN_BAG, "开袋");
    funMap.put(FUN_COVER_IN, "封入");
    funMap.put(FUN_OUT_OPEN, "出开");
    funMap.put(FUN_RECEIVE, "接收");
  }

  //public static Map<String, String> orgMap;
  public static JSONObject orgMap;

  int function; // 1
  /** 机构号，数字串. 002706001 */
  String organcode;
  String time; // "20170426155745"
  String scaner1; // "张三(123456xx)"
  String scaner2; // "李四(123456xx)"

  public int timeSecond;
  String orgName; // 建行
  String funTypeName; // 封袋
  String formatTime; // 标准时间格式

  public int getFunction() {
    return function;
  }

  public HandoverBean setFunction(int function) {
    this.function = function;
    return this;
  }

  public String getOrgancode() {
    return organcode;
  }

  public HandoverBean setOrgancode(String organcode) {
    this.organcode = organcode;
    return this;
  }

  public String getTime() {
    if (formatTime == null) {
      formatTime = DateFormatUtil.getStringTime(time, DateFormatUtil.FORMAT_yyyyMMddHHmmss);
    }
    return formatTime;
  }

  public void setTime(String time) {
    this.time = time;
  }

  public String getScaner1() {
    return scaner1;
  }

  public HandoverBean setScaner1(String scaner1) {
    this.scaner1 = scaner1;
    return this;
  }

  public String getScaner2() {
    return scaner2;
  }

  public HandoverBean setScaner2(String scaner2) {
    this.scaner2 = scaner2;
    return this;
  }

  /**
   * 从20200101 到给定时间 的秒数.
   *
   * @param timeMillis 给定时间
   * @return 从20200101 到给定时间 的秒数
   */
  public HandoverBean setTimeSecond(long timeMillis) {
    long l = (timeMillis - HandoverBean.TIME_20200101) / 1000;
    this.timeSecond = (int) l;
    return this;
  }

  public String getOrgName() {
    if (orgName == null && orgMap != null) {
      //orgName = orgMap.optString(organcode);
      try {
        orgName = orgMap.getString(organcode);
      } catch (JSONException e) {
      }
    }
    return orgName;
  }

  public HandoverBean setOrgName(String orgName) {
    this.orgName = orgName;
    return this;
  }

  public String getFunTypeName() {
    if (funTypeName == null) {
      funTypeName = funMap.get(function);
    }
    return funTypeName;
  }

  @Override public String toString() {
    return "HandoverBean{" +
        "function=" + function +
        ", organcode='" + organcode + '\'' +
        ", time='" + time + '\'' +
        ", scaner1='" + scaner1 + '\'' +
        ", scaner2='" + scaner2 + '\'' +
        ", timeSecond=" + timeSecond +
        ", orgName='" + orgName + '\'' +
        ", funTypeName='" + funTypeName + '\'' +
        ", formatTime='" + formatTime + '\'' +
        '}';
  }

  public String toJsonString() {
    return new Gson().toJson(this);
  }

  public static HandoverBean parseJsonStr(String jsonStr) {
    return new Gson().fromJson(jsonStr, HandoverBean.class);
  }

  public static List<HandoverBean> parseJsonArr(String jsonStr) {
    Type type = new TypeToken<List<HandoverBean>>() {
    }.getType();
    return new Gson().fromJson(jsonStr, type);
  }

  public static final int ONE_ITEM_LEN = 28;

  public static List<HandoverBean> parseData(byte[] data) {
    if (data == null || data.length % ONE_ITEM_LEN != 0) {
      return null;
    }

    List<HandoverBean> list = new ArrayList<>();
    int count = data.length / ONE_ITEM_LEN;
    for (int i = 0; i < count; i++) {
      int from = i * ONE_ITEM_LEN;
      int to = from + ONE_ITEM_LEN;
      byte[] oneItem = Arrays.copyOfRange(data, from, to);
      HandoverBean handoverBean = parseOneData(oneItem);
      if (handoverBean != null) {
        list.add(handoverBean);
      }
    }
    return list;
  }

  private static HandoverBean parseOneData(byte[] data) {
    if (data == null || data.length != ONE_ITEM_LEN) {
      return null;
    }
    HandoverBean handoverBean = new HandoverBean();
    handoverBean.function = data[0];
    handoverBean.funTypeName = funMap.get(data[0]);

    Long org = BytesUtil.bytes2Long(data, 4, 4);
    handoverBean.organcode = String.format("%09d", org);

    Long time = BytesUtil.bytes2Long(data, 8, 4);
    handoverBean.formatTime = DateFormatUtil.getStringTime(time * 1000 + TIME_20200101);

    try {
      handoverBean.scaner1 = new String(data, 12, 8, "gbk").trim();
      handoverBean.scaner2 = new String(data, 20, 8, "gbk").trim();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    LogUtils.d("handoverBean:%s", handoverBean);
    return handoverBean;
  }

  public boolean checkData() {
    if (FUN_RECEIVE < function
        || function < FUN_COVER_BAG
        //|| organcode == null
        || !RegularUtil.matchDecimalString(organcode)
        || scaner1 == null
        || scaner2 == null) {
      LogUtils.i("%s", "function=" + function +
          ", organcode='" + organcode + '\'' +
          ", scaner1='" + scaner1 + '\'' +
          ", scaner2='" + scaner2);
      return false;
    }
    return true;
  }

  public byte[] toBytes() {
    if (!checkData()) {
      return null;
    }
    byte[] funBuff = new byte[] { (byte) function, 0, 0, 0 };
    int org = Integer.parseInt(organcode);
    byte[] orgBuff = BytesUtil.long2Bytes(org, 4);
    byte[] timeBuff = BytesUtil.long2Bytes(timeSecond, 4);
    byte[] scanner1 = null;
    byte[] scanner2 = null;
    try {
      scanner1 = Arrays.copyOf(scaner1.getBytes("gbk"), 8);
      scanner2 = Arrays.copyOf(scaner2.getBytes("gbk"), 8);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    LogUtils.d("%s-%s-%s-%s-%s",
        BytesUtil.bytes2HexString(funBuff),
        BytesUtil.bytes2HexString(orgBuff),
        BytesUtil.bytes2HexString(timeBuff),
        BytesUtil.bytes2HexString(scanner1),
        BytesUtil.bytes2HexString(scanner2)
    );
    return BytesUtil.concatArray(funBuff, orgBuff, timeBuff, scanner1, scanner2);
  }
}
