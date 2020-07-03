package org.orsoul.baselib.util;

import android.os.Environment;

import com.apkfuns.log2file.LogFileEngineFactory;
import com.apkfuns.logutils.LogLevel;
import com.apkfuns.logutils.LogUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.Utils;

public class LogHelper {

    private static final String TAG_LOG = "myLog";
    public static final String LOG_FILE_PATH;

    static {
        // /!appName/logs/
        LOG_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/!" + AppUtils.getAppName() + "/logs/";
    }

    public static void initLog(boolean allowLog) {
        // debug模式 logcat输出
        LogUtils.getLogConfig()
                .configAllowLog(allowLog)
                .configShowBorders(false)
                .configTagPrefix(TAG_LOG)
                .configFormatTag("%t %c{-4}");
    }

    /**
     * 保存log到文件
     */
    public static void initFileLog(boolean allowLog) {
        LogUtils.getLog2FileConfig().configLog2FileEnable(allowLog)
                // targetSdkVersion >= 23 需要确保有写sdcard权限
                .configLog2FilePath(LOG_FILE_PATH)
                .configLog2FileLevel(LogLevel.TYPE_DEBUG)
                .configLog2FileNameFormat("%d{yyyyMMdd}.txt")
                .configLogFileEngine(new LogFileEngineFactory(Utils.getApp().getApplicationContext()));
    }
}
