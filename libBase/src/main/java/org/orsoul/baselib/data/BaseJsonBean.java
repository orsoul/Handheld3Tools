package org.orsoul.baselib.data;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.orsoul.baselib.util.ClassUtil;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/** json实体类 基类. */
public class BaseJsonBean<T> {
  int code;
  T data;
  List<T> list;

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

  public static void main(String[] args) {
    show1();
  }

  static void show1() {
    Gson gson = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
      @Override public boolean shouldSkipField(FieldAttributes f) {
        //System.out.println("shouldSkipField:" + f.getName());
        return !f.getName().equals("list")
            && !f.getName().equals("fingerIndex")
            && !f.getName().equals("userId")
            && !f.getName().equals("feature")
            && !f.getName().equals("fingerName");
      }

      @Override public boolean shouldSkipClass(Class<?> clazz) {
        return false;
      }
    }).create();
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
