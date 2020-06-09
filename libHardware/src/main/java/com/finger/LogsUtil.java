package com.finger;

import android.text.TextUtils;
import android.util.Log;

import java.util.List;


/**
 * @author orsoul
 * @time 2015-6-23 上午11:33:49
 * @des 日志级别是LEVEL_ALL显示所有信息, 包括System.out.println信息
 * @des 日志级别是LEVEL_OFF关闭所有信息, 包括System.out.println信息
 * @updateAuthor TODO
 * @updateTime TODO
 * @updateDes TODO
 */
public class LogsUtil {
    public static final int LEVEL_TEST = 8;
    /**
     * 日志输出级别NONE
     */
    public static final int LEVEL_OFF = 0;
    /**
     * 日志输出级别ALL
     */
    public static final int LEVEL_ALL = 7;
    /**
     * 日志输出级别V
     */
    public static final int LEVEL_VERBOSE = 1;
    /**
     * 日志输出级别D
     */
    public static final int LEVEL_DEBUG = 2;
    /**
     * 日志输出级别I
     */
    public static final int LEVEL_INFO = 3;
    /**
     * 日志输出级别W
     */
    public static final int LEVEL_WARN = 4;
    /**
     * 日志输出级别E
     */
    public static final int LEVEL_ERROR = 5;
    /**
     * 日志输出级别S,自定义定义的一个级别
     */
    public static int LEVEL_SYSTEM = 6;
    /**
     * 日志输出时的TAG
     */
    private static final String TAG = "Fanfull";
    /**
     * 写文件的锁对象
     */
    private static final Object mLogLock = new Object();
    //	private static int mDebugLevel = LEVEL_TEST;
    //	private static int mDebugLevel = LEVEL_OFF;
    /**
     * 是否允许输出log
     */
    private static int mDebugLevel = LEVEL_ALL;
    /**
     * 用于记时的变量
     */
    private static int RWLEVE = 3;//1E2I3D4V
    private static long mTimestamp = 0;

    public static int getDebugLevel() {
        return mDebugLevel;
    }

    public static void setDebugLevel(int level) {
        mDebugLevel = level;
    }

    public static int getRWLEVE() {
        return RWLEVE;
    }

    public static void setRWLEVE(int level) {
        RWLEVE = level;
    }

    /**---------------日志输出,已固定TAG  begin---------------**/

    /**
     * 以级别为 d 的形式输出LOG
     */
    public static void v(String tag, Object msg) {
        //if (mDebugLevel >= LEVEL_VERBOSE) {
        Log.v(tag, String.valueOf(msg));
        // }
        if (RWLEVE > 3) {
            String s = "v:" + tag + ":" + msg.toString();
            //wl(s,0);
        }
    }

    /**
     * 以级别为 d 的形式输出LOG
     */
    public static void v(Object msg) {
        v(TAG, msg);
    }

    /**
     * 以级别为 d 的形式输出LOG
     */
    public static void d(String tag, Object msg) {
        if (mDebugLevel >= LEVEL_DEBUG) {
            Log.d(tag, String.valueOf(msg));
        }
        if (RWLEVE > 2) {
            String s = "debug Log:" + tag + ":" + msg.toString();
            //wl(s,0);
        }
    }

    /**
     * 以级别为 d 的形式输出LOG
     */
    public static void d(Object msg) {
        d(TAG, msg);
    }

    /**
     * 以级别为 i 的形式输出LOG
     */
    public static void i(String tag, Object msg) {
        if (mDebugLevel >= LEVEL_INFO) {
            Log.i(tag, String.valueOf(msg));
        }
        if (RWLEVE > 1) {
            String s = "info Log:" + tag + ":" + msg.toString();
            // wl(s,0);
        }
    }

    /**
     * 以级别为 i 的形式输出LOG
     */
    public static void i(String msg) {
        i(TAG, msg);
    }

