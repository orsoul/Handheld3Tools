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
import com.fanfull.handheldtools.ui.base.InitModuleActivity;
import com.fanfull.libhard.lock_zc.PsamHelper;
import com.fanfull.libhard.rfid.APDUParser;
import com.fanfull.libhard.rfid.IRfidListener;
import com.fanfull.libhard.rfid.PSamCmd;
import com.fanfull.libhard.rfid.RfidController;
import com.fanfull.libjava.util.BytesUtil;
import com.fanfull.libjava.util.ClockUtil;
import com.fanfull.libjava.util.ThreadUtil;

import org.orsoul.baselib.lock3.Lock3Util;
import org.orsoul.baselib.lock3.bean.Lock3Bean;
import org.orsoul.baselib.util.HtmlUtil;
import org.orsoul.baselib.util.SoundHelper;
import org.orsoul.baselib.util.ViewUtil;

import java.util.Arrays;
import java.util.Random;

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
        buttonView.setText("NFC??????");
      } else {
        buttonView.setText("M1??????");
      }
    });
  }

  @Override
  protected void initModule() {
    nfcController = RfidController.getInstance();
    nfcController.setListener(new IRfidListener() {
      @Override
      public void onOpen(boolean openSuccess) {
        runOnUiThread(() -> {
          dismissLoadingView();
          if (openSuccess) {
            tvShow.setText("???????????????\n??????1 -> ???39\n??????2 -> ???39\n??????0 -> psam?????????\n??????6 -> cpu???\n");
          } else {
            tvShow.setText("???????????????\n");
          }
        });
      }

      @Override
      public void onReceiveData(byte[] data) {
        LogUtils.d("rec:%s", BytesUtil.bytes2HexString(data));
      }
    });
    showLoadingView("????????????????????????...");
    nfcController.open();
  }

  @Override
  protected void onEnterPress() {
    //        nfcController.findNfcOrM1Async();
    //        nfcController.readNfcAsync(0x04, 64);
    byte[] nfcOrM1 = nfcController.findNfcOrM1();
    String info;
    if (nfcOrM1 != null) {
      SoundHelper.playToneSuccess();
      info = BytesUtil.bytes2HexString(nfcOrM1);
      switchNfc.setChecked(nfcOrM1.length == 7);
    } else {
      SoundHelper.playToneFailed();
      info = "????????????";
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
          /* nfc?????? */
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
            int status = Lock3Util.getStatus(readbuf[0], t_14[0] & 0x0F, uidbuf, false);

            Spanned colorSpanned = HtmlUtil.getColorSpanned(
                0x000099, "\n?????????:%s\n?????????:%s\n???ID:%s\n",
                Lock3Util.getStatusDesc(status),
                String.format("%.3f", vReal),
                BytesUtil.bytes2HexString(bagIdBuff));
            info = colorSpanned;
            SoundHelper.playToneSuccess();
          } else {
            SoundHelper.playToneFailed();
            info = "?????????";
          }
        } else {
          /* M1 ?????? */
          byte[] block8 = nfcController.read456Block();
          if (block8 != null) {
            SoundHelper.playToneSuccess();
            info = String.format("????????????%s\n%s", BytesUtil.bytes2HexString(block8),
                new String(block8));
          } else {
            SoundHelper.playToneFailed();
            info = "?????????";
          }
        }
        appendShow(info);
        LogUtils.d(info);
        break;
      case R.id.btn_barcode_stopScan:
        if (isNfcMode) {
          /* nfc?????? */
          boolean writeStatus = nfcController.writeStatus(1);
          if (writeStatus) {
            SoundHelper.playToneSuccess();
            info = String.format("????????????????????? F1");
          } else {
            SoundHelper.playToneFailed();
            info = "??????????????????";
          }
        } else {
          /* M1 ?????? */
          byte[] data = new byte[48];
          Arrays.fill(data, (byte) new Random().nextInt(100));
          //                boolean res = nfcController.writeM1(8, data);
          boolean res = nfcController.write456Block(data, 1);
          if (res) {
            SoundHelper.playToneSuccess();
            info = String.format("???456????????????%s", BytesUtil.bytes2HexString(data));
          } else {
            SoundHelper.playToneFailed();
            info = "???8?????????";
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
    byte[] pSamBuff = new byte[128];
    int len;
    switch (keyCode) {
      case KeyEvent.KEYCODE_0:
        len = nfcController.send2PSam(PSamCmd.CMD_PSAM_GET_CHALLENGE, pSamBuff);
        if (0 < len) {
          String s = BytesUtil.bytes2HexString(pSamBuff, len);
          if (APDUParser.checkReply(pSamBuff, len)) {
            ViewUtil.appendShow("psam??????????????? ??????:" + s, tvShow);
          } else {
            ViewUtil.appendShow("psam??????????????? ??????:" + s, tvShow);
          }
        } else {
          ViewUtil.appendShow("psam??????????????? ????????????", tvShow);
        }
        return true;
      case KeyEvent.KEYCODE_1:
        //len = nfcController.send2PSam(PSamCmd.genCmdGetInfo(1, 0), pSamBuff);
        //if (0 < len) {
        //  String s = BytesUtil.bytes2HexString(pSamBuff, len);
        //  if (APDUParser.checkReply(pSamBuff, len)) {
        //    ViewUtil.appendShow("???????????? ??????:" + s, tvShow);
        //  } else {
        //    ViewUtil.appendShow("???????????? ??????:" + s, tvShow);
        //  }
        //} else {
        //  ViewUtil.appendShow("???????????? ????????????", tvShow);
        //}
        //return true;
        byte[] bytes = new byte[16];
        Arrays.fill(bytes, (byte) 0x39);
        boolean b = nfcController.writeM1(9, bytes);
        if (b) {
          ViewUtil.appendShow("??????9???39 ??????", tvShow);
        } else {
          ViewUtil.appendShow("??????9??? ??????", tvShow);
        }
        return true;
      case KeyEvent.KEYCODE_2:
        byte[] block9 = nfcController.readM1(9);
        if (block9 != null) {
          ViewUtil.appendShow(String.format("9??????%s", BytesUtil.bytes2HexString(block9)), tvShow);
        } else {
          ViewUtil.appendShow("??????9?????????", tvShow);
        }
        return true;
      case KeyEvent.KEYCODE_3:
        ThreadUtil.execute(() -> testReadNfc());
        break;
      case KeyEvent.KEYCODE_4:
        ThreadUtil.execute(() -> testReadNfc2());
        break;
      case KeyEvent.KEYCODE_5:
        int status = keyCode - KeyEvent.KEYCODE_0;
        nfcController.writeStatus(status);
        return true;
      case KeyEvent.KEYCODE_6:
        //String cpuCard = readCpuCard();
        String cpuCard = PsamHelper.readCpuCard();
        if (null != cpuCard) {
          ViewUtil.appendShow("cpuCard:" + cpuCard, tvShow);
        } else {
          ViewUtil.appendShow("??????cpuCard ??????", tvShow);
        }
        return true;
      case KeyEvent.KEYCODE_7:
        byte[] verifyUser = new byte[2];
        int verifyUserLen = nfcController.send2PSam(PSamCmd.getCmdVerifyUser(), verifyUser);
        if (0 < verifyUserLen) {
          String s = BytesUtil.bytes2HexString(verifyUser, verifyUserLen);
          ViewUtil.appendShow("???????????? ??????:" + s, tvShow);
        } else {
          ViewUtil.appendShow("???????????? ??????", tvShow);
        }
        return true;
      case KeyEvent.KEYCODE_8:
        byte[] sta = new byte[2];
        byte[] epc = BytesUtil.hexString2Bytes("000F46311000000000000000");
        byte[] elsData = BytesUtil.hexString2Bytes("0100000000000000000000000000");
        int l1 = nfcController.send2PSam(PSamCmd.getCmdGenElsCmd(1, epc, elsData), sta);
        if (0 < l1) {
          String s = BytesUtil.bytes2HexString(sta, l1);
          ViewUtil.appendShow("?????? ??????:" + s, tvShow);
        } else {
          ViewUtil.appendShow("?????? ??????", tvShow);
        }
        return true;
      case KeyEvent.KEYCODE_9:
        nfcController.readM1(keyCode - KeyEvent.KEYCODE_0);
        return true;
      default:
        break;
    }
    return super.onKeyDown(keyCode, event);
  }

  private void testReadNfc() {
    int len;
    ClockUtil.runTime(true);
    byte[] nfc = nfcController.findNfc();
    LogUtils.d("findCard:%s", ClockUtil.runTime(true));
    byte[] data = new byte[200];
    boolean readNfc = nfcController.readNfc(4, data, false);
    long runTime = ClockUtil.runTime();
    LogUtils.d("readNfc:%s,%s,%s", runTime, readNfc, BytesUtil.bytes2HexString(data));
  }

  private void testReadNfc2() {
    int len = 0;
    byte[] data = new byte[4];
    Random random = new Random();
    ClockUtil.runTime(true);
    byte[] nfc = nfcController.findNfc();
    LogUtils.d("findCard:%s", ClockUtil.runTime(true));
    for (int i = 0; i < 50; i++) {
      int sa = random.nextInt(100) + 4;
      boolean readNfc = nfcController.readNfc(sa, data, false);
      if (readNfc) {
        len++;
      }
    }
    long runTime = ClockUtil.runTime();
    LogUtils.d("readNfc:%s,%s", runTime, len);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }
}
