package com.orsoul.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class MyProgressBar extends ProgressBar {

  private Paint mPaint;
  private Rect mRect;
  private String mProgressText = "未开始扫描";

  public MyProgressBar(Context context, AttributeSet attrs) {
    super(context, attrs);

    initPaint();
  }

  public MyProgressBar(Context context) {
    this(context, null);
  }

  /**
   * ��ʼ�� ����
   */
  private void initPaint() {
    mPaint = new Paint();
    mPaint.setColor(Color.WHITE);
  }

  @Override
  public synchronized void setProgress(int progress) {
    //        this.setText(progress);
    super.setProgress(progress);
  }

  @Override
  protected synchronized void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    if (null == mRect) {
      mRect = new Rect();
    }
    mPaint.getTextBounds(mProgressText, 0, mProgressText.length(), mRect);
    int x = getWidth() / 2 - mRect.centerX();
    int y = getHeight() / 2 - mRect.centerY();
    canvas.drawText(mProgressText, x, y, mPaint);
  }

  private void setText(int progress) {
    if (progress == getMax()) {
      this.mProgressText = "扫描完成";
    } else {
      this.mProgressText = "已扫描:" + (progress);
    }
  }

  public void setText(String text) {
    mProgressText = text;
    //        this.setProgress(this.getProgress());// 为了 让 myprogressbar 调用 onDraw()方法,从而更新 文字信息
  }
}
