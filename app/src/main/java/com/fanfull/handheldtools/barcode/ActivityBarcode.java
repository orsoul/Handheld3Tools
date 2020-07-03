package com.fanfull.handheldtools.barcode;

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
import com.fanfull.libhard.barcode.BarcodeUtil;
import com.fanfull.libhard.barcode.IBarcodeListener;
import com.fanfull.libhard.barcode.impl.BarcodeController;

import org.orsoul.baselib.util.ClickUtil;
import org.orsoul.baselib.util.HtmlUtil;
import org.orsoul.baselib.util.SoundUtils;
import org.orsoul.baselib.util.ThreadUtil;

public class ActivityBarcode extends InitModuleActivity {

    private Button btnScan;
    private Button btnStopScan;
    private TextView tvShow;
    private Switch switchRep;
    private Switch switchSound;

    private BarcodeController barcodeController;
    private int recCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_barcode);
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
        BarcodeController.initBarcodeController(this);
        barcodeController = BarcodeController.getInstance();
        //        barcodeController = BarcodeController.newInstance(barcodeController);

        showLoadingView("正在打开二维读头...");
        barcodeController.setBarcodeListener(new IBarcodeListener() {
            @Override
            public void onOpen() {
                runOnUi(() -> {
                    dismissLoadingView();
                    btnScan.setEnabled(true);
                    tvShow.setText("打开成功.enter->扫描,按键2->上电,按键5->下电");
                });
                //                barcodeController.startReadThread();
                //                btnScan.setEnabled(true);
            }

            @Override
            public void onScan() {
                runOnUi(() -> btnScan.setEnabled(false));
            }

            @Override
            public void onStopScan() {
                runOnUi(() -> btnScan.setEnabled(true));
            }

            @Override
            public void onReceiveData(byte[] data) {
                //                barcodeController.stopReadThread();
                byte[] myBarcode = BarcodeUtil.decodeBarcode(data);
                String barcode;
                if (myBarcode != null) {
                    barcode = new String(myBarcode);
                } else {
                    barcode = new String(data);
                }
                boolean repScan = switchRep.isChecked();
                runOnUi(() -> {
                    Spanned colorSpanned = HtmlUtil.getColorSpanned("\n%s: %s", ++recCount, barcode);
                    appendShow(colorSpanned);
                    //                    appendShow(String.format("\n%s: %s", ++recCount, barcode));
                    btnScan.setEnabled(!repScan);
                });

                if (repScan) {
                    barcodeController.scanAsync();
                }

                if (switchSound.isChecked()) {
                    SoundUtils.playToneScanOne();
                }
            }
        });

        ThreadUtil.execute(() -> barcodeController.open());
    }

    private void appendShow(Object text) {
        if (text instanceof Spanned) {
            tvShow.append((Spanned) text);
        } else {
            tvShow.append(String.valueOf(text));
        }
        int offset = tvShow.getLineCount() * tvShow.getLineHeight();
        if (offset > tvShow.getHeight()) {
            tvShow.scrollTo(0, offset - tvShow.getHeight());
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_barcode_scan:
                barcodeController.scanAsync();
                break;
            case R.id.btn_barcode_stopScan:
                barcodeController.cancelScan();
                break;
            case R.id.tv_barcode_show:
                if (ClickUtil.isFastDoubleClick()) {
                    tvShow.setText(null);
                    recCount = 0;
                }
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
            case KeyEvent.KEYCODE_ENTER:
                if (barcodeController.isScanning()) {
                    btnStopScan.performClick();
                } else if (btnScan.isEnabled()) {
                    btnScan.performClick();
                }
                break;
            case KeyEvent.KEYCODE_1:
                //                barcodeController.init(this);
                break;
            case KeyEvent.KEYCODE_2:
                barcodeController.powerOn();
                appendShow("\n开始上电");
                break;
            case KeyEvent.KEYCODE_3:
                //                barcodeController.scanAsync();
                break;
            case KeyEvent.KEYCODE_4:
                //                barcodeController.uninit();
                break;
            case KeyEvent.KEYCODE_5:
                barcodeController.powerOff();
                appendShow("\n已下电");
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
    public void onBackPressed() {
        //        LogUtils.d("onBackPressed");
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        barcodeController.release();
        barcodeController = null;
        super.onDestroy();
    }
}
