package com.fanfull.handheldtools.ui;

import android.os.Bundle;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.handheldtools.R;
import com.fanfull.handheldtools.ui.base.InitModuleActivity;
import com.fanfull.libhard.barcode.BarcodeUtil;
import com.fanfull.libhard.barcode.IBarcodeListener;
import com.fanfull.libhard.barcode.impl.BarcodeController;
import com.fanfull.libhard.rfid.IRfidListener;
import com.fanfull.libhard.rfid.RfidController;
import com.fanfull.libjava.util.BytesUtil;
import com.fanfull.libjava.util.ClockUtil;
import com.fanfull.libjava.util.ThreadUtil;

import org.orsoul.baselib.util.HtmlUtil;
import org.orsoul.baselib.util.SoundHelper;

public class OldBagActivity extends InitModuleActivity {

  private Button btnScan;
  private Button btnReadM1;
  private TextView tvShow;
  private Switch switchRep;
  private Switch switchSound;

  private BarcodeController barcodeController;
  private RfidController nfcController;
  private int recCount;
  private String barcode;
  private String m1Barcode;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void initView() {
    setContentView(R.layout.activity_old_bag);
    tvShow = findViewById(R.id.tv_barcode_show);
    tvShow.setMovementMethod(ScrollingMovementMethod.getInstance());
    btnScan = findViewById(R.id.btn_barcode_scan);
    btnReadM1 = findViewById(R.id.btn_barcode_stopScan);

    tvShow.setOnClickListener(this);
    btnScan.setOnClickListener(this);
    btnReadM1.setOnClickListener(this);

    switchRep = findViewById(R.id.switch_barcode_rep);
    switchSound = findViewById(R.id.switch_barcode_sound);
  }

  @Override
  protected void initModule() {
    BarcodeController.initBarcodeController(this);
    barcodeController = BarcodeController.getInstance();
    //        barcodeController = BarcodeController.newInstance(barcodeController);

    showLoadingView("???????????????...");
    barcodeController.setBarcodeListener(new IBarcodeListener() {
      @Override public void onOpen(boolean openSuccess) {
        runOnUiThread(() -> {
          dismissLoadingView();
          if (!openSuccess) {
            tvShow.append("\n????????????????????????");
            return;
          }
          btnScan.setEnabled(true);
          tvShow.append("\n????????????????????????.Enter -> ??????/????????????");
        });
        //                barcodeController.startReadThread();
        //                btnScan.setEnabled(true);
      }

      @Override public void onScan() {
        runOnUiThread(() -> btnScan.setText("????????????"));
      }

      @Override public void onStopScan() {
        runOnUiThread(() -> btnScan.setText("??????"));
        //runOnUiThread(() -> btnScan.setEnabled(true));
      }

      @Override public void onReceiveData(byte[] data) {
        //                barcodeController.stopReadThread();
        byte[] myBarcode = BarcodeUtil.decodeBarcode(data);
        if (myBarcode != null) {
          barcode = new String(myBarcode);
        } else {
          barcode = new String(data);
        }
        runOnUiThread(() -> {
          Spanned colorSpanned =
              HtmlUtil.getColorSpanned(0x000066, "\n%s - %s", barcode,
                  TextUtils.equals(barcode, m1Barcode));
          appendShow(colorSpanned);
          LogUtils.i("barcode:%s", barcode);
          //                    appendShow(String.format("\n%s: %s", ++recCount, barcode));
          btnScan.setText("??????");
        });
        SoundHelper.playToneBiu();
      }
    });

    nfcController = RfidController.getInstance();
    nfcController.setListener(new IRfidListener() {
      @Override public void onOpen(boolean openSuccess) {
        runOnUiThread(() -> {
          dismissLoadingView();
          if (!openSuccess) {
            tvShow.setText("\n???????????? ???????????????");
            return;
          }
          tvShow.setText("???????????? ???????????????. ??????2 -> ??????");
        });
      }

      @Override public void onReceiveData(byte[] data) {
        LogUtils.d("rec:%s", BytesUtil.bytes2HexString(data));
      }
    });
    RfidController.getInstance().open();
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
    String info;
    switch (v.getId()) {
      case R.id.btn_barcode_scan:
        if (barcodeController.isScanning()) {
          barcodeController.cancelScan();
        } else {
          barcodeController.scanAsync();
        }
        break;
      case R.id.btn_barcode_stopScan:
        btnReadM1.setEnabled(false);
        byte[] block8 = nfcController.read456Block();
        if (block8 != null) {
          SoundHelper.playToneSuccess();
          m1Barcode = new String(block8);
          Spanned colorSpanned =
              HtmlUtil.getColorSpanned(0x0000FF, "\n%s - %s", m1Barcode,
                  TextUtils.equals(barcode, m1Barcode));
          appendShow(colorSpanned);

          String hex = BytesUtil.bytes2HexString(block8);
          appendShow(String.format("\n%s", hex));
          LogUtils.i("M1 hex:%s", hex);
        } else {
          SoundHelper.playToneFailed();
          info = "???????????????";
          //appendShow(info);
        }
        //LogUtils.i(info);
        LogUtils.i("M1 block:%s", m1Barcode);
        btnReadM1.setEnabled(true);
        break;
      case R.id.tv_barcode_show:
        if (ClockUtil.isFastDoubleClick()) {
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
        btnScan.performClick();
        break;
      case KeyEvent.KEYCODE_1:
        //barcodeController.init(this);
        //appendShow("\n?????????");
        break;
      case KeyEvent.KEYCODE_2:
        btnReadM1.performClick();
        //barcodeController.powerOn();
        //appendShow("\n????????????");
        break;
      case KeyEvent.KEYCODE_3:
        //                barcodeController.scanAsync();
        break;
      case KeyEvent.KEYCODE_4:
        //barcodeController.uninit();
        //appendShow("\n????????????");
        break;
      case KeyEvent.KEYCODE_5:
        //barcodeController.powerOff();
        //appendShow("\n?????????");
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
    nfcController.release();
    nfcController = null;
    super.onDestroy();
  }
}
