package org.orsoul.baselib.util;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

import com.apkfuns.logutils.LogUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.DeviceUtils;

import java.lang.reflect.Method;

public class DeviceInfoUtils {
  public static void showDeviceInfo() {
    LogUtils.d("isDeviceRooted:%s", DeviceUtils.isDeviceRooted());
    LogUtils.d("isAppRoot:%s", AppUtils.isAppRoot());
    LogUtils.d("isTablet:%s", DeviceUtils.isTablet());
    LogUtils.d("MacAddress:%s", DeviceUtils.getMacAddress());
    LogUtils.d("UniqueDeviceId:%s", DeviceUtils.getUniqueDeviceId());
    LogUtils.d("Model:%s", DeviceUtils.getModel());
    LogUtils.d("Brand:%s", getBrand());
    LogUtils.d("OSVersion:%s", getOSVersion());
  }

  /** 手机品牌 */
  public static String getBrand() {
    return android.os.Build.BRAND;
  }

  /** 获取机型 */
  public static String getModel() {
    return android.os.Build.MODEL;
  }

  /** 获取操作系统版本 */
  public static String getOSVersion() {
    return android.os.Build.VERSION.RELEASE;
  }

  public static String getMyDeviceModel() {
    return String.format("FanFull_%s", android.os.Build.MODEL);
  }

  public static String getMyDeviceId() {
    return DeviceUtils.getUniqueDeviceId();
  }

  /**
   * 设备关机. <br/>
   * 需要系统APP：android:sharedUserId="android.uid.system" <br/>
   * 需要权限：uses-permission android:name="android.permission.SHUTDOWN" <br/>
   */
  public static void shutdown(Context context, boolean needConfirm) {
    String action;
    // Intent.ACTION_REQUEST_SHUTDOWN
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
      action = "android.intent.action.ACTION_REQUEST_SHUTDOWN";
    } else {
      // android 8.1
      action = "com.android.internal.intent.action.REQUEST_SHUTDOWN";
    }
    Intent shutdown = new Intent(action);
    // Intent.EXTRA_KEY_CONFIRM
    shutdown.putExtra("android.intent.extra.KEY_CONFIRM", needConfirm);
    shutdown.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    try {
      context.startActivity(shutdown);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /** 通过反射关机. <br/> */
  public static void shutdown(boolean needConfirm) {
    try {
      //获得ServiceManager类
      Class<?> serviceManager = Class.forName("android.os.ServiceManager");
      //获得ServiceManager的getService方法
      Method getService = serviceManager.getMethod("getService", java.lang.String.class);

      //调用getService获取RemoteService
      Object oRemoteService = getService.invoke(null, Context.POWER_SERVICE);

      //获得IPowerManager.Stub类
      Class<?> cStub = Class.forName("android.os.IPowerManager$Stub");
      //获得asInterface方法
      Method asInterface = cStub.getMethod("asInterface", android.os.IBinder.class);
      //调用asInterface方法获取IPowerManager对象
      Object oIPowerManager = asInterface.invoke(null, oRemoteService);
      //获得shutdown()方法
      Method shutdown = oIPowerManager.getClass().getMethod(
          "shutdown", boolean.class, boolean.class);
      //调用shutdown()方法
      shutdown.invoke(oIPowerManager, needConfirm, false);
    } catch (Exception e) {
      LogUtils.w("%s", e.getMessage());
    }
  }

  /** 广播 重启设备. */
  public static void reboot(Context context) {
    Intent intent = new Intent(Intent.ACTION_REBOOT);
    intent.putExtra("nowait", 1);
    //intent.putExtra("interval", 5);
    //intent.putExtra("window", 1);
    context.sendBroadcast(intent);
  }

  /** PowerManager 重启设备. */
  public static void reboot2(Context context) {
    PowerManager pManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    pManager.reboot("");
  }
}
