package org.orsoul.baselib.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

public class ViewUtil {
  /** @param v 设置 v 获取焦点. */
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

  /** 设置 TextView 可滑动 */
  public static void setMovementMethod(TextView tv) {
    if (tv != null) {
      tv.setMovementMethod(ScrollingMovementMethod.getInstance());
    }
  }

  public static void appendShow(Object text, TextView tvShow, boolean l) {
    if (tvShow == null) {
      return;
    } else if (text == null) {
      tvShow.setText(null);
      return;
    }

    //tvShow.setMovementMethod(ScrollingMovementMethod.getInstance());

    if (text instanceof Spanned) {
      tvShow.append((Spanned) text);
    } else {
      tvShow.append(String.format("%s", text));
    }
    if (l) {
      tvShow.append("\n");
    }
    int offset = tvShow.getLineCount() * tvShow.getLineHeight();
    if (offset > tvShow.getHeight()) {
      tvShow.scrollTo(0, offset - tvShow.getHeight());
    }
  }

  public static void appendShow(Object text, TextView tvShow) {
    appendShow(text, tvShow, true);
  }

  public static void appendShow(TextView tvShow, Object text) {
    appendShow(text, tvShow, true);
  }

  public static StateListDrawable getStateListDrawable(Context context, int idNormal, int idPress) {

    StateListDrawable listDrawable = new StateListDrawable();
    Drawable normal = context.getResources().getDrawable(idNormal);
    Drawable press = context.getResources().getDrawable(idPress);
    listDrawable.addState(new int[]{android.R.attr.state_pressed}, press);
    listDrawable.addState(new int[]{-android.R.attr.state_pressed}, normal);
    return listDrawable;
  }

  /**
   * @param pos 1:left, 2:top, 3:right, other:bottom
   */
  public static void setCompoundDrawables(TextView tv, int drawableRes, int pos) {
    Drawable dra = tv.getResources().getDrawable(drawableRes);
    dra.setBounds(0, 0, dra.getMinimumWidth(), dra.getMinimumHeight());
    if (pos == 1) {
      tv.setCompoundDrawables(dra, null, null, null);
    } else if (pos == 2) {
      tv.setCompoundDrawables(null, dra, null, null);
    } else if (pos == 3) {
      tv.setCompoundDrawables(null, null, dra, null);
    } else {
      tv.setCompoundDrawables(null, null, null, dra);
    }
  }
}
