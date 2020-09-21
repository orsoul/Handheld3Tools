package com.fanfull.contexts;

/**
 * @author Administrator
 * <p>
 * app 中常用的 常量
 */
public interface MyContexts {
  /**
   * sharedpreference配置文件名
   */
  String PREFERENCE_NAME = "Cniao_Pref_Common"; // sharedpreference配置文件名
  String DB_NAME = "init_db";
  /**
   * ip 配置文件的 文件名
   */
  String IP_CONFIG = "config.xml"; // ip 配置文件的 文件名
  /**
   * ip
   */
  String Key_IP1 = "Key_IP1";
  /**
   * 端口
   */
  String KEY_PORT1 = "KEY_PORT1"; //
  /**
   * ip
   */
  String Key_IP2 = "Key_IP2";
  /**
   * 端口
   */
  String KEY_PORT2 = "KEY_PORT2"; //

  String KEY_LAST_DIQU = "KEY_LAST_DIQU"; //保存上一次选择的地区
  String KEY_LAST_CAN = "KEY_LAST_CAN"; //保存上一次选择的残损
  String KEY_LAST_BAG_TYPE = "KEY_LAST_BAG_TYPE"; //保存上一次选择的袋类型
  String KEY_LAST_INIT_NUMBER = "KEY_LAST_INIT_NUMBER";

  String KEY_LAST_ZX_NUMBER = "KEY_LAST_ZX_NUMBER";
  /**
   * 超高频卡 默认 读功率
   */
  String KEY_NORMAL_READ_POWER = "KEY_NORMAL_READ_POWER";
  /**
   * 超高频卡 默认 写功率
   */
  String KEY_NORMAL_WRITE_POWER = "KEY_NORMAL_WRITE_POWER";
  /**
   * 超高频卡 动态 读功率
   */
  String KEY_DYNAMIC_READ_POWER = "KEY_DYNAMIC_READ_POWER";
  /**
   * 超高频卡 动态 写功率
   */
  String KEY_DYNAMIC_WRITE_POWER = "KEY_DYNAMIC_WRITE_POWER";

  /**
   * 指纹开关
   */
  String KEY_FINGER_PRINT_ENABLE = "KEY_FINGERPRINT_ENABLE";

  String IS_LAST_PI_NUMBER = "IS_LAST_PI_NUMBER";
  String IS_LAST_TYPE_NUMBER = "IS_LAST_TYPE_NUMBER";

  /*对话框 提示信息*/
  String TEXT_DIALOG_TITLE = "提示"; //
  String DIALOG_MESSAGE_SCANED = "该捆已被扫描过,或券别有误!"; //
  String DIALOG_MESSAGE_BARCODE_WRONG = "封签数据错误!"; //
  String DIALOG_MESSAGE_QUIT = "是否退出当前页面?"; //
  String DIALOG_MESSAGE_EPC_NO_MATCH = "锁片还未插上，请插上锁片后再操作！"; //
  String DIALOG_MESSAGE_LOCK_BUNCH = "您确定要锁定批?"; //

  String ACTION_EXIT_APP = "ACTION_EXIT_APP";
  /**
   * 确认
   */
  String TEXT_OK = "确认";
  /**
   * 取消
   */
  String TEXT_CANCEL = "取消";

  String LOCK_PI_SUCCESS = "锁定批成功";
  String BAG_LOACK_NOT_INIT = "袋锁尚未初始化";
  String BAG_HAD_INIT = "重复初始化基金袋！";
}
