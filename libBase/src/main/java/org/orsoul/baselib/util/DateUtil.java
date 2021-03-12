package org.orsoul.baselib.util;

import java.util.Calendar;

public final class DateUtil {
  /**
   * 获取 days 天前 此时的时间
   */
  public static long getTimeInMillisBefore(long baseTime, int days) {
    Calendar cal = Calendar.getInstance();
    if (0 < baseTime) {
      cal.setTimeInMillis(baseTime);
    }
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.add(Calendar.DAY_OF_YEAR, -days);
    return cal.getTimeInMillis();
  }

  public static long getTimeInMillisBefore(int days) {
    return getTimeInMillisBefore(-1, days);
  }

  public static long getMillisBetween(long startTime, long endTime) {
    return endTime - startTime;
  }

  public static long getMillisBetween(String startDate, String endDate, String format) {
    long start = DateFormatUtil.parseString2Long(startDate, format);
    long end = DateFormatUtil.parseString2Long(endDate, format);
    return getMillisBetween(start, end);
  }

  public static long getMillisBetween(String startDate, long endDate, String format) {
    long start = DateFormatUtil.parseString2Long(startDate, format);
    return getMillisBetween(start, endDate);
  }

  public static long getMillisBetween(long startDate, String endDate, String format) {
    long end = DateFormatUtil.parseString2Long(endDate, format);
    return getMillisBetween(startDate, end);
  }

  public static void main(String[] args) {
    new DateUtil().test();
  }

  void test() {
    String startDate = "2021-01-01";
    String endDate = "2022-01-01";
    long millisBetween = getMillisBetween(startDate, endDate, "yyyy-MM-dd");
    System.out.printf("%s\n", millisBetween);

    long end = System.currentTimeMillis();
    String current = DateFormatUtil.getStringTime(end);
    millisBetween = getMillisBetween(startDate, end, "yyyy-MM-dd");
    System.out.printf("Between:%s\n", millisBetween);
    long start = DateFormatUtil.parseString2Long(startDate, "yyyy-MM-dd");
    System.out.printf("start:%s,Between:%s\n", start, millisBetween);
    System.out.printf("current:%s, get:%s\n", current,
        DateFormatUtil.getStringTime(start + millisBetween));
  }
}
