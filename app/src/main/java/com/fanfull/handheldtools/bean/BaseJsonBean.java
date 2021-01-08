package com.fanfull.handheldtools.bean;

import com.fanfull.libhard.finger.bean.FingerBean;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/** json实体类 基类. */
public class BaseJsonBean<T> {
  int code;
  T data;

  public BaseJsonBean() {
  }

  public BaseJsonBean(int code, T data) {
    this.code = code;
    this.data = data;
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  public String toJsonString() {
    return new Gson().toJson(this);
  }

  @Override public String toString() {
    return toJsonString();
  }

  public static <T> BaseJsonBean<T> jsonString2Obj(String json, Class<T> clazz) {
    Type type = new ParameterizedTypeImpl(BaseJsonBean.class, new Class[] { clazz });
    return new Gson().fromJson(json, type);
  }

  public static <T> BaseJsonBean<List<T>> jsonString2List(String json, Class<T> clazz) {
    // 生成List<T> 中的 List<T>
    Type listType = new ParameterizedTypeImpl(List.class, new Class[] { clazz });
    // 根据List<T>生成完整的Result<List<T>>
    Type type = new ParameterizedTypeImpl(BaseJsonBean.class, new Type[] { listType });
    return new Gson().fromJson(json, type);
  }

  public static void main(String[] args) {
    show2();
  }

  private static void show1() {
    String jsonObj =
        "{\"code\":1,\"data\":{\"uid\":70080,\"cid\":\"0023200002\",\"name\":\"a01\",\"privileges\":[\"audit\",\"isolate\",\"operate\"]}}";
    String jsonArr =
        "{\"code\":1,\"data\":[{\"uid\":70080,\"cid\":\"0023200002\",\"name\":\"a01\",\"privileges\":[\"audit\",\"isolate\",\"operate\"]},{\"uid\":67562,\"cid\":\"0023200001\",\"name\":\"a02\",\"privileges\":[\"audit\",\"isolate\",\"operate\"]},{\"uid\":70080,\"cid\":\"1000000002\",\"name\":\"a04\",\"privileges\":[\"audit\",\"isolate\",\"operate\"]}]}";
    BaseJsonBean<UserInfoBean> jsonBeanObj =
        BaseJsonBean.jsonString2Obj(jsonObj, UserInfoBean.class);
    System.out.println(jsonBeanObj);

    BaseJsonBean<List<UserInfoBean>> jsonBeanList =
        BaseJsonBean.jsonString2List(jsonArr, UserInfoBean.class);
    System.out.println(jsonBeanList);
  }

  private static void show2() {
    Gson gson = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
      @Override public boolean shouldSkipField(FieldAttributes f) {
        System.out.println("shouldSkipField:" + f.getName());
        return !f.getName().equals("fingerIndex")
            && !f.getName().equals("userId")
            && !f.getName().equals("feature")
            && !f.getName().equals("fingerName");
      }

      @Override public boolean shouldSkipClass(Class<?> clazz) {
        //System.out.println("shouldSkipClass:" + clazz);
        return false;
      }
    }).create();

    FingerBean fingerBean = new FingerBean(7, "CCCC", "fingerId", 0);
    fingerBean.setUserId("userId");
    fingerBean.setFingerName("fingerName");
    fingerBean.setUserName("setUserName");
    List<FingerBean> list = new ArrayList<>();
    list.add(fingerBean);
    String s = gson.toJson(list);
    System.out.printf("json:%s\n", s);
    System.out.printf("json:%s\n", new Gson().toJson(list));
  }

  public static class ParameterizedTypeImpl implements ParameterizedType {
    private final Class raw;
    private final Type[] args;

    public ParameterizedTypeImpl(Class raw, Type[] args) {
      this.raw = raw;
      this.args = args != null ? args : new Type[0];
    }

    @Override
    public Type[] getActualTypeArguments() {
      return args;
    }

    @Override
    public Type getRawType() {
      return raw;
    }

    @Override
    public Type getOwnerType() {
      return null;
    }
  }
}
