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
        initCrashHandler();
    }

    private void initCrashHandler() {
        Cockroach.install(new Cockroach.ExceptionHandler() {
            // handlerException内部建议手动try{  你的异常处理逻辑  }catch(Throwable e){ } ，以防handlerException内部再次抛出异常，导致循环调用handlerException
            @Override
            public void handlerException(final Thread thread, final Throwable throwable) {
                //开发时使用Cockroach可能不容易发现bug，所以建议开发阶段在handlerException中用Toast谈个提示框，
                //由于handlerException可能运行在非ui线程中，Toast又需要在主线程，所以new了一个new Handler(Looper.getMainLooper())，
                //所以千万不要在下面的run方法中执行耗时操作，因为run已经运行在了ui线程中。
                //new Handler(Looper.getMainLooper())只是为了能弹出个toast，并无其他用途
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        //                        LogUtils.w("crash case:%s", throwable.getMessage());
                        throwable.printStackTrace();
                        ToastUtils.showLong(String.format("crash case:%s", throwable.getMessage()));
                        //                        LogHelper.saveCrashLog(throwable);
                        CrashLogUtil.saveCrashReport(throwable);
                    }
                });
            }
        });
    }

}
