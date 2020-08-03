package com.fanfull.handheldtools;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.apkfuns.logutils.LogUtils;
import com.fanfull.handheldtools.base.BaseApplication;
import com.fanfull.handheldtools.ui.BarcodeActivity;
import com.fanfull.handheldtools.ui.FingerActivity;
import com.fanfull.handheldtools.ui.InitBag3Activity;
import com.fanfull.handheldtools.ui.NfcActivity;
import com.fanfull.handheldtools.ui.OldBagActivity;
import com.fanfull.handheldtools.ui.SocketActivity;
import com.fanfull.handheldtools.ui.UhfActivity;
import com.fanfull.libhard.rfid.RfidController;
import com.fanfull.libhard.uhf.UhfController;
import com.finger.FingerPrint;
import org.orsoul.baselib.util.CrashLogUtil;
import org.orsoul.baselib.util.SoundUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    findViewById(R.id.btn_uhf).setOnClickListener(this);
    findViewById(R.id.btn_nfc).setOnClickListener(this);
    findViewById(R.id.btn_init_bag3).setOnClickListener(this);
    findViewById(R.id.btn_main_socket).setOnClickListener(this);
    findViewById(R.id.btn_barcode).setOnClickListener(this);
    findViewById(R.id.btn_finger).setOnClickListener(this);
    findViewById(R.id.btn_old_bag).setOnClickListener(this);

    SoundUtils.loadSounds(BaseApplication.getContext());

    //        AutoCompleteTextView autoView = findViewById(R.id.auto);
    //        String[] ips = new String[]{
    //                "192.168.11.177",
    //                "192.168.11.197",
    //                "192.168.11.107",
    //        };
    //        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ips);
    //        autoView.setAdapter(adapter);
    //        DeviceInfo.showDeviceInfo();

    //        FingerPrint.getInstance().open();
    //        FingerPrint.getInstance().startSearchFinger();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
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
        FingerPrint.getInstance().open();
        break;
      case KeyEvent.KEYCODE_2:
        FingerPrint.getInstance().startSearchFinger();
        break;
      case KeyEvent.KEYCODE_3:
        FingerPrint.getInstance().stopSearchFinger();
        break;
      case KeyEvent.KEYCODE_7:
        throw new RuntimeException("test crash");
      case KeyEvent.KEYCODE_8:
        CrashLogUtil.logException(new RuntimeException("test log exception"));
        break;
      default:
        break;
    }

    return super.onKeyDown(keyCode, event);
  }

  @Override
  protected void onDestroy() {
    LogUtils.getLog2FileConfig().flushAsync();
    FingerPrint.getInstance().stopSearchAndClose();

    if (UhfController.getInstance().isOpen()) {
      UhfController.getInstance().release();
    }
    if (RfidController.getInstance().isOpen()) {
      RfidController.getInstance().release();
    }
    super.onDestroy();
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
    }
  }
}
