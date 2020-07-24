package org.orsoul.baselib.util.lock;

/** 袋类别 枚举类. */
public enum EnumBagType {
  Bag_100("01", "100元", "佰元"),
  Bag_50("03", "50元", "伍拾元"),
  Bag_20("04", "20元", "贰拾元"),
  Bag_10("05", "10元", "拾元"),
  Bag_5("06", "5元", "伍元"),
  Bag_1("02", "1元", "壹元"),
  Bag_UNDEFINE("", "未定义", "未定义"),
  ;

  private String bagType;
  private String name;
  private String name2;

  EnumBagType(String bagType, String name, String name2) {
    this.bagType = bagType;
    this.name = name;
    this.name2 = name2;
  }

  public String getBagType() {
    return bagType;
  }

  public String getName() {
    return name;
  }

  public String getName2() {
    return name2;
  }

  public static EnumBagType getByBagId(String bagId) {
    return getByType(BagIdParser.getBagType(bagId));
  }

  public static EnumBagType getByType(String bagType) {
    for (EnumBagType enumBagType : values()) {
      if (enumBagType.getBagType().equals(bagType)) {
        return enumBagType;
      }
    }
    return Bag_UNDEFINE;
  }

  public static String getNameByType(String bagType) {
    return getByType(bagType).getName();
  }

  public static String getName2ByType(String bagType) {
    return getByType(bagType).getName2();
  }

  public static String getNameByBagId(String bagId) {
    return getByBagId(bagId).getName();
  }

  public static String getName2ByBagId(String bagId) {
    return getByBagId(bagId).getName2();
  }
}
