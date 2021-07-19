package org.orsoul.baselib.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.widget.EditText;

import com.lxj.xpopup.impl.InputConfirmPopupView;

public class MyInputPopupView extends InputConfirmPopupView {
  /**
   * @param bindLayoutId 在Confirm弹窗基础上需要增加一个id为et_input的EditText
   */
  public MyInputPopupView(Context context, int bindLayoutId) {
    super(context, bindLayoutId);
  }

  @Override protected void initPopupContent() {
    super.initPopupContent();

    EditText et_input = getEditText();
    et_input.post(() -> {
      //BitmapDrawable defaultDrawable = createBitmapDrawable(
      //    getResources(),
      //    et_input.getMeasuredWidth(),
      //    Color.parseColor("#888888"));
      //BitmapDrawable focusDrawable = createBitmapDrawable(
      //    getResources(), et_input.getMeasuredWidth(), XPopup.getPrimaryColor());
      ////et_input.setBackgroundDrawable(XPopupUtils.createSelector(defaultDrawable, focusDrawable));
      //et_input.setBackground(XPopupUtils.createSelector(defaultDrawable, focusDrawable));
      et_input.setBackground(null);
      if (0 < inputType) {
        et_input.setInputType(inputType);
      }
    });
    //if (inputConfirmListener1 != null) {
    //  getConfirmTextView().setOnClickListener(new OnClickListener() {
    //    @Override public void onClick(View view) {
    //      inputConfirmListener1.onConfirm();
    //    }
    //  });
    //}
  }

  int inputType = -1;

  /**
   * @param type InputType.TYPE_NUMBER_VARIATION_PASSWORD
   */
  public void setInputType(int type) {
    inputType = type;
    //EditText et_input = getEditText();
    //et_input.post(() -> et_input.setInputType(type));
  }

  public static BitmapDrawable createBitmapDrawable(Resources resources, int width, int color) {
    Bitmap bitmap = Bitmap.createBitmap(width, 20, Bitmap.Config.ARGB_4444);
    Canvas canvas = new Canvas(bitmap);
    Paint paint = new Paint();
    paint.setColor(color);
    canvas.drawRect(0, 0, bitmap.getWidth(), 4, paint);
    paint.setColor(Color.TRANSPARENT);
    canvas.drawRect(0, 4, bitmap.getWidth(), 20, paint);
    BitmapDrawable bitmapDrawable = new BitmapDrawable(resources, bitmap);
    bitmapDrawable.setGravity(Gravity.BOTTOM);
    return bitmapDrawable;
  }
}
