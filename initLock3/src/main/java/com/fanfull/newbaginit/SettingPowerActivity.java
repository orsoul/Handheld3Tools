package com.fanfull.newbaginit;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import com.apkfuns.logutils.LogUtils;
import com.fanfull.base.BaseActivity;
import com.fanfull.contexts.MyContexts;
import com.fanfull.factory.ThreadPoolFactory;
import com.fanfull.initbag3.R;
import com.fanfull.operation.UHFOperation;
import com.fanfull.utils.SPUtils;

public class SettingPowerActivity extends BaseActivity {

  private TextView tvNormalRead;
  private TextView tvNormalWrite;
  private TextView tvDynamicRead;
  private TextView tvDynamicWrite;

  private SeekBar etNormalReadSeekBar;
  private SeekBar etNormalWriteSeekBar;
  private SeekBar etDynamicReadSeekBar;
  private SeekBar etDynamicWriteSeekBar;

  private Button mBtnSave;
  private Button mBtnCancel;
  private Button mBtnSetNormal;

  private UHFOperation mUHFOp = null;

  @Override
  protected void initView() {
    super.initView();
    setContentView(R.layout.activity_setting_power);

    tvNormalRead = (TextView) findViewById(R.id.tv_setting_power_normal_read_power);
    tvNormalWrite = (TextView) findViewById(R.id.tv_setting_power_normal_write_power);
    tvDynamicRead = (TextView) findViewById(R.id.tv_setting_power_dynamic_read_power);
    tvDynamicWrite = (TextView) findViewById(R.id.tv_setting_power_dynamic_write_power);

    etNormalReadSeekBar = (SeekBar) findViewById(R.id.seekBar_setting_power_normal_read_power);
    etNormalWriteSeekBar = (SeekBar) findViewById(R.id.seekBar_setting_power_normal_write_power);
    etDynamicReadSeekBar = (SeekBar) findViewById(R.id.seekBar_setting_power_dynamic_read_power);
    etDynamicWriteSeekBar = (SeekBar) findViewById(R.id.seekBar_setting_power_dynamic_write_power);

    mBtnSave = (Button) findViewById(R.id.btn_activity_setting_power_save);
    mBtnCancel = (Button) findViewById(R.id.btn_activity_setting_power_cancel);
    mBtnSetNormal = (Button) findViewById(R.id.btn_activity_setting_power_set_normal);
  }

