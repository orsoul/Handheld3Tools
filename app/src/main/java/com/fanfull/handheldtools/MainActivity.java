package com.fanfull.handheldtools;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.apkfuns.logutils.LogUtils;
import com.fanfull.handheldtools.barcode.ActivityBarcode;
import com.fanfull.handheldtools.barcode.OldBagActivity;
import com.fanfull.handheldtools.base.BaseApplication;
import com.fanfull.handheldtools.uhf.ActivityNfc;
import com.fanfull.handheldtools.uhf.ActivityUhf;
import com.fanfull.handheldtools.uhf.FingerActivity;
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
        LogUtils.d("%s:  RepeatCount:%s Action:%s long:%s shift:%s meta:%X",
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
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_uhf:
                startActivity(new Intent(this, ActivityUhf.class));
                break;
            case R.id.btn_nfc:
                startActivity(new Intent(this, ActivityNfc.class));
                break;
            case R.id.btn_barcode:
                startActivity(new Intent(this, ActivityBarcode.class));
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
