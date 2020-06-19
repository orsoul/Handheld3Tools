package com.fanfull.handheldtools.barcode;

import android.os.Bundle;
import android.view.KeyEvent;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.handheldtools.R;
import com.fanfull.libhard.barcode.BarcodeController;
import com.fanfull.libhard.barcode.BarcodeOperationRd;
import com.fanfull.libhard.barcode.IBarcodeListener;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityBarcode extends AppCompatActivity {

    BarcodeOperationRd barcodeOp;
    BarcodeController barcodeController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);

        barcodeOp = new BarcodeOperationRd();
        barcodeController = BarcodeController.newInstance(barcodeOp);

        new Thread(new Runnable() {
            @Override
            public void run() {
                barcodeController.open(ActivityBarcode.this);
            }
        }).start();

        barcodeController.setBarcodeListener(new IBarcodeListener() {
            @Override
            public void onReceiveData(byte[] data) {
                barcodeController.stopReadThread();
            }
        });
        barcodeController.startReadThread();
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
            case KeyEvent.KEYCODE_1:
                barcodeOp.init(this);
                break;
            case KeyEvent.KEYCODE_2:
                barcodeOp.powerOn();
                break;
            case KeyEvent.KEYCODE_3:
                barcodeOp.scan();
                break;
            case KeyEvent.KEYCODE_4:
                barcodeOp.uninit();
                break;
            case KeyEvent.KEYCODE_5:
                barcodeOp.powerOff();
                break;
            case KeyEvent.KEYCODE_6:
                barcodeOp.cancelScan();
                break;
            case KeyEvent.KEYCODE_7:
                barcodeController.startReadThread();
                break;
            case KeyEvent.KEYCODE_8:
                barcodeController.stopReadThread();
                break;
            default:
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        barcodeOp.uninit();
        super.onDestroy();
    }
}
