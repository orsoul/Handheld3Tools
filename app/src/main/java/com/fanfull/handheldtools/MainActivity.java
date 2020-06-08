package com.fanfull.handheldtools;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.finger.FingerPrint;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FingerPrint.getInstance().open();
        FingerPrint.getInstance().startSearchFinger();
    }

    @Override
    protected void onDestroy() {
        FingerPrint.getInstance().stopSearchAndClose();
        super.onDestroy();
    }
}
