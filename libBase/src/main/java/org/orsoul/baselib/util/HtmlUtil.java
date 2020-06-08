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

    public static String getColorText(String format, Object[] textArr, int[] colors) {
        for (int i = 0; i < textArr.length; i++) {
            textArr[i] = getColorText(String.valueOf(textArr[i]), colors[i]);
        }
        format = format.replaceAll("\n", "<br/>");
        return String.format(format, textArr);
    }

    public static String getColorText(int color, String format, Object... args) {
        for (int i = 0; i < args.length; i++) {
            args[i] = getColorText(String.valueOf(args[i]), color);
        }
        format = format.replaceAll("\n", "<br/>");
        return String.format(format, args);
    }

    public static String getColorText(String format, Object... args) {
        return getColorText(defaultColor, format, args);
    }

    public static Spanned getColorSpanned(String text, int color) {
        return Html.fromHtml(getColorText(text, color));
    }

    public static Spanned getColorSpanned(String text) {
        return Html.fromHtml(getColorText(text));
    }

    public static Spanned getColorSpanned(int color, String format, Object... args) {
        String colorText = getColorText(color, format, args);
        return Html.fromHtml(colorText);
    }

    public static Spanned getColorSpanned(String format, Object... args) {
        String colorText = getColorText(format, args);
        return Html.fromHtml(colorText);
    }

    public static Builder newSpannedBuilder() {
        return new Builder();
    }

    public static class Builder {
        private StringBuilder sb = new StringBuilder();

        public Builder setTextAndColor(String text, int color) {
            sb.append(getColorText(text, color));
            return this;
        }

        public Builder setTextAndColor(String text) {
            return setTextAndColor(text, defaultColor);
        }

        public Builder setText(String text) {
            sb.append(text);
            return this;
        }

        public Builder setBr() {
            sb.append("<br/>");
            return this;
        }

        public Spanned build() {
            return Html.fromHtml(sb.toString());
        }
    }

    public static void main(String[] args) {
        //        String text = HtmlUtil.getColorText("1:%s\n2:%s", 1, 2);
        //        System.out.println(text);

        float fff = 12.345F;
        System.out.println(String.format("%.2f", fff));
    }
}
