package com.orsoul.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.fanfull.initbag3.R;

/**
 * 自定义控件实现 数字的输入
 *
 * @author 2015-10-8
 */
public class NumberInputer2 extends LinearLayout {

  private CheckBox mCb;
  private EditText mEtNumber;
  private Button mBtnPlus;
  private Button mBtnMinus;

  public NumberInputer2(Context context) {
    super(context, null);
  }

  public NumberInputer2(Context context, AttributeSet attrs) {
    super(context, attrs);
    /**
     * 初始化控件
     */
    View view = LayoutInflater.from(context).inflate(
        R.layout.number_inputer2, null);
    mBtnPlus = (Button) view.findViewById(R.id.tv_number_inputer_plus);
    mBtnMinus = (Button) view.findViewById(R.id.tv_number_inputer_minus);
    mEtNumber = (EditText) view.findViewById(R.id.et_number_inputer_num);
    mCb = (CheckBox) view.findViewById(R.id.cb_number_inputer2);
    addView(view);

    initEvent();
  }

  private void initEvent() {
    mBtnPlus.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        int num = getNumber() + 1;
        if (num <= 30) {
          setNumber(num);
        }
      }
    });
    mBtnMinus.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        int num = getNumber() - 1;
        if (0 < num) {
          setNumber(num);
        } else {
          setNumber(1);
        }
      }
    });
    mCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        mEtNumber.setEnabled(isChecked);
        mBtnPlus.setEnabled(isChecked);
        mBtnMinus.setEnabled(isChecked);
      }
    });
    // 限制 编辑框 输入的 数值 范围 在 1-30范围内
    mEtNumber.addTextChangedListener(new TextWatcher() {

      @Override
      public void onTextChanged(CharSequence s, int start, int before,
          int count) {
        try {
          Editable text = mEtNumber.getText();
          int num = Integer.parseInt(text.toString());
          if (30 < num) {
            text.delete(text.length() - 1, text.length());
          }
        } catch (NumberFormatException nfe) {
          // 编辑框可能会出现 空字符串, 忽略 parseInt()抛出的异常
        }
      }

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count,
          int after) {

      }

      @Override
      public void afterTextChanged(Editable s) {

      }
    });
  }

  public int getNumber() {
    try {
      return Integer.parseInt(mEtNumber.getText().toString());
    } catch (NumberFormatException nfe) {
      return 1;
    }
  }

  public void setText(Object text) {
    mCb.setText(String.valueOf(text));
  }

  public void setNumber(int num) {
    mEtNumber.setText(String.valueOf(num));
  }

  public void setChecked(boolean checked) {
    mCb.setChecked(checked);
  }

  @Override
  public void setFocusable(boolean focusable) {
    mEtNumber.setFocusable(focusable);
  }

  public void setEtOnEditorActionListener(
      TextView.OnEditorActionListener listener) {
    mEtNumber.setOnEditorActionListener(listener);
  }
}
