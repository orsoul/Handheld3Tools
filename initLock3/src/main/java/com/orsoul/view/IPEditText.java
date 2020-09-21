package com.orsoul.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import com.fanfull.initbag3.R;

/**
 * 自定义控件实现ip地址特殊输入
 *
 * @author 2015-9-11
 */
public class IPEditText extends LinearLayout {

  private EditText mIP1;
  private EditText mIP2;
  private EditText mIP3;
  private EditText mIP4;

  private String mText1;
  private String mText2;
  private String mText3;
  private String mText4;

  public IPEditText(Context context, AttributeSet attrs) {
    super(context, attrs);

    /**
     * 初始化控件
     */
    View view = LayoutInflater.from(context).inflate(
        R.layout.custom_my_edittext, this);
    mIP1 = (EditText) findViewById(R.id.ip_first);
    mIP2 = (EditText) findViewById(R.id.ip_second);
    mIP3 = (EditText) findViewById(R.id.ip_third);
    mIP4 = (EditText) findViewById(R.id.ip_fourth);

    OnClickListener listener = new OnClickListener() {
      @Override
      public void onClick(View v) {

        ((EditText) v).setText("");
      }
    };

    mIP1.setOnClickListener(listener);
    mIP2.setOnClickListener(listener);
    mIP3.setOnClickListener(listener);
    mIP4.setOnClickListener(listener);

    setOnEditorActionListener(mIP1);
    setOnEditorActionListener(mIP2);
    setOnEditorActionListener(mIP3);
    setOnEditorActionListener(mIP4);

    OperatingEditText();
  }

