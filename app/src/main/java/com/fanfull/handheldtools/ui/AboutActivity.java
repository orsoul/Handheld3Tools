package com.fanfull.handheldtools.ui;

import android.os.Bundle;
import android.view.KeyEvent;

import com.apkfuns.logutils.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.fanfull.handheldtools.BuildConfig;
import com.fanfull.handheldtools.R;
import com.fanfull.handheldtools.ui.base.BaseActivity;

import org.orsoul.baselib.NetworkCallbackApplication;
import org.orsoul.baselib.util.DeviceInfoUtils;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutActivity extends BaseActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    AboutPage aboutPage = new AboutPage(this)
        .isRTL(false)
        //.enableDarkMode(false)
        //.setCustomFont(String) // or Typeface
        .setImage(R.mipmap.ic_launcher)
        .setDescription("手持测试工具");

    Element element = new Element();
    element.setTitle(
        String.format("版本：%s_%s", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
    aboutPage.addItem(element);

    element = new Element();
    element.setTitle(String.format("设备IP：%s", NetworkUtils.getIPAddress(true)));
    aboutPage.addItem(element);

    aboutPage.addGroup("devices");

    element = new Element();
    element.setTitle(String.format("设备型号：%s\n设备号：\n%s",
        DeviceInfoUtils.getMyDeviceModel(),
        DeviceInfoUtils.getMyDeviceId()));
    aboutPage.addItem(element);

    if (BuildConfig.DEBUG) {
      String addressLog = NetworkCallbackApplication.getDebugDBAddress();
      LogUtils.i("DebugDB:%s", addressLog);
      element = new Element();
      element.setTitle(String.format("数据管理：\n%s", addressLog));
      aboutPage.addItem(element);
    }

    setContentView(aboutPage.create());
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    return super.onKeyDown(keyCode, event);
  }

  @Override public void onBackPressed() {
    super.onBackPressed();
  }
}
