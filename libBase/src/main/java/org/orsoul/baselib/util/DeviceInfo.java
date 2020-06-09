package org.orsoul.baselib.util;

import com.apkfuns.logutils.LogUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.DeviceUtils;

public class DeviceInfo {
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
}
