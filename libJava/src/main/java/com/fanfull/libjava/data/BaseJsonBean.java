package com.fanfull.libjava.data;

import com.fanfull.libjava.util.ClassUtil;
import com.google.gson.Gson;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/** json实体类 基类. */
public class BaseJsonBean<T> {
  protected int code;
  protected T data;
  protected List<T> list;

  public BaseJsonBean() {
  }

  public BaseJsonBean(int code, T data) {
    this.code = code;
    this.data = data;
  }

  public BaseJsonBean(int code, List<T> list) {
    this.code = code;
    this.list = list;
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

  public List<T> getList() {
    return list;
  }

  public void setList(List<T> list) {
    this.list = list;
  }

  public String toJsonString() {
    return new Gson().toJson(this);
  }

  @Override public String toString() {
    return toJsonString();
  }

  public static <T extends BaseJsonBean> T parseJson2Obj(String json, Class<T> clz) {
    Class[] generics = ClassUtil.getGenericsFromClass(clz);
    Type type = new ParameterizedTypeImpl(clz, generics);
    return new Gson().fromJson(json, type);
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

  /**
   * 根据json字符串生成 T<G> 类型的实体.
   *
   * @param clz 主类class
   * @param genericClz 主类泛型的class
   */
  public static <T, G> T json2Obj(String json, Class<T> clz, Class<G> genericClz) {
    // 生成 T<G>
    Type type = new ParameterizedTypeImpl(clz, new Class[] { genericClz });
    return new Gson().fromJson(json, type);
  }

  /**
   * 根据json字符串生成 T<T> 类型的实体.
   *
   * @param clz 主类及其泛型的class
   */
  public static <T> T json2Obj(String json, Class<T> clz) {
    return json2Obj(json, clz, clz);
  }

  /**
   * 根据json字符串生成 T<List<G>> 类型的实体.
   *
   * @param clz 主类class
   * @param genericClz 主类泛型的class
   */
  public static <T, G> T json2ListObj(String json, Class<T> clz, Class<G> genericClz) {
    // 生成 T<List<G>> 中的 List<G>
    Type listType = new BaseJsonBean.ParameterizedTypeImpl(List.class, new Class[] { genericClz });
    // 有List<G>后 生成完整的 T<List<G>>
    Type type = new BaseJsonBean.ParameterizedTypeImpl(clz, new Type[] { listType });
    return new Gson().fromJson(json, type);
  }

  /**
   * 根据json字符串生成 T<List<T>> 类型的实体.
   *
   * @param clz 主类及其泛型的class
   */
  public static <T> T json2ListObj(String json, Class<T> clz) {
    return json2ListObj(json, clz, clz);
  }

  public static void main(String[] args) {
    testJson2Obj();
    testJson2ListObj();
  }

  static class Home {
    String adds;
  }

  static class Person extends BaseJsonBean<Home> {
    String name;
  }

  static class Boss extends BaseJsonBean<List<Home>> {
    String name;
  }

  static void testJson2Obj() {
    System.out.println("==== testJson2Obj ====");

    String json = "{\"code\":1,\"name\":\"Jack\",\"data\":{\"adds\":\"深圳\"}}";
    System.out.printf("json:%s\n", json);
    Person person = json2Obj(json, Person.class, Home.class);
    System.out.printf("person:%s\n", person);
  }

  static void testJson2ListObj() {
    String json = "{\"code\":1,\"name\":\"Jack\",\"data\":[{\"adds\":\"北京\"},{\"adds\":\"深圳\"}]}";
    Boss person = json2ListObj(json, Boss.class, Home.class);
    System.out.printf("Boss:%s\n", person);
    System.out.printf("json:%s\n", json);
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