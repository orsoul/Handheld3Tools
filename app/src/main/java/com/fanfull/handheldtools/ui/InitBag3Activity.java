package com.fanfull.handheldtools.ui;

import android.os.Bundle;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.apkfuns.logutils.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.fanfull.handheldtools.R;
import com.fanfull.handheldtools.ui.base.InitModuleActivity;
import com.fanfull.libhard.lock3.task.InitBagTask;
import com.fanfull.libhard.rfid.IRfidListener;
import com.fanfull.libhard.rfid.RfidController;
import com.fanfull.libhard.uhf.IUhfListener;
import com.fanfull.libhard.uhf.UhfCmd;
import com.fanfull.libhard.uhf.UhfController;

import org.orsoul.baselib.lock3.EnumBagType;
import org.orsoul.baselib.lock3.EnumCity;
import org.orsoul.baselib.lock3.EnumMoneyType;
import org.orsoul.baselib.lock3.bean.BagIdParser;
import org.orsoul.baselib.util.BytesUtil;
import org.orsoul.baselib.util.ClockUtil;
import org.orsoul.baselib.util.SoundHelper;
import org.orsoul.baselib.util.ThreadUtil;
import org.orsoul.baselib.util.ViewUtil;

public class InitBag3Activity extends InitModuleActivity {

  private TextView tvShow;
  private Button btnOk;
  private Switch switchCheck;

  private UhfController uhfController;
  private RfidController rfidController;

  private InitBagTask initBagTask;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override protected void initModule() {
    uhfController = UhfController.getInstance();
    rfidController = RfidController.getInstance();

    uhfController.setListener(new IUhfListener() {
      @Override public void onOpen(boolean openSuccess) {
        runOnUi(() -> {
          if (!openSuccess) {
            dismissLoadingView();
            ViewUtil.appendShow("超高频初始失败.", tvShow);
            return;
          }
          //tvShow.setText("打开成功.\n"
          //    + "3连接击->清空\n"
          //    + "Enter->开始/停止 连续扫描\n"
          //    + "按键7->设置功率\n"
          //    + "按键8->开启 EPC、TID同读\n"
          //    + "按键9->关闭 EPC、TID同读\n\n");
          //ViewUtil.appendShow("超高频初始成功", tvShow);
          ViewUtil.appendShow("超高频初始成功.", tvShow);
          rfidController.open();
        });

        if (openSuccess) {
          uhfController.send(UhfCmd.CMD_GET_DEVICE_VERSION);
          SystemClock.sleep(100);
          uhfController.send(UhfCmd.CMD_GET_DEVICE_ID);
          SystemClock.sleep(100);
          uhfController.send(UhfCmd.CMD_GET_FAST_ID);
        }
      }

      @Override
      public void onReceiveData(byte[] data) {
        byte[] parseData = UhfCmd.parseData(data);
        if (parseData == null) {
          LogUtils.i("parseData failed:%s", BytesUtil.bytes2HexString(data));
          return;
        }
        int cmdType = data[4] & 0xFF;
        Object info = null;
        switch (cmdType) {
          case UhfCmd.RECEIVE_TYPE_GET_DEVICE_VERSION:
            info = String.format("设备版本：v%s.%s.%s", parseData[0], parseData[1], parseData[2]);
            break;
          case UhfCmd.RECEIVE_TYPE_GET_DEVICE_ID:
            info = String.format("设备Id：%s", BytesUtil.bytes2HexString(parseData));
            break;
          case UhfCmd.RECEIVE_TYPE_GET_FAST_ID:
            if (parseData[0] == 1) {
              info = "EPC、TID同时读取 开启";
            } else {
              info = "EPC、TID同时读取 关闭";
            }
            break;
          case UhfCmd.RECEIVE_TYPE_FAST_EPC:
            info =
                String.format("readEpcWithTid:%s", BytesUtil.bytes2HexString(parseData));
            break;
          case UhfCmd.RECEIVE_TYPE_READ:
            info =
                String.format("read:%s", BytesUtil.bytes2HexString(parseData));
            break;
          case UhfCmd.RECEIVE_TYPE_WRITE:
            if (parseData.length == 0) {
              info = "写成功";
            } else {
              info = String.format("写失败,cause:%X", parseData[0]);
            }
            break;
          default:
            LogUtils.w("parseData cmdType:%02X, %s", cmdType,
                BytesUtil.bytes2HexString(parseData));
        }
        if (info != null) {
          LogUtils.i("parseData:%s", info);
          Object obj = info;
          //runOnUiThread(() -> ViewUtil.appendShow(obj, tvShow));
        }
      }
    });

    rfidController.setListener(new IRfidListener() {
      @Override
      public void onOpen(boolean openSuccess) {
        runOnUi(() -> {
          dismissLoadingView();
          if (!openSuccess) {
            ViewUtil.appendShow("高频模块初始失败", tvShow);
            return;
          }
          //tvShow.setText("初始化成功");
          ViewUtil.appendShow("高频模块初始成功", tvShow);
          initBagTask = new MyInitBagTask();
          initSpinner();
          btnOk.setEnabled(true);
          switchCheck.setEnabled(true);
        });
      }

      @Override
      public void onReceiveData(byte[] data) {
        LogUtils.d("recNfc:%s", BytesUtil.bytes2HexString(data));
      }
    });

    showLoadingView("正在初始化模块...");
    uhfController.open();
  }

