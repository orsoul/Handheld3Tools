package com.fanfull.handheldtools.ui;

import android.os.Bundle;
import android.view.KeyEvent;

import com.amitshekhar.DebugDB;
import com.apkfuns.logutils.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.fanfull.handheldtools.BuildConfig;
import com.fanfull.handheldtools.ui.base.BaseActivity;

import org.orsoul.baselib.util.DeviceInfoUtils;
import org.orsoul.baselib.util.SoundHelper;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutActivity extends BaseActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    AboutPage aboutPage = new AboutPage(this)
        .isRTL(false)
        .enableDarkMode(false)
        //.setCustomFont(String) // or Typeface
        //.setImage(R.drawable.ic_launcher_foreground)
        .setDescription("手持测试工具");

    Element element = new Element();
    element.setTitle(
        String.format("版本：%s_%s", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
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

  private int soundId = -1;

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    SoundHelper soundHelper = SoundHelper.getInstance();
    if (KeyEvent.KEYCODE_1 <= keyCode && keyCode <= KeyEvent.KEYCODE_8) {
      //
      //SoundUtils.playNumber(keyCode - KeyEvent.KEYCODE_0);
      SoundHelper.playNum(keyCode - KeyEvent.KEYCODE_0);
    } else if (KeyEvent.KEYCODE_PERIOD == keyCode) {
      SoundHelper.playNum((int) (Math.random() * 1000), 1.2F);
    } else {
      switch (keyCode) {
        case KeyEvent.KEYCODE_0:
          if (soundId < 1) {
            soundId = soundHelper.play(
                SoundHelper.TONE_DIDA, soundHelper.getVolume(0.7F), 0, 1);
          }
          LogUtils.d("%s:%s", SoundHelper.TONE_DIDA, soundId);
          break;
        case KeyEvent.KEYCODE_DEL:
          if (0 < soundId) {
            soundHelper.stop(soundId);
            soundId = -1;
          }
          break;
        case KeyEvent.KEYCODE_9:
          if (0 < soundId) {
            soundHelper.resume(soundId);
          }
          break;
        case KeyEvent.KEYCODE_SHIFT_LEFT:
        case KeyEvent.KEYCODE_F2:
          if (0 < soundId) {
            soundHelper.pause(soundId);
          }
          break;
      }
    }

    return super.onKeyDown(keyCode, event);
  }

  @Override public void onBackPressed() {
    super.onBackPressed();
  }
}
