package org.orsoul.baselib.lock3;

import java.util.ArrayList;
import java.util.List;
import org.orsoul.baselib.lock3.bean.BagIdParser;

/** 城市代号. */
public enum EnumCity {

  wh("027", "武汉"),
  cd("028", "成都"),
  qd("532", "青岛"),
  zb("533", "淄博"),
  dz("534", "德州"),
  yt("535", "烟台"),
  hf("536", "潍坊"),
  ez("711", "鄂州"),

  UNDEFINE("", "未定义"),
  ;

  private String code;
  private String name;

  EnumCity(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public String getCode() {
    return code;
  }

  public String getName() {
    return name;
  }

  public static EnumCity getByBagId(String bagId) {
    return getByType(BagIdParser.getCityCode(bagId));
  }

  public static EnumCity getByType(String code) {
    for (EnumCity enumType : values()) {
      if (enumType.getCode().equals(code)) {
        return enumType;
      }
    }
    return UNDEFINE;
  }

  public static EnumCity getByName(String name) {
    for (EnumCity enumType : values()) {
      if (enumType.getName().equals(name)) {
        return enumType;
      }
    }
    return UNDEFINE;
  }

  public static String getNameByCode(String cityCode) {
    return getByType(cityCode).getName();
  }

  public static String getNameByBagId(String bagId) {
    return getByBagId(bagId).getName();
  }

  public static String getCodeByName(String name) {
    return getByName(name).getCode();
  }

  public static List<String> getNames() {
    List<String> list = new ArrayList<>();
    for (EnumCity enumType : values()) {
      list.add(enumType.getName());
    }
    return list;
  }
}
