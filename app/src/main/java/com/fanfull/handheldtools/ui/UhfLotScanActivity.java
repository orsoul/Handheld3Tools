package com.fanfull.handheldtools.ui;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.apkfuns.logutils.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.fanfull.handheldtools.MapUtil;
import com.fanfull.handheldtools.R;
import com.fanfull.handheldtools.ui.base.InitModuleActivity;
import com.fanfull.libhard.uhf.IUhfListener;
import com.fanfull.libhard.uhf.LotScanUhfTask;
import com.fanfull.libhard.uhf.UhfCmd;
import com.fanfull.libhard.uhf.UhfController;
import com.fanfull.libjava.util.BytesUtil;
import com.fanfull.libjava.util.ClockUtil;
import com.lxj.xpopup.XPopup;

import org.orsoul.baselib.util.SoundHelper;
import org.orsoul.baselib.util.ViewUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class UhfLotScanActivity extends InitModuleActivity {

  private TextView tvShow;
  private Button btnSetPower;
  private Button btnFastId;
  private Button btnStart;

  private UhfController uhfController;

  private byte[] buff12;
  private boolean fastIdOn;
  private boolean readingLot;
  private int readLotCount;

  private Set<String> bagSet;
  private MapUtil<String> mapUtil;

  private LotScanUhfTask lotScanUhfTask;

  public UhfLotScanActivity() {}

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override protected void initView() {
    setContentView(R.layout.activity_uhf_lot_scan);
    tvShow = findViewById(R.id.tv_uhf_lot_show);
    tvShow.setMovementMethod(ScrollingMovementMethod.getInstance());
    btnSetPower = findViewById(R.id.btn_uhf_lot_set_power);
    btnFastId = findViewById(R.id.btn_uhf_lot_scan_stop);
    btnStart = findViewById(R.id.btn_uhf_lot_scan);

    tvShow.setOnClickListener(this);
    btnFastId.setOnClickListener(this);
    btnStart.setOnClickListener(this);
    btnSetPower.setOnClickListener(this);

    btnFastId.setEnabled(false);
    btnStart.setEnabled(false);
    btnSetPower.setEnabled(false);

    ViewUtil.requestFocus(tvShow);
  }

  @Override protected void initModule() {
    uhfController = UhfController.getInstance();
    lotScanUhfTask = new MyLotScanUhfTask(uhfController, new IUhfListener() {
      @Override public void onReceiveData(byte[] data) {
      }

      @Override public void onOpen(boolean openSuccess) {
        runOnUiThread(() -> {
          dismissLoadingView();
          if (!openSuccess) {
            tvShow.setText("???????????????");
            return;
          }
          tvShow.setText("????????????.\n"
              + "3?????????->??????\n"
              //+ "Enter->??????/?????? ????????????\n"
              + "Enter->???EPC\n"
              + "??????0->USE??? ???0\n"
              + "??????1->??????TID 12byte\n"
              + "??????3->??????epc???tid\n"
              + "??????4->?????????EPC 12byte\n"
              + "??????6->?????????USE 32byte\n"
              + "??????7->?????? EPC???TID??????\n"
              + "??????9->?????? EPC???TID??????\n\n");
          btnFastId.setEnabled(true);
          btnStart.setEnabled(true);
          btnSetPower.setEnabled(true);
          bagSet = new HashSet<>();
          mapUtil = new MapUtil<>(new HashMap<>());
        });
      }
    });

    showLoadingView("???????????????????????????...");
    uhfController.open();
  }

  @Override public void onClick(View v) {
    v.setEnabled(false);
    Object info = null;
    switch (v.getId()) {
      case R.id.btn_uhf_lot_scan:
        boolean res = uhfController.readEpc(buff12);
        long t = 0;
        boolean b;
        bagSet.clear();
        b = lotScanUhfTask.startScan(0);
        if (!b) {
          info = "????????????????????????!!!";
        } else {
          info = "????????????????????????";
        }
        ToastUtils.showShort(info.toString());
        ViewUtil.appendShow(info, tvShow);
        //if (lotScanUhfTask.isWork()) {
        //  b = lotScanUhfTask.stopScan();
        //  info = "??????????????????";
        //} else {
        //  b = lotScanUhfTask.startScan(0);
        //  info = "??????????????????";
        //}
        //if (!b) {
        //  info += "??????!!!";
        //} else {
        //  info += "??????";
        //}
        //ToastUtils.showShort(info.toString());
        //ViewUtil.appendShow(info, tvShow);
        break;
      case R.id.btn_uhf_lot_scan_stop:
        b = lotScanUhfTask.stopScan();
        if (!b) {
          info = "????????????????????????!!!";
        } else {
          info = "????????????????????????";
        }
        ToastUtils.showShort(info.toString());
        ViewUtil.appendShow(info, tvShow);
        //byte[] tidBuff = new byte[12];
        //ClockUtil.runTime(true);
        //boolean tidLen = uhfController.readTid(tidBuff);
        //t = ClockUtil.runTime();
        //if (!tidLen) {
        //  info = String.format("???tid??????,??????:%s", t);
        //} else {
        //  buff12 = tidBuff;
        //  info = String.format("tid:%s,??????:%s", BytesUtil.bytes2HexString(buff12), t);
        //}
        //LogUtils.d(info);
        //ViewUtil.appendShow(info, tvShow);
        break;
      case R.id.btn_uhf_lot_set_power:
        byte[] power = uhfController.getPower();
        if (power == null) {
          info = "??????????????????";
        } else {
          info = String.format("???/?????????: %s/%s | ?????????:%s | %s",
              power[0], power[1], power[2], power[3] == 0 ? "??????" : "??????");
        }
        ViewUtil.appendShow(info, tvShow);
        showSetPower();
        break;
      case R.id.tv_uhf_lot_show:
        if (3 == ClockUtil.fastClickTimes()) {
          tvShow.setText(null);
        }
        break;
    }
    v.setEnabled(true);
  }

  @Override protected void onEnterPress() {
    btnStart.performClick();
    //        LogUtils.d("onEnterPress");
    //if (readingLot) {
    //  return;
    //}
    //if (uhfController.isOpen()) {
    //  readLotCount = 0;
    //  readingLot = uhfController.send(UhfCmd.getReadLotCmd(20));
    //}
  }

  boolean isRead;

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    LogUtils.v("%s:  RepeatCount:%s Action:%s long:%s shift:%s meta:%X",
        KeyEvent.keyCodeToString(keyCode),
        event.getRepeatCount(),
        event.getAction(),
        event.isLongPress(),
        event.isShiftPressed(),
        event.getMetaState()
    );
    //boolean res;
    //byte[] data = new byte[12];
    switch (keyCode) {
      case KeyEvent.KEYCODE_1:
        ClockUtil.runTime(true);
        boolean fastTid = uhfController.fastTid(0, buff12);
        long t = ClockUtil.runTime();
        String info;
        if (!fastTid) {
          info = String.format("??????Tid??????,??????:%s", t);
        } else {
          info = String.format("??????Tid:%s,??????:%s", BytesUtil.bytes2HexString(buff12), t);
        }
        LogUtils.d(info);
        ViewUtil.appendShow(info, tvShow);
        break;
      case KeyEvent.KEYCODE_2:
        break;
      case KeyEvent.KEYCODE_3:
        ClockUtil.runTime(true);
        byte[] bytes = uhfController.readEpcWithTid(200);
        t = ClockUtil.runTime();
        //String info;
        if (bytes == null) {
          info = String.format("???epcTid??????,??????:%s", t);
        } else if (bytes.length == 24) {
          info = String.format("epc:%s\ntid:%s,??????:%s",
              BytesUtil.bytes2HexString(bytes, 0, 12),
              BytesUtil.bytes2HexString(bytes, 12, bytes.length), t);
        } else {
          info = String.format("epc:%s,??????:%s", BytesUtil.bytes2HexString(bytes), t);
        }
        LogUtils.d(info);
        ViewUtil.appendShow(info, tvShow);
        break;
      case KeyEvent.KEYCODE_4:
        Arrays.fill(buff12, (byte) new Random().nextInt(256));

        ClockUtil.runTime(true);
        boolean writeEpc = uhfController.writeEpc(buff12);
        t = ClockUtil.runTime();
        if (!writeEpc) {
          info = String.format("?????????epc??????,?????????%s", t);
        } else {
          info = String.format("?????????epd???%s,?????????%s", BytesUtil.bytes2HexString(buff12), t);
        }
        LogUtils.d(info);
        ViewUtil.appendShow(info, tvShow);
        //testWriteFilter();
        break;
      case KeyEvent.KEYCODE_5:
        break;
      case KeyEvent.KEYCODE_6:
        Arrays.fill(buff12, (byte) new Random().nextInt(256));
        ClockUtil.runTime(true);
        boolean writeUse = uhfController.writeUse(0, buff12);
        t = ClockUtil.runTime();
        if (!writeUse) {
          info = String.format("?????????use??????,?????????%s", t);
        } else {
          info = String.format("?????????use???%s,?????????%s", BytesUtil.bytes2HexString(buff12), t);
        }
        LogUtils.d(info);
        ViewUtil.appendShow(info, tvShow);
        break;
      case KeyEvent.KEYCODE_7:
        //if (socketService == null) {
        //  socketService = new SocketServiceDemo();
        //  socketService.start();
        //} else {
        //  socketService.closeService();
        //}
        fastIdOn = true;
        uhfController.send(UhfCmd.getSetFastIdCmd(fastIdOn));
        break;
      case KeyEvent.KEYCODE_8:
        //byte[] setPwdCmd = UhfCmd.getSetPwdCmd(0x00000000, null, 0, 0, 0x0FC2A0);
        //byte[] setPwdCmd = UhfCmd.getSetPwdCmd(0x00000000, null, 0, 0, 0b11111100000000000000);
        //byte[] setPwdCmd = UhfCmd.getSetPwdCmd(0x00000000, null, 0, 0, 0B00000000001111111111);
        //byte[] setPwdCmd = UhfCmd.getSetPwdCmd(0x00000000, null, 0, 0, 0B11111111111111111111);
        //byte[] setPwdCmd = UhfCmd.getSetPwdCmd(0x00000000, null, 0, 0, 0B11111111110000000000);
        //uhfController.send(setPwdCmd);
        break;
      case KeyEvent.KEYCODE_9:
        fastIdOn = false;
        uhfController.send(UhfCmd.getSetFastIdCmd(fastIdOn));
        break;
      case KeyEvent.KEYCODE_0:
        ClockUtil.runTime(true);
        final byte[] data0 = new byte[192];
        writeUse = uhfController.writeUse(0, data0);
        t = ClockUtil.runTime();
        if (!writeUse) {
          info = String.format("???use??????,?????????%s", t);
        } else {
          info = String.format("???use len???%s,?????????%s", data0.length, t);
        }
        LogUtils.d(info);
        ViewUtil.appendShow(info, tvShow);
        break;
      //if (isRead) {
      //  isRead = false;
      //  break;
      //} else {
      //  isRead = true;
      //  int[] count = new int[1];
      //  ThreadUtil.execute(() -> {
      //    while (isRead) {
      //      byte[] epc = new byte[12];
      //      uhfController.readEpc(epc);
      //      String s = BytesUtil.bytes2HexString(epc);
      //      runOnUiThread(() -> tvShow.setText((++count[0]) + ":" + s + "\n"));
      //      SystemClock.sleep(50);
      //    }
      //  });
      //}
      default:
        break;
    }

    return super.onKeyDown(keyCode, event);
  }

  @Override protected void onDestroy() {
    lotScanUhfTask.stopScan();
    uhfController.release();
    isRead = false;
    super.onDestroy();
  }

  private void showSetPower() {
    new XPopup.Builder(this).asInputConfirm(
        "????????????", "???????????????5 ~ 25,??????6.8????????????????????????6???8", "?????????.?????????", text -> {
          if (text == null) {
            return;
          }

          if (!text.matches("\\d+[\\.\\d+]*")) {
            ToastUtils.showLong("???????????????????????????????????????6.8????????????????????????6???8");
            return;
          }

          String[] split = text.split("\\.");
          int r = Integer.parseInt(split[0]);
          int w = r;
          if (split.length == 2) {
            w = Integer.parseInt(split[1]);
          }
          if (UhfCmd.MAX_POWER < r || r < UhfCmd.MIN_POWER ||
              UhfCmd.MAX_POWER < w || w < UhfCmd.MIN_POWER) {
            ToastUtils.showShort("????????????????????????");
            return;
          }

          boolean b = uhfController.setPower(r, w, 0, true, false);
          String res1;
          if (b) {
            res1 = String.format("?????????/??????????????????%s / %s", r, w);
          } else {
            res1 = "??????????????????";
          }
          ToastUtils.showShort(res1);
          ViewUtil.appendShow(res1, tvShow);
        }).show();
  }

  private void testWriteFilter() {
    byte[] tid6 = new byte[6];
    boolean res = uhfController.read(UhfCmd.MB_TID, 0x03, tid6,
        500, UhfCmd.MB_TID, 0x03, null);
    if (!res) {
      LogUtils.d("%s", "read tid6 failed");
      return;
    }
    LogUtils.d("tid6:%s", BytesUtil.bytes2HexString(tid6));
    byte[] epc12 = new byte[12];
    Arrays.fill(epc12, (byte) new Random().nextInt(256));
    //epc12 = BytesUtil.hexString2Bytes("05532103044A45D23E618072");

    res = uhfController.write(UhfCmd.MB_EPC, 0x02, epc12,
        500, UhfCmd.MB_TID, 0x03, tid6);
    if (!res) {
      LogUtils.d("%s", "write epc12 failed");
    } else {
      LogUtils.d("epc12:%s", BytesUtil.bytes2HexString(epc12));
    }
  }

  class MyLotScanUhfTask extends LotScanUhfTask {
    public MyLotScanUhfTask(UhfController uhfController, IUhfListener listener) {
      super(uhfController, listener);
    }

    @Override public void onReceiveEpc(String epc, String tid, int progress, int total) {
      //String bag = BytesUtil.bytes2HexString(epc);
      //boolean add = bagSet.add(epc);
      int add = mapUtil.add(epc);
      if (add == 1) {
        int size = mapUtil.getMap().size();
        SoundHelper.playNum(size);
        //LogUtils.i("add:%s", bagSet);
        LogUtils.i("addNew:%s", epc);
        runOnUiThread(() -> ViewUtil.appendShow(size + ":" + epc, tvShow));
      } else {
        //SoundHelper.playToneBiu();
        LogUtils.d("addOld:%s - %s", epc, add);
      }
    }

    @Override public void onStop(int progress, int total) {
      long l = ClockUtil.runTime();
      int s = (int) (l / 1000);
      String format = String.format("\n??????:%s, %ss, %s???/???\n%s", progress, s, progress / s,
          mapUtil.getFormatString());
      LogUtils.i(format);
      runOnUiThread(() -> {
        SoundHelper.playToneDrop();
        //dismissLoadingView();
        //btnStart.setText("??????");
        ViewUtil.appendShow(format, tvShow);
        mapUtil.clear();
        //ViewUtil.appendShow(mapUtil.getFormatString(), tvShow);
      });
    }

    @Override public void onStart(boolean success, int progress, int total) {
      runOnUiThread(() -> {
        if (success) {
          ClockUtil.runTime(true);
          SoundHelper.playToneSuccess();
          //showLoadingView("????????????...");
          ViewUtil.appendShow("????????????...", tvShow);
          //btnStart.setText("??????");
        } else {
          SoundHelper.playToneFailed();
          //btnStart.setText("??????");
        }
      });
    }
  }
}