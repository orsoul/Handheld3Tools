package com.fanfull.handheldtools.ui;

import android.os.Bundle;
import android.os.SystemClock;
import android.text.Html;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
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
import com.fanfull.libjava.util.BytesUtil;
import com.fanfull.libjava.util.ClockUtil;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnConfirmListener;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.orsoul.baselib.lock3.Lock3Util;
import org.orsoul.baselib.lock3.LockCoder;
import org.orsoul.baselib.lock3.bean.HandoverBean;
import org.orsoul.baselib.lock3.bean.Lock3Bean;
import org.orsoul.baselib.lock3.bean.Lock3InfoUnit;
import org.orsoul.baselib.util.HtmlUtil;
import org.orsoul.baselib.util.SoundHelper;
import org.orsoul.baselib.util.ViewUtil;

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
            ViewUtil.appendShow("?????????????????????!!!", tvShow);
            return;
          }
          //tvShow.setText("????????????.\n"
          //    + "3?????????->??????\n"
          //    + "Enter->??????/?????? ????????????\n"
          //    + "??????7->????????????\n"
          //    + "??????8->?????? EPC???TID??????\n"
          //    + "??????9->?????? EPC???TID??????\n\n");
          //ViewUtil.appendShow("?????????????????????", tvShow);
          ViewUtil.appendShow("?????????????????????", tvShow);
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
            info = String.format("????????????:v%s.%s.%s", parseData[0], parseData[1], parseData[2]);
            break;
          case UhfCmd.RECEIVE_TYPE_GET_DEVICE_ID:
            info = String.format("??????Id:%s", BytesUtil.bytes2HexString(parseData));
            break;
          case UhfCmd.RECEIVE_TYPE_GET_FAST_ID:
            if (parseData[0] == 1) {
              info = "EPC???TID???????????? ??????";
            } else {
              info = "EPC???TID???????????? ??????";
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
              info = "?????????";
            } else {
              info = String.format("?????????,cause:%X", parseData[0]);
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
            ViewUtil.appendShow("????????????????????????!!!", tvShow);
            return;
          }
          // TODO: 2020-11-13 ???????????????
          //tvShow.setText("???????????????");
          //ViewUtil.appendShow("????????????????????????", tvShow);
          ViewUtil.appendShow("????????????????????????\n"
              + "??????1~4->F1~F4\n"
              + "??????6->???tid????????????epc", tvShow);
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

    showLoadingView("?????????????????????...");
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
          showLoadingView("????????????????????????...");
        } else {
          ToastUtils.showShort("?????????????????????????????????");
        }
        break;
      case R.id.btn_check_bag_stopScan:
        showSetUhfPower(this);
        //String info;
        //if (readLockTask != null) {
        //  info = String.format("??????????????????:%s", readLockTask.otherNoHaveList);
        //  LogUtils.wtf("????????????:%s", readLockTask.scanList);
        //} else {
        //  info = "??????????????????";
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
          msg = "??????tid???epc??????";
        }
        ViewUtil.appendShow(msg, tvShow);
        break;
      //Lock3Operation.getInstance().writeLockNfc();
      //if (!readLockTask.isRunning()) {
      //  ClockUtil.runTime(true);
      //  readLockTask.startThread();
      //} else {
      //  ToastUtils.showShort("???????????????????????????...");
      //}
      //return true;
    }
    if (0 < fn) {
      boolean res = Lock3Operation.getInstance().writeLockStatus(fn);
      String info;
      if (res) {
        info = String.format("??????????????? %s", Lock3Util.getStatusDesc(fn));
        SoundHelper.playToneSuccess();
      } else {
        info = String.format("??????????????? F%s", fn);
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
    LogUtils.d("???????????????:%s", lock3Bean.getCoverCode());

    String coverCode;
    Lock3InfoUnit infoUnit = lock3Bean.getInfoUnit(Lock3Bean.SA_PIECE_TID);
    Lock3InfoUnit infoUnitCover = lock3Bean.getInfoUnit(Lock3Bean.SA_COVER_EVENT);

    String compareMsg;
    String pieceTid = lock3Bean.getPieceTid();
    String businessTid = lock3Bean.getTidFromPiece();

    byte[] key;
    if (pieceTid == null) {
      compareMsg = "????????????";
      if (businessTid.startsWith("E280")) {
        key = Arrays.copyOf(infoUnit.buff, 12);
      } else {
        key = Arrays.copyOf(infoUnit.buff, 6);
      }
    } else if (!pieceTid.equals(businessTid)) {
      LogUtils.i("\n??????Tid:%s\n??????Tid:%s", pieceTid, businessTid);
      String businessTid6 = businessTid.substring(0, 12);
      String pieceTid6 = pieceTid.substring(12);
      LogUtils.i("\n??????Tid6:%s\n??????Tid6:%s", pieceTid6, businessTid6);
      compareMsg = String.format("%s???%s",
          pieceTid6, Objects.equals(pieceTid6, businessTid6) ? "??????" : "?????????");
      key = Arrays.copyOf(infoUnit.buff, 6);
    } else {
      compareMsg = "????????????";
      key = Arrays.copyOf(infoUnit.buff, 12);
    }

    boolean b = LockCoder.myEncrypt(infoUnitCover.buff, key, false);
    if (b) {
      String tid1 = BytesUtil.bytes2HexString(infoUnitCover.buff);
      infoUnitCover.buff[3] = (byte) (infoUnitCover.buff[3] & 0x0F);
      String tid2 = BytesUtil.bytes2HexString(infoUnitCover.buff);
      coverCode = String.format("???????????????:%s\n%s", tid2.startsWith(lock3Bean.getBagId()), tid1);
      //LogUtils.d("??????:%s", BytesUtil.bytes2HexString(infoUnitCover.buff));
    } else {
      coverCode = String.format("%s-????????????", BytesUtil.bytes2HexString(infoUnitCover.buff));
      //coverCode = "????????????";
    }

    //if (pieceTid == null) {
    //  compareMsg = "????????????";
    //} else if (pieceTid.equals(lock3Bean.getTidFromPiece())) {
    //  compareMsg = "????????????";
    //} else {
    //  String tid6 = lock3Bean.getTidFromPiece().substring(0, 12);
    //  String tid12 = pieceTid.substring(12);
    //  compareMsg = String.format("%s???%s",
    //      tid12, Objects.equals(tid6, tid12) ? "??????" : "?????????");
    //}

    String colorText = HtmlUtil.getColorText(
        "??????epc:%s\n"
            + "??????tid:%s\n"
            + "??????tid:%s\n" // ??????tid??????
            + "??????tid:%s\n"
            + "??????tid:%s\n"
            + "bagId:%s\n"
            + "?????????:%s\n"
            + "???????????????:%s\n"
            + "??????:%s,    uid:%s\n"
            + "????????????:%s\n"
            + "????????????:%s\n"
            + "?????????:%s\n"
            + "?????????:%s\n"
            + "????????????:%s\n"
            + "????????????:%s\n"
            + "========================\n???????????????:%s\n",
        lock3Bean.getPieceEpc(),
        lock3Bean.getPieceTid(),
        compareMsg,
        lock3Bean.getTidFromPiece(),
        lock3Bean.getTidFromLock(),
        lock3Bean.getBagId(),
        Lock3Util.getStatusDesc(lock3Bean.getStatus()),
        Lock3Util.getStatusCheckDesc(lock3Bean.getStatusCheck()),
        Lock3Util.parseV2String(lock3Bean.getVoltage()),
        BytesUtil.bytes2HexString(lock3Bean.uidBuff),
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
        orgName = "????????????";
      }
      sb.append(HtmlUtil.getColorText("??????:%s(%s)\n"
              + "??????:%s(%s)\n"
              + "?????????:%s\n"
              + "?????????:%s\n"
              + "??????:%s\n",
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
        tvShow.append("\n??????:" + ClockUtil.runTime() + "\n");
        tvShow.scrollTo(0, 0);
        LogUtils.d("\n%s", tvShow.getText());
        //ViewUtil.appendShow("??????:" + ClockUtil.runTime(), tvShow);
        //tvShow.scrollTo(0, 0);
      });
    }

    @Override protected void onFailed(int errorCode) {
      String msg;
      switch (errorCode) {
        case -1:
          msg = String.format("??????????????????cause:%s ????????????", errorCode);
          break;
        case -2:
          msg = String.format("??????????????????cause:%s nfc????????????", errorCode);
          break;
        case -3:
          msg = String.format("??????????????????cause:%s ???tid??????", errorCode);
          break;
        case -4:
          msg = String.format("??????????????????cause:%s ???epc??????", errorCode);
          break;
        case -5:
          msg = String.format("??????????????????cause:%s ???nfc??????", errorCode);
          break;
        default:
          msg = String.format("??????????????????cause:%s ???????????????", errorCode);
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
        .asConfirm(null, "?????????????????????????????????????????????XPopup?????????????????????????????????????????????????????????????????????????????????\n" +
                "??????????????????:??????????????????????????????????????????Id?????????XPopup?????????View???\n????????????????????????Id????????????WIKI[????????????]?????????",
            "??????", "XPopup??????",
            new OnConfirmListener() {
              @Override
              public void onConfirm() {
              }
            }, null, false)//??????????????????
        .show();
  }
}
