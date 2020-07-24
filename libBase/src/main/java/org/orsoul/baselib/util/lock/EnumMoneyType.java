package org.orsoul.baselib.util.lock;

/**
 * 券别-币种 枚举类.
 * 1 完整 2 残损.
 * 1 已清分 2 未清分 3 已复点 4 未复点.
 */
public enum EnumMoneyType {

  WZ("1", "完整券"),
  CS("2", "残损券"),

  QF("4", "已清分"),
  QFN("5", "未清分"),
  FD("6", "已复点"),
  FDN("7", "未复点"),

  UNDEFINE("", "未定义"),
  ;

  private String type;
  private String name;

  EnumMoneyType(String moneyType, String name) {
    this.type = moneyType;
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public static EnumMoneyType getByBagId(String bagId) {
    return getByType(BagIdParser.getBagType(bagId));
  }

  public static EnumMoneyType getByType(String moneyType) {
    for (EnumMoneyType enumType : values()) {
      if (enumType.getType().equals(moneyType)) {
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
}
