package com.fanfull.handheldtools.uhf;

import android.os.Bundle;
import android.view.View;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.handheldtools.R;
import com.fanfull.handheldtools.base.InitModuleActivity;
import com.fanfull.libhard.nfc.IRfidListener;
import com.fanfull.libhard.nfc.RfidController;

import org.orsoul.baselib.util.ArrayUtils;

public class ActivityNfc extends InitModuleActivity {

    private RfidController nfcController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
    }

    @Override
    protected void initModule() {
        nfcController = RfidController.getInstance();
        nfcController.setListener(new IRfidListener() {
            @Override
            public void onOpen() {
                runOnUi(() -> {
                    dismissLoadingView();
                });
            }

            @Override
            public void onScan() {

            }

            @Override
            public void onStopScan() {

            }

            @Override
            public void onReceiveData(byte[] data) {
                LogUtils.d("rec:%s", ArrayUtils.bytes2HexString(data));
            }
        });
        showLoadingView("正在打开高频读头...");
        nfcController.open();
    }

    @Override
    protected void onEnterPress() {
        //        nfcController.findNfcOrM1Async();
        nfcController.readNfcAsync(0x04, 64);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }

    @Override
    protected void initView() {
        super.initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
