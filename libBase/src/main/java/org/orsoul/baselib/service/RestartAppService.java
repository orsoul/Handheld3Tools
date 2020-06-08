package org.orsoul.baselib.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

/**
 * 重启APP的service，此service在独立的进程中运行
 */
public class RestartAppService extends Service {
    public static final String KEY_DELAY = "KEY_DELAY";
    public static final String KEY_PACKAGE_NAME = "KEY_PACKAGE_NAME";
    public static final int DELAY = 2000;

    public RestartAppService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null == intent) return Service.START_STICKY;
        int stopDelayed = intent.getIntExtra(KEY_DELAY, DELAY);
        final String PackageName = intent.getStringExtra(KEY_PACKAGE_NAME);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(PackageName);
                startActivity(LaunchIntent);
                RestartAppService.this.stopSelf();
            }
        }, stopDelayed);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void restartApp(Context context, long delay) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, RestartAppService.class);
        intent.putExtra(KEY_PACKAGE_NAME, context.getPackageName());
        intent.putExtra(KEY_DELAY, delay);
        context.startService(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