  @Override protected void onEnterPress() {
    btnOk.performClick();
  }

  @Override public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_init_bag3_ok:
        if (initBagTask.isStopped()) {
          //btnOk.setEnabled(false);
          //switchCheck.setEnabled(false);
          ClockUtil.runTime(true);
          ThreadUtil.executeInSingleThread(initBagTask);
        } else {
          ToastUtils.showShort("正在初始化，请稍后...");
        }
        break;
      case R.id.tv_init_bag_show:
        if (3 == ClockUtil.fastClickTimes()) {
          tvShow.setText("");
        }
        break;
      default:
    }
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    return super.onKeyDown(keyCode, event);
  }

  @Override protected void initView() {
    setContentView(R.layout.activity_init_bag3);
    tvShow = findViewById(R.id.tv_init_bag_show);
    tvShow.setMovementMethod(ScrollingMovementMethod.getInstance());
    tvShow.setOnClickListener(this);

    btnOk = findViewById(R.id.btn_init_bag3_ok);
    btnOk.setOnClickListener(this);

    switchCheck = findViewById(R.id.switch_init_bag);
    switchCheck.setOnCheckedChangeListener(
        (buttonView, isChecked) -> initBagTask.setCheckData(isChecked));

    btnOk.setEnabled(false);
    switchCheck.setEnabled(false);
  }

  private void initSpinner() {
    Spinner spCityType = findViewById(R.id.spinner_init_bag_cityType);
    Spinner spMoneyType = findViewById(R.id.spinner_init_bag_moneyType);
    Spinner spBagType = findViewById(R.id.spinner_init_bag_bagType);

    spCityType.setAdapter(new ArrayAdapter<>(this,
        R.layout.support_simple_spinner_dropdown_item, EnumCity.getNames()));
    spMoneyType.setAdapter(new ArrayAdapter<>(this,
        R.layout.support_simple_spinner_dropdown_item, EnumMoneyType.getNames()));
    spBagType.setAdapter(new ArrayAdapter<>(this,
        R.layout.support_simple_spinner_dropdown_item, EnumBagType.getNames()));

    AdapterView.OnItemSelectedListener itemSelectedListener =
        new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String name = parent.getSelectedItem().toString();
            LogUtils.d("select: %s", name);
            switch (parent.getId()) {
              case R.id.spinner_init_bag_cityType:
                initBagTask.setCityCode(EnumCity.getCodeByName(name));
                break;
              case R.id.spinner_init_bag_moneyType:
                initBagTask.setMoneyType(EnumMoneyType.getTypeByName(name));
                break;
              case R.id.spinner_init_bag_bagType:
                initBagTask.setBagType(EnumBagType.getTypeByName(name));
                break;
            }
            LogUtils.d("%s", initBagTask.getBagIdParser());
          }

          @Override public void onNothingSelected(AdapterView<?> parent) {

          }
        };

    spCityType.setOnItemSelectedListener(itemSelectedListener);
    spMoneyType.setOnItemSelectedListener(itemSelectedListener);
    spBagType.setOnItemSelectedListener(itemSelectedListener);

    spCityType.setSelection(2);
    spMoneyType.setSelection(0);
    spBagType.setSelection(1);
  }

  @Override protected void onDestroy() {
    uhfController.release();
    rfidController.release();
    super.onDestroy();
  }

  private class MyInitBagTask extends InitBagTask {

    @Override protected void onStart(BagIdParser bagIdParser) {
      runOnUiThread(() -> {
        btnOk.setEnabled(false);
        switchCheck.setEnabled(false);
      });
    }

    @Override protected void onSuccess(BagIdParser bagIdParser) {
      super.onSuccess(bagIdParser);
      SoundHelper.playToneSuccess();
      long runTime = ClockUtil.runTime();
      ToastUtils.showShort("初始化成，用时：%.2f秒", runTime / 1000.0);
      runOnUiThread(() -> {
        ViewUtil.appendShow(String.format("成功：%s", bagIdParser.getFormatBagId()), tvShow);
        btnOk.setEnabled(true);
        switchCheck.setEnabled(true);
      });
    }

    @Override protected void onProgress(int progress) {
      super.onProgress(progress);
    }

    @Override protected void onFailed(int readRes, String info) {
      super.onFailed(readRes, info);
      runOnUiThread(() -> {
        ViewUtil.appendShow(String.format("%s : %s", info, readRes), tvShow);
        btnOk.setEnabled(true);
        switchCheck.setEnabled(true);
      });
    }
  }
}
