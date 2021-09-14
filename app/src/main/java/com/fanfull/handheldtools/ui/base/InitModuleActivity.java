package com.fanfull.handheldtools.ui.base;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ToastUtils;
import com.fanfull.handheldtools.R;
import com.fanfull.libhard.rfid.RfidController;
import com.fanfull.libhard.uhf.UhfCmd;
import com.fanfull.libhard.uhf.UhfController;
import com.fanfull.libjava.util.ClockUtil;
import com.lxj.xpopup.XPopup;

public abstract class InitModuleActivity extends BaseActivity implements View.OnClickListener {
  protected UhfController uhfController;
  protected RfidController rfidController;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initModule();
  }

  protected void initModule(boolean openUhf, boolean openRfid) {
    uhfController = UhfController.getInstance();
    showLoadingView("正在初始化模块...");
    if (openUhf && !uhfController.open()) {
      dismissLoadingView();
      ToastUtils.showShort("初始化超高频失败");
      finish();
      return;
    }

    rfidController = RfidController.getInstance();
    if (openRfid && !rfidController.open()) {
      dismissLoadingView();
      ToastUtils.showShort("初始化高频失败");
      finish();
      return;
    }

    dismissLoadingView();
  }

  protected void onOpenSuccess(int type) {
  }

  protected void onOpenFailed(int type) {
  }

  protected void initModule() {
  }

  protected void unInitModule() {
    if (uhfController != null && uhfController.isOpen()) {
      uhfController.release();
      uhfController = null;
    }
    if (rfidController != null && rfidController.isOpen()) {
      rfidController.release();
      rfidController = null;
    }
  }

  @Override public void onClick(View v) {
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_PERIOD) {
      if (uhfController != null) {
        showSetUhfPower(this);
      }
    }
    return super.onKeyDown(keyCode, event);
  }

  @Override public void onBackPressed() {
    if (!ClockUtil.isFastDoubleClick()) {
      ToastUtils.showShort(R.string.text_click_again_quit);
      return;
    }
    super.onBackPressed();
  }

  @Override protected void onDestroy() {
    unInitModule();
    super.onDestroy();
  }

  protected void showSetUhfPower(Context context) {
    new XPopup.Builder(this).asInputConfirm(
        "输入功率", "功率范围：6 ~ 25,输入6.8即读写功率分别为6、8", "读功率.写功率", text -> {
          if (text == null) {
            return;
          }

          if (!text.matches("\\d+[\\.\\d+]*")) {
            ToastUtils.showLong("输入格式不合法，正确格式：6.8，读写功率分别为6、8");
            return;
          }

          String[] split = text.split("\\.");
          int r = Integer.parseInt(split[0]);
          int w = r;
          if (split.length == 2) {
            w = Integer.parseInt(split[1]);
          }
          if (UhfCmd.MAX_POWER < r || r < UhfCmd.MIN_POWER ||
              UhfCmd.MAX_POWER < w || w < UhfCmd.MIN_POWER) {
            ToastUtils.showShort("功率超出允许范围");
            return;
          }

          boolean b = uhfController.setPower(r, w, 0, true, false);
          String res1;
          if (b) {
            res1 = String.format("设置读/写功率成功：%s / %s", r, w);
          } else {
            res1 = "设置功率失败";
          }
          ToastUtils.showShort(res1);
        }).show();
  }
}
