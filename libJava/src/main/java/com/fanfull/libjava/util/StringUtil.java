package com.fanfull.libjava.util;

import java.util.Collection;

public final class StringUtil {
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
