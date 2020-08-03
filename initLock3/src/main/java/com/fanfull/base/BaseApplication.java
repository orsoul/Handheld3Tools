package com.fanfull.base;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.blankj.utilcode.util.ToastUtils;
import com.fanfull.contexts.MyContexts;
import com.fanfull.db.DaoMaster;
import com.fanfull.db.DaoMaster.OpenHelper;
import com.fanfull.db.DaoSession;
import com.wanjian.cockroach.Cockroach;
import org.orsoul.baselib.util.CrashLogUtil;
import org.orsoul.baselib.util.LogHelper;
import org.orsoul.baselib.util.SoundUtils;

public class BaseApplication extends Application {
  private static Context context;
  private static DaoMaster daoMaster;
  private static DaoSession daoSession;

  @Override
  public void onCreate() {
    super.onCreate();
    context = getApplicationContext();

    LogHelper.initLog(true);
    LogHelper.initFileLog(true, this);
    SoundUtils.loadSounds(this);
    //initCrashHandler();
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

  public static Context getContext() {
    return context;
  }

  public static DaoMaster getDaoMaster(Context context) {
    if (daoMaster == null) {
      OpenHelper helper = new DaoMaster.DevOpenHelper(context,
          MyContexts.DB_NAME, null);
      daoMaster = new DaoMaster(helper.getWritableDatabase());
    }
    return daoMaster;
  }

  public static DaoSession getDaoSession(Context context) {
    if (daoSession == null) {
      if (daoMaster == null) {
        daoMaster = getDaoMaster(context);
      }
      daoSession = daoMaster.newSession();
    }
    return daoSession;
  }
}
