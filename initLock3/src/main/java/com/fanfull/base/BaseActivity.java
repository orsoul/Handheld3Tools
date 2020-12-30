package com.fanfull.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import com.apkfuns.logutils.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.fanfull.contexts.MyContexts;
import com.fanfull.initbag3.R;
import com.fanfull.utils.DialogUtil;
import org.orsoul.baselib.util.ClockUtil;
import org.orsoul.baselib.util.SoundUtils;

public class BaseActivity extends Activity implements
    android.view.View.OnClickListener {

  protected String TAG = this.getClass().getSimpleName();

  protected ConnectivityManager mConnManager;
  //protected NetWorkBroadcastReceiver receiver;
  protected DialogUtil dialogUtil;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);

    dialogUtil = new DialogUtil(this);

    initView();

    initData();

    initEvent();
  }

  protected void initView() {
  }

  protected void initData() {
  }

  protected void initEvent() {
  }

  protected void runOnUi(Runnable runnable) {
    runOnUiThread(runnable);
  }

  @Override
  public void onClick(View v) {
    // 点击声音
    SoundUtils.playToneDrop();
  }

  @Override
  public void onUserInteraction() {
    super.onUserInteraction();
  }

  @Override
  protected void onStart() {
    super.onStart();
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    // unregisterReceiver(receiver);
  }

  @Override
  protected void onStop() {

    super.onStop();
  }

  @Override
  protected void onDestroy() {

    super.onDestroy();
    //if (null != receiver) {
    //  unregisterReceiver(receiver);
    //}
  }

  public void onClickBack(View v) {
    onBackPressed();
    //        finish();
  }

  protected class NetWorkBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

      String action = intent.getAction();
      LogUtils.i("action:" + action);

      if (MyContexts.ACTION_EXIT_APP.equals(action)) {
        LogUtils.i((context).getClass().getSimpleName() + "  exit");
        // 退出 app
        finish();
        //                if (context != null) {
        //                    if (context instanceof Activity) {
        //                        ((Activity) context).finish();
        //                    } else if (context instanceof FragmentActivity) {
        //                        ((FragmentActivity) context).finish();
        //                    } else if (context instanceof Service) {
        //                        ((Service) context).stopSelf();
        //                    }
        //                }
      } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
        NetworkInfo gprs = mConnManager
            .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = mConnManager
            .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!gprs.isConnected() || !wifi.isConnected()) {
          AlertDialog.Builder ab = new AlertDialog.Builder(
              BaseActivity.this);
          ab.setMessage("网络无法连接，请检查网络!");
          ab.setPositiveButton("确定",
              new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog,
                    int which) {
                  Intent intent = null;
                  /**
                   * 判断手机系统的版本！如果API大于10 就是3.0+
                   * 因为3.0以上的版本的设置和3.0以下的设置不一样，调用的方法不同
                   */
                  if (android.os.Build.VERSION.SDK_INT > 10) {
                    intent = new Intent(
                        android.provider.Settings.ACTION_WIFI_SETTINGS);
                  } else {
                    intent = new Intent();
                    ComponentName component = new ComponentName(
                        "com.android.settings",
                        "com.android.settings.WirelessSettings");
                    intent.setComponent(component);
                    intent.setAction("android.intent.action.VIEW");
                  }
                  startActivity(intent);
                }
              });
          ab.setNegativeButton("取消", null);
          ab.create().show();
        }
      }
    }
  }

  // @Override
  // public boolean onKeyDown(int keyCode, KeyEvent event) {
  // // 设置 返回键 事件
  // if (keyCode == KeyEvent.KEYCODE_BACK) {
  // // 按键音效
  // SoundUtils.play(SoundUtils.DROP_SOUND);
  // StaticString.information = null;
  //
  // Intent intent = new Intent();
  // intent.putExtra("falg", false);
  // setResult(2, intent);
  // finish();
  // }
  // return super.onKeyDown(keyCode, event);
  // }

  @Override
  public boolean onTouchEvent(MotionEvent event) {

    return super.onTouchEvent(event);
  }

  @Override public void onBackPressed() {
    if (ClockUtil.isFastDoubleClick()) {
      super.onBackPressed();
    } else {
      ToastUtils.showShort(R.string.login_again_click);
    }
  }
}
