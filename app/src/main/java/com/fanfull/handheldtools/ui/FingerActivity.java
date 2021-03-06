package com.fanfull.handheldtools.ui;

import android.os.Bundle;
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
import com.fanfull.libhard.finger.IFingerListener;
import com.fanfull.libhard.finger.bean.FingerBean;
import com.fanfull.libhard.finger.db.FingerPrintSQLiteHelper;
import com.fanfull.libhard.finger.impl.FingerPrintCmd;
import com.fanfull.libhard.finger.impl.FingerPrintTask;
import com.fanfull.libhard.finger.impl.FingerprintController;
import com.fanfull.libhard.uhf.IUhfListener;
import com.fanfull.libhard.uhf.UhfCmd;
import com.fanfull.libhard.uhf.UhfController;
import com.fanfull.libjava.util.BytesUtil;
import com.fanfull.libjava.util.ClockUtil;
import com.fanfull.libjava.util.ThreadUtil;
import com.lxj.xpopup.XPopup;

import org.orsoul.baselib.util.ViewUtil;

import java.util.List;

public class FingerActivity extends InitModuleActivity {

  private TextView tvShow;
  private Button btnSearch;
  private Button btnNum;

  private int fingerId = 127;
  private FingerprintController fingerprintController;
  private FingerPrintTask fingerPrintTask;
  private FingerPrintSQLiteHelper fingerPrintDbHelper;

  private UhfController uhfController;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override protected void initView() {
    setContentView(R.layout.activity_finger);
    tvShow = findViewById(R.id.tv_finger_show);
    tvShow.setMovementMethod(ScrollingMovementMethod.getInstance());
    tvShow.setOnClickListener(this);

    btnSearch = findViewById(R.id.btn_finger_search);
    btnSearch.setOnClickListener(this);
    btnSearch.setEnabled(false);

    btnNum = findViewById(R.id.btn_finger_num);
    btnNum.setOnClickListener(this);
    btnNum.setEnabled(false);

    Switch fingerAdd = findViewById(R.id.switch_finger_reg);
    fingerAdd.setOnCheckedChangeListener((buttonView, isChecked) -> {
      if (fingerPrintTask != null) {
        fingerPrintTask.setAddMode(isChecked);
        fingerPrintTask.setGetFeature(isChecked);
      }
      if (isChecked) {
        btnSearch.setText(R.string.v_add_finger);
      } else {
        btnSearch.setText(R.string.v_match_finger);
      }
    });
  }

