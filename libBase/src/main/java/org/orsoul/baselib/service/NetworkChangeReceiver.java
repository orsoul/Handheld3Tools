package org.orsoul.baselib.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.aware.WifiAwareManager;
import android.os.Build;

import com.apkfuns.logutils.LogUtils;

import java.util.HashMap;
import java.util.Map;

public class NetworkChangeReceiver extends BroadcastReceiver {

  private static final Map<Context, NetworkChangeReceiver> changeObserverMap = new HashMap<>();

  NetChangeObserver netChangeObserver;
  boolean isPause;

  public NetworkChangeReceiver(NetChangeObserver netChangeObserver) {
    this.netChangeObserver = netChangeObserver;
  }

  //WifiAwareManager awareManager;
  //ConnectivityManager connMgr;

  @Override public void onReceive(Context context, Intent intent) {
    //LogUtils.d("%s", intent.getAction());
    switch (intent.getAction()) {
      case WifiAwareManager.ACTION_WIFI_AWARE_STATE_CHANGED:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          WifiAwareManager awareManager =
              (WifiAwareManager) context.getSystemService(Context.WIFI_AWARE_SERVICE);
        }
        break;
      case ConnectivityManager.CONNECTIVITY_ACTION:
        //ConnectivityManager connMgr =
        //    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (!isPause && netChangeObserver != null) {
          handleNetChange(context, netChangeObserver);
        }
        break;
    }
  }

  private void handleNetChange(Context context, NetChangeObserver netChangeObserver) {
    ConnectivityManager connMgr =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    netChangeObserver.onNetChange(connMgr);

    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    int wifiState = wifiManager.getWifiState();
    LogUtils.d("isWifiEnabled:%s", wifiManager.isWifiEnabled());
    LogUtils.d("wifiState:%s", wifiState);

    NetworkInfo wifiNetInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    NetworkInfo.State state = wifiNetInfo.getState();
    LogUtils.d("NetworkInfo.State:%s", state);
    //LogUtils.d("DetailedState:%s", wifiNetInfo.getDetailedState());

    NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();
    /* 网络连接 */
    if (activeNetworkInfo != null) {
      LogUtils.d("activeNetworkInfo:%s", activeNetworkInfo);
      if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
        netChangeObserver.onConnect(true);
      } else if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
        netChangeObserver.onConnect(false);
      }
      return;
    }

    /* 网络断开 */

  }

  public static void register(Context context, NetChangeObserver observer) {
    if (changeObserverMap.containsKey(context)) {
      return;
    }

    IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      intentFilter.addAction(WifiAwareManager.ACTION_WIFI_AWARE_STATE_CHANGED);
    }
    NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver(observer);
    context.registerReceiver(networkChangeReceiver, intentFilter);
    changeObserverMap.put(context, networkChangeReceiver);
  }

  public static void unregister(Context context) {
    NetworkChangeReceiver networkChangeReceiver = changeObserverMap.get(context);
    if (networkChangeReceiver != null) {
      context.unregisterReceiver(networkChangeReceiver);
      changeObserverMap.remove(context);
    }
  }

  public static void pause(Context context) {
    NetworkChangeReceiver networkChangeReceiver = changeObserverMap.get(context);
    if (networkChangeReceiver != null) {
      networkChangeReceiver.isPause = true;
    }
  }

  public static void resume(Context context) {
    NetworkChangeReceiver networkChangeReceiver = changeObserverMap.get(context);
    if (networkChangeReceiver != null) {
      networkChangeReceiver.isPause = false;
    }
  }

  public interface NetChangeObserver {

    default void onNetChange(ConnectivityManager connMgr) {
      //NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();
      //if (activeNetworkInfo != null) {
      //  if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
      //    onConnect(true);
      //  } else if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
      //    onConnect(false);
      //  }
      //  return;
      //}
    }

    default void onConnect(boolean isWifiConnect) {
      if (isWifiConnect) {
        onWifiConnect();
      } else {
        onMobileConnect();
      }
    }

    void onDisconnect();

    default void onMobileConnect() {}

    default void onWifiConnect() {}
  }
}