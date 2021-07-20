package org.orsoul.baselib.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.HashMap;
import java.util.Map;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class NetworkChangeUtil {

  private static final Map<Context, ConnectivityManager.NetworkCallback> CALLBACK_MAP =
      new HashMap<>();

  boolean isPause;

  public static void register(Context context,
      ConnectivityManager.NetworkCallback callback) {
    ConnectivityManager cm =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    if (cm == null) {
      return;
    }
    //cm.requestNetwork(new NetworkRequest.Builder().build(), callback);

    NetworkRequest request = new NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build();
    cm.registerNetworkCallback(request, callback);

    //cm.registerDefaultNetworkCallback(callback);
  }

  public static void unregister(Context context) {
    ConnectivityManager.NetworkCallback callback = CALLBACK_MAP.get(context);
    if (callback != null) {
      ConnectivityManager cm =
          (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
      if (cm == null) {
        return;
      }
      cm.unregisterNetworkCallback(callback);
      CALLBACK_MAP.remove(context);
    }
  }

  //public static void pause(Context context) {
  //  NetworkChangeUtil networkChangeReceiver = CALLBACK_MAP.get(context);
  //  if (networkChangeReceiver != null) {
  //    networkChangeReceiver.isPause = true;
  //  }
  //}
  //public static void resume(Context context) {
  //  NetworkChangeUtil networkChangeReceiver = CALLBACK_MAP.get(context);
  //  if (networkChangeReceiver != null) {
  //    networkChangeReceiver.isPause = false;
  //  }
  //}
}