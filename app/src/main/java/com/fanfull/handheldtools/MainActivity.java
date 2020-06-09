package com.fanfull.handheldtools;

import android.os.Bundle;

import com.finger.FingerPrint;

import org.orsoul.baselib.util.DeviceInfo;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DeviceInfo.showDeviceInfo();

        FingerPrint.getInstance().open();
        FingerPrint.getInstance().startSearchFinger();
    }

    @Override
    protected void onDestroy() {
        FingerPrint.getInstance().stopSearchAndClose();
        super.onDestroy();
    }
}
