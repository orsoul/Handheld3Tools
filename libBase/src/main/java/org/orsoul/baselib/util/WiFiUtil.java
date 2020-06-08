package org.orsoul.baselib.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import com.apkfuns.logutils.LogUtils;

import org.orsoul.baselib.util.utilcode.AppUtil;
import org.orsoul.baselib.util.utilcode.NetworkUtils;

import java.util.List;

import static android.content.Context.WIFI_SERVICE;

/**
 * WiFi相关工具类。
 */
public final class WiFiUtil {

    private WiFiUtil() {}

    public static boolean isEnabled() {
        return NetworkUtils.getWifiEnabled();
    }

    public static boolean isConnected() {
        return NetworkUtils.isWifiConnected();
    }

    public static boolean isAvailable() {
        return NetworkUtils.isWifiAvailable();
    }

    private static int lastWifiNetId = -1;

    /**
     * 停用当前 wifi 连接。停用后该连接不会重连。
     *
     * @return
     */
    public static boolean disableNetwork() {
        @SuppressLint("WifiManagerLeak")
        WifiManager manager = (WifiManager) AppUtil.getApp().getSystemService(WIFI_SERVICE);
        if (manager == null) return false;
        lastWifiNetId = manager.getConnectionInfo().getNetworkId();
        LogUtils.d("NetworkId: %s", lastWifiNetId);
        return manager.disableNetwork(lastWifiNetId);
    }

    /**
     * 启用 指定wifi连接
     *
     * @param wifiNetId
     * @return
     */
    public static boolean enableNetwork(int wifiNetId) {
        @SuppressLint("WifiManagerLeak")
        WifiManager manager = (WifiManager) AppUtil.getApp().getSystemService(WIFI_SERVICE);
        if (manager == null) return false;
        LogUtils.d("enableNetwork: %s", wifiNetId);
        return manager.enableNetwork(wifiNetId, false);
    }

    /**
     * 启用 上一次停用的wifi连接
     *
     * @return
     */
    public static boolean enableNetwork() {
        //        if (lastWifiNetId != -1) {
        return enableNetwork(lastWifiNetId);
        //        } else {
        //            return false;
        //        }
    }

    /**
     * 断开当前wifi连接，断开后系统会重连。
     *
     * @return
     */
    public static boolean disconnect() {
        @SuppressLint("WifiManagerLeak")
        WifiManager manager = (WifiManager) AppUtil.getApp().getSystemService(WIFI_SERVICE);
        if (manager == null) return false;
        return manager.disconnect();
    }

    public static boolean reconnect() {
        @SuppressLint("WifiManagerLeak")
        WifiManager manager = (WifiManager) AppUtil.getApp().getSystemService(WIFI_SERVICE);
        if (manager == null) return false;
        return manager.reconnect();
    }

    public static void openWiFiSetting(Context context) {
        Intent intent = null;
        if (Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT) {
            intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
        } else {
            // intent = new Intent();
            // ComponentName component = new ComponentName(
            // "com.android.settings",
            // "com.android.settings.WirelessSettings");
            // intent.setComponent(component);
            // intent.setAction("android.intent.action.VIEW");
            String PACKAGENAME = "com.android.settings";
            intent = new Intent();
            intent.setAction(Intent.ACTION_MAIN);
            intent.setClassName(PACKAGENAME, PACKAGENAME + ".wifi.WifiSettings");
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static boolean setEnabled(boolean enable) {
        @SuppressLint("WifiManagerLeak")
        WifiManager manager = (WifiManager) AppUtil.getApp().getSystemService(WIFI_SERVICE);
        if (manager == null) return false;
        return manager.setWifiEnabled(enable);
    }

    /**
     * 获取已连接 wifi 热点的 SSID
     *
     * @return 如果wifi已连接返回 SSID，否则返回 ""
     */
    public static String getConnectedSSID() {
        @SuppressLint("WifiManagerLeak")
        WifiManager manager = (WifiManager) AppUtil.getApp().getSystemService(WIFI_SERVICE);
        if (manager == null) return "";

        WifiInfo wifiInfo = manager.getConnectionInfo();
        List<WifiConfiguration> cig = manager.getConfiguredNetworks();


        if (wifiInfo == null) return "";

        return formatSSID(wifiInfo.getSSID());
    }

    /**
     * 根据ssid获取本机已保存的wifi连接配置
     *
     * @param SSID
     * @return
     */
    public static WifiConfiguration getWifiConfigBySSID(String SSID) {
        @SuppressLint("WifiManagerLeak")
        WifiManager manager = (WifiManager) AppUtil.getApp().getSystemService(WIFI_SERVICE);
        if (manager == null) return null;

        List<WifiConfiguration> cfgs = manager.getConfiguredNetworks();

        SSID = "\"" + SSID + "\"";
        for (WifiConfiguration cfg : cfgs) {
            if (cfg.SSID.equals(SSID)) {
                return cfg;
            }
        }
        return null;
    }

    private static String formatSSID(String SSID) {
        if (SSID == null) return "";
        final int length = SSID.length();
        if (length > 2 && (SSID.charAt(0) == '"') && SSID.charAt(length - 1) == '"') {
            return SSID.substring(1, length - 1);
        }
        return "";
    }
}