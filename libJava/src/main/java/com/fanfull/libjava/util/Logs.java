package com.fanfull.libjava.util;

public final class Logs {
  public static void out(Object obj) {
    System.out.printf("%s (myLog-%s):%s\n", DateFormatUtil.getStringTime(),
        Thread.currentThread().getName(), obj);
  }

  public static void out(String format, Object... args) {
    out(String.format(format, args));
  }
}
