package org.orsoul.baselib.util;

import android.content.Context;
import android.os.Environment;
import com.apkfuns.log2file.LogFileEngineFactory;
import com.apkfuns.logutils.LogLevel;
import com.apkfuns.logutils.LogUtils;
import com.blankj.utilcode.util.AppUtils;

public class LogHelper {

  private static final String TAG_LOG = "myLog";
  public static final String LOG_FILE_PATH;

  static {
    /* /!appName/logs/ */
    LOG_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
        + "/!" + AppUtils.getAppName() + "/logs/";
  }

  public static void initLog(boolean allowLog) {
    // debug模式 logcat输出
    LogUtils.getLogConfig()
        .configAllowLog(allowLog)
        .configShowBorders(false)
        .configTagPrefix(TAG_LOG)
        .configFormatTag("%t %c{-3}");
  }

  /** 设置保存log到文件. */
  public static void initFileLog(boolean allowLog, Context context, String fileNameFormat,
      int level) {
    LogUtils.getLog2FileConfig().configLog2FileEnable(allowLog)
        // targetSdkVersion >= 23 需要确保有写sdcard权限
        .configLog2FilePath(LOG_FILE_PATH)
        .configLog2FileLevel(level)
        .configLog2FileNameFormat(fileNameFormat)
        .configLogFileEngine(new LogFileEngineFactory(context));
  }

  /** 设置保存log到文件，保存level为LogLevel.TYPE_ERROR . */
  public static void initFileLog(boolean allowLog, Context context, String fileNameFormat) {
    initFileLog(allowLog, context, fileNameFormat, LogLevel.TYPE_ERROR);
  }

  /** 设置保存log到文件，文件格式为：yyyyMMdd_HH.txt，保存level为LogLevel.TYPE_ERROR . */
  public static void initFileLog(boolean allowLog, Context context) {
    initFileLog(allowLog, context, "%d{yyyyMMdd_HH}.txt");
  }
}
