package com.fanfull.handheldtools;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.apkfuns.logutils.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.fanfull.handheldtools.ui.AboutActivity;
import com.fanfull.handheldtools.ui.BagCheckActivity;
import com.fanfull.handheldtools.ui.BagSearchActivity;
import com.fanfull.handheldtools.ui.BarcodeActivity;
import com.fanfull.handheldtools.ui.CoverBagActivity;
import com.fanfull.handheldtools.ui.FingerActivity;
import com.fanfull.handheldtools.ui.InitBag3Activity;
import com.fanfull.handheldtools.ui.NfcActivity;
import com.fanfull.handheldtools.ui.OldBagActivity;
import com.fanfull.handheldtools.ui.SocketActivity;
import com.fanfull.handheldtools.ui.SoundActivity;
import com.fanfull.handheldtools.ui.UhfActivity;
import com.fanfull.handheldtools.ui.base.BaseActivity;
import com.fanfull.handheldtools.ui.view.SetIpPortHelper;
import com.fanfull.libhard.rfid.RfidController;
import com.fanfull.libhard.uhf.UhfController;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;

import org.orsoul.baselib.util.AppUtil;
import org.orsoul.baselib.util.CrashLogUtil;
import org.orsoul.baselib.util.DeviceInfoUtils;
import org.orsoul.baselib.util.SoundHelper;

public class MainActivity extends BaseActivity {
  @Override protected void onCreate(Bundle savedInstanceState) {
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
    findViewById(R.id.btn_sound).setOnClickListener(this);
    findViewById(R.id.btn_main_cover_bag).setOnClickListener(this);
    findViewById(R.id.btn_main_bag_search).setOnClickListener(this);

    //SoundUtils.loadSounds(MyApplication.getInstance());
    SoundHelper.loadSounds(MyApplication.getInstance());
    //SoundHelper.getInstance().loadTone(getApplicationContext());
    //SoundHelper.getInstance().loadNum(getApplicationContext());

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
        SetIpPortHelper.showIpPortSetting(this, (ip, port, s) -> {
          LogUtils.d("ip:%s:%s", ip, port);
          ToastUtils.showShort("ip:%s:%s", ip, port);
          return false;
        });
        break;
      case KeyEvent.KEYCODE_2:
        break;
      case KeyEvent.KEYCODE_3:
        DeviceInfoUtils.shutdown(true);
        break;
      case KeyEvent.KEYCODE_4:
        new XPopup.Builder(this)
            //.isDarkTheme(true)
            .hasShadowBg(true)
            //                            .hasBlurBg(true)
            //                            .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
            .asBottomList("请选择一项", new String[] { "条目1", "条目2", "条目3", "条目4", "条目5" },
                new OnSelectListener() {
                  @Override
                  public void onSelect(int position, String text) {
                    ToastUtils.showShort("click " + text);
                  }
                }).show();
        //DeviceInfoUtils.reboot(this);
        //DeviceInfoUtils.reboot2(this);
        break;
      case KeyEvent.KEYCODE_5:
        break;
      case KeyEvent.KEYCODE_6:
        CrashLogUtil.logException(new RuntimeException("test log exception"));
        break;
      case KeyEvent.KEYCODE_7:
        throw new RuntimeException("test crash");
      case KeyEvent.KEYCODE_8:
        break;
      case KeyEvent.KEYCODE_9:
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

  @Override protected void onDestroy() {
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

  @Override public void onClick(View v) {
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
      case R.id.btn_sound:
        startActivity(new Intent(this, SoundActivity.class));
        break;
      case R.id.btn_main_cover_bag:
        startActivity(new Intent(this, CoverBagActivity.class));
        break;
      case R.id.btn_main_bag_search:
        startActivity(new Intent(this, BagSearchActivity.class));
        break;
    }
  }
}
