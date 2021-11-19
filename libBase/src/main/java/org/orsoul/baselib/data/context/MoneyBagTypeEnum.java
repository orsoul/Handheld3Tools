package org.orsoul.baselib.data.context;

import java.util.ArrayList;
import java.util.List;

public enum MoneyBagTypeEnum {
  RMB_100_0("01", "101001", "纸100元", "2000000"),
  RMB_100_1("01", "101511", "纸100元（05版）", "2000000"),
  RMB_100_2("01", "101547", "纸100元(2015版)", "2000000"),
  RMB_100_3("01", "101501", "纸100元（5套99版）", "2000000"),

  RMB_50_0("03", "101002", "纸50元", "1000000"),
  RMB_50_1("03", "101510", "纸50元（05版）", "1000000"),
  RMB_50_2("03", "101502", "纸50元（5套）", "1000000"),

  RMB_20_0("04", "101003", "纸20元", "400000"),
  RMB_20_1("04", "101509", "纸20元（05版）", "400000"),
  RMB_20_2("04", "101503", "纸20元（5套）", "400000"),

  RMB_10_0("05", "101004", "纸10元", "200000"),
  RMB_10_1("05", "101527", "纸10元（05版新包装）", "200000"),
  RMB_10_2("05", "101508", "纸10元（05版）", "200000"),
  RMB_10_3("05", "101504", "纸10元（5套）", "200000"),

  RMB_5_0("06", "101005", "纸5元", "100000"),
  RMB_5_1("06", "101528", "纸5元（05版新包装）", "100000"),
  RMB_5_2("06", "101507", "纸5元（05版）", "100000"),
  RMB_5_3("06", "101505", "纸5元（5套）", "100000"),

  RMB_1_1("02", "101529", "纸1元（5套新包装）", "20000"),
  RMB_1_2("02", "101506", "纸1元（5套）", "20000"),
  RMB_UNDEFINE("", "", "未定义纸币", ""),
  ;

  private String psTypeId; // 前置纸币ID
  private String typeID; // 金电卷别ID
  private String typeName;
  private String total;

  MoneyBagTypeEnum(String psTypeId, String typeID, String typeName, String total) {
    this.psTypeId = psTypeId;
    this.typeID = typeID;
    this.typeName = typeName;
    this.total = total;
  }

  public static boolean bagTypeEquals(String voucherType1, String voucherType2) {
    return getBagTypeByVoucherType(voucherType1).equals(getBagTypeByVoucherType(voucherType2));
  }

  public static String getBagTypeByVoucherType(String voucherType) {
    return getMoneyBagType(voucherType).getPsTypeId();
  }

  public static String getValByVoucherType(String voucherType) {
    return getMoneyBagType(voucherType).getTotal();
  }

  public static MoneyBagTypeEnum getMoneyBagType(String voucherType) {
    for (MoneyBagTypeEnum moneyBagTypeEnum : values()) {
      if (moneyBagTypeEnum.getTypeID().equalsIgnoreCase(voucherType)) {
        return moneyBagTypeEnum;
      }
    }
    return RMB_UNDEFINE;
  }

  /**
   * 获取 某个券别下的 所有版别
   */
  public static List<MoneyBagTypeEnum> getMoneyBagTypeList(String bagType) {
    List<MoneyBagTypeEnum> list = new ArrayList<>();
    for (MoneyBagTypeEnum moneyBagTypeEnum : values()) {
      if (moneyBagTypeEnum.getPsTypeId().equalsIgnoreCase(bagType)) {
        list.add(moneyBagTypeEnum);
      }
    }
    return list;
  }

  public String getPsTypeId() {
    return psTypeId;
  }

  public String getTotal() {
    return total;
  }

  public String getTypeID() {
    return typeID;
  }

  public String getTypeName() {
    return typeName;
  }
}
