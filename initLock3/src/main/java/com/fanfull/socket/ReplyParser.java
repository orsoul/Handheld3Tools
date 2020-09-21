package com.fanfull.socket;

import android.os.SystemClock;
import com.fanfull.contexts.StaticString;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Keung
 * @ClassName: JudgeReplay
 * @Description: 判断回复信息
 * @date 2014-9-17 上午09:39:27
 */
public class ReplyParser {

  /**
   * 捆标签 的回复信息
   */
  private final static int REPLY_BARCODE = 77;
  public final static String SPLIT_FLAG = "#";

  private static boolean judgeReplyStoped;

  public ReplyParser() {

  }

  /**
   * @return StaticString.information != null
   * @Description 判断 后台是否已经回复
   */
  public static Boolean waitReply() {
    int count = 0;
    judgeReplyStoped = false;
    //        StaticString.information = null;
    while (StaticString.information == null) {
      synchronized (ReplyParser.class) {
        if (judgeReplyStoped) {
          return false;
        }
      }

      count++;
      if (80 <= count) {
        // LogUtils.s("judgeReplay" + count);
        return false;
      }
      SystemClock.sleep(50);
    }
    return true;
  }

  public static void stopWaitReply() {
    synchronized (ReplyParser.class) {
      judgeReplyStoped = true;
    }
  }

  public static String parseReply(String information) {
    int responseCode = -1;
    try {
      responseCode = Integer.valueOf(information.substring(1, 3));
    } catch (NumberFormatException ex) {
      ex.printStackTrace();
      return "服务器回复超时";
    } catch (NullPointerException ex) {
      ex.printStackTrace();
      return "服务器回复超时";
    }

    switch (responseCode) {
      // 捆封签 回复
      case REPLY_BARCODE:
        int code = -1;
        try {
          code = Integer.valueOf(information.substring(4, 6));
        } catch (NumberFormatException ex) {
          ex.printStackTrace();
          return "服务器回复失败!";
        }

        switch (code) {
          case 01:
            return "重复捆编号";
          case 03:
            return "后台服务器保存数据异常";
          default:
            return "动作失败";
        }
      case 02:
        int info2 = 90;
        try {
          info2 = Integer.valueOf(information.substring(4, 7));
          System.err.println("JudgeReplay+judgeScan+info2" + info2);
        } catch (NumberFormatException ex) {
          ex.printStackTrace();
        }
        switch (info2) {
          case 0:
            return "OK";
          case 1:
            return "条码不符，非法数据包";
          case 2:
            return "非法币值";
          case 3:
            return "当前没有打开的批，手持需要重新登陆(手持提示：没有打开的批)";
          case 5:
            return "电脑数据出错,保存手工处理过程信息失败";
          case 7:
            return "手持所在的批和当前打开的批不匹配";
          case 9:
            return "当前条码不允许更换";
          case 14:
            return "条码被非正常更换";
          case 16:
            return "重复扫描";
          case 19:
            return "批不允许再接收";
          case 20:
            return "数据包错误，包括上传的数据的长度不合法";
          case 21:
            return "任务不允许接收";
          case 22:
            return "封签条码不属于当前库";
          case 23:
            return "袋未锁定";
          case 26:
            return "该袋已经被手工处理";
          case 29:
            return "该袋已经被封签";
          case 31:
            return "该袋还没有被封签过";
          case 32:
            return "该袋已经被入库";
          case 33:
            return "该条码已经被标识为失效状态，不能进行扫描操作";
          case 34:
            return "该袋已经出库";
          case 35:
            return "该袋还未在当前库进行入库操作，请先入库，再出库";
          case 36:
            return "该袋已经执行过开袋";
          case 37:
            return "该袋尚未出库";
          case 39:
            return "当前是已清分的批。不允许接收未清分袋";
          case 40:
            return "当前是未清分的批。不允许接收已清分袋";
          case 80:
            return "操作成功";
          case 70:
            return "操作成功";
          default:
            return "动作失败";
        }
      case 27:
        int info27 = Integer.valueOf(information.substring(4, 6));
        switch (info27) {
          case 0:
            return "当前批不存在";
          case 1:
            return "锁定批成功";
          case 2:
            return "当前批已锁定";
          case 3:
            return "未达计划数量，不能手持结束批";
          case 5:
            return "电脑数据出错,保存手工处理过程信息失败";
          case 7:
            return "手持所在的批和当前打开的批不匹配";
          case 9:
            return "当前条码不允许更换";
          case 14:
            return "条码被非正常更换";
          case 16:
            return "重复扫描";
          case 19:
            return "批不允许再接收";
          case 20:
            return "数据包错误，包括上传的数据的长度不合法";
          case 21:
            return "任务不允许接收";
          case 22:
            return "封签条码不属于当前库";
          case 23:
            return "袋未锁定";
          case 26:
            return "该袋已经被手工处理";
          case 29:
            return "该袋已经被封签";
          case 31:
            return "该袋还没有被封签过";
          case 32:
            return "该袋已经被入库";
          case 33:
            return "该条码已经被标识为失效状态，不能进行扫描操作";
          case 34:
            return "该袋已经出库";
          case 35:
            return "该袋还未在当前库进行入库操作，请先入库，再出库";
          case 36:
            return "该袋已经执行过开袋";
          case 37:
            return "该袋尚未出库";
          case 39:
            return "当前是已清分的批。不允许接收未清分袋";
          case 40:
            return "当前是未清分的批。不允许接收已清分袋";
          default:
            return "动作失败";
        }
      case 40:
        int info40 = Integer.valueOf(information.substring(4, 6));
        switch (info40) {
          case 0:
            return "生成袋流转信息出错";
          case 1:
            return "开袋成功";
          case 2:
            return "当前批的袋扫描信息时出错";
          case 3:
            return "提取袋流转过程信息时出错";
          case 4:
            return "该袋已经开袋过";
          case 5:
            return "生成袋流转信息失败";
          case 6:
            return "更新批信息失败";
          case 7:
            return "保存袋信息失败";
          case 8:
            return "验证用户身份,无效";
          case 9:
            return "其他异常";
          default:
            return "动作失败";
        }
      case 26:
        int info26 = Integer.valueOf(information.substring(4, 6));
        switch (info26) {
          case 0:
            return "可以换袋";
          case 9:
            return "系统没准备好更换程序";
          case 8:
            return "获取换袋所需的信息出现异常";
          default:
            return "动作失败";
        }
    }
    return "动作失败";
  }

