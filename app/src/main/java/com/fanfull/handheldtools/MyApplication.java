package com.fanfull.handheldtools;

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
