package com.fanfull.handheldtools.ui;

import android.os.Bundle;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import com.apkfuns.logutils.LogUtils;
import com.fanfull.handheldtools.R;
import com.fanfull.handheldtools.base.InitModuleActivity;
import com.fanfull.libhard.nfc.IRfidListener;
import com.fanfull.libhard.nfc.RfidController;
import com.fanfull.libhard.uhf.IUhfListener;
import com.fanfull.libhard.uhf.UhfCmd;
import com.fanfull.libhard.uhf.UhfController;
import org.orsoul.baselib.util.ArrayUtils;
import org.orsoul.baselib.util.ViewUtil;

public class InitBag3Activity extends InitModuleActivity {

  private TextView tvShow;

  private UhfController uhfController;
  private RfidController rfidController;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override protected void initModule() {
    uhfController = UhfController.getInstance();
    uhfController.setListener(new IUhfListener() {
      @Override
      public void onOpen() {
        rfidController.open();
        runOnUi(() -> {
          //tvShow.setText("打开成功.\n"
          //    + "3连接击->清空\n"
          //    + "Enter->开始/停止 连续扫描\n"
          //    + "按键7->设置功率\n"
          //    + "按键8->开启 EPC、TID同读\n"
          //    + "按键9->关闭 EPC、TID同读\n\n");
          //ViewUtil.appendShow("超高频初始成功", tvShow);
          tvShow.setText("超高频初始成功.\n正在初始化高频...");
        });
        uhfController.send(UhfCmd.CMD_GET_DEVICE_VERSION);
        SystemClock.sleep(100);
        uhfController.send(UhfCmd.CMD_GET_DEVICE_ID);
        SystemClock.sleep(100);
        uhfController.send(UhfCmd.CMD_GET_FAST_ID);
      }

      @Override
      public void onScan() {

      }

      @Override
      public void onStopScan() {

      }

      @Override
      public void onReceiveData(byte[] data) {
        byte[] parseData = UhfCmd.parseData(data);
        if (parseData == null) {
          LogUtils.i("parseData failed:%s", ArrayUtils.bytes2HexString(data));
          return;
        }
        int cmdType = data[4] & 0xFF;
        Object info = null;
        switch (cmdType) {
          case UhfCmd.RECEIVE_TYPE_GET_DEVICE_VERSION:
            info = String.format("设备版本：v%s.%s.%s", parseData[0], parseData[1], parseData[2]);
            break;
          case UhfCmd.RECEIVE_TYPE_GET_DEVICE_ID:
            info = String.format("设备Id：%s", ArrayUtils.bytes2HexString(parseData));
            break;
          case UhfCmd.RECEIVE_TYPE_GET_FAST_ID:
            if (parseData[0] == 1) {
              info = "EPC、TID同时读取 开启";
            } else {
              info = "EPC、TID同时读取 关闭";
            }
            break;
          case UhfCmd.RECEIVE_TYPE_READ:
            info =
                String.format("read:%s", ArrayUtils.bytes2HexString(parseData));
            break;
          case UhfCmd.RECEIVE_TYPE_WRITE:
            if (parseData.length == 0) {
              info = "写成功";
            } else {
              info = String.format("写失败,cause:%X", parseData[0]);
            }
            break;
          default:
            LogUtils.v("parseData:%s", ArrayUtils.bytes2HexString(parseData));
        }
        if (info != null) {
          Object obj = info;
          runOnUiThread(() -> ViewUtil.appendShow(obj, tvShow));
        }
      }
    });
    showLoadingView("正在初始化模块...");
    uhfController.open();

    rfidController = RfidController.getInstance();
    rfidController.setListener(new IRfidListener() {
      @Override
      public void onOpen() {
        runOnUi(() -> {
          dismissLoadingView();
          //tvShow.setText("初始化成功");
          ViewUtil.appendShow("高频模块初始成功", tvShow);
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
  }

  @Override protected void onEnterPress() {
    super.onEnterPress();
  }

  @Override public void onClick(View v) {
    super.onClick(v);
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    return super.onKeyDown(keyCode, event);
  }

  @Override protected void initView() {
    setContentView(R.layout.activity_init_bag3);
    tvShow = findViewById(R.id.tv_init_bag_show);
    tvShow.setMovementMethod(ScrollingMovementMethod.getInstance());
  }

  @Override protected void onDestroy() {
    super.onDestroy();
  }
}
