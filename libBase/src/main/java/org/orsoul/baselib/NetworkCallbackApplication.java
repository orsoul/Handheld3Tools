package org.orsoul.baselib;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Build;
import androidx.annotation.RequiresApi;
import com.apkfuns.logutils.LogUtils;
import java.util.HashSet;
import java.util.Set;

/**
 * 监听网络变化的Application，需要api21.
 */
public abstract class NetworkCallbackApplication extends Application {
  private static Set<NetworkCallback> networkCallbackSet;

  @Override
  public void onCreate() {
    super.onCreate();

    networkCallbackSet = new HashSet<>();
    setNetworkCallback(new ConnectivityManager.NetworkCallback() {
      private boolean isConnected;

      @Override public void onAvailable(Network network) {
        if (!isConnected) {
          LogUtils.v("onAvailable %s", network);
          isConnected = true;
          for (NetworkCallback networkCallback : networkCallbackSet) {
            networkCallback.onNetworkChange(true);
          }
        }
      }

      @Override public void onLost(Network network) {
        LogUtils.v("onLost %s", network);
        isConnected = false;
        for (NetworkCallback networkCallback : networkCallbackSet) {
          networkCallback.onNetworkChange(false);
        }
      }
    });
  }

  public static boolean addNetworkCallback(NetworkCallback callback) {
    return networkCallbackSet.add(callback);
  }

  public static boolean removeNetworkCallback(NetworkCallback callback) {
    return networkCallbackSet.remove(callback);
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  private void setNetworkCallback(ConnectivityManager.NetworkCallback callback) {
    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    if (cm == null) {
      return;
    }
    cm.requestNetwork(new NetworkRequest.Builder().build(), callback);
  }

  public interface NetworkCallback {
    void onNetworkChange(boolean isConnected);
  }
}
