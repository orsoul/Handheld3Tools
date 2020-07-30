package com.fanfull.handheldtools.ui;

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
import com.fanfull.libhard.rfid.IRfidListener;
import com.fanfull.libhard.rfid.RfidController;
import java.util.Arrays;
import java.util.Random;
import org.orsoul.baselib.util.ArrayUtils;
import org.orsoul.baselib.util.ClockUtil;
import org.orsoul.baselib.util.HtmlUtil;
import org.orsoul.baselib.util.SoundUtils;
import org.orsoul.baselib.util.ViewUtil;
import org.orsoul.baselib.util.lock.Lock3Bean;
import org.orsoul.baselib.util.lock.Lock3Util;

public class NfcActivity extends InitModuleActivity {

  private Button btnScan;
  private Button btnStopScan;
  private TextView tvShow;
  private Switch switchNfc;
  private boolean isNfcMode = true;

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

    switchNfc = findViewById(R.id.switch_nfc_mode);
    switchNfc.setOnCheckedChangeListener((buttonView, isChecked) -> {
      isNfcMode = isChecked;
      if (isChecked) {
        buttonView.setText("NFC模式");
      } else {
        buttonView.setText("M1模式");
      }
    });
  }

  @Override
  protected void initModule() {
    nfcController = RfidController.getInstance();
    nfcController.setListener(new IRfidListener() {
      @Override
      public void onOpen(boolean openSuccess) {
        runOnUi(() -> {
          dismissLoadingView();
          if (openSuccess) {
            tvShow.setText("初始化成功\n按键1 -> 写39\n按键2 -> 读39");
          } else {
            tvShow.setText("初始化失败\n");
          }
        });
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
      switchNfc.setChecked(nfcOrM1.length == 7);
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
    Object info;
    switch (v.getId()) {
      case R.id.btn_barcode_scan:
        if (isNfcMode) {
          /* nfc模式 */
          Lock3Bean lock3Bean = new Lock3Bean();
          lock3Bean.addBaseSa();
          boolean b = nfcController.readLockNfc(lock3Bean);
          if (b) {
            byte[] readbuf = lock3Bean.getInfoUnit(Lock3Bean.SA_STATUS).buff;
            byte[] t_14 = lock3Bean.getInfoUnit(Lock3Bean.SA_KEY_NUM).buff;
            byte[] mVbuf = lock3Bean.getInfoUnit(Lock3Bean.SA_VOLTAGE).buff;
            byte[] bagIdBuff = lock3Bean.getInfoUnit(Lock3Bean.SA_BAG_ID).buff;
            byte[] uidbuf = lock3Bean.uidBuff;

            float vReal = Lock3Util.parseV(mVbuf[3]);
            int status = Lock3Util.getStatus(readbuf[0], t_14[0] & 0x0F, uidbuf, true);

            Spanned colorSpanned = HtmlUtil.getColorSpanned(
                0x000099, "\n标志位:%s\n电压值:%s\n袋ID:%s\n工作模式:%s\n启用码:%s\n",
                Lock3Util.getStatusDesc(status),
                String.format("%.3f", vReal),
                ArrayUtils.bytes2HexString(bagIdBuff),
                readbuf[8] == (byte) 0xFF ? "测试模式" : "产品模式",
                ArrayUtils.bytes2HexString(readbuf, 4, 8));
            info = colorSpanned;
            SoundUtils.playInitSuccessSound();
          } else {
            SoundUtils.playFailedSound();
            info = "读失败";
          }
        } else {
          /* M1 模式 */
          byte[] block8 = nfcController.read456Block();
          if (block8 != null) {
            SoundUtils.playInitSuccessSound();
            info = String.format("读成功：%s\n%s", ArrayUtils.bytes2HexString(block8),
                new String(block8));
          } else {
            SoundUtils.playFailedSound();
            info = "读失败";
          }
        }
        appendShow(info);
        LogUtils.d(info);
        break;
      case R.id.btn_barcode_stopScan:
        if (isNfcMode) {
          /* nfc模式 */
          boolean writeStatus = nfcController.writeStatus(1);
          if (writeStatus) {
            SoundUtils.playInitSuccessSound();
            info = String.format("标志位成功写为 F1");
          } else {
            SoundUtils.playFailedSound();
            info = "写标志位失败";
          }
        } else {
          /* M1 模式 */
          byte[] data = new byte[48];
          Arrays.fill(data, (byte) new Random().nextInt(100));
          //                boolean res = nfcController.writeM1(8, data);
          boolean res = nfcController.write456Block(data, 1);
          if (res) {
            SoundUtils.playInitSuccessSound();
            info = String.format("写456区成功：%s", ArrayUtils.bytes2HexString(data));
          } else {
            SoundUtils.playFailedSound();
            info = "写8区失败";
          }
        }
        appendShow(info);
        break;
      case R.id.tv_barcode_show:
        if (3 == ClockUtil.fastClickTimes()) {
          tvShow.setText(null);
          appendShow("");
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
      case KeyEvent.KEYCODE_0:
      case KeyEvent.KEYCODE_1:
        byte[] bytes = new byte[16];
        Arrays.fill(bytes, (byte) 0x39);
        boolean b = nfcController.writeM1(9, bytes);
        if (b) {
          ViewUtil.appendShow("写第9区39 成功", tvShow);
        } else {
          ViewUtil.appendShow("写第9区 失败", tvShow);
        }
        return true;
      case KeyEvent.KEYCODE_2:
        byte[] block9 = nfcController.readM1(9);
        if (block9 != null) {
          ViewUtil.appendShow(String.format("9区：%s", ArrayUtils.bytes2HexString(block9)), tvShow);
        } else {
          ViewUtil.appendShow("读第9区失败", tvShow);
        }
        return true;
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
