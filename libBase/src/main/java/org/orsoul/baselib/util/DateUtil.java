package org.orsoul.baselib.util;

import java.util.Calendar;

public abstract class DateUtil {
  public static void main(String[] args) {

  }

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
}
