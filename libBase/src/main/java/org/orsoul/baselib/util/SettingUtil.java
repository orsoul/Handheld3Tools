package org.orsoul.baselib.util;

import android.content.Context;
import android.content.Intent;

import com.blankj.utilcode.util.Utils;

public class SettingUtil {
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
}