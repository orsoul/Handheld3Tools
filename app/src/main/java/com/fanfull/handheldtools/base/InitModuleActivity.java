package com.fanfull.handheldtools.base;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.apkfuns.logutils.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.fanfull.handheldtools.R;

import org.orsoul.baselib.util.ClickUtil;

import androidx.annotation.Nullable;

public abstract class InitModuleActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initModule();
    }

    protected void initModule() {}

    protected void onEnterPress() {}

    protected void runOnUi(Runnable runnable) {
        runOnUiThread(runnable);
    }

    @Override
    public void onClick(View v) {}

    @Override
    public void onBackPressed() {
        if (!ClickUtil.isFastDoubleClick()) {
            ToastUtils.showShort(R.string.text_click_again_quit);
            return;
        }
        super.onBackPressed();
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
            case KeyEvent.KEYCODE_ENTER:
                onEnterPress();
                return true;
            case KeyEvent.KEYCODE_1:
                //                barcodeController.init(this);
                break;
            case KeyEvent.KEYCODE_2:
                break;
            case KeyEvent.KEYCODE_3:
                //                barcodeController.scan();
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
}