    /**
     * 以级别为 w 的形式输出LOG
     */
    public static void w(String tag, Object msg) {
        if (mDebugLevel >= LEVEL_WARN) {
            Log.w(tag, String.valueOf(msg));
        }
        if (RWLEVE > 0) {
            String s = "Warm Log:" + tag + ":" + msg.toString();
            //wl(s,1);
        }
    }

    /**
     * 以级别为 w 的形式输出LOG
     */
    public static void w(Object msg) {
        w(TAG, msg);
    }

    /**
     * 以级别为 w 的形式输出LOG信息和Throwable
     */
    public static void w(String msg, Throwable tr) {
        if (mDebugLevel >= LEVEL_WARN && null != msg) {
            Log.w(TAG, msg, tr);
        }
    }

    /**
     * 以级别为 w 的形式输出Throwable
     */
    public static void w(Throwable tr) {
        w("", tr);
    }

    /**
     * 以级别为 e 的形式输出Throwable
     */
    public static void e(Throwable tr) {
        if (mDebugLevel >= LEVEL_ERROR) {
            Log.e(TAG, "", tr);

        }
    }

    /**
     * 以级别为 e 的形式输出LOG信息和Throwable
     */
    public static void e(String msg, Throwable tr) {
        if (mDebugLevel >= LEVEL_ERROR && null != msg) {
            Log.e(TAG, msg, tr);

        }

    }

    /**
     * 以级别为 e 的形式输出LOG
     */
    public static void e(String tag, Object msg) {
        if (mDebugLevel >= LEVEL_ERROR) {
            Log.e(tag, String.valueOf(msg));
        }
    }

    /**
     * 以级别为 e 的形式输出LOG
     */
    public static void e(Object msg) {
        e(TAG, msg);
    }

    /**
     * 以级别为 s 的形式输出LOG,主要是为了System.out.println,稍微格式化了一下
     */
    public static void sf(Object msg) {
        if (mDebugLevel >= LEVEL_ERROR) {
            System.out.println("====== " + String.valueOf(msg) + " ======");
        }
    }

    /**
     * 以级别为 s 的形式输出LOG,主要是为了System.out.println
     */
    public static void s(Object msg) {
        if (mDebugLevel >= LEVEL_ERROR) {
            System.out.println(msg);
        }
    }


    /**---------------日志输出,未固定TAG  end---------------**/

    /**
     * 把Log存储到文件中
     *
     * @param log  需要存储的日志
     * @param path 存储路径
     */
    public static void log2File(String log, String path) {
        log2File(log, path, true);
    }

    public static void log2File(String log, String path, boolean append) {
        synchronized (mLogLock) {
            //			FileUtils.writeFile(log + "\r\n", path, append);
        }
    }

    /**
     * 以级别为 e 的形式输出msg信息,附带时间戳，用于输出一个时间段起始点
     *
     * @param msg 需要输出的msg
     */
    public static void msgStartTime(String msg) {
        mTimestamp = System.currentTimeMillis();
        if (!TextUtils.isEmpty(msg)) {
            e("[Started：" + mTimestamp + "]" + msg);
        }
    }

    /**
     * 以级别为 e 的形式输出msg信息,附带时间戳，用于输出一个时间段结束点* @param msg 需要输出的msg
     */
    public static void elapsed(String msg) {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - mTimestamp;
        mTimestamp = currentTime;
        e("[Elapsed：" + elapsedTime + "]" + msg);
    }

    public static <T> void printList(List<T> list) {
        if (list == null || list.size() < 1) {
            return;
        }
        int size = list.size();
        i("---begin---");
        for (int i = 0; i < size; i++) {
            i(i + ":" + list.get(i).toString());
        }
        i("---end---");
    }

    public static <T> void printArray(T[] array) {
        if (array == null || array.length < 1) {
            return;
        }
        int length = array.length;
        i("---begin---");
        for (int i = 0; i < length; i++) {
            i(i + ":" + array[i].toString());
        }
        i("---end---");
    }


    //level 0

}
