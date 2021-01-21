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
import com.fanfull.libhard.lock3.task.ReadLockTask;
import com.fanfull.libhard.rfid.IRfidListener;
import com.fanfull.libhard.rfid.RfidController;
import com.fanfull.libhard.uhf.IUhfListener;
import com.fanfull.libhard.uhf.UhfCmd;
import com.fanfull.libhard.uhf.UhfController;

import org.orsoul.baselib.lock3.EnumBagType;
import org.orsoul.baselib.lock3.EnumCity;
import org.orsoul.baselib.lock3.EnumMoneyType;
import org.orsoul.baselib.lock3.Lock3Util;
import org.orsoul.baselib.lock3.bean.HandoverBean;
import org.orsoul.baselib.lock3.bean.Lock3Bean;
import org.orsoul.baselib.util.BytesUtil;
import org.orsoul.baselib.util.ClockUtil;
import org.orsoul.baselib.util.HtmlUtil;
import org.orsoul.baselib.util.SoundUtils;
import org.orsoul.baselib.util.ThreadUtil;
import org.orsoul.baselib.util.ViewUtil;

import java.util.ArrayList;
import java.util.List;

public class BagCheckActivity extends InitModuleActivity {

  private TextView tvShow;
  private Button btnOk;
  private Button btnShow;
  private Switch switchCheck;

  private UhfController uhfController;
  private RfidController rfidController;