  /**
   * @description 出入库时, 将 后台回复 的任务列表封装成 list
   */
  public static List<String> parse2List(String information) {

    List<String> list = new ArrayList<String>();
    if (null == information) {
      return list;
    }

    int start = 7;
    int end = information.lastIndexOf(' ');
    String[] infos = information.substring(start, end).split(" ");
    for (String info : infos) {
      String msg = parse2String(info);
      list.add(msg);
    }
    return list;
  }

  private static String parse2String(String info) {
    // 数量
    String sum = info.substring(info.length() - 6, info.length() - 2);
    sum = String.valueOf(Integer.parseInt(sum));
    System.out.println("number:" + info);
    System.out.println("sum:" + sum);

    // 判断完整
    String complettion;
    if (info.charAt(info.length() - 2) == '1') {
      complettion = "完整";
    } else {
      complettion = "残损";
    }

    // 判断清分
    String distribution;
    if (info.charAt(info.length() - 1) == '1') {
      distribution = "清分";
    } else {
      distribution = "未清分";
    }

    // 银行名称
    String bankName = null;
    int bankCode = Integer.parseInt(info.substring(4, 6));
    if ((bankCode == 01) && (info.charAt(8) == '1')) {
      bankName = "人民银行一科";
    } else if ((bankCode == 01) && (info.charAt(8) == '2')) {
      bankName = "人民银行二科";
    } else if ((bankCode == 01) && (info.charAt(8) == '3')) {
      bankName = "人民银行清分";
    } else if (bankCode == 02) {
      bankName = "建设银行";
    } else if (bankCode == 03) {
      bankName = "农业银行";
    } else if (bankCode == 04) {
      bankName = "工商银行";
    } else if (bankCode == 05) {
      bankName = "中信银行";
    } else if (bankCode == 06) {
      bankName = "交通银行";
    } else if (bankCode == 07) {
      bankName = "招商银行";
    } else if (bankCode == 8) {
      bankName = "深发银行";
    } else if (bankCode == 9) {
      bankName = "广发银行";
    } else if (bankCode == 10) {
      bankName = "浦发银行";
    } else if (bankCode == 11) {
      bankName = "民生银行";
    } else if (bankCode == 12) {
      bankName = "华夏银行";
    } else if (bankCode == 13) {
      bankName = "邮政银行";
    } else if (bankCode == 14) {
      bankName = "光大银行";
    } else if (bankCode == 15) {
      bankName = "中信银行";
    } else if (bankCode == 16) {
      bankName = "兴业银行";
    } else if (bankCode == 17) {
      bankName = "开发银行";
    } else if (bankCode == 20) {
      bankName = "农业商业银行";
    } else if (bankCode == 21) {
      bankName = "农村信用社";
    } else if (bankCode == 22) {
      bankName = "平安银行";
    } else if (bankCode == 23) {
      bankName = "上海银行";
    } else if (bankCode == 24) {
      bankName = "浙商银行";
    } else if (bankCode == 40) {
      bankName = "成都银行";
    } else if (bankCode == 41) {
      bankName = "曲商银行";
    } else if (bankCode == 42) {
      bankName = "玉商银行";
    } else if (bankCode == 43) {
      bankName = "楚村银行";
    } else if (bankCode == 44) {
      bankName = "丽富银行";
    } else if (bankCode == 45) {
      bankName = "稠州银行";
    } else {
      bankName = "其它地方";
    }
    return bankName + SPLIT_FLAG + sum + "袋   " + complettion + "/" + distribution;
  }

  /*
   * public String judgeLogin(String information) { int info =
   * Integer.valueOf(information.substring(4, 6)); switch (info) { case 0:
   * return "无效用户名/密码"; case 2: return "校验错误重新登陆"; default: break; } return
   * null; }
   */
}
