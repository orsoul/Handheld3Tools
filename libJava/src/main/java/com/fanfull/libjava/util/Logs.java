package com.fanfull.libjava.util;

public final class Logs {
  private static String timeFormat = "yyyy-MM-dd HH:mm:ss.SSS";

  public static void out(Object obj) {
    System.out.printf("%s (myLog-%s):%s\n", DateFormatUtil.getStringTime(timeFormat),
        Thread.currentThread().getName(), obj);
  }

  public static void out(String format, Object... args) {
    out(String.format(format, args));
  }

  public static void main(String[] args) {
    for (int i = 0; i < 20; i++) {
      Logs.out("%s", i);
    }
  }
}
