package org.orsoul.baselib.util;

import java.util.Calendar;

public abstract class DateUtil {
  public static void main(String[] args) {

  }

  /**
   * 获取 day 天 前 零点零分零秒 时的时间
   */
  private static long getTimeInMillisBefore(int day) {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.add(Calendar.DAY_OF_YEAR, -day);
    return cal.getTimeInMillis();
  }
}
