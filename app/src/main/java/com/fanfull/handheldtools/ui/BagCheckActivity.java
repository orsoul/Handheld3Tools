package com.fanfull.handheldtools.ui;

import android.os.Bundle;
import android.os.SystemClock;
import android.text.Html;
import android.text.Spanned;
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
import com.fanfull.libhard.lock3.Lock3Operation;
import com.fanfull.libhard.lock3.task.ReadLockTask;
import com.fanfull.libhard.rfid.IRfidListener;
import com.fanfull.libhard.rfid.RfidController;
import com.fanfull.libhard.uhf.IUhfListener;
import com.fanfull.libhard.uhf.UhfCmd;
import com.fanfull.libhard.uhf.UhfController;
import com.fanfull.libjava.util.AESCoder;
import com.fanfull.libjava.util.BytesUtil;
import com.fanfull.libjava.util.ClockUtil;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnConfirmListener;

import org.orsoul.baselib.lock3.EnumBagType;
import org.orsoul.baselib.lock3.EnumCity;
import org.orsoul.baselib.lock3.EnumMoneyType;
import org.orsoul.baselib.lock3.Lock3Util;
import org.orsoul.baselib.lock3.bean.HandoverBean;
import org.orsoul.baselib.lock3.bean.Lock3Bean;
import org.orsoul.baselib.lock3.bean.Lock3InfoUnit;
import org.orsoul.baselib.util.HtmlUtil;
import org.orsoul.baselib.util.SoundHelper;
import org.orsoul.baselib.util.ViewUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class BagCheckActivity extends InitModuleActivity {

  private TextView tvShow;
  private Button btnOk;
  private Button btnShow;
  private Switch switchUhf;

  //private UhfController uhfController;
  //private RfidController rfidController;

  private ReadAllLockTask allLockTask;

  private byte[] buffTid;
  private byte[] buffEpc;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override protected void initModule() {
    uhfController = UhfController.getInstance();
    rfidController = RfidController.getInstance();

    uhfController.setListener(new IUhfListener() {
      @Override public void onOpen(boolean openSuccess) {
        runOnUiThread(() -> {
          if (!openSuccess) {
            dismissLoadingView();
            ViewUtil.appendShow("超高频初始失败!!!", tvShow);
            return;
          }
          //tvShow.setText("打开成功.\n"
          //    + "3连接击->清空\n"
          //    + "Enter->开始/停止 连续扫描\n"
          //    + "按键7->设置功率\n"
          //    + "按键8->开启 EPC、TID同读\n"
          //    + "按键9->关闭 EPC、TID同读\n\n");
          //ViewUtil.appendShow("超高频初始成功", tvShow);
          ViewUtil.appendShow("超高频初始成功", tvShow);
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

      @Override public void onReceiveData(byte[] data) {
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
      @Override public void onOpen(boolean openSuccess) {
        runOnUiThread(() -> {
          dismissLoadingView();
          if (!openSuccess) {
            ViewUtil.appendShow("高频模块初始失败!!!", tvShow);
            return;
          }
          // TODO: 2020-11-13 初始化成功
          //tvShow.setText("初始化成功");
          //ViewUtil.appendShow("高频模块初始成功", tvShow);
          ViewUtil.appendShow("高频模块初始成功\n"
              + "按键1~4->F1~F4\n"
              + "按键6->读tid后过滤读epc", tvShow);
          allLockTask = new ReadAllLockTask();
          allLockTask.setReadUhf(switchUhf.isChecked());
          //initBagTask = new InitBag3Activity.MyInitBagTask();
          //initSpinner();
          btnOk.setEnabled(true);
          switchUhf.setEnabled(true);
          //switchUhf.setChecked(false);
        });
      }

      @Override public void onReceiveData(byte[] data) {
        LogUtils.d("recNfc:%s", BytesUtil.bytes2HexString(data));
      }
    });

    showLoadingView("正在初始化模块...");
    //ViewUtil.appendShow("", tvShow);
    uhfController.open();
  }

  @Override protected void onEnterPress() {
    btnOk.performClick();
  }

  @Override public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_check_bag_scan:
        if (allLockTask.startThread()) {
          ClockUtil.runTime(true);
          showLoadingView("正在读取袋锁信息...");
        } else {
          ToastUtils.showShort("读袋锁信息线程已在运行");
        }
        break;
      case R.id.btn_check_bag_stopScan:
        showSetUhfPower(this);
        //String info;
        //if (readLockTask != null) {
        //  info = String.format("天线柜未读到：%s", readLockTask.otherNoHaveList);
        //  LogUtils.wtf("手持已扫：%s", readLockTask.scanList);
        //} else {
        //  info = "未初始化模块";
        //}
        //LogUtils.wtf(info);
        //LogUtils.getLog2FileConfig().flushAsync();
        //ViewUtil.appendShow(info, tvShow);
        break;
      case R.id.tv_check_bag_show:
        if (3 == ClockUtil.fastClickTimes()) {
          tvShow.setText("");
        }
        break;
      default:
    }
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (event.getRepeatCount() != 0) {
      return true;
    }

    int fn = -1;
    switch (keyCode) {
      case KeyEvent.KEYCODE_1:
      case KeyEvent.KEYCODE_2:
      case KeyEvent.KEYCODE_3:
      case KeyEvent.KEYCODE_4:
        fn = keyCode - KeyEvent.KEYCODE_0;
        break;
      case KeyEvent.KEYCODE_6:
        if (buffTid == null) {
          buffTid = new byte[12];
          buffEpc = new byte[12];
        }
        boolean b = uhfController.readTidAndEpc(buffTid, buffEpc);
        String msg;
        if (b) {
          SoundHelper.playToneSuccess();
          String tid = BytesUtil.bytes2HexString(buffTid);
          msg = String.format("epc:%s\ntid:%s-%s",
              BytesUtil.bytes2HexString(buffEpc), tid.substring(0, 12), tid.substring(12));
        } else {
          SoundHelper.playToneFailed();
          msg = "读取tid、epc失败";
        }
        ViewUtil.appendShow(msg, tvShow);
        break;
      //Lock3Operation.getInstance().writeLockNfc();
      //if (!readLockTask.isRunning()) {
      //  ClockUtil.runTime(true);
      //  readLockTask.startThread();
      //} else {
      //  ToastUtils.showShort("正在初始化，请稍后...");
      //}
      //return true;
    }
    if (0 < fn) {
      boolean res = Lock3Operation.getInstance().setLockStatus(fn);
      String info;
      if (res) {
        info = String.format("更改成功， %s", Lock3Util.getStatusDesc(fn));
        SoundHelper.playToneSuccess();
      } else {
        info = String.format("更改失败， F%s", fn);
        SoundHelper.playToneFailed();
      }
      LogUtils.d(info);
      ViewUtil.appendShow(info, tvShow);
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  @Override protected void initView() {
    setContentView(R.layout.activity_bag_check);
    tvShow = findViewById(R.id.tv_check_bag_show);
    tvShow.setMovementMethod(ScrollingMovementMethod.getInstance());
    tvShow.setOnClickListener(this);

    btnOk = findViewById(R.id.btn_check_bag_scan);
    btnOk.setOnClickListener(this);

    btnShow = findViewById(R.id.btn_check_bag_stopScan);
    btnShow.setOnClickListener(this);

    switchUhf = findViewById(R.id.switch_check_bag_nfc_mode);
    switchUhf.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          allLockTask.setReadUhf(isChecked);
        });

    btnOk.setEnabled(false);
    //switchUhf.setEnabled(true);
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
                //initBagTask.setCityCode(EnumCity.getCodeByName(name));
                break;
              case R.id.spinner_init_bag_moneyType:
                //initBagTask.setMoneyType(EnumMoneyType.getTypeByName(name));
                break;
              case R.id.spinner_init_bag_bagType:
                //initBagTask.setBagType(EnumBagType.getTypeByName(name));
                break;
            }
            //LogUtils.d("%s", initBagTask.getBagIdParser());
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

  private Spanned parse(Lock3Bean lock3Bean) {
    if (lock3Bean == null) {
      return null;
    }
    HtmlUtil.setDefaultColor(0x0000FF);
    LogUtils.d("原始封签码:%s", lock3Bean.getCoverCode());

    String coverCode;
    Lock3InfoUnit infoUnit = lock3Bean.getInfoUnit(Lock3Bean.SA_PIECE_TID);
    Lock3InfoUnit infoUnitCover = lock3Bean.getInfoUnit(Lock3Bean.SA_COVER_EVENT);
    if (infoUnit != null && infoUnit.isDoSuccess()) {
      byte[] key = Arrays.copyOf(infoUnit.buff, 6);
      boolean b = AESCoder.myEncrypt(infoUnitCover.buff, key, false);
      coverCode = String.format("%s-解密:%s", BytesUtil.bytes2HexString(infoUnitCover.buff), b);
      //LogUtils.d("解密:%s", BytesUtil.bytes2HexString(infoUnitCover.buff));
    } else {
      coverCode = String.format("%s-解密异常", BytesUtil.bytes2HexString(infoUnitCover.buff));
      //coverCode = "解密异常";
    }

    String pieceTid = lock3Bean.getPieceTid();
    String compareMsg;
    if (pieceTid == null) {
      compareMsg = "未读锁片";
    } else {
      String tid6 = lock3Bean.getTidFromPiece().substring(0, 12);
      String tid12 = pieceTid.substring(12);
      compareMsg = String.format("%s，%s",
          tid12, Objects.equals(tid6, tid12) ? "相等" : "不相等");
    }

    String colorText = HtmlUtil.getColorText(
        "锁片epc：%s\n"
            + "锁片tid：%s\n"
            + "后六tid：%s\n" // 业务tid对比
            + "业务tid：%s\n"
            + "锁内tid：%s\n"
            + "bagId：%s\n"
            + "标志位：%s\n"
            + "空袋检测位：%s\n"
            + "电压：%s\n"
            + "启用状态：%s\n"
            + "测试模式：%s\n"
            + "封签码：%s\n"
            + "流水号：%s\n"
            + "密钥编号：%s\n"
            + "交接索引：%s\n"
            + "========================\n袋流转索引：%s\n",
        lock3Bean.getPieceEpc(),
        lock3Bean.getPieceTid(),
        compareMsg,
        lock3Bean.getTidFromPiece(),
        lock3Bean.getTidFromLock(),
        lock3Bean.getBagId(),
        Lock3Util.getStatusDesc(lock3Bean.getStatus()),
        Lock3Util.getStatusCheckDesc(lock3Bean.getStatusCheck()),
        Lock3Util.parseV2String(lock3Bean.getVoltage()),
        Lock3Util.getEnableDesc(lock3Bean.getEnable()),
        lock3Bean.isTestMode(),
        coverCode,
        lock3Bean.getCoverSerial(),
        lock3Bean.getKeyNum(),
        lock3Bean.getHandoverIndex(),
        lock3Bean.getCirculationIndex());

    List<HandoverBean> list = lock3Bean.getHandoverBeanList();
    if (list == null || list.isEmpty()) {
      return Html.fromHtml(colorText);
    }

    StringBuilder sb = new StringBuilder(colorText);
    for (HandoverBean handoverBean : list) {
      String orgName = handoverBean.getOrgName();
      if (orgName == null) {
        orgName = "无机构名";
      }
      sb.append(HtmlUtil.getColorText("业务：%s(%s)\n"
              + "机构：%s(%s)\n"
              + "操作人：%s\n"
              + "复核人：%s\n"
              + "时间：%s\n",
          handoverBean.getFunTypeName(),
          handoverBean.getFunction(),
          handoverBean.getOrgancode(),
          orgName,
          handoverBean.getScaner1(),
          handoverBean.getScaner2(),
          handoverBean.getTime())
      );
    }
    return Html.fromHtml(sb.toString());
  }

  private class ReadAllLockTask extends ReadLockTask {
    @Override protected void onProgress(int res, long progress, long total) {
      super.onProgress(res, progress, total);
    }

    @Override protected void onSuccess(Lock3Bean lock3Bean) {
      runOnUiThread(() -> {
        SoundHelper.playToneSuccess();
        dismissLoadingView();
        Spanned parse = parse(lock3Bean);
        //ViewUtil.appendShow(null, tvShow);
        //ViewUtil.appendShow(parse, tvShow);
        tvShow.setText(parse);
        //tvShow.append("\n");
        tvShow.append("\n用时：" + ClockUtil.runTime() + "\n");
        tvShow.scrollTo(0, 0);
        LogUtils.d("\n%s", tvShow.getText());
        //ViewUtil.appendShow("用时：" + ClockUtil.runTime(), tvShow);
        //tvShow.scrollTo(0, 0);
      });
    }

    @Override protected void onFailed(int errorCode) {
      String msg;
      switch (errorCode) {
        case -1:
          msg = String.format("读袋锁失败，cause：%s 参数错误", errorCode);
          break;
        case -2:
          msg = String.format("读袋锁失败，cause：%s nfc寻卡失败", errorCode);
          break;
        case -3:
          msg = String.format("读袋锁失败，cause：%s 读tid失败", errorCode);
          break;
        case -4:
          msg = String.format("读袋锁失败，cause：%s 读epc失败", errorCode);
          break;
        case -5:
          msg = String.format("读袋锁失败，cause：%s 读nfc失败", errorCode);
          break;
        default:
          msg = String.format("读袋锁失败，cause：%s 未定义失败", errorCode);
      }
      LogUtils.d("%s", msg);
      runOnUiThread(() -> {
        SoundHelper.playToneFailed();
        dismissLoadingView();
        ViewUtil.appendShow(msg, tvShow);
      });
    }
  }

  void show() {
    long i = 2l + 12;
    new XPopup.Builder(this)
        .asConfirm(null, "您可以复用项目已有布局，来使用XPopup强大的交互能力和逻辑封装，弹窗的布局完全由你自己控制。\n" +
                "需要注意的是：你自己的布局必须提供一些控件Id，否则XPopup找不到View。\n具体需要提供哪些Id，请查看WIKI[内置弹窗]一章。",
            "关闭", "XPopup牛逼",
            new OnConfirmListener() {
              @Override
              public void onConfirm() {
              }
            }, null, false)//绑定已有布局
        .show();
  }
}
