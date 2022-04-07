package com.fanfull.handheldtools.ui.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.handheldtools.MyApplication;
import com.fanfull.handheldtools.ui.AboutActivity;
import com.fanfull.handheldtools.ui.view.DialogUtil;

import org.orsoul.baselib.service.RestartAppService;
import org.orsoul.baselib.util.AppUtil;
import org.orsoul.baselib.util.SoundHelper;

public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener,
    MyApplication.NetworkCallback {

  protected DialogUtil dialogUtil;
  private BroadcastReceiver broadcastReceiver;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initView();
    dialogUtil = new DialogUtil(this);
    MyApplication.addNetworkCallback(this);
  }

  protected void regReceiver() {
    if (broadcastReceiver == null) {
      broadcastReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
          LogUtils.i("Action:%s", intent.getAction());
          ConnectivityManager cm =
              (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
          if (cm == null) {
            return;
          }
          NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
          if (activeNetworkInfo != null) {
            LogUtils.i("ActiveNetworkInfo:%s", activeNetworkInfo);
            NetworkInfo.State state = activeNetworkInfo.getState();
            onNetworkChange(activeNetworkInfo.getState() == NetworkInfo.State.CONNECTED);
            if (state == NetworkInfo.State.CONNECTED) {
              onNetworkChange(true);
            } else if (state == NetworkInfo.State.DISCONNECTED) {
              onNetworkChange(false);
            }
          } else {

          }
        }
      };
      IntentFilter filter = new IntentFilter();
      filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
      registerReceiver(broadcastReceiver, filter);
    }
  }

  @Override public void onNetworkChange(boolean isConnected) {
  }

  protected void unregReceiver() {
    if (broadcastReceiver != null) {
      unregisterReceiver(broadcastReceiver);
      //broadcastReceiver = null;
    }
  }

  @Override protected void onResume() {
    //regReceiver();
    super.onResume();
  }

  @Override protected void onPause() {
    unregReceiver();
    super.onPause();
  }

  protected void initView() {
  }

  protected void showLoadingView(String msg) {
    dialogUtil.showLoadingView(msg);
  }

  protected void showLoadingView() {
    dialogUtil.showLoadingView();
  }

  protected void dismissLoadingView() {
    dialogUtil.dismissLoadingView();
  }

  protected void showDialog(String info) {
    dialogUtil.showDialog(info);
  }

  protected void showDialogOnUi(String info) {
    runOnUiThread(() -> showDialog(info));
  }

  protected void onEnterPress() {
  }

  @Override public void onClick(View v) {
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    //LogUtils.v("%s:  RepeatCount:%s Action:%s long:%s shift:%s meta:%X",
    //    KeyEvent.keyCodeToString(keyCode),
    //    event.getRepeatCount(),
    //    event.getAction(),
    //    event.isLongPress(),
    //    event.isShiftPressed(),
    //    event.getMetaState()
    //);
    switch (keyCode) {
      case KeyEvent.KEYCODE_2:
        SoundHelper.setAudioVolume(true);
        break;
      case KeyEvent.KEYCODE_5:
        SoundHelper.setAudioVolume(8, true, true);
        break;
      case KeyEvent.KEYCODE_8:
        SoundHelper.setAudioVolume(false);
        break;
      case KeyEvent.KEYCODE_ENTER:
        onEnterPress();
        break;
      case KeyEvent.KEYCODE_SHIFT_LEFT:
      case KeyEvent.KEYCODE_F2:
        startActivity(new Intent(this, AboutActivity.class));
        break;
      default:
        return super.onKeyDown(keyCode, event);
    }

    return true;
  }

  protected void showRelaunchAppDialog(boolean mustRelaunch, String info) {
    dialogUtil.createAndShowDialog(mustRelaunch, "提示", info,
        "重启APP", mustRelaunch ? null : "退出", null, new DialogUtil.MyDialogOnClickListener() {
          @Override public void onClickPositive(DialogInterface dialog) {
            //AppUtil.exitAPP(false);
            RestartAppService.restartApp(BaseActivity.this, 0);
          }

          @Override public void onClickNegative(DialogInterface dialog) {
            AppUtil.exitAPP(true);
          }
        });
  }

  @Override
  protected void onDestroy() {
    LogUtils.i("onDestroy: %s ", this.getClass().getSimpleName());
    dialogUtil.destroy();
    //dialogUtil = null;
    MyApplication.removeNetworkCallback(this);
    super.onDestroy();
  }
}