  private ReadNfcEpcTask readLockTask;
  private ReadAllLockTask allLockTask;

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
          // TODO: 2020-11-13 初始化成功
          //tvShow.setText("初始化成功");
          ViewUtil.appendShow("高频模块初始成功", tvShow);
          readLockTask = new ReadNfcEpcTask();
          allLockTask = new ReadAllLockTask();
          //initBagTask = new InitBag3Activity.MyInitBagTask();
          //initSpinner();
          btnOk.setEnabled(true);
          switchCheck.setEnabled(true);
          switchCheck.setChecked(false);
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
      case R.id.btn_check_bag_scan:
        if (!readLockTask.isRunning()) {
          //btnOk.setEnabled(false);
          //switchCheck.setEnabled(false);
          ClockUtil.runTime(true);
          readLockTask.startThread();
        } else {
          ToastUtils.showShort("正在初始化，请稍后...");
        }
        break;
      case R.id.btn_check_bag_stopScan:
        String info;
        if (readLockTask != null) {
          info = String.format("天线柜未读到：%s", readLockTask.otherNoHaveList);
          LogUtils.wtf("手持已扫：%s", readLockTask.scanList);
        } else {
          info = "未初始化模块";
        }
        LogUtils.wtf(info);
        LogUtils.getLog2FileConfig().flushAsync();
        ViewUtil.appendShow(info, tvShow);
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
    switch (keyCode) {
      case KeyEvent.KEYCODE_1:
        if (allLockTask.startThread()) {
          ClockUtil.runTime(true);
          showLoadingView("正在读取袋锁信息...");
        } else {
          ToastUtils.showShort("读袋锁信息线程已在运行");
        }
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

    switchCheck = findViewById(R.id.switch_check_bag_nfc_mode);
    switchCheck.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          readLockTask.setReadEpc(isChecked);
          allLockTask.setReadUhf(isChecked);
        });

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

  private class ReadNfcEpcTask extends ThreadUtil.ThreadRunnable {

    public List<String> scanList;
    public List<String> otherNoHaveList;
    public List<String> otherList;
    byte[] bagIdBuff = new byte[12];
    byte[] epcBuff = new byte[12];
    boolean isReadEpc = true;

    public void setReadEpc(boolean isChecked) {
      isReadEpc = isChecked;
    }

    public ReadNfcEpcTask() {
      scanList = new ArrayList<>(300);
      otherNoHaveList = new ArrayList<>();
      otherList = new ArrayList<>(300);
      String[] split =
          "0502710104B58EFA6F57800A,0502710104228BFA6F578098,050271010470B0FA6F5780F1,050271010495C1520F5580AF,05027101041468FA6F57804D,05027101046F87FA6F5780D9,05027101049F670A1A55804E,0502710104B37F1A745A800B,05027101041A8CFA6F5780A7,05027101045EA4520F558001,0502710104C265FA6F578096,050271010431380A1A5580BF,0502710104AC65FA6F5780F8,05027101045294520F55803D,050271010488B4FA6F57800D,05027101045167FA6F578007,05027101047868FA6F578021,05027101045169FA6F578009,05027101043588FA6F57808C,05027101042B9C520F55804C,05027101047B8C520F55800C,0502710104ABB5FA6F57802F,0502710104C9692A7057805E,05027101049166FA6F5780C6,050271010499A7520F5580C5,0502710104B3B3FA6F578031,05027101043CB3FA6F5780BE,0502710104938AFA6F578028,05027101047DB3FA6F5780FF,0502710104A4A5520F5580FA,05027101043D62FA6F57806E,05027501047FC8321E44802C,05027101042886FA6F57809F,050271010491B2FA6F578012,05027101040A8CFA6F5780B7,050271010479B3FA6F5780FB,0502710104378EFA6F578088,05027101047E8BFA6F5780C4,0502710104B88FFA6F578006,050271010449A1520F558013,05027101047C8CFA6F5780C1,050271010448B0FA6F5780C9,05027101043BBD520F55807D,0502710104A5660A1A558075,05027101047FB3FA6F5780FD,050271010414380A1A55809A,05027101040B8CFA6F5780B6,050271010425380A1A5580AB,0502710104BF352A70578074,05027101041869FA6F578040,0502710104AD87FA6F57801B,0502710104B6350A1A558035,050271010431B4FA6F5780B4,050271010446B3FA6F5780C4,05027101044EB0FA6F5780CF,05027101049DBC0A1A558097,05027101048769FA6F5780DF,0502710104B89C520F5580DF,0502710104158DFA6F5780A9,0502710104B29C520F5580D5,050271010443BE520F558006,050271010437690A1A5580E8,050271010491BC0A1A55809B,05027101048E380A1A558000,05027101046DA3520F558035,05027101047187FA6F5780C7,0502710104628EFA6F5780DD,0502710104118DFA6F5780AD,050271010478BE520F55803D,05027101043EB2FA6F5780BD,0502710104B08BFA6F57800A,050271010447C90A1A558038,0502710104229F520F558046,0502710104208F520F558054,05027101043CB8520F55807F,0502710104AF52420F558016,0502710104B486FA6F578003,0502710104B3340A1A558031,05027101042794520F558048,0502710104249C520F558043,05027101048EBE0A1A558086,0502710104D069FA6F578088,0502710104CE68FA6F578097,0502710104988EFA6F578027,05027101046763FA6F578035,050271010430B0FA6F5780B1,0502710104AD670A1A55807C,0502710104178BFA6F5780AD,05027101041B690A1A5580C4,05027101040A69FA6F578052,05027101042A300A1A5580AC,050271010440310A1A5580C7,050271010431BD0A1A55803A,0502710104B2380A1A55803C,050271010497C80A1A5580E9,05027101043A310A1A5580BD,0502710104458CFA6F5780F8,05027101042E9F520F55804A,050271010427B1FA6F5780A7,050271010432690A1A5580ED,0502710104698D520F55801F,0502710104AB660A1A55807B,050271010444B2FA6F5780C7,0502710104B58DFA6F578009,050271010477BC520F558030,0502710104888F520F5580FC,05027101047DC00A1A55800B,050271010495670A1A558044,050271010451320A1A5580D5,0502710104188BFA6F5780A2,0502710104A2BC0A1A5580A8,05027101045DBF520F558019,050271010464BD0A1A55806F,0502710104748CFA6F5780C9,05027101045AB0FA6F5780DB,050271010448C00A1A55803E,05027101040C8C520F55807B,05027101042C9C520F55804B,05027101048486FA6F578033,05027101047C9C520F55801B,05027101047AB1FA6F5780FA,0502710104CB662A70578053,05027101041C9C520F55807B,05027101048869FA6F5780D0,05027101046BB4FA6F5780EE,05027101042AB1FA6F5780AA,05027101048A64FA6F5780DF,05027101041467FA6F578042,0502710104598BFA6F5780E3,05027101043792520F55805E,050271010473A6520F55802E,05027101047FBD520F558039,0502710104CC68FA6F578095,0502710104979B520F5580F7,050271010453B4FA6F5780D6,05027101047AA7520F558026,0502710104219C520F558046,0502710104A2B2FA6F578021,0502710104C964FA6F57809C,0502710104AEB0FA6F57802F,0502710104CF68FA6F578096,05027101045B89FA6F5780E3,05027101043C9C520F55805B,05027101046C690A1A5580B3,050271010457B0FA6F5780D6,050271010418690A1A5580C7,05027101043193520F558059,05027101048069FA6F5780D8,050271010433330A1A5580B6,05027101046B670A1A5580BA,0502710104BE662A70578026,0502710104CC692A7057805B,050271010482B2FA6F578001,05027101049593520F5580FD,0502710104189B520F558078,0502710104CC662A70578054,0502710104B28BFA6F578008,050271010428690A1A5580F7,0502710104A78FFA6F578019,0502710104289C520F55804F,0502710104588BFA6F5780E2,05027101048E9C520F5580E9,05027101047FB2FA6F5780FC,050271010468A3520F558030,05027101049ABC0A1A558090,0502710104B2B3FA6F578030,0502710104A0330A1A558025,05027101049EC1520F5580A4,0502710104674042315080F7,050271010476670A1A5580A7,0502710104B7B2FA6F578034,050271010456A1520F55800C,05027101041C690A1A5580C3,05027101044C69FA6F578014"
              .split(",");
      for (String bagId : split) {
        otherList.add(bagId);
      }
    }

    @Override public void run() {
      boolean readNfc = rfidController.readNfc(Lock3Bean.SA_BAG_ID, bagIdBuff, true);
      boolean readEpc = false;
      if (isReadEpc) {
        readEpc = uhfController.readEpc(epcBuff);
      }

      String bagIdNfc = null;
      String bagIdEpc = null;
      if (readNfc) {
        bagIdNfc = BytesUtil.bytes2HexString(bagIdBuff);
      }
      if (readEpc) {
        bagIdEpc = BytesUtil.bytes2HexString(epcBuff);
      }
      onRead(bagIdNfc, bagIdEpc);
    } // end run()

    protected void onRead(String bagIdNfc, String bagIdEpc) {
      String info;
      boolean otherNoHave = false;
      if (bagIdNfc != null && bagIdEpc != null) {
        if (!scanList.contains(bagIdNfc)) {
          scanList.add(bagIdNfc);
        } else {
          //scanList.indexOf(bagIdNfc);
        }
        if (!otherList.contains(bagIdNfc)) {
          otherNoHaveList.add(bagIdNfc);
          otherNoHave = true;
        }
        int size = scanList.indexOf(bagIdNfc) + 1;
        SoundUtils.playNumber(size);
        info = String.format("%s,Nfc:%s\n%s,Epc:%s", size, bagIdNfc, size, bagIdEpc);
      } else if (bagIdNfc != null) {
        if (!scanList.contains(bagIdNfc)) {
          scanList.add(bagIdNfc);
        } else {
          //scanList.indexOf(bagIdNfc);
        }
        if (!otherList.contains(bagIdNfc)) {
          otherNoHaveList.add(bagIdNfc);
          otherNoHave = true;
        }
        //int size = scanList.size();
        int size = scanList.indexOf(bagIdNfc) + 1;
        SoundUtils.playNumber(size);
        info = String.format("%s,Nfc:%s", size, bagIdNfc);
      } else if (bagIdEpc != null) {
        SoundUtils.playToneFailed();
        info = String.format("Epc:%s", bagIdEpc);
      } else {
        SoundUtils.playToneFailed();
        info = "读Nfc和Epc失败";
      }

      boolean finalOtherNoHave = otherNoHave;
      runOnUi(() -> {
        ViewUtil.appendShow(info, tvShow);
        if (finalOtherNoHave) {
          SoundUtils.playToneSuccess();
          ViewUtil.appendShow("发现未在列表中", tvShow);
        }
      });
    }
  }

  private Spanned parse(Lock3Bean lock3Bean) {
    if (lock3Bean == null) {
      return null;
    }
    HtmlUtil.setDefaultColor(0x0000FF);
    String colorText = HtmlUtil.getColorText("锁片epc：%s\n"
            + "锁片tid：%s\n"
            + "业务tid：%s\n"
            + "锁内tid：%s\n"
            + "bagId：%s\n"
            + "标志位：%s\n"
            + "电压：%s\n"
            + "启用状态：%s\n"
            + "测试模式：%s\n"
            + "封签码：%s\n"
            + "流水号：%s\n"
            + "密钥编号：%s\n"
            + "交接索引：%s\n"
            + "袋流转索引：%s\n",
        lock3Bean.getPieceEpc(),
        lock3Bean.getPieceTid(),
        lock3Bean.getTidFromPiece(),
        lock3Bean.getTidFromLock(),
        lock3Bean.getBagId(),
        Lock3Util.getStatusDesc(lock3Bean.getStatus()),
        String.format("%.3f", lock3Bean.getVoltage()),
        lock3Bean.getEnable(),
        lock3Bean.isTestMode(),
        lock3Bean.getCoverCode(),
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
      sb.append(HtmlUtil.getColorText("业务：%s(%s)\n"
              + "机构：%s(%s)\n"
              + "操作人：%s\n"
              + "复核人：%s\n"
              + "时间：%s\n",
          handoverBean.getFunTypeName(),
          handoverBean.getFunction(),
          handoverBean.getOrgancode(),
          handoverBean.getOrgName(),
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
      SoundUtils.playToneSuccess();
      runOnUiThread(() -> {
        dismissLoadingView();
        Spanned parse = parse(lock3Bean);
        ViewUtil.appendShow(null, tvShow);
        ViewUtil.appendShow(parse, tvShow);
        ViewUtil.appendShow("用时：" + ClockUtil.runTime(), tvShow);
      });
    }

    @Override protected void onFailed(int errorCode) {
      super.onFailed(errorCode);
      runOnUiThread(() -> {
        dismissLoadingView();
        ViewUtil.appendShow("读袋锁信息失败.cause:" + errorCode, tvShow);
      });
    }
  }
}