  @Override public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_finger_search:
        if (fingerPrintTask.startThread()) {
          showLoadingView("????????????...");
          btnSearch.setEnabled(false);
          btnNum.setEnabled(false);
        } else {
          ToastUtils.showShort("????????????????????????");
        }
        break;
      case R.id.btn_finger_num:
        btnSearch.setEnabled(false);
        btnNum.setEnabled(false);
        int[] resBuff = new int[1];
        int res = fingerprintController.getFingerNum(resBuff);
        if (res == 0) {
          ViewUtil.appendShow(String.format("????????????????????????%s", resBuff[0]), tvShow);
        } else {
          ViewUtil.appendShow(String.format("???????????????????????????cause???%s", res),
              tvShow);
        }
        btnSearch.setEnabled(true);
        btnNum.setEnabled(true);
        break;
      case R.id.tv_finger_show:
        if (3 == ClockUtil.fastClickTimes()) {
          ViewUtil.appendShow(tvShow, "");
        }
        break;
    }
  }

  @Override protected void initModule() {
    uhfController = UhfController.getInstance();
    uhfController.setListener(new IUhfListener() {
      @Override public void onOpen(boolean openSuccess) {
        runOnUiThread(() -> {
          dismissLoadingView();
          if (!openSuccess) {
            ViewUtil.appendShow("????????? ?????????????????????", tvShow);
          } else {
            ViewUtil.appendShow("????????? ???????????????", tvShow);
          }
        });
      }

      @Override
      public void onReceiveData(byte[] data) {
        byte[] parseData = UhfCmd.parseData(data);
        if (parseData == null) {
          //LogUtils.i("parseData failed:%s", BytesUtil.bytes2HexString(data));
          return;
        }
        int cmdType = data[4] & 0xFF;
        Object info = null;
        switch (cmdType) {
          case UhfCmd.RECEIVE_TYPE_GET_DEVICE_VERSION:
            info = String.format("???????????????v%s.%s.%s", parseData[0], parseData[1], parseData[2]);
            break;
          case UhfCmd.RECEIVE_TYPE_GET_DEVICE_ID:
            info = String.format("??????Id???%s", BytesUtil.bytes2HexString(parseData));
            break;
          case UhfCmd.RECEIVE_TYPE_GET_FAST_ID:
            if (parseData[0] == 1) {
              info = "EPC???TID???????????? ??????";
            } else {
              info = "EPC???TID???????????? ??????";
            }
            break;
          case UhfCmd.RECEIVE_TYPE_SET_FAST_ID:
            // 1:????????? 0?????????
            if (parseData[0] == 1) {
              info = "EPC???TID???????????? ??????";
              info = "EPC???TID???????????? ??????";
            } else {
              info = "?????? EPC???TID?????? ??????";
            }
            break;
          case UhfCmd.RECEIVE_TYPE_READ_LOT:
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
            LogUtils.v("parseData:%s", BytesUtil.bytes2HexString(parseData));
        }
        if (info != null) {
        }
      }
    });

    fingerprintController = FingerprintController.getInstance();
    fingerprintController.setListener(new IFingerListener() {
      @Override public void onOpen(boolean openSuccess) {
        runOnUiThread(() -> {
          dismissLoadingView();
          if (!openSuccess) {
            tvShow.setText("??????????????????????????????");
          } else {
            tvShow.setText("???????????????\n"
                + "??????1 -> ???????????????\n"
                + "??????2 -> ????????????2\n"
                + "??????3 -> ???????????????????????????\n"
                + "??????4 -> ????????????\n"
                + "??????5 -> ?????????????????????\n"
                + "??????6 -> ???Epc\n"
                + "??????7 -> ???????????????---??????--->?????????\n"
                + "??????8 -> ?????????????????????\n"
                + "??????9 -> ???????????????\n");
            btnSearch.setEnabled(true);
            btnNum.setEnabled(true);

            showLoadingView("???????????????????????????...");
            uhfController.open();
          }
        });
      }

      @Override public void onReceiveData(byte[] data) {
        //ViewUtil.appendShow("info", tvShow);
        //LogUtils.v("onReceiveData:%s", ArrayUtils.bytes2HexString(data));
      }
    });
    showLoadingView("????????????????????????...");
    fingerprintController.init(this);
    //fingerprintController.open();
    //FingerPrintSQLiteHelper.init(this);
    fingerPrintDbHelper = FingerPrintSQLiteHelper.getInstance();

    /* ???????????? */
    fingerPrintTask = new FingerPrintTask(new FingerPrintTask.FingerSearchListener() {
      int count;

      @Override public void onNoFinger() {
        runOnUiThread(() -> {
          count++;
          showLoadingView("?????????????????? " + count);
        });
      }

      @Override public void onAddSuccess(FingerBean fingerBean, boolean isSaveInDB) {
        count = 0;
        runOnUiThread(() -> {
          ViewUtil.appendShow(String.format("???????????????FingerIndex???%s, %s",
              fingerBean.getFingerIndex(), isSaveInDB ? "???????????????" : "??????????????????"), tvShow);
          btnSearch.setEnabled(true);
          btnNum.setEnabled(true);
          dismissLoadingView();
        });
      }

      @Override public void onSearchSuccess(FingerBean fingerBean, boolean isSaveInDB) {
        count = 0;
        runOnUiThread(() -> {
          ViewUtil.appendShow(String.format("???????????????FingerIndex???%s, %s",
              fingerBean.getFingerIndex(), isSaveInDB ? "???????????????" : "??????????????????"), tvShow);
          if (isSaveInDB) {
            ViewUtil.appendShow(String.format("FingerId???%s, FingerVersion???%s",
                fingerBean.getFingerId(), fingerBean.getFingerVersion()), tvShow);
          }
          btnSearch.setEnabled(true);
          btnNum.setEnabled(true);
          dismissLoadingView();
        });
      }

      @Override public void onSearchNoMatch() {
        count = 0;
        fingerPrintTask.stopThread();
        runOnUiThread(() -> {
          dismissLoadingView();
          ViewUtil.appendShow("??????????????????", tvShow);
          btnSearch.setEnabled(true);
          btnNum.setEnabled(true);
        });
      }

      @Override public void onFailed(boolean isAddMode, int errorCode) {
        if (errorCode != FingerPrintCmd.RES_CODE_TIMEOUT) {
          LogUtils.d("errorCode:%s", errorCode);
          return;
        }
        count = 0;
        runOnUiThread(() -> {
          dismissLoadingView();
          if (isAddMode) {
            ViewUtil.appendShow(String.format("???????????????case:%s", errorCode), tvShow);
          } else {
            ViewUtil.appendShow(String.format("???????????????case:%s", errorCode), tvShow);
          }
          btnSearch.setEnabled(true);
          btnNum.setEnabled(true);
        });
      }
    });
  }

  @Override
  protected void onEnterPress() {
    if (btnSearch.isEnabled()) {
      onClick(btnSearch);
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    switch (keyCode) {
      case KeyEvent.KEYCODE_0:
        fingerprintController.operation.send(FingerPrintCmd.CMD_READ_SYS_PARA);
        break;
      case KeyEvent.KEYCODE_1:
        int res = fingerprintController.operation.genFingerFeature();
        if (res == 0) {
          ViewUtil.appendShow(String.format("???????????????????????????"), tvShow);
        } else {
          ViewUtil.appendShow(String.format("??????????????????????????????cause???%s", res), tvShow);
        }
        break;
      case KeyEvent.KEYCODE_2:
        ThreadUtil.execute(() -> {
          int[] resBuff = new int[2];
          int res1 = fingerprintController.operation.searchFinger2(resBuff);
          runOnUiThread(() -> {
            if (res1 == 0) {
              ViewUtil.appendShow(String.format("???????????????fingerID???%s,    score:%s",
                  resBuff[0], resBuff[1]), tvShow);
              byte[] bytes = new byte[512];
              fingerprintController.operation.getFingerFeature(resBuff[0], bytes);
            } else {
              ViewUtil.appendShow(String.format("???????????????case:%s", res1), tvShow);
            }
          });
        });
        break;
      case KeyEvent.KEYCODE_3:
        new XPopup.Builder(this).asInputConfirm("????????????", "????????????ID", fingerId + "", "????????????ID",
            text -> {
              try {
                fingerId = Integer.parseInt(text);
              } catch (Exception e) {
                ToastUtils.showShort("??????????????????");
                return;
              }

              ThreadUtil.execute(() -> {
                byte[] fingerFeature = new byte[512];
                int res1 = fingerprintController.operation.addFinger2(fingerId, fingerFeature);
                long[] dbId = new long[] { -1L };
                if (res1 == 0) {
                  FingerBean fingerBean = new FingerBean(fingerId, fingerFeature);
                  dbId[0] = fingerPrintDbHelper.saveOrUpdate(fingerBean);
                }
                runOnUiThread(() -> {
                  if (res1 == 0) {
                    ViewUtil.appendShow(
                        String.format("???????????????fingerId???%s???db Id???%s", fingerId, dbId[0]), tvShow);
                  } else {
                    ViewUtil.appendShow(String.format("???????????????case:%s", res1), tvShow);
                  }
                });
              });
            }).show();
        break;
      case KeyEvent.KEYCODE_4:
        btnSearch.setEnabled(false);
        btnNum.setEnabled(false);
        boolean deleteSuccess = fingerprintController.deleteFinger(fingerId);
        ViewUtil.appendShow(String.format("???????????? %s ????????????%s", fingerId, deleteSuccess), tvShow);
        LogUtils.d("???????????? %s ????????????%s", fingerId, deleteSuccess);
        btnSearch.setEnabled(true);
        btnNum.setEnabled(true);
        break;
      case KeyEvent.KEYCODE_5:
        List<FingerBean> fingerBeanList = fingerPrintDbHelper.queryAllFinger();
        int[] resBuff = new int[1];
        res = fingerprintController.getFingerNum(resBuff);
        if (res == 0) {
          ViewUtil.appendShow(String.format("????????????????????????%s???????????????%s",
              fingerBeanList.size(), resBuff[0]), tvShow);
        } else {
          ViewUtil.appendShow(String.format("????????????????????????%s??????????????????????????????", fingerBeanList.size()), tvShow);
        }
        for (int i = 0; i < fingerBeanList.size(); i++) {
          ViewUtil.appendShow(String.format("====== %s ======\n%s",
              i, fingerBeanList.get(i).toInfo()), tvShow);
        }
        break;
      case KeyEvent.KEYCODE_6:
        byte[] fastEpc = uhfController.readEpcWithTid(800);
        ViewUtil.appendShow(String.format("readEpcWithTid???%s", BytesUtil.bytes2HexString(fastEpc)),
            tvShow);
        break;
      case KeyEvent.KEYCODE_7:
        List<FingerBean> fingerBeans = fingerPrintDbHelper.queryAllFinger();
        LogUtils.d("fingerBean size:%s", fingerBeans.size());
        LogUtils.v("fingerBeans:%s", fingerBeans);
        if (fingerBeans.isEmpty()) {
          return true;
        }
        showLoadingView();
        ThreadUtil.execute(() -> {
          int res1 = fingerprintController.loadFinger(fingerBeans);
          runOnUiThread(() -> {
            ViewUtil.appendShow(String.format("???????????????????????????%s / %s", res1, fingerBeans.size()),
                tvShow);
            dismissLoadingView();
          });
        });
        break;
      case KeyEvent.KEYCODE_8:
        List<Integer> indexList = fingerPrintDbHelper.queryAllFingerIndex();
        LogUtils.d("indexList:%s", indexList);
        fingerPrintDbHelper.clearAll();
        ViewUtil.appendShow(String.format("?????????????????????,num: %s", indexList.size()), tvShow);
        break;
      case KeyEvent.KEYCODE_9:
        btnSearch.setEnabled(false);
        btnNum.setEnabled(false);
        boolean clearSuccess = fingerprintController.clearFinger();
        LogUtils.d("??????????????? ?????????:%s", clearSuccess);
        ViewUtil.appendShow(String.format("??????????????? ????????????%s", clearSuccess), tvShow);
        btnSearch.setEnabled(true);
        btnNum.setEnabled(true);
        break;
      default:
        return super.onKeyDown(keyCode, event);
    }
    return true;
  }

  @Override
  protected void onDestroy() {
    fingerprintController.release();
    if (fingerPrintDbHelper != null) {
      fingerPrintDbHelper.close();
    }
    if (uhfController != null) {
      uhfController.release();
    }
    super.onDestroy();
  }
}
