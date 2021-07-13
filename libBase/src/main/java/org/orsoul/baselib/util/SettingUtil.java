package org.orsoul.baselib.util;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.blankj.utilcode.util.Utils;

public final class SettingUtil {
  private static final String PACKAGENAME = "com.android.settings";

  /** 打开系统WiFi设置. */
  public static void openWiFiSetting(Context context) {
    Intent intent = null;
    if (10 < android.os.Build.VERSION.SDK_INT) { // API大于10 就是3.0+
      intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
    } else {
      // intent = new Intent();
      // ComponentName component = new ComponentName(
      // "com.android.settings",
      // "com.android.settings.WirelessSettings");
      // intent.setComponent(component);
      // intent.setAction("android.intent.action.VIEW");
      intent = new Intent();
      intent.setAction(Intent.ACTION_MAIN);
      String PACKAGENAME = "com.android.settings";
      intent.setClassName(PACKAGENAME, PACKAGENAME + ".wifi.WifiSettings");
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent);
  }

  /** 打开系统WiFi设置. */
  public static void openWiFiSetting() {
    openWiFiSetting(Utils.getApp());
  }

  public static void openDisplaySettings(Context context) {
    Intent intent = new Intent(Intent.ACTION_MAIN);
    intent.setClassName(PACKAGENAME, PACKAGENAME + ".DisplaySettings");
    context.startActivity(intent);
  }

  public static void turnScreenOn(Context context) {
    if (context == null) {
      return;
    }
    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    PowerManager.WakeLock mWakelock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
        PowerManager.SCREEN_DIM_WAKE_LOCK, "tag");
    mWakelock.acquire();
    mWakelock.release();
  }
}