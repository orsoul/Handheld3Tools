package com.fanfull.handheldtools;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.apkfuns.logutils.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.fanfull.handheldtools.ui.AboutActivity;
import com.fanfull.handheldtools.ui.BagCheckActivity;
import com.fanfull.handheldtools.ui.BarcodeActivity;
import com.fanfull.handheldtools.ui.FingerActivity;
import com.fanfull.handheldtools.ui.InitBag3Activity;
import com.fanfull.handheldtools.ui.NfcActivity;
import com.fanfull.handheldtools.ui.OldBagActivity;
import com.fanfull.handheldtools.ui.SocketActivity;
import com.fanfull.handheldtools.ui.UhfActivity;
import com.fanfull.handheldtools.ui.base.BaseActivity;
import com.fanfull.handheldtools.ui.view.SetIpPortView;
import com.fanfull.libhard.rfid.RfidController;
import com.fanfull.libhard.uhf.UhfController;

import org.orsoul.baselib.util.AppUtil;
import org.orsoul.baselib.util.CrashLogUtil;
import org.orsoul.baselib.util.SoundUtils;

public class MainActivity extends BaseActivity {

  //private AudioManager audio;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    findViewById(R.id.btn_uhf).setOnClickListener(this);
    findViewById(R.id.btn_nfc).setOnClickListener(this);
    findViewById(R.id.btn_init_bag3).setOnClickListener(this);
    findViewById(R.id.btn_check_bag).setOnClickListener(this);
    findViewById(R.id.btn_main_socket).setOnClickListener(this);
    findViewById(R.id.btn_barcode).setOnClickListener(this);
    findViewById(R.id.btn_finger).setOnClickListener(this);
    findViewById(R.id.btn_old_bag).setOnClickListener(this);

    SoundUtils.loadSounds(MyApplication.getInstance());

    //AutoCompleteTextView autoView = findViewById(R.id.auto);
    //String[] ips = new String[]{
    //        "192.168.11.177",
    //        "192.168.11.197",
    //        "192.168.11.107",
    //};
    //ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ips);
    //autoView.setAdapter(adapter);
    //DeviceInfo.showDeviceInfo();

    //new Thread() {
    //  @Override public void run() {
    //    try {
    //      SerialPort serial = SerialPort
    //          .newBuilder("/dev/ttyMT0", 115200)
    //          .flags(0)
    //          .parity('n')
    //          .build();
    //    } catch (IOException e) {
    //      e.printStackTrace();
    //    }
    //  }
    //}.start();
    //setCallback();
    //audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
  }

  @Override public void onNetworkChange(boolean isConnected) {
    LogUtils.d("网络已连接?:%s", isConnected);
    if (isConnected) {
      ToastUtils.showShort("网络已连接");
    } else {
      ToastUtils.showShort("网络断开");
    }
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    LogUtils.v("%s:  RepeatCount:%s Action:%s long:%s shift:%s meta:%X",
        KeyEvent.keyCodeToString(keyCode),
        event.getRepeatCount(),
        event.getAction(),
        event.isLongPress(),
        event.isShiftPressed(),
        event.getMetaState()
    );
    switch (keyCode) {
      case KeyEvent.KEYCODE_1:
        SetIpPortView.showIpPortSetting(this, (ip, port) -> {
          LogUtils.d("ip:%s:%s", ip, port);
          ToastUtils.showShort("ip:%s:%s", ip, port);
          return false;
        });
        break;
      case KeyEvent.KEYCODE_2:
        SoundUtils.setVolume(true);
        break;
      case KeyEvent.KEYCODE_3:
        break;
      case KeyEvent.KEYCODE_4:
      case KeyEvent.KEYCODE_5:
        SoundUtils.setVolume(8, true);
        break;
      case KeyEvent.KEYCODE_6:
        CrashLogUtil.logException(new RuntimeException("test log exception"));
        break;
      case KeyEvent.KEYCODE_7:
        throw new RuntimeException("test crash");
      case KeyEvent.KEYCODE_8:
        SoundUtils.setVolume(false);
        //audio.adjustStreamVolume(
        //    AudioManager.STREAM_MUSIC,
        //    AudioManager.ADJUST_LOWER,
        //    AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
        break;
      case KeyEvent.KEYCODE_9:
        new Thread(() -> {
          int t = 12 / (1 - 1);
        }).start();
        break;
      case KeyEvent.KEYCODE_SHIFT_LEFT:
      case KeyEvent.KEYCODE_F2:
        startActivity(new Intent(this, AboutActivity.class));
        break;
      default:
        return super.onKeyDown(keyCode, event);
    }

    return super.onKeyDown(keyCode, event);
  }

  @Override
  protected void onDestroy() {
    if (UhfController.getInstance().isOpen()) {
      UhfController.getInstance().release();
    }
    if (RfidController.getInstance().isOpen()) {
      RfidController.getInstance().release();
    }
    LogUtils.getLog2FileConfig().flushAsync();

    super.onDestroy();

    AppUtil.killProcess();
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_uhf:
        startActivity(new Intent(this, UhfActivity.class));
        break;
      case R.id.btn_nfc:
        startActivity(new Intent(this, NfcActivity.class));
        break;
      case R.id.btn_init_bag3:
        startActivity(new Intent(this, InitBag3Activity.class));
        break;
      case R.id.btn_main_socket:
        startActivity(new Intent(this, SocketActivity.class));
        break;
      case R.id.btn_barcode:
        startActivity(new Intent(this, BarcodeActivity.class));
        break;
      case R.id.btn_finger:
        startActivity(new Intent(this, FingerActivity.class));
        break;
      case R.id.btn_old_bag:
        startActivity(new Intent(this, OldBagActivity.class));
        break;
      case R.id.btn_check_bag:
        startActivity(new Intent(this, BagCheckActivity.class));
        break;
    }
  }
}
