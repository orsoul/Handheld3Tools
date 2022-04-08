package com.fanfull.handheldtools;

import android.app.Application;

import com.apkfuns.logutils.LogUtils;
import com.simple.spiderman.SpiderMan;

import org.orsoul.baselib.NetworkCallbackApplication;
import org.orsoul.baselib.util.LogHelper;

public class MyApplication extends NetworkCallbackApplication {
  private static MyApplication context;

  public static MyApplication getInstance() {
    return context;
  }

  @Override public void onCreate() {
    super.onCreate();
    context = this;
    LogHelper.initLog(true);
    LogHelper.initFileLog(true, this);
    initCrashHandler();
    SpiderMan.setTheme(R.style.SpiderManTheme_Dark);
  }

  @Override protected void onCrashOnMainThread(Application context) {
    LogUtils.wtf("onCrashOnMainThread:%s", "");
    //ToastUtils.showShort("主线程发生异常，将重启APP");
    //RestartAppService.restartApp(context, 500);
  }

  private void initCaocConfig() {
    //        CaocConfig.Builder.create()
    //                .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
    //                .enabled(false) //default: true
    //                .showErrorDetails(false) //default: true
    //                .showRestartButton(false) //default: true
    //                .logErrorOnRestart(false) //default: true
    //                .trackActivities(true) //default: false
    //                .minTimeBetweenCrashesMs(2000) //default: 3000
    //                .errorDrawable(R.drawable.ic_custom_drawable) //default: bug image
    //                .restartActivity(YourCustomActivity.class) //default: null (your app's launch activity)
    //                .errorActivity(YourCustomErrorActivity.class) //default: null (default error activity)
    //                .eventListener(null) //default: null
    //                .apply();
  }
}
