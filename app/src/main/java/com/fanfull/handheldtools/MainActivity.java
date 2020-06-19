package com.fanfull.handheldtools;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.handheldtools.barcode.ActivityBarcode;
import com.finger.FingerPrint;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_barcode).setOnClickListener(this);

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
            case KeyEvent.KEYCODE_2:
            case KeyEvent.KEYCODE_3:
            case KeyEvent.KEYCODE_4:
            case KeyEvent.KEYCODE_5:
            case KeyEvent.KEYCODE_6:
                break;
            default:
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        FingerPrint.getInstance().stopSearchAndClose();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_barcode:
                startActivity(new Intent(this, ActivityBarcode.class));
                break;
        }
    }
}
