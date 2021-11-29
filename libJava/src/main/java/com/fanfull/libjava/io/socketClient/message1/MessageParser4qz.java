package com.fanfull.libjava.io.socketClient.message1;

import com.fanfull.libjava.util.Logs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MessageParser4qz {
  /**
   * 检查并分段接收到的字符串，必须包含头尾标识符.会截去头尾标识符。
   *
   * @return 第一段和最后一段必须是数字串，分段成功返回长度至少为3的字符串数组，否则返回null.
   */
  public static String[] splitRecInfoWithHead(String recString) {
    if (!checkRec(recString)) {
      //LogUtils.w("split failed:%s", recString);
      return null;
    }
    String[] split =
        recString.substring(1, recString.length() - 1).split(BaseSocketMessage4qz.CH_SPLIT);
    if (split.length < 3) {
      return null;
    }
    return split;
  }

  /**
   * 检查并分段接收到的字符串，必须不包含头尾标识符.
   *
   * @return 第一段和最后一段必须是数字串，分段成功返回长度至少为3的字符串数组，否则返回null.
   */
  public static String[] splitRecInfoWithoutHead(String recString) {
    if (recString == null
        || !recString.matches("^\\d+ .+ \\d+$")) {
      //LogUtils.w("split failed:%s", recString);
      return null;
    }
    String[] split = recString.split(BaseSocketMessage4qz.CH_SPLIT);
    if (split.length < 3) {
      return null;
    }
    return split;
  }

  /**
   * 检查并分段接收的字符串，有无 头尾标识符 皆可.
   *
   * @return 第一段和最后一段必须是数字串，分段成功返回长度至少为3的字符串数组，否则返回null.
   */
  public static String[] splitRecInfo(String recString) {
    String[] split = splitRecInfoWithoutHead(recString);
    split = split != null ? split : splitRecInfoWithHead(recString);
    if (split != null) {
      split = replaceSpChar(split);
    }
    return split;
  }

  /** 格式错误返回null；否则返回分割后的字符串数组，去掉头尾的 *#. */
  public static String[] splitRec(String recString) {
    if (!MessageParser4qz.checkRec(recString)) {
      return null;
    }
    String[] split =
        recString.substring(1, recString.length() - 1).split(BaseSocketMessage4qz.CH_SPLIT);
    return MessageParser4qz.replaceSpChar(split);
  }

  public static boolean checkSplit(String[] split) {
    if (split == null || split.length < 3) {
      return false;
    } else {
      return true;
    }
  }

  public static boolean checkRec(String rec) {
    return rec != null && rec.matches("^\\*\\d+ .+ \\d+#$");
  }

  /**
   * 生成通讯指令.无需包含 $、# 头尾标识
   *
   * @param args 至少有3个参数，为null的参数会被忽略
   */
  public static String genProtocol(Object... args) {
    if (args == null || args.length < 3) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    sb.append(BaseSocketMessage4qz.CH_HEAD_SEND); // #
    for (int i = 0; i < args.length; i++) {
      if (args[i] != null) {
        sb.append(args[i]).append(BaseSocketMessage4qz.CH_SPLIT);
      }
    }
    sb.setLength(sb.length() - 1);
    sb.append(BaseSocketMessage4qz.CH_END);
    return sb.toString();
  }

  public static String[] replaceSpChar(String... source) {
    if (source == null || source.length == 0) {
      return source;
    }
    String[] newStr = new String[source.length];
    for (int i = 0; i < source.length; i++) {
      newStr[i] = replaceSpChar(source[i]);
    }
    return newStr;
  }

  public static String replaceSpChar(String source) {
    if (source == null || source.equals("")) {
      return source;
    }
    if (source.contains(BaseSocketMessage4qz.SP_HEAD_SEND)) {
      source =
          source.replaceAll(BaseSocketMessage4qz.SP_HEAD_SEND, BaseSocketMessage4qz.CH_HEAD_SEND);
    }
    if (source.contains(BaseSocketMessage4qz.SP_SPACE)) {
      source = source.replaceAll(BaseSocketMessage4qz.SP_SPACE, BaseSocketMessage4qz.CH_SPLIT);
    }
    if (source.contains(BaseSocketMessage4qz.SP_END)) {
      source = source.replaceAll(BaseSocketMessage4qz.SP_END, BaseSocketMessage4qz.CH_END);
    }
    if (source.contains(BaseSocketMessage4qz.SP_HEAD_REC)) {
      source =
          source.replaceAll(BaseSocketMessage4qz.SP_HEAD_REC, BaseSocketMessage4qz.CH_HEAD_REC);
    }
    return source;
  }

  static final int HEAD_REC = '*';
  static final int TAIL_REC = '#';
  /** 前半部分 指令 */
  static byte[] firstHalfMsg;

  /**
   * 粘包、分包处理
   */
  public static List<byte[]> slice(byte[] recData, int len) {

    List<byte[]> list = new ArrayList<>();

    /* 遍历接收数据，定位协议头、尾 */
    int headIndex = -1;
    int endIndex = -1;
    for (int i = 0; i < len; i++) {

      if (headIndex < 0 && HEAD_REC == recData[i]) {
        // 定位协议头
        headIndex = i;
      } else if (endIndex < 0 && TAIL_REC == recData[i]) {
        // 定位协议尾
        endIndex = i;

        if (headIndex < 0) {
          // 先出现 协议尾
          if (firstHalfMsg != null) {
            // 后半部分 与 前半部分 拼完整包
            byte[] oneMsg = new byte[firstHalfMsg.length + endIndex + 1];
            System.arraycopy(firstHalfMsg, 0, oneMsg, 0, firstHalfMsg.length);
            System.arraycopy(recData, 0, oneMsg, firstHalfMsg.length, endIndex + 1);
            list.add(oneMsg);
            firstHalfMsg = null;
          }
          endIndex = -1;
          continue;
        }
      }

      if (0 <= headIndex) {
        if (headIndex < endIndex) {
          // 遍历过程发现 完整指令，切出完整指令 继续遍历
          byte[] oneMsg = Arrays.copyOfRange(recData, headIndex, endIndex + 1);
          list.add(oneMsg);
          headIndex = -1;
          endIndex = -1;
          firstHalfMsg = null;
        }
      }
    } // end for

    // 遍历完
    if (0 <= headIndex && endIndex < 0) {
      // 只出现 协议头，记录 前半部分数据
      firstHalfMsg = Arrays.copyOfRange(recData, headIndex, len);
    } else if (firstHalfMsg != null && headIndex < 0 && endIndex < 0) {
      // 未出现协议头、协议尾，将数据 并入 前半部分数据
      byte[] tmp = new byte[firstHalfMsg.length + len];
      System.arraycopy(firstHalfMsg, 0, tmp, 0, firstHalfMsg.length);
      System.arraycopy(recData, 0, tmp, firstHalfMsg.length, len);
      firstHalfMsg = tmp;
    }
    return list;
  }

  static void testSlice(List<String> recList) {
    Logs.out("==== 测试数据接收数据量 %s ====", recList.size());
    for (String msg : recList) {
      Logs.out("接收数据:%s", msg);
      byte[] recData = msg.getBytes();
      List<byte[]> sliceList = slice(recData, recData.length);
      if (!sliceList.isEmpty()) {
        Logs.out("== 解析出指令数 %s ==", sliceList.size());
        for (int i = 0; i < sliceList.size(); i++) {
          Logs.out("%s:%s", i, new String(sliceList.get(i)));
        }
      }
    }
  }

  public static void main(String[] args) {
    List<String> recList = new ArrayList<>();

    // 1条 完整
    System.out.println("\n1条 完整");
    recList.add("*0123456789#");
    testSlice(recList);
    recList.clear();

    // 1条 垃圾
    System.out.println("\n1条 垃圾");
    recList.add("0123456789");
    testSlice(recList);
    recList.clear();

    // 1条 垃圾 半包
    System.out.println("\n1条 垃圾 半包");
    recList.add("0123456789#");
    testSlice(recList);
    recList.clear();

    // 1条： 2个半包1
    System.out.println("\n1条： 2个半包1");
    recList.add("*01234");
    recList.add("56789#");
    testSlice(recList);
    recList.clear();

    // 1条： 3个半包1
    System.out.println("\n1条： 3个半包1");
    recList.add("*012");
    recList.add("34");
    recList.add("56789#");
    testSlice(recList);
    recList.clear();

    // 1条： 4个半包1
    System.out.println("\n1条： 4个半包1");
    recList.add("*012");
    recList.add("34");
    recList.add("567");
    recList.add("89#");
    testSlice(recList);
    recList.clear();

    System.out.println("\n1条： 垃圾 + 完整");
    recList.add("012*3456789#");
    testSlice(recList);
    recList.clear();

    System.out.println("\n1条： 垃圾 | 【半包 | [完整] | 半包】");
    recList.add("aaaaaaa");
    recList.add("*012");
    recList.add("*34#");
    recList.add("321#");
    testSlice(recList);
    recList.clear();

    System.out.println("\n2条： 完整");
    recList.add("*01234#*56789#");
    testSlice(recList);
    recList.clear();

    System.out.println("\n3条： 完整");
    recList.add("*012#*3456#*789#");
    testSlice(recList);
    recList.clear();

    System.out.println("\n2条： 垃圾 + [完整] + 垃圾 + [完整] + 垃圾");
    recList.add("qwe#*3456#asd*789#rtwe");
    testSlice(recList);
    recList.clear();

    System.out.println("\n4条： 【半包 | 半包】 + [完整] + 垃圾 + [完整] + 【半包 | 半包】");
    recList.add("*01");
    recList.add("34#*56#asd*78#*9");
    recList.add("87654321#");
    testSlice(recList);
    recList.clear();

    System.out.println("\n3条：垃圾 | [完整] | 垃圾 | [完整] | 垃圾 | [完整]");
    recList.add("aaaaaa");
    recList.add("*0123#");
    recList.add("bbbbbb");
    recList.add("*456#");
    recList.add("cccccc");
    recList.add("*789#");
    testSlice(recList);
    recList.clear();
  }
}
