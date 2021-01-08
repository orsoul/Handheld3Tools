package org.orsoul.baselib.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;

public final class ClassUtil {

  public static void main(String[] args) {
    class Test<T, E, G> {
    }
    class TestSub extends Test<Integer, String, Comparable<String>> {
    }
    class TestSub2 extends Test {
    }
    Test test = new TestSub();

    HashMap<String, Integer> map = new HashMap<>();
    //Class<?> classT = getClassT(map.getClass(), 0);
    //System.out.println(classT.getSimpleName());

    Type type = test.getClass().getGenericSuperclass();
    //showObjectClass(TestSub.class);
    showInterface(TestSub.class);

    type = String.class.getGenericSuperclass();
    //showObjectClass(String.class);
    showInterface(String.class);

    //System.out.println(getInterfaceT("", 1));
    System.out.println(Arrays.toString(getGenericFromInterface(String.class, 1)));
    //System.out.println(Arrays.toString(getGenericFromClass(String.class)));
    //System.out.println(Arrays.toString(getGenericFromClass(TestSub.class)));
    //System.out.println(Arrays.toString(getGenericFromClass(TestSub2.class)));
  }

  static void showObjectClass(Class clz) {
    Type type = clz.getGenericSuperclass();
    System.out.println(clz.getSimpleName() + " getGenericSuperclass:" + type);
    if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      Type[] types = parameterizedType.getActualTypeArguments();
      System.out.println(Arrays.toString(types));
    }
  }

  static void showInterface(Class clz) {
    Type[] types = clz.getGenericInterfaces();
    System.out.println(Arrays.toString(types));
    //if (type instanceof ParameterizedType) {
    //  ParameterizedType parameterizedType = (ParameterizedType) type;
    //  Type[] types = parameterizedType.getActualTypeArguments();
    //  System.out.println(Arrays.toString(types));
    //}
  }

  /**
   * 获取指定位置接口上的泛型.
   *
   * @param interfaceIndex 实现的第 interfaceIndex 个接口，从0开始.
   * @return 获取泛型成功返回类型数组，指定接口无泛型返回空数组，指定位置无接口返回null
   */
  public static Class[] getGenericFromInterface(Class clz, int interfaceIndex) {
    Type[] interfaces = clz.getGenericInterfaces();
    if (interfaces.length <= interfaceIndex || interfaceIndex < 0) {
      // 接口无泛型
      return null;
    } else if (!(interfaces[interfaceIndex] instanceof ParameterizedType)) {
      // 接口无泛型
      return new Class[0];
    }
    ParameterizedType parameterizedType = (ParameterizedType) interfaces[interfaceIndex];

    Type[] geneType = parameterizedType.getActualTypeArguments();
    Class[] reVal = new Class[geneType.length];
    for (int i = 0; i < geneType.length; i++) {
      reVal[i] = (Class) geneType[i];
    }
    return reVal;
    //return checkType(type, index);
  }

  /**
   * 获取clz父类上的泛型信息.
   *
   * @return 父类存在泛型信息返回类型数组，否则返回null.
   */
  public static Class[] getGenericsFromClass(Class clz) {
    Type type = clz.getGenericSuperclass();
    if (!(type instanceof ParameterizedType)) {
      return null;
    }

    ParameterizedType parameterizedType = (ParameterizedType) type;
    Type[] types = parameterizedType.getActualTypeArguments();
    Class[] reVal = new Class[types.length];
    for (int i = 0; i < types.length; i++) {
      reVal[i] = (Class) types[i];
    }
    return reVal;
  }

  /**
   * 获取clz父类上的第genericIndex个泛型信息.
   *
   * @param genericIndex 从0开始，父类上的第genericIndex个泛型信息
   * @return 父类相应位置上存在泛型返回给泛型的类型，否则返回null.
   */
  public static Class getGenericFromClass(Class clz, int genericIndex) {
    Class[] classes = getGenericsFromClass(clz);
    if (classes == null || classes.length <= genericIndex || genericIndex < 0) {
      return null;
    }
    return classes[genericIndex];
  }

  /**
   * 获取clz父类上的第0个泛型信息.
   *
   * @return 父类存在泛型返回给泛型的类型，否则返回null.
   */
  public static Class getGenericFromClass(Class clz) {
    return getGenericFromClass(clz, 0);
  }
}
