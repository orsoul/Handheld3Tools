package com.fanfull.activity;

import android.os.Bundle;
import com.amitshekhar.DebugDB;
import com.apkfuns.logutils.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.fanfull.base.BaseActivity;
import com.fanfull.initbag3.BuildConfig;
import com.fanfull.initbag3.R;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;
import org.orsoul.baselib.util.DeviceInfoUtils;

public class AboutActivity extends BaseActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    AboutPage aboutPage = new AboutPage(this)
        .isRTL(false)
        .enableDarkMode(false)
        //.setCustomFont(String) // or Typeface
        .setImage(R.drawable.login_logo)
        .setDescription("锁3初始化APP，非技术人员请谨慎操作");

    Element element = new Element();
    element.setTitle(
        String.format("软件版本：%s_%s", BuildConfig.VERSION_NAME, BuildConfig.BUILD_TYPE));
    aboutPage.addItem(element);

    element = new Element();
    element.setTitle(String.format("设备IP：%s", NetworkUtils.getIPAddress(true)));
    aboutPage.addItem(element);

    element = new Element();
    element.setTitle(String.format("设备型号：%s\n设备号：\n%s",
        DeviceInfoUtils.getMyDeviceModel(),
        DeviceInfoUtils.getMyDeviceId()));
    aboutPage.addItem(element);

    if (BuildConfig.DEBUG) {
      String addressLog = DebugDB.getAddressLog();
      LogUtils.i("DebugDB:%s", addressLog);
      element = new Element();
      element.setTitle(String.format("数据管理：\n%s", addressLog));
      aboutPage.addItem(element);
    }

    setContentView(aboutPage.create());
  }

  @Override public void onBackPressed() {
    finish();
  }
}
