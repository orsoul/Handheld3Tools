package org.orsoul.baselib.lock3.bean;

import com.fanfull.libjava.data.BaseJsonBean;
import com.fanfull.libjava.util.Logs;
import com.google.gson.Gson;

import java.util.List;
import java.util.Objects;

public class BagIdBean extends BaseJsonBean<BagIdBean> {

  String bagId; // "050279012111121622200001"
  String paperTypeId;// "03"
  String voucherTypeId;//"101001"

  int bagCount;
  List<BagIdBean> bagList;

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BagIdBean bagIdBean = (BagIdBean) o;
    return Objects.equals(bagId, bagIdBean.bagId);
  }

  @Override public int hashCode() {
    return Objects.hash(bagId);
  }

  public static BagIdBean parse(String json) {
    return BaseJsonBean.json2ListObj(json, BagIdBean.class);
  }

  public static void main(String[] args) {
    String json = "{\"bagCount\":1,\"bagList\":[{\"bagId\":\"050279012111121622200001\"," +
        "\"paperTypeId\":\"03\",\"voucherTypeId\":\"101001\"}]}";
    BagIdBean parse = parse(json);
    Logs.out("json:%s", json);
    Logs.out("BagIdBean:%s", parse);
    Logs.out("BagIdBean:%s", new Gson().toJson(parse));
  }
}
