package org.orsoul.baselib.util;

import java.util.regex.Pattern;

/**
 * 常用正则表达式
 *
 * @author orsoul
 */
public final class RegularUtil {

  public interface RegexConstants {
    /** 正则表达式，匹配10进制数字串 */
    public static final String REGEX_INTEGER_10 = "^[-+]?\\d+$";
    /** 正则表达式，匹配16进制数字串 */
    public static final String REGEX_INTEGER_16 = "^(0X|0x)?[0-9a-fA-F]+$";
    /** 正则表达式，匹配IP，出现沉冗的0或大于255为非法，如：出现00、01、023、255均匹配失败，192.168.1.1通过 */
    public static final String REGEX_IP =
        "^(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])[.]\\1[.]\\1[.]\\1$";
    /** 正则表达式，匹配网址 */
    public static final String REGEX_WEBSITE =
        "(ht|f)tp(s?)\\:\\/\\/[0-9a-zA-Z]([-.\\w]*[0-9a-zA-Z])*(:(0-9)*)*(\\/?)([a-zA-Z0-9\\-\\.\\?\\,\\'\\/\\\\\\+&amp;%\\$#_]*)?";
    /** 正则表达式，匹配中文字符 */
    public static final String REGEX_CN_WORD = "[\u4e00-\u9fa5]";
  }

  public static void main(String[] args) {
    //System.out.println(isMatch("(.{2})*", "12a67"));
    //System.out.println(isMatch("(.{2})*", "12a672"));
    //System.out.println(isMatch("(.{2})*", "12"));
    System.out.println(matchDecimalString("+123"));
    //System.out.println(matchDecimalString("-123"));
    //System.out.println(matchDecimalString("0123"));
    System.out.println(isMatch(RegexConstants.REGEX_INTEGER_16, "0x0X12aF7"));
    System.out.println(isMatch(RegexConstants.REGEX_INTEGER_16, "0x12aF7"));
    System.out.println(isMatch(RegexConstants.REGEX_INTEGER_16, "0X12aF7"));
    System.out.println(isMatch(RegexConstants.REGEX_INTEGER_16, "12aF7g"));
    //    System.out.println(isMatch(RegexConstants.REGEX_INTEGER_16, null));
  }

  public static boolean isMatch(final String regex, final CharSequence input) {
    return input != null && input.length() > 0 && Pattern.matches(regex, input);
  }

  /** 匹配IP，出现沉冗的0或大于255为非法，如：出现00、01、023、255均匹配失败，192.168.1.1通过 */
  public static boolean matchIP(CharSequence input) {
    return isMatch(RegexConstants.REGEX_IP, input);
  }

  /** 匹配网址 */
  public static boolean matchWebSite(CharSequence input) {
    return isMatch(RegexConstants.REGEX_WEBSITE, input);
  }

  /** 匹配中文字符 */
  public static boolean matchCnWord(CharSequence input) {
    return isMatch(RegexConstants.REGEX_CN_WORD, input);
  }

  /** 匹配16进制字符串 */
  public static boolean matchHexString(CharSequence input) {
    return isMatch(RegexConstants.REGEX_INTEGER_16, input);
  }

  /** 匹配10进制字符串 */
  public static boolean matchDecimalString(CharSequence input) {
    return isMatch(RegexConstants.REGEX_INTEGER_10, input);
  }
}
