package com.fanfull.contexts;

public class StaticString {

  public static final String IP = "192.168.18.100";
  public static final int PORT = 10001; //
  /**
   * 青岛第二道门 ip
   */
  public static String IP_SECOND = "192.168.18.111";
  /**
   * 青岛第二道门 端口
   */
  public static int PORT_SECOND = 10001; //
  /**
   * 青岛第三道门 ip
   */
  public static String IP_THIRD = "192.168.18.111";
  /**
   * 青岛第三道门 端口
   */
  public static int PORT_THIRD = 10001; //

  public static Boolean netThread_flag = true;

  public static Boolean isOpenSmallScreen = true;
  /**
   * 保存 从服务器 返回的 信息
   */
  public static String information;//返回信息

  public static boolean isDialog = true;
  public static String RFIDCard = "68B5B88A";  //卡号
  //    public static SoundPool soundPool=null;
  public static int TOAST_SHOW_TIME = 2000;
  /**
   * 操作类型:
   * 1 封袋
   * 2 入库
   */
  public static String TYPE = "operation_type";

  public static String password = null;// 密码
  public static String tasknumber = null;// 任务编号
  public static String pinumber = "0000000000000000000000";// 批编号
  public static String barcode = null;// 条码
  public static String bagbarcode = null;// 袋中的条码
  public static String reason = null;// 换袋原因
  public static String bagid = null;// 袋子ID
  public static String tid = null;// 封签码
  public static String bagtidcode = null;// 封签事件码
  public static String barcode_old = null;// 旧条码
  public static String bagtype = null;// 袋类型，记录封签的时候选择的完整，清分，复点等信息

  public static String SCAN_OK = null;// 扫捆成功
  public static int YK_TOTAL_NUM = 0;
  public static int YK_PART_NUM = 0;
  public static int PRE_SCAN_NUM = 0;

  public static byte COVER_FLAG_1 = (byte) 0x23;
  public static byte COVER_FLAG_2 = (byte) 0x5f;
  public static byte COVER_FLAG_3 = (byte) 0xe8;
  public static byte COVER_FLAG_4 = (byte) 0x41;
}
