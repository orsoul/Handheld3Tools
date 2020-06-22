package org.orsoul.baselib.util;

import android.os.Environment;
import android.util.Log;

import com.blankj.utilcode.util.AppUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CrashLogUtil {

    private static final String TAG = CrashLogUtil.class.getSimpleName();
    public static final String CRASH_REPORT_PATH;

    private static final String EXCEPTION_SUFFIX = "_exception";
    private static final String CRASH_SUFFIX = "_crash";
    private static final String FILE_EXTENSION = ".txt";

    static {
        // /!appName/crash/
        CRASH_REPORT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/!" + AppUtils.getAppName() + "/crash/";
        //        File file = new File(CRASH_REPORT_PATH);
        //        if (!file.exists()) {
        //            file.mkdirs();
        //        }
    }

    private CrashLogUtil() {
        //this class is not publicly instantiable
    }

    private static String getCrashLogTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH-mm-ss", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    public static void saveCrashReport(final Throwable throwable) {
        String filename = getCrashLogTime() + CRASH_SUFFIX + FILE_EXTENSION;
        writeToFile(CRASH_REPORT_PATH, filename, getStackTrace(throwable));
    }


    public static void logException(final Exception exception) {
        ThreadUtil.execute(new Runnable() {
            @Override
            public void run() {
                final String filename = getCrashLogTime() + EXCEPTION_SUFFIX + FILE_EXTENSION;
                writeToFile(CRASH_REPORT_PATH, filename, getStackTrace(exception));
            }
        });
    }

    private static void writeToFile(String crashReportPath, String filename, String crashLog) {

        File crashDir = new File(crashReportPath);
        if (!crashDir.exists() && !crashDir.mkdirs()) {
            Log.e(TAG, "Path provided doesn't exists : " + crashDir + "\n");
            return;
        }

        BufferedWriter bufferedWriter;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(
                    crashReportPath + File.separator + filename));

            bufferedWriter.write(crashLog);
            bufferedWriter.flush();
            bufferedWriter.close();
            Log.d(TAG, "crash report saved in : " + crashReportPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getStackTrace(Throwable e) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);

        e.printStackTrace(printWriter);
        String crashLog = result.toString();
        printWriter.close();
        return crashLog;
    }
}
