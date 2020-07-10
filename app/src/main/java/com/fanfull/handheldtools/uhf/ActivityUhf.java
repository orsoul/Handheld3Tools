package com.fanfull.handheldtools.uhf;

import android.os.Bundle;
import android.view.KeyEvent;
import com.apkfuns.logutils.LogUtils;
import com.fanfull.handheldtools.R;
import com.fanfull.handheldtools.base.InitModuleActivity;
import com.fanfull.libhard.finger.impl.FingerPrintCmd;
import com.fanfull.libhard.uhf.IUhfListener;
import com.fanfull.libhard.uhf.UhfCmd;
import com.fanfull.libhard.uhf.UhfController;

public class ActivityUhf extends InitModuleActivity {

    private UhfController uhfController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void initView() {
        super.initView();
        setContentView(R.layout.activity_uhf);
    }

    @Override
    protected void initModule() {
        uhfController = UhfController.getInstance();
        uhfController.setListener(new IUhfListener() {
            @Override
            public void onOpen() {
                runOnUi(() -> {
                    dismissLoadingView();
                    //                    btnScan.setEnabled(true);
                    //                    tvShow.setText("打开成功.enter->扫描,按键2->上电,按键5->下电");
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

            }
        });
        showLoadingView("正在打开超高频读头...");
        uhfController.open();
    }

    @Override
    protected void onEnterPress() {
        //        LogUtils.d("onEnterPress");
        if (uhfController.isOpen()) {
            uhfController.send(UhfCmd.CMD_FAST_READ_EPC);
        }
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
                //                barcodeController.init(this);
              uhfController.send(FingerPrintCmd.CMD_GET_IMAGE);
                break;
            case KeyEvent.KEYCODE_2:
                break;
            case KeyEvent.KEYCODE_3:
                //                barcodeController.scanAsync();
                break;
            case KeyEvent.KEYCODE_4:
                //                barcodeController.uninit();
                break;
            case KeyEvent.KEYCODE_5:
                break;
            case KeyEvent.KEYCODE_6:
                //                barcodeController.cancelScan();
                break;
            case KeyEvent.KEYCODE_7:
                //                barcodeController.startReadThread();
                break;
            case KeyEvent.KEYCODE_8:
                //                barcodeController.stopReadThread();
                break;
            default:
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        uhfController.release();
        super.onDestroy();
    }
}
