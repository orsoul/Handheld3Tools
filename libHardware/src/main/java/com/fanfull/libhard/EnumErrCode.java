package com.fanfull.libhard;

/**
 * 硬件模块读取数据结果.
 */
public enum EnumErrCode {

  SUCCESS(0, "执行成功"),
  ARGS_ERR(-1, "参数错误"),
  FAILED(-2, "执行失败"),
  TIMEOUT(-3, "超时"),

  FIND_CARD_NOTHING(-4, "无高频卡"),
  FIND_CARD_FAILED(-6, "寻高频卡失败"),
  READ_NFC_FAILED(-7, "读NFC失败"),
  WRITE_NFC_FAILED(-8, "写NFC失败"),
  ;

  public int code;
  public String msg;

  EnumErrCode(int code, String msg) {
    this.code = code;
    this.msg = msg;
  }
}
