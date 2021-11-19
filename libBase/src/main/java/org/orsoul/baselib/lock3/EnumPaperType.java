package org.orsoul.baselib.lock3;

import org.orsoul.baselib.lock3.bean.BagIdParser;

import java.util.ArrayList;
import java.util.List;

/**
 * 券别-币种 枚举类.
 * 1 完整 2 残损.
 * 1 已清分 2 未清分 3 已复点 4 未复点.
 */
public enum EnumPaperType {

  WZ("1", "完整"),
  CS("2", "残损"),

  QF("1", "已清分"),
  QFN("2", "未清分"),
  FD("3", "已复点"),
  FDN("4", "未复点"),

  YX("10", "原封新券", "01"),

  WQ("11", "完整-已清分", "02"),
  WQN("12", "完整-未清分", "03"),
  WF("13", "完整-已复点", "06"),
  WFN("14", "完整-未复点", "07"),

  CQ("21", "残损-已清分", "08"),
  CQN("22", "残损-未清分", "09"),
  CF("23", "残损-已复点", "04"),
  CFN("24", "残损-未复点", "05"),

  UNDEFINE("", "未定义"),
  ;

  private String type;
  private String name;
  /** 中钞id */
  private String idZc;

  EnumPaperType(String moneyType, String name) {
    this.type = moneyType;
    this.name = name;
  }

  EnumPaperType(String moneyType, String name, String idZc) {
    this.type = moneyType;
    this.name = name;
    this.idZc = idZc;
  }

  public String getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public String getIdZc() {
    return idZc;
  }

  public static EnumPaperType getByBagId(String bagId) {
    return getByType(BagIdParser.getBagType(bagId));
  }

  public static EnumPaperType getByType(String moneyType) {
    for (EnumPaperType enumType : values()) {
      if (enumType.getType().equals(moneyType)) {
        return enumType;
      }
    }
    return UNDEFINE;
  }

  public static EnumPaperType getByName(String name) {
    for (EnumPaperType enumType : values()) {
      if (enumType.getName().equals(name)) {
        return enumType;
      }
    }
    return UNDEFINE;
  }

  public static String getNameByType(String type) {
    return getByType(type).getName();
  }

  public static String getNameByBagId(String bagId) {
    return getByBagId(bagId).getName();
  }

  public static String getTypeByName(String name) {
    return getByName(name).getType();
  }

  public static List<String> getNames() {
    List<String> list = new ArrayList<>();
    for (EnumPaperType enumType : values()) {
      list.add(enumType.getName());
    }
    return list;
  }
}
