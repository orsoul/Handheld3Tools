package com.fanfull.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.fanfull.initbag3.R;

/**
 * @author Administrator 自定义组合控件
 */
public class ActivityHeadItemView extends FrameLayout {
  private TextView mTv;
  private ImageView mIm;

  /**
   * 配置文件中，反射实例化设置属性参数
   */
  public ActivityHeadItemView(Context context, AttributeSet attrs) {
    super(context, attrs);

    View item = View.inflate(getContext(), R.layout.view_activtiy_title,
        null);
    mTv = (TextView) item.findViewById(R.id.tv_item_activity_title);
    mIm = (ImageView) item.findViewById(R.id.im_item_activity_back);
    addView(item);

    /*
     * 首先通过context.obtainStyledAttributes获得所有属性数组;
     * 然后通过TypedArray类的getXXXX()系列接口获得相应的值。
     */
    TypedArray typedArray = context.obtainStyledAttributes(attrs,
        R.styleable.ActivityHeadItemView);

    CharSequence text = typedArray
        .getText(R.styleable.ActivityHeadItemView_android_text);
    if (text != null) {
      mTv.setText(text);
    }

    Drawable drawable = typedArray
        .getDrawable(R.styleable.ActivityHeadItemView_android_src);
    if (drawable != null) {
      mIm.setImageDrawable(drawable);
    }

    boolean showImage = typedArray.getBoolean(
        R.styleable.ActivityHeadItemView_showImage, true);
    if (showImage) {
      mIm.setVisibility(View.VISIBLE);
    } else {
      mIm.setVisibility(View.GONE);
    }

    typedArray.recycle();
  }

  /**
   * 代码实例化调用该构造函数
   */
  public ActivityHeadItemView(Context context) {
    this(context, null);
  }

  /**
   * 图片 设置点击事件
   */
  public void setBackClickListener(OnClickListener listener) {
    // 通过自定义组合控制，把事件传递给子组件
    mIm.setOnClickListener(listener);
  }

  public void setTitleClickListener(OnClickListener listener) {
    mTv.setOnClickListener(listener);
  }

  /**
   *
   */
  public void setText(String text) {
    mTv.setText(text);
  }
}
