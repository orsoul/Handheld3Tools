package com.fanfull.handheldtools.uhf;

import android.os.Bundle;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.handheldtools.R;
import com.fanfull.handheldtools.base.InitModuleActivity;
import com.fanfull.libhard.nfc.IRfidListener;
import com.fanfull.libhard.nfc.RfidController;

import org.orsoul.baselib.util.ArrayUtils;
import org.orsoul.baselib.util.SoundUtils;

import java.util.Arrays;
import java.util.Random;

public class ActivityNfc extends InitModuleActivity {

    private Button btnScan;
    private Button btnStopScan;
    private TextView tvShow;
    private Switch switchRep;
    private Switch switchSound;

    private RfidController nfcController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_nfc);
        tvShow = findViewById(R.id.tv_barcode_show);
        tvShow.setMovementMethod(ScrollingMovementMethod.getInstance());
        btnScan = findViewById(R.id.btn_barcode_scan);
        btnStopScan = findViewById(R.id.btn_barcode_stopScan);

        tvShow.setOnClickListener(this);
        btnScan.setOnClickListener(this);
        btnStopScan.setOnClickListener(this);

        switchRep = findViewById(R.id.switch_barcode_rep);
        switchSound = findViewById(R.id.switch_barcode_sound);
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
        //        nfcController.readNfcAsync(0x04, 64);
        byte[] nfcOrM1 = nfcController.findNfcOrM1();
        String info;
        if (nfcOrM1 != null) {
            SoundUtils.playInitSuccessSound();
            info = ArrayUtils.bytes2HexString(nfcOrM1);
        } else {
            SoundUtils.playFailedSound();
            info = "寻卡失败";
        }
        appendShow(info);
    }

    private void appendShow(Object text) {
        if (text instanceof Spanned) {
            tvShow.append((Spanned) text);
        } else {
            tvShow.append(String.format("\n%s", text));
        }
        int offset = tvShow.getLineCount() * tvShow.getLineHeight();
        if (offset > tvShow.getHeight()) {
            tvShow.scrollTo(0, offset - tvShow.getHeight());
        }
    }

    @Override
    public void onClick(View v) {
        String info;
        switch (v.getId()) {
            case R.id.btn_barcode_scan:
                //                byte[] block8 = nfcController.readM1(8);
                byte[] block8 = nfcController.read456Block();
                if (block8 != null) {
                    SoundUtils.playInitSuccessSound();
                    info = String.format("读8区成功：%s", ArrayUtils.bytes2HexString(block8));
                } else {
                    SoundUtils.playFailedSound();
                    info = "读失败";
                }
                appendShow(info);
                break;
            case R.id.btn_barcode_stopScan:
                byte[] data = new byte[48];
                Arrays.fill(data, (byte) new Random().nextInt(100));
                //                boolean res = nfcController.writeM1(8, data);
                boolean res = nfcController.write456Block(data, 1);
                if (res) {
                    SoundUtils.playInitSuccessSound();
                    info = String.format("写8区成功：%s", ArrayUtils.bytes2HexString(data));
                } else {
                    SoundUtils.playFailedSound();
                    info = "写8区失败";
                }
                appendShow(info);
                break;

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
            case KeyEvent.KEYCODE_0:
                byte[] bytes = new byte[16];
                Arrays.fill(bytes, (byte) 0x39);
                nfcController.writeM1(9, bytes);
                return true;
            case KeyEvent.KEYCODE_1:
            case KeyEvent.KEYCODE_2:
            case KeyEvent.KEYCODE_3:
            case KeyEvent.KEYCODE_4:
            case KeyEvent.KEYCODE_5:
                int status = keyCode - KeyEvent.KEYCODE_0;
                nfcController.writeStatus(status);
                return true;
            case KeyEvent.KEYCODE_6:
            case KeyEvent.KEYCODE_7:
            case KeyEvent.KEYCODE_8:
            case KeyEvent.KEYCODE_9:
                nfcController.readM1(keyCode - KeyEvent.KEYCODE_0);
                return true;
            default:
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
