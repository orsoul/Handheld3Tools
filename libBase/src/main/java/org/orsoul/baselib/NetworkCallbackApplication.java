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
import com.wanjian.cockroach.Cockroach;

import org.orsoul.baselib.util.CrashLogUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
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
    initNetworkCallback();
  }

  protected void initNetworkCallback() {
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

  protected void initCrashHandler() {
    // handlerException内部建议手动try{  你的异常处理逻辑  }catch(Throwable e){ } ，以防handlerException内部再次抛出异常，导致循环调用handlerException
    Cockroach.install((thread, throwable) -> {
      //开发时使用Cockroach可能不容易发现bug，所以建议开发阶段在handlerException中用Toast谈个提示框，
      //由于handlerException可能运行在非ui线程中，Toast又需要在主线程，所以new了一个new Handler(Looper.getMainLooper())，
      //所以千万不要在下面的run方法中执行耗时操作，因为run已经运行在了ui线程中。
      //new Handler(Looper.getMainLooper())只是为了能弹出个toast，并无其他用途
      //new Handler(Looper.getMainLooper()).post(() -> {
      //});

      String format = String.format("crash catch, case:%s", throwable.getMessage());
      ToastUtils.showLong(format);
      LogUtils.wtf(format);

      //throwable.printStackTrace();
      final Writer result = new StringWriter();
      final PrintWriter printWriter = new PrintWriter(result);
      throwable.printStackTrace(printWriter);
      String log = result.toString();
      LogUtils.w("%s", log);
      //                        LogHelper.saveCrashLog(throwable);
      CrashLogUtil.saveCrashReport(log);
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
    return networkCallbackSet.add(callback);
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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
