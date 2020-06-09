package org.orsoul.baselib.util;

import android.view.View;

public class ViewUtil {
    /**
     * @param v 设置 v 获取焦点
     */
    public static void requestFocus(View v) {
        if (null == v) {
            return;
        }
        v.setFocusable(true);
        v.setFocusableInTouchMode(true);

        v.requestFocus();
        v.requestFocusFromTouch();
    }

    public static void clearFocus(View v) {
        if (null == v) {
            return;
        }
        v.setFocusable(false);
        v.setFocusableInTouchMode(false);

        v.clearFocus();
    }
}
