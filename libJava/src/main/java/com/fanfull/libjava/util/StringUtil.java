package com.fanfull.libjava.util;

import java.util.Collection;
import java.util.Map;

public final class StringUtil {
  public static String getFullStringFromList(Map<String, String> map57, Map<String, String> map58,
      String subString) {
    for (String fullString : map57.keySet()) {
      if (fullString.contains(subString)) {
        return fullString;
      }
    }
    for (String fullString : map58.keySet()) {
      if (fullString.contains(subString)) {
        return fullString;
      }
    }
    return null;
  }

  /**
   * 根据 子字符串，从list中取得 包含 子串 的完整字符串.
   *
   * @param list 完整字符的 list
   * @param subString 子字符串
   * @return 无包含子串的完整字符串 返回null,否则返回 完整字符串.
   */
  public static String getFullStringFromList(Collection<String> list, String subString) {
    if (list == null || list.isEmpty() || subString == null) {
      return null;
    }

    for (String fullString : list) {
      if (fullString.contains(subString)) {
        return fullString;
      }
    }
    return null;
  }
}
