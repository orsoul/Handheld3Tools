package org.orsoul.baselib.util;

/**
 * 点击 次数工具类
 */
public class ClickUtil {
    private static final long TIME_GAP = 2500;
    private static final long FAST_TIME_GAP = 1000;
    private static long sLastClickTime = 0;

    /**
     * @return 本次 click() 与 上一次click() 之间的 时间隔。第一次运行返回 当前时间
     */
    public static long click() {
        long reVal = System.currentTimeMillis() - sLastClickTime;
        sLastClickTime = System.currentTimeMillis();
        return reVal;
    }

    /**
     * @param timeGap 时间隔
     * @return 此方法 本次执行的时间 与 上一次执行的时间 间隔 小于 timeGap 时 返回true
     */
    public static boolean isFastDoubleClick(long timeGap) {
        return click() < timeGap;
    }

    public static boolean isFastDoubleClick() {
        return isFastDoubleClick(TIME_GAP);
    }

    private static int sFastClickCount;

    /** 返回 快速连击的 次数 */
    public static int fastClickTimes(long timeGap) {
        if (isFastDoubleClick(timeGap)) {
            sFastClickCount++;
        } else {
            sFastClickCount = 1;
        }
        return sFastClickCount;
    }

    /** 返回 快速连击的 次数 */
    public static int fastClickTimes() {
        return fastClickTimes(FAST_TIME_GAP);
    }
}
