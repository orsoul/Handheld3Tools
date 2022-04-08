package org.orsoul.baselib;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.apkfuns.logutils.LogUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.orsoul.baselib.util.AppUtil;
import org.orsoul.baselib.util.CrashLogUtil;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * 监听网络变化的Application，需要api21.
 */
public abstract class NetworkCallbackApplication extends Application {
  private static Set<NetworkCallback> networkCallbackSet;

  @Override public void onCreate() {
    super.onCreate();
    //initNetworkCallback();
  }

  /** 在主线程中 产生未捕捉的异常时 回调此方法，此时一般执行 重启 APP的操作. */
  protected void onCrashOnMainThread(Application context) {
  }

  protected void initNetworkCallback() {
    networkCallbackSet = new HashSet<>();
    setNetworkCallback(new ConnectivityManager.NetworkCallback() {
      private boolean isConnected;

      @Override public void onAvailable(Network network) {
        LogUtils.v("isConnected:%s network:%s", isConnected, network);
        if (!isConnected) {
          isConnected = true;
          for (NetworkCallback networkCallback : networkCallbackSet) {
            networkCallback.onNetworkChange(true);
          }
        }
      }

      @Override public void onLost(Network network) {
        LogUtils.v("isConnected:%s network:%s", isConnected, network);
        if (isConnected) {
          isConnected = false;
          for (NetworkCallback networkCallback : networkCallbackSet) {
            networkCallback.onNetworkChange(false);
          }
        }
      }
    });
  }

  protected void initCrashHandler() {
    // handlerException内部建议手动try{  你的异常处理逻辑  }catch(Throwable e){ } ，以防handlerException内部再次抛出异常，导致循环调用handlerException
    Cockroach.install((thread, throwable) -> {
      //开发时使用Cockroach可能不容易发现bug，所以建议开发阶段在handlerException中用Toast谈个提示框，
      //handlerException可能运行在非ui线程中

      try {
        String format = String.format("crash catch, case:%s", throwable.getMessage());
        ToastUtils.showLong(format);
        //LogUtils.wtf(format);

        String stackTrace = CrashLogUtil.getStackTrace(throwable);
        LogUtils.wtf("%s", stackTrace);
        LogUtils.getLog2FileConfig().flushAsync();
        CrashLogUtil.saveCrashReport(stackTrace);

        if (AppUtil.isMainThread()) {
          onCrashOnMainThread(this);
        }
      } catch (Exception e) {
        LogUtils.wtf("异常处理中出异常：%s", e.getMessage());
        e.printStackTrace();
      }
    });
  }

  public static String getDebugDBAddress() {
    if (BuildConfig.DEBUG) {
      try {
        Class<?> debugDB = Class.forName("com.amitshekhar.DebugDB");
        Method getAddressLog = debugDB.getMethod("getAddressLog");
        Object value = getAddressLog.invoke(null);
        return String.valueOf(value);
      } catch (Exception ignore) {
      }
    }
    return null;
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public static boolean addNetworkCallback(NetworkCallback callback) {
    if (networkCallbackSet == null || callback == null) {
      //LogUtils.d("add null");
      return false;
    }
    boolean add = networkCallbackSet.add(callback);
    LogUtils.d("add %s,%s", add, callback);
    LogUtils.v("all %s", networkCallbackSet);
    return add;
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public static boolean removeNetworkCallback(NetworkCallback callback) {
    if (networkCallbackSet == null) {
      return true;
    }
    boolean add = networkCallbackSet.remove(callback);
    LogUtils.d("remove %s,%s", add, callback);
    LogUtils.v("all %s", networkCallbackSet);
    return add;
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  private void setNetworkCallback(ConnectivityManager.NetworkCallback callback) {
    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    if (cm == null) {
      return;
    }
    cm.requestNetwork(new NetworkRequest.Builder().build(), callback);

    //NetworkRequest request = new NetworkRequest.Builder()
    //    .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
    //    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    //    .build();
    //cm.registerNetworkCallback(request, callback);

    //cm.registerDefaultNetworkCallback(callback);
  }

  public interface NetworkCallback {
    void onNetworkChange(boolean isConnected);
  }
}
