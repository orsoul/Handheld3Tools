package org.orsoul.baselib.lock3.bean;

import org.orsoul.baselib.data.context.MoneyBagTypeEnum;
import org.orsoul.baselib.lock3.EnumPaperType;

/**
 * 完整/残损、清分、复点、券别、版别
 */
public class MoneyBean {
  String moneyId;
  String moneyName;
  String voucherId;
  String voucherName;

  EnumPaperType paperType;
  MoneyBagTypeEnum moneyType;

  public EnumPaperType getPaperType() {
    return paperType;
  }

  public void setPaperType(EnumPaperType paperType) {
    this.paperType = paperType;
  }

  public MoneyBagTypeEnum getMoneyType() {
    return moneyType;
  }

  public void setMoneyType(MoneyBagTypeEnum moneyType) {
    this.moneyType = moneyType;
  }

  /** 完整-未清分 */
  public String getPaperName() {
    return paperType == null ? null : paperType.getName();
  }

  /** 01 */
  public String getPaperIdZc() {
    return paperType == null ? null : paperType.getIdZc();
  }

  /** 纸100元（05版） */
  public String getVoucherName() {
    return moneyType == null ? null : moneyType.getTypeName();
  }

  /** 纸100元（05版） */
  public String getVoucherId() {
    return moneyType == null ? null : moneyType.getTypeID();
  }
}
