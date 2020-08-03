package com.fanfull.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.fanfull.initbag3.R;

/**
 * @author Administrator 自定义组合控件
 */
public class CoverBagItemView extends RelativeLayout {
  private TextView mTv;
  private ImageView mIm;
  private TextView mTvDes;

  /**
   * 配置文件中，反射实例化设置属性参数
   */
  public CoverBagItemView(Context context, AttributeSet attrs) {
    super(context, attrs);
    View item = View.inflate(getContext(), R.layout.item_cover_bag_activity,
        null);
    mTv = (TextView) item.findViewById(R.id.tv_item_coverbag);
    mTvDes = (TextView) item.findViewById(R.id.tv_item_coverbag_des);
    mIm = (ImageView) item.findViewById(R.id.im_item_coverbag);
    addView(item);


    /*
     * 首先通过context.obtainStyledAttributes获得所有属性数组;
     * 然后通过TypedArray类的getXXXX()系列接口获得相应的值。
     */
    TypedArray typedArray = context.obtainStyledAttributes(attrs,
        R.styleable.CoverBagItemView);

    CharSequence text = typedArray
        .getText(R.styleable.CoverBagItemView_android_text);
    if (text != null) {
      mTv.setText(text);
    }
    typedArray.recycle();
  }

  /**
   * 代码实例化调用该构造函数
   */
  public CoverBagItemView(Context context) {
    this(context, null);
  }

  /**
   * @description 设置控件文本
   */
  public void setText(String text) {
    mTv.setText(text);
  }

  /**
   * @description 设控件状态描述
   */
  public void setDesText(String text) {
    mTvDes.setText(text);
  }

  /**
   * @param Checked true=绿色钩 false=灰色钩
   * @description 设置状态
   */
  public void setChecked(boolean Checked) {
    mTvDes.setText("");
    if (Checked) {
      mTv.setEnabled(false);
      mIm.setEnabled(false);
      Drawable drawable = getResources().getDrawable(R.drawable.coverbag_checked);
      mIm.setImageDrawable(drawable);
    } else {
      mTv.setEnabled(true);
      mIm.setEnabled(true);
      Drawable drawable = getResources().getDrawable(R.drawable.coverbag_unchecked);
      mIm.setImageDrawable(drawable);
    }
  }

  public static final String DESC_DOING = "操作中..";
  public static final String DESC_FAILED = "操作失败!";

  /**
   * 设置 描述 信息
   *
   * @param doing true=操作中.. flase=操作失败!
   */
  public void setDoing(boolean doing) {
    if (doing) {
      mTvDes.setEnabled(false);
      mTvDes.setText(DESC_DOING);
      Drawable drawable = getResources().getDrawable(R.drawable.coverbag_unchecked);
      mIm.setImageDrawable(drawable);
    } else {
      mTvDes.setEnabled(true);
      mTvDes.setText(DESC_FAILED);
      Drawable drawable = getResources().getDrawable(R.drawable.coverbag_checked_failed);
      mIm.setImageDrawable(drawable);
    }
  }

  public void setDoing(boolean doing, String desc) {
    if (doing) {
      mTvDes.setEnabled(false);
      mTvDes.setText(desc);
      Drawable drawable = getResources().getDrawable(R.drawable.coverbag_unchecked);
      mIm.setImageDrawable(drawable);
    } else {
      mTvDes.setEnabled(true);
      mTvDes.setText(desc);
      Drawable drawable = getResources().getDrawable(R.drawable.coverbag_checked_failed);
      mIm.setImageDrawable(drawable);
    }
  }
}
