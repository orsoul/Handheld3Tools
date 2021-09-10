package com.fanfull.libjava.util;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * SimpleDateFormat 的格式定义:EEEE代表星期 MMMM代表中文月份 格式为
 * 
 * Letter  Date or Time Component  Presentation  Examples
 G  Era designator  Text  AD
 y  Year  Year  1996; 96
 Y  Week year  Year  2009; 09
 M  Month in year (context sensitive)  Month  July; Jul; 07
 L  Month in year (standalone form)  Month  July; Jul; 07
 w  Week in year  Number  27
 W  Week in month  Number  2
 D  Day in year  Number  189
 d  Day in month  Number  10
 F  Day of week in month  Number  2
 E  Day name in week  Text  Tuesday; Tue
 u  Day number of week (1 = Monday, ..., 7 = Sunday)  Number  1
 a  Am/pm marker  Text  PM
 H  Hour in day (0-23)  Number  0
 k  Hour in day (1-24)  Number  24
 K  Hour in am/pm (0-11)  Number  0
 h  Hour in am/pm (1-12)  Number  12
 m  Minute in hour  Number  30
 s  Second in minute  Number  55
 S  Millisecond  Number  978
 z  Time zone  General time zone  Pacific Standard Time; PST; GMT-08:00
 Z  Time zone  RFC 822 time zone  -0800
 X  Time zone  ISO 8601 time zone  -08; -0800; -08:00
 */

/**
 * @author orsoul
 * @常用格式: "yyyy-MM-dd HH:mm:ss"
 */
public final class DateFormatUtil {

  /** 默认 时间的 字符串格式 */
  public static final String FORMAT_NORMAL = "yyyy-MM-dd HH:mm:ss";
  public final static String FORMAT_yyyyMMddHHmmss = "yyyyMMddHHmmss";
  /** MM-dd */
  public static final String FORMAT_DATE = "MM-dd";
  /** HH:mm:ss */
  public static final String FORMAT_TIME = "HH:mm:ss";

  /**
   * @param date 指定时间, 为null表示当前时间
   * @param format 时间格式,例如:yyyy-MM-dd HH:mm:ss
   * @return 指定时间的 字符串形式; 若 format 格式非法, 返回 null
   */
  public static String getStringTime(Date date, String format) {
    DateFormat formatter = null;
    try {
      formatter = new SimpleDateFormat(format);
      // formatter = new SimpleDateFormat(format, Locale.CHINA);
    } catch (Exception e) {
      // format 格式非法
      return null;
    }
    if (null == date) {
      date = new Date();
    }
    String dateString = formatter.format(date);
    return dateString;
  }

  /**
   * @param milliseconds 指定时间
   * @param format 时间格式,例如:yyyy-MM-dd HH:mm:ss
   * @return 指定时间的 字符串形式; 若 format 格式非法, 返回 null
   */
  public static String getStringTime(long milliseconds, String format) {
    Date date = new Date(milliseconds);
    return getStringTime(date, format);
  }

  /**
   * 时间格式 转换.
   *
   * @param strTime 源时间
   * @param srcFormat 源时间 格式
   * @param desFormat 目标格式
   * @return 格式非法返回 null
   */
  public static String getStringTime(String strTime, String srcFormat, String desFormat) {
    Date date = parseString2Date(strTime, srcFormat);
    return getStringTime(date, desFormat);
  }

  /**
   * 时间格式 转换成：yyyy-MM-dd HH:mm:ss.
   *
   * @param strTime 源时间
   * @param srcFormat 源时间 格式
   * @return 格式非法返回 null
   */
  public static String getStringTime(String strTime, String srcFormat) {
    Date date = parseString2Date(strTime, srcFormat);
    return getStringTime(date, FORMAT_NORMAL);
  }

  /**
   * @return date的字符串形式: yyyy-MM-dd HH:mm:ss
   */
  public static String getStringTime(Date date) {
    return getStringTime(date, FORMAT_NORMAL);
  }

  /**
   * @return date的字符串形式: yyyy-MM-dd HH:mm:ss
   */
  public static String getStringTime(long date) {
    return getStringTime(date, FORMAT_NORMAL);
  }

  /**
   * @return 当前时间
   */
  public static String getStringTime(String format) {
    return getStringTime(System.currentTimeMillis(), format);
  }

  /**
   * @return 当前时间的默认字符串形式: yyyy-MM-dd HH:mm:ss
   */
  public static String getStringTime() {
    return getStringTime(FORMAT_NORMAL);
  }

  /**
   * @param strTime 字符串的形式的时间,如:2000-05-30 16:25:55
   * @param format 时间的字符串形式,如:yyyy-MM-dd HH:mm:ss
   * @return Date
   */
  public static Date parseString2Date(String strTime, String format) {
    SimpleDateFormat formatter = new SimpleDateFormat(format);
    // 从 第0个字符开始解析
    ParsePosition pos = new ParsePosition(0);
    Date date = formatter.parse(strTime, pos);
    return date;
  }

  /**
   * @param strTime 默认格式的时间:2000-05-30 16:25:55
   * @return Date
   * @des 默认格式:yyyy-MM-dd HH:mm:ss
   */
  public static Date parseString2Date(String strTime) {
    return parseString2Date(strTime, FORMAT_NORMAL);
  }

  /**
   * 将字符串形式的时间 转成 milliseconds
   *
   * @param strTime 字符串的形式的时间,如:2000-05-30 16:25:55
   * @param format 时间的字符串形式,如:yyyy-MM-dd HH:mm:ss
   * @return milliseconds
   */
  public static long parseString2Long(String strTime, String format) {
    return parseString2Date(strTime, format).getTime();
  }

  /**
   * 将字符串形式的时间 转成 milliseconds
   *
   * @param strTime 字符串的形式的时间,如:2000-05-30 16:25:55
   * @return milliseconds
   */
  public static long parseString2Long(String strTime) {
    return parseString2Long(strTime, FORMAT_NORMAL);
  }

  public static void main(String[] args) {
    new DateFormatUtil().test();
  }

  void test() {
    String dateString = "2021-01-01";
    Date date = parseString2Date(dateString, "yyyy-MM-dd");
    System.out.printf("%s\n", date);

    String stringTime = getStringTime(date.getTime(), FORMAT_NORMAL);
    System.out.printf("%s\n", stringTime);

    Logs.out("%s", getStringTime(System.currentTimeMillis() - 2 * 60 * 60 * 1000));
  }
}
