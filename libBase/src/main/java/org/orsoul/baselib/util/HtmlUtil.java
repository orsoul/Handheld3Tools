package org.orsoul.baselib.util;

import android.text.Html;
import android.text.Spanned;

public abstract class HtmlUtil {

    private static int defaultColor = 0xFF0000;

    public static void setDefaultColor(int color) {
        defaultColor = color;
    }

    public static String getColorText(String text, int color) {
        return String.format("<font color=\"#%06X\">%s</font>", color, text);
    }

    public static String getColorText(String text) {
        return getColorText(text, defaultColor);
    }

    public static Spanned getColorSpanned(String text, int color) {
        return Html.fromHtml(getColorText(text, color));
    }

    public static Spanned getColorSpanned(String format, Object... args) {
        int index = 0;
        while (0 <= (index = format.indexOf("%r", index))) {
            args[index] = getColorText(String.valueOf(args[index]));
        }
        format = format.replaceAll("%r", "%s");
        return Html.fromHtml(String.format(format, args));
    }
}
