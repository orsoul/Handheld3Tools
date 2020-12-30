package com.fanfull.handheldtools.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.apkfuns.logutils.LogUtils;
import com.fanfull.handheldtools.ui.view.DialogUtil;

public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener,
    BaseApplication.NetworkCallback {

  protected DialogUtil dialogUtil;
  private BroadcastReceiver broadcastReceiver;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initView();
    dialogUtil = new DialogUtil(this);
    BaseApplication.addNetworkCallback(this);
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

  @Override
  public void onClick(View v) {
  }

  @Override
  protected void onDestroy() {
    LogUtils.i("onDestroy: %s ", this.getClass().getSimpleName());
    dialogUtil.destroy();
    dialogUtil = null;
    BaseApplication.removeNetworkCallback(this);
    super.onDestroy();
  }
}
