package org.orsoul.baselib.util;

import android.text.Html;
import android.text.Spanned;

public abstract class HtmlUtil {

    private static int defaultColor = 0xFF0000;
    private static String PLACEHOLDER = "%c";

    public static void setDefaultColor(int color) {
        defaultColor = color;
    }

    public static String getColorText(String text, int color) {
        return String.format("<font color=\"#%06X\">%s</font>", color, text);
    }

    public static String getColorText(String text) {
        return getColorText(text, defaultColor);
    }

    public static String getColorText(String format, Object... args) {
        for (int i = 0; i < args.length; i++) {
            args[i] = getColorText(String.valueOf(args[i]));
        }
        format = format.replaceAll("\n", "<br/>");
        return String.format(format, args);
    }

    public static Spanned getColorSpanned(String text, int color) {
        return Html.fromHtml(getColorText(text, color));
    }

    public static Spanned getColorSpanned(String format, Object... args) {
        String colorText = getColorText(format, args);
        return Html.fromHtml(colorText);
    }

    public static void main(String[] args) {
        String text = HtmlUtil.getColorText("1:%s\n2:%s", 1, 2);
        System.out.println(text);
    }
}
