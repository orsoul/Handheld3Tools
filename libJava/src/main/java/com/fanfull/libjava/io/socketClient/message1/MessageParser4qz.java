package com.fanfull.libjava.io.socketClient.message1;

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
}
