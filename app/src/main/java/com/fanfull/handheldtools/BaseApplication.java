package com.fanfull.handheldtools;

import android.app.Application;
import android.content.Context;

import org.orsoul.baselib.util.LogHelper;

public class BaseApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        LogHelper.initLog(true);
    }

    public static Context getContext() {
        return context;
    }
}
