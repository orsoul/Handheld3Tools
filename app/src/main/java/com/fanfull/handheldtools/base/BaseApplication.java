package com.fanfull.handheldtools.base;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.blankj.utilcode.util.ToastUtils;
import com.wanjian.cockroach.Cockroach;
import org.orsoul.baselib.util.CrashLogUtil;
import org.orsoul.baselib.util.LogHelper;

public class BaseApplication extends Application {
    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        LogHelper.initLog(true);
        LogHelper.initFileLog(true, this);
        initCrashHandler();
    }

    private void initCrashHandler() {
        // handlerException内部建议手动try{  你的异常处理逻辑  }catch(Throwable e){ } ，以防handlerException内部再次抛出异常，导致循环调用handlerException
        Cockroach.install((thread, throwable) -> {
            //开发时使用Cockroach可能不容易发现bug，所以建议开发阶段在handlerException中用Toast谈个提示框，
            //由于handlerException可能运行在非ui线程中，Toast又需要在主线程，所以new了一个new Handler(Looper.getMainLooper())，
            //所以千万不要在下面的run方法中执行耗时操作，因为run已经运行在了ui线程中。
            //new Handler(Looper.getMainLooper())只是为了能弹出个toast，并无其他用途
            new Handler(Looper.getMainLooper()).post(() -> {
                //                        LogUtils.w("crash case:%s", throwable.getMessage());
                throwable.printStackTrace();
                ToastUtils.showLong(String.format("crash case:%s", throwable.getMessage()));
                //                        LogHelper.saveCrashLog(throwable);
                CrashLogUtil.saveCrashReport(throwable);
            });
        });
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