  /**
   * 获得EditText中的内容,当每个Edittext的字符达到三位时,自动跳转到下一个EditText,当用户点击.时,
   * 下一个EditText获得焦点
   */
  private void OperatingEditText() {
    mIP1.addTextChangedListener(new TextWatcher() {

      @Override
      public void onTextChanged(CharSequence s, int start, int before,
          int count) {
        /**
         * 获得EditTe输入内容,做判断,如果大于255,提示不合法,当数字为合法的三位数下一个EditText获得焦点,
         * 用户点击啊.时,下一个EditText获得焦点
         */
        if (s != null && s.length() > 0) {
          if (s.length() > 2 || s.toString().trim().contains(".")) {
            if (s.toString().trim().contains(".")) {
              mText1 = s.toString().substring(0, s.length() - 1);
              mIP1.setText(mText1);
            } else {
              mText1 = s.toString().trim();
            }

            try {
              if (Integer.parseInt(mText1) > 255) {
                Toast.makeText(getContext(), "请输入合法的ip地址",
                    Toast.LENGTH_SHORT).show();
                delete(mIP1);
                return;
              }
            } catch (NumberFormatException nfe) {
              // 扑捉到 text 的格式错误, 忽略
              return;
            }

            mIP2.setFocusable(true);
            mIP2.requestFocus();
          }
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

    mIP2.addTextChangedListener(new TextWatcher() {

      @Override
      public void onTextChanged(CharSequence s, int start, int before,
          int count) {
        /**
         * 获得EditTe输入内容,做判断,如果大于255,提示不合法,当数字为合法的三位数下一个EditText获得焦点,
         * 用户点击啊.时,下一个EditText获得焦点
         */
        if (s != null && s.length() > 0) {
          if (s.length() > 2 || s.toString().trim().contains(".")) {
            if (s.toString().trim().contains(".")) {
              mText2 = s.toString().substring(0, s.length() - 1);
              mIP2.setText(mText2);
            } else {
              mText2 = s.toString().trim();
            }
            try {
              if (Integer.parseInt(mText2) > 255) {
                Toast.makeText(getContext(), "请输入合法的ip地址",
                    Toast.LENGTH_SHORT).show();
                delete(mIP2);
                return;
              }
            } catch (NumberFormatException nfe) {
              // 扑捉到 parseInt(text) 的格式错误, 忽略
              return;
            }

            mIP3.setFocusable(true);
            mIP3.requestFocus();
          }
        }

        /**
         * 当用户需要删除时,此时的EditText为空时,上一个EditText获得焦点
         */
        if (start == 0 && s.length() == 0) {
          mIP1.setFocusable(true);
          mIP1.requestFocus();
          mIP1.setSelection(mIP1.getText().length());
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

    mIP3.addTextChangedListener(new TextWatcher() {

      @Override
      public void onTextChanged(CharSequence s, int start, int before,
          int count) {
        /**
         * 获得EditTe输入内容,做判断,如果大于255,提示不合法,当数字为合法的三位数下一个EditText获得焦点,
         * 用户点击啊.时,下一个EditText获得焦点
         */
        if (s != null && s.length() > 0) {
          if (s.length() > 2 || s.toString().trim().contains(".")) {
            if (s.toString().trim().contains(".")) {
              mText3 = s.toString().substring(0, s.length() - 1);
              mIP3.setText(mText3);
            } else {
              mText3 = s.toString().trim();
            }

            try {
              if (Integer.parseInt(mText3) > 255) {
                Toast.makeText(getContext(), "请输入合法的ip地址",
                    Toast.LENGTH_SHORT).show();
                delete(mIP3);
                return;
              }
            } catch (NumberFormatException nfe) {
              // 扑捉到 parseInt(text) 的格式错误, 忽略
              return;
            }

            mIP4.setFocusable(true);
            mIP4.requestFocus();
          }
        }

        /**
         * 当用户需要删除时,此时的EditText为空时,上一个EditText获得焦点
         */
        if (start == 0 && s.length() == 0) {
          mIP2.setFocusable(true);
          mIP2.requestFocus();
          mIP2.setSelection(mIP2.getText().length());
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

    mIP4.addTextChangedListener(new TextWatcher() {

      @Override
      public void onTextChanged(CharSequence s, int start, int before,
          int count) {
        /**
         * 获得EditTe输入内容,做判断,如果大于255,提示不合法,当数字为合法的三位数下一个EditText获得焦点,
         * 用户点击啊.时,下一个EditText获得焦点
         */
        if (s != null && s.length() > 0) {
          mText4 = s.toString().trim();

          try {
            if (3 < mText4.length()
                || Integer.parseInt(mText4) > 255) {
              Toast.makeText(getContext(), "请输入合法的ip地址",
                  Toast.LENGTH_SHORT).show();
              delete(mIP4);
              return;
            }
          } catch (NumberFormatException nfe) {
            // 扑捉到 parseInt(text) 的格式错误, 忽略
            delete(mIP4);
            return;
          }
        }

        /**
         * 当用户需要删除时,此时的EditText为空时,上一个EditText获得焦点
         */
        if (start == 0 && s.length() == 0) {
          mIP3.setFocusable(true);
          mIP3.requestFocus();
          mIP3.setSelection(mIP3.getText().length());
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

  /**
   * @description 删除 EditText 中的 最后一个字符
   */
  protected void delete(EditText editText) {
    Editable editable = editText.getText();
    editable.delete(editable.length() - 1, editable.length());
  }

  /**
   * @return 返回IP, 若IP未输入完成, 返回null
   */
  public String getText() {
    if (isCompleted()) {
      return mText1 + "." + mText2 + "." + mText3 + "." + mText4;
    } else {
      return null;
    }
  }

  /**
   * @return 判断 ip 是否输入完成
   */
  public boolean isCompleted() {
    mText1 = mIP1.getText().toString();
    mText2 = mIP2.getText().toString();
    mText3 = mIP3.getText().toString();
    mText4 = mIP4.getText().toString();
    return !TextUtils.isEmpty(mText4) && !TextUtils.isEmpty(mText3)
        && !TextUtils.isEmpty(mText2) && !TextUtils.isEmpty(mText1);
  }

  public boolean setIp(String ip) {
    if (TextUtils.isEmpty(ip)) {
      return false;
    }
    String[] split = ip.split("\\.");
    if (4 != split.length) {
      return false;
    }
    mIP1.setText(split[0]);
    mIP2.setText(split[1]);
    mIP3.setText(split[2]);
    mIP4.setText(split[3]);

    return true;
  }

  private void setOnEditorActionListener(EditText et) {
    et.setOnEditorActionListener(new OnEditorActionListener() {

      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

        /*判断是否是“GO”键*/
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          /*隐藏软键盘*/
          InputMethodManager imm = (InputMethodManager) v
              .getContext().getSystemService(
                  Context.INPUT_METHOD_SERVICE);
          if (imm.isActive()) {
            imm.hideSoftInputFromWindow(
                v.getApplicationWindowToken(), 0);
          }
          return true;
        }
        return false;
      }
    });
  }
}