  @Override
  protected void initData() {

    super.initData();

    mUHFOp = UHFOperation.getInstance();

    SystemClock.sleep(100); // 注意,主线程中休眠! 获取 UHFOperacation 实例后 要失眠一段时间才能 获取 功率
    int[] dwPower = new int[2];
    mUHFOp.getPower(dwPower);

    SPUtils.putInt(getApplicationContext(), MyContexts.KEY_NORMAL_READ_POWER, dwPower[0]);
    SPUtils.putInt(getApplicationContext(), MyContexts.KEY_NORMAL_WRITE_POWER, dwPower[1]);

    int normalRead = SPUtils.getInt(getApplicationContext(),
        MyContexts.KEY_NORMAL_READ_POWER, 20);
    int normalWrite = SPUtils.getInt(getApplicationContext(),
        MyContexts.KEY_NORMAL_WRITE_POWER, 20);
    int dynamicRead = SPUtils.getInt(getApplicationContext(),
        MyContexts.KEY_DYNAMIC_READ_POWER, 10);
    int dynamicWrite = SPUtils.getInt(getApplicationContext(),
        MyContexts.KEY_DYNAMIC_WRITE_POWER, 10);
    tvNormalRead.setText(String.valueOf(normalRead));
    //        tvNormalRead.addTextChangedListener(textWatcher1);
    tvNormalWrite.setText(String.valueOf(normalWrite));
    tvDynamicRead.setText(String.valueOf(dynamicRead));
    tvDynamicWrite.setText(String.valueOf(dynamicWrite));

    etNormalReadSeekBar.setProgress(normalRead * 4 - 20);//默认读功率控制Bar的当前值设置为功率当前值
    //        etNormalReadSeekBar.setOnSeekBarChangeListener(seekListener1);
    etNormalReadSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress,
          boolean fromUser) {
        //Log.i(TAG,"onProgressChanged");
        String s = Integer.toString(progress / 4 + 5);
        tvNormalRead.setText(s);
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });
    etNormalWriteSeekBar.setProgress(normalWrite * 4 - 20);//默认写功率控制Bar的当前值设置为功率当前值
    etNormalWriteSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        //Log.i(TAG,"onStopTrackingTouch");
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
        //Log.i(TAG,"onStartTrackingTouch");
      }

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress,
          boolean fromUser) {
        //Log.i(TAG,"onProgressChanged");

        String s = Integer.toString(progress / 4 + 5);

        tvNormalWrite.setText(s);
      }
    });
    etDynamicReadSeekBar.setProgress(dynamicRead * 4 - 20);//动态读功率控制Bar的当前值设置为功率当前值
    etDynamicReadSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        //Log.i(TAG,"onStopTrackingTouch");
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
        //Log.i(TAG,"onStartTrackingTouch");
      }

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress,
          boolean fromUser) {
        //Log.i(TAG,"onProgressChanged");

        String s = Integer.toString(progress / 4 + 5);

        tvDynamicRead.setText(s);
      }
    });
    etDynamicWriteSeekBar.setProgress(dynamicWrite * 4 - 20);//动态写功率控制Bar的当前值设置为功率当前值
    etDynamicWriteSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        //Log.i(TAG,"onStopTrackingTouch");
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
        //Log.i(TAG,"onStartTrackingTouch");
      }

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress,
          boolean fromUser) {
        //Log.i(TAG,"onProgressChanged");

        String s = Integer.toString(progress / 4 + 5);

        tvDynamicWrite.setText(s);
      }
    });
  }

  @Override
  protected void initEvent() {
    super.initEvent();

    ButtonListener listener = new ButtonListener();
    mBtnSave.setOnClickListener(listener);
    mBtnCancel.setOnClickListener(listener);
    mBtnSetNormal.setOnClickListener(listener);
  }

  private final int SET_POWER_SUCCESS = 1;
  private final int SET_POWER_FAILED = 2;
  private Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      switch (msg.what) {
        case SET_POWER_SUCCESS:
          Toast.makeText(getApplicationContext(), "设置功率成功!",
              Toast.LENGTH_SHORT).show();
          finish();
          break;
        case SET_POWER_FAILED:
          Toast.makeText(getApplicationContext(), "设置功率失败!",
              Toast.LENGTH_SHORT).show();
          break;
        default:
          break;
      }
    }
  };

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  ;

  class ButtonListener implements View.OnClickListener {
    int nRead;
    int nWrite;
    int dRead;
    int dWrite;

    @Override
    public void onClick(View v) {

      switch (v.getId()) {
        case R.id.btn_activity_setting_power_save:
          try {
            nRead = Integer.parseInt(tvNormalRead.getText().toString());
            nWrite = Integer.parseInt(tvNormalWrite.getText()
                .toString());
            dRead = Integer
                .parseInt(tvDynamicRead.getText().toString());
            dWrite = Integer.parseInt(tvDynamicWrite.getText()
                .toString());
          } catch (NumberFormatException e) {
            Toast.makeText(getApplicationContext(), "格式转换异常!",
                Toast.LENGTH_SHORT).show();
            LogUtils.d("格式转换异常!");
            return;
          }

          // 判断 功率数值 是否 合法
          if ((30 < nRead || nRead < 5) || (30 < nWrite || nWrite < 5)
              || (30 < dRead || dRead < 5)
              || (30 < dWrite || dWrite < 5)) {
            Toast.makeText(getApplicationContext(), "输入的功率必须在5-30之间!",
                Toast.LENGTH_SHORT).show();
            return;
          }

          ThreadPoolFactory.getNormalPool().execute(new Runnable() {
            @Override
            public void run() {
              if (mUHFOp.setPower(nRead, nWrite)) {
                SPUtils.putInt(getApplicationContext(),
                    MyContexts.KEY_NORMAL_READ_POWER, nRead);
                SPUtils.putInt(getApplicationContext(),
                    MyContexts.KEY_NORMAL_WRITE_POWER, nWrite);
                SPUtils.putInt(getApplicationContext(),
                    MyContexts.KEY_DYNAMIC_READ_POWER, dRead);
                SPUtils.putInt(getApplicationContext(),
                    MyContexts.KEY_DYNAMIC_WRITE_POWER, dWrite);
                mHandler.sendEmptyMessage(SET_POWER_SUCCESS);
              } else {
                mHandler.sendEmptyMessage(SET_POWER_FAILED);
              }
            }
          });

          break;
        case R.id.btn_activity_setting_power_cancel:
          finish();
          break;
        case R.id.btn_activity_setting_power_set_normal:
          tvNormalRead.setText(String.valueOf(20));
          etNormalReadSeekBar.setProgress(
              Integer.valueOf(tvNormalRead.getText().toString()) * 4 - 20);
          tvNormalWrite.setText(String.valueOf(20));
          etNormalWriteSeekBar.setProgress(
              Integer.valueOf(tvNormalWrite.getText().toString()) * 4 - 20);
          tvDynamicRead.setText(String.valueOf(10));
          etDynamicReadSeekBar.setProgress(
              Integer.valueOf(tvDynamicRead.getText().toString()) * 4 - 20);
          tvDynamicWrite.setText(String.valueOf(10));
          etDynamicWriteSeekBar.setProgress(
              Integer.valueOf(tvDynamicWrite.getText().toString()) * 4 - 20);
          break;

        default:
          break;
      }
    }
  }

  private OnSeekBarChangeListener seekListener1 = new OnSeekBarChangeListener() {
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
      //Log.i(TAG,"onStopTrackingTouch");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
      //Log.i(TAG,"onStartTrackingTouch");
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
        boolean fromUser) {
      //Log.i(TAG,"onProgressChanged");

      String s = Integer.toString(progress / 4 + 5);

      tvNormalRead.setText(s);
    }
  };

  private OnSeekBarChangeListener seekListener2 = new OnSeekBarChangeListener() {
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
      //Log.i(TAG,"onStopTrackingTouch");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
      //Log.i(TAG,"onStartTrackingTouch");
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
        boolean fromUser) {
      //Log.i(TAG,"onProgressChanged");

      String s = Integer.toString(progress / 4 + 5);

      tvNormalWrite.setText(s);
    }
  };

  private OnSeekBarChangeListener seekListener3 = new OnSeekBarChangeListener() {
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
      //Log.i(TAG,"onStopTrackingTouch");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
      //Log.i(TAG,"onStartTrackingTouch");
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
        boolean fromUser) {
      //Log.i(TAG,"onProgressChanged");

      String s = Integer.toString(progress / 4 + 5);

      tvDynamicRead.setText(s);
    }
  };

  private OnSeekBarChangeListener seekListener4 = new OnSeekBarChangeListener() {
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
      //Log.i(TAG,"onStopTrackingTouch");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
      //Log.i(TAG,"onStartTrackingTouch");
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
        boolean fromUser) {
      //Log.i(TAG,"onProgressChanged");

      String s = Integer.toString(progress / 4 + 5);

      tvDynamicWrite.setText(s);
    }
  };

  //    private TextWatcher textWatcher1 = new TextWatcher() {
  //
  //        @Override
  //        public void onTextChanged(CharSequence s, int start, int before,
  //        int count) {
  //        //
  ////        System.out.println("-1-onTextChanged-->"
  ////        + edText.getText().toString() + "<--");
  //        }
  //
  //        @Override
  //        public void beforeTextChanged(CharSequence s, int start, int count,
  //        int after) {
  //        //
  ////        System.out.println("-2-beforeTextChanged-->"
  ////        + edText.getText().toString() + "<--");
  //
  //        }
  //
  //        @Override
  //        public void afterTextChanged(Editable s) {
  //        //
  ////        System.out.println("-3-afterTextChanged-->"
  ////        + edText.getText().toString() + "<--");
  //        int value = Integer.valueOf(tvNormalRead.getText().toString());
  //        etNormalReadSeekBar.setProgress(value*4);
  //        }
  //        };

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {

    if (keyCode == KeyEvent.KEYCODE_4) {
      finish();
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }
}
