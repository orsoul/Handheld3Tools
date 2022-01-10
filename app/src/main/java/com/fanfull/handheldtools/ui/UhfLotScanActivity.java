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
    btnFastId = findViewById(R.id.btn_uhf_lot_set_fast_id);
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
            tvShow.setText("初始化失败");
            return;
          }
          tvShow.setText("打开成功.\n"
              + "3连接击->清空\n"
              //+ "Enter->开始/停止 连续扫描\n"
              + "Enter->读EPC\n"
              + "按键0->USE区 置0\n"
              + "按键1->快读TID 12byte\n"
              + "按键3->同读epc、tid\n"
              + "按键4->随机写EPC 12byte\n"
              + "按键6->随机写USE 32byte\n"
              + "按键7->开启 EPC、TID同读\n"
              + "按键9->关闭 EPC、TID同读\n\n");
          btnFastId.setEnabled(true);
          btnStart.setEnabled(true);
          btnSetPower.setEnabled(true);
          bagSet = new HashSet<>();
          mapUtil = new MapUtil<>(new HashMap<>());
        });
      }
    });

    showLoadingView("正在打开超高频读头...");
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
        if (lotScanUhfTask.isWork()) {
          b = lotScanUhfTask.stopScan();
          info = "停止连续扫描";
        } else {
          b = lotScanUhfTask.startScan(0);
          info = "开启连续扫描";
        }
        if (!b) {
          info += "失败";
          ToastUtils.showShort(info.toString());
          ViewUtil.appendShow(info, tvShow);
        }
        break;
      case R.id.btn_uhf_lot_set_fast_id:
        byte[] tidBuff = new byte[12];
        ClockUtil.runTime(true);
        boolean tidLen = uhfController.readTid(tidBuff);
        t = ClockUtil.runTime();
        if (!tidLen) {
          info = String.format("读tid失败,用时:%s", t);
        } else {
          buff12 = tidBuff;
          info = String.format("tid:%s,用时:%s", BytesUtil.bytes2HexString(buff12), t);
        }
        LogUtils.d(info);
        ViewUtil.appendShow(info, tvShow);
        break;
      case R.id.btn_uhf_lot_set_power:
        byte[] power = uhfController.getPower();
        if (power == null) {
          info = "获取功率失败";
        } else {
          info = String.format("读/写功率: %s/%s | 天线号:%s | %s",
              power[0], power[1], power[2], power[3] == 0 ? "开环" : "闭环");
        }
        ViewUtil.appendShow(info, tvShow);
        showSetPower();
        break;
      case R.id.tv_uhf_lot_show:
        if (3 == ClockUtil.fastClickTimes()) {
          ViewUtil.appendShow("", tvShow);
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
          info = String.format("快读Tid失败,用时:%s", t);
        } else {
          info = String.format("快读Tid:%s,用时:%s", BytesUtil.bytes2HexString(buff12), t);
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
          info = String.format("读epcTid失败,用时:%s", t);
        } else if (bytes.length == 24) {
          info = String.format("epc:%s\ntid:%s,用时:%s",
              BytesUtil.bytes2HexString(bytes, 0, 12),
              BytesUtil.bytes2HexString(bytes, 12, bytes.length), t);
        } else {
          info = String.format("epc:%s,用时:%s", BytesUtil.bytes2HexString(bytes), t);
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
          info = String.format("随机写epc失败,用时：%s", t);
        } else {
          info = String.format("随机写epd：%s,用时：%s", BytesUtil.bytes2HexString(buff12), t);
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
          info = String.format("随机写use失败,用时：%s", t);
        } else {
          info = String.format("随机写use：%s,用时：%s", BytesUtil.bytes2HexString(buff12), t);
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
          info = String.format("写use失败,用时：%s", t);
        } else {
          info = String.format("写use len：%s,用时：%s", data0.length, t);
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
        "输入功率", "功率范围：5 ~ 25,输入6.8即读写功率分别为6、8", "读功率.写功率", text -> {
          if (text == null) {
            return;
          }

          if (!text.matches("\\d+[\\.\\d+]*")) {
            ToastUtils.showLong("输入格式不合法，正确格式：6.8，读写功率分别为6、8");
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
            ToastUtils.showShort("功率超出允许范围");
            return;
          }

          boolean b = uhfController.setPower(r, w, 0, true, false);
          String res1;
          if (b) {
            res1 = String.format("设置读/写功率成功：%s / %s", r, w);
          } else {
            res1 = "设置功率失败";
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
      String format = String.format("次数:%s, %ss, %s次/秒\n%s", progress, s, progress / s,
          mapUtil.getFormatString());
      LogUtils.i(format);
      runOnUiThread(() -> {
        SoundHelper.playToneDrop();
        //dismissLoadingView();
        btnStart.setText("开始");
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
          //showLoadingView("正在扫描...");
          ViewUtil.appendShow("正在扫描...", tvShow);
          btnStart.setText("停止");
        } else {
          SoundHelper.playToneFailed();
          btnStart.setText("开始");
        }
      });
    }
  }
}