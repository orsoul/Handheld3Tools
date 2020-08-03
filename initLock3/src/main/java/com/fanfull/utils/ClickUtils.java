package com.fanfull.utils;

import android.content.Context;

public class ClickUtils {
  private static long time;
  private static long lastClickTime;

  /**
   * @param ctr 在给定 gas 内, 执行 ClickTwiceRunnable.intimeToDo(),
   * 超出 gas ,执行 ClickTwiceRunnable.outtimeToDo()
   * @param gas 两次 点击的时间隔
   */
  public static void clickTwiceToDo(ClickTwiceRunnable ctr, long gas) {
    if (System.currentTimeMillis() - time < gas) {
      ctr.intimeToDo();
    } else {
      ctr.outtimeToDo();
    }
    time = System.currentTimeMillis();
  }

  /**
   *
   */
  public static void clickTwiceToDo(ClickTwiceRunnable ctr) {
    clickTwiceToDo(ctr, 2500);
  }

  public interface ClickTwiceRunnable {
    /**
     * 在时间隔内 执行此方法
     */
    void intimeToDo();

    /**
     * 超过时间隔执行 此方法
     */
    void outtimeToDo();
  }

  protected Context getApplicationContext() {

    return null;
  }

  // 2次点击的间隔必须500ms以上才可以
  public static boolean isFastDoubleClick() {

    long time = System.currentTimeMillis();
    long timeD = time - lastClickTime;
    if (0 < timeD && timeD < 500) {
      return false;
    }
    lastClickTime = time;
    return true;
  }
}
