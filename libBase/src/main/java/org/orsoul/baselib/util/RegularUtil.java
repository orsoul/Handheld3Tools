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
    /** 正则表达式，匹配小于或等于255的数字串. */
    public static final String REGEX_255 = "(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)";
    /** 正则表达式，匹配IP，出现沉冗的0或大于255为非法，如：出现00、01、023、255均匹配失败，192.168.1.1通过 */
    public static final String REGEX_IP =
        REGEX_255 + "[.]" + REGEX_255 + "[.]" + REGEX_255 + "[.]" + REGEX_255;
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
    String ip = "192.168.18.10";
    System.out.printf("ip:%s - %s\n", ip, matchIP(ip));
    String res = "(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)";
    System.out.println(isMatch(res, "168"));
    System.out.println(isMatch(res, "255"));
    System.out.println(isMatch(res, "0"));
    System.out.println(isMatch(res, "256"));
    System.out.println(isMatch(res, "01"));
    String reg_ip = String.format("^(%s)[.](%s)[.](%s)[.](%s)$", res, res, res, res);
    System.out.println(isMatch(reg_ip, ip));
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
