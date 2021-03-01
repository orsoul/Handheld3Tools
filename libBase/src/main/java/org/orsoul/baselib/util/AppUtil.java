package org.orsoul.baselib.util;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;

import com.blankj.utilcode.util.Utils;

import java.util.List;

public final class AppUtil {

  public static void killProcess() {
    android.os.Process.killProcess(android.os.Process.myPid());
    System.exit(0);
  }

  /**
   * 关闭所有activity.
   *
   * @param killProcess true 关闭所有activity后杀死当前进程.
   */
  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public static void exitAPP(boolean killProcess) {
    ActivityManager activityManager =
        (ActivityManager) Utils.getApp().getApplicationContext().getSystemService(
            Context.ACTIVITY_SERVICE);
    List<ActivityManager.AppTask> appTaskList = activityManager.getAppTasks();
    for (ActivityManager.AppTask appTask : appTaskList) {
      //LogUtils.d(":%s",appTask.si);
      appTask.finishAndRemoveTask();
    }
    //        appTaskList.get(0).finishAndRemoveTask();
    if (killProcess) {
      android.os.Process.killProcess(android.os.Process.myPid());
      System.exit(0);
    }
  }
}
