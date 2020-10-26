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
import com.fanfull.handheldtools.base.InitModuleActivity;
import com.fanfull.libhard.finger.IFingerListener;
import com.fanfull.libhard.finger.bean.FingerBean;
import com.fanfull.libhard.finger.db.FingerPrintSQLiteHelper;
import com.fanfull.libhard.finger.impl.FingerPrintCmd;
import com.fanfull.libhard.finger.impl.FingerprintController;
import com.lxj.xpopup.XPopup;
import org.orsoul.baselib.util.BytesUtil;
import org.orsoul.baselib.util.ClockUtil;
import org.orsoul.baselib.util.ThreadUtil;
import org.orsoul.baselib.util.ViewUtil;

public class FingerActivity extends InitModuleActivity {

  private TextView tvShow;
  private Button btnSearch;
  private Button btnNum;

  private int fingerId = 127;
  private FingerprintController fingerprintController;
  private FingerprintController.FingerPrintTask fingerPrintTask;
  private FingerPrintSQLiteHelper fingerPrintDbHelper;

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
      }
    });
  }

  @Override public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_finger_search:
        if (fingerPrintTask.startRun()) {
          showLoadingView("识别指纹...");
          btnSearch.setEnabled(false);
          btnNum.setEnabled(false);
        } else {
          ToastUtils.showShort("指纹识别已在进行");
        }
        break;
      case R.id.btn_finger_num:
        btnSearch.setEnabled(false);
        btnNum.setEnabled(false);
        int[] resBuff = new int[1];
        int res = fingerprintController.getFingerNum(resBuff);
        if (res == 0) {
          ViewUtil.appendShow(String.format("已添加指纹数量：%s", resBuff[0]), tvShow);
        } else {
          ViewUtil.appendShow(String.format("获取指纹数量失败，cause：%s", res),
              tvShow);
        }
        btnSearch.setEnabled(true);
        btnNum.setEnabled(true);
        break;
      case R.id.tv_finger_show:
        if (3 == ClockUtil.fastClickTimes()) {
          ViewUtil.appendShow(null, tvShow);
        }
        break;
    }
  }

  @Override protected void initModule() {
    fingerPrintDbHelper = new FingerPrintSQLiteHelper(this);
    fingerprintController = FingerprintController.getInstance();
    fingerprintController.setListener(new IFingerListener() {
      @Override public void onOpen(boolean openSuccess) {
        runOnUi(() -> {
          dismissLoadingView();
          if (!openSuccess) {
            tvShow.setText("模块初始化失败！！！");
            //ViewUtil.appendShow("模块初始化失败！！！", tvShow);
          } else {
            //ViewUtil.appendShow("模块初始化失败！！！", tvShow);
            tvShow.setText("初始化成功\n"
                + "按键1 -> 生成特征码\n"
                + "按键2 -> 搜索指纹2\n"
                + "按键3 -> 添加指纹到指定位置\n"
                + "按键9 -> 清空指纹库\n");
            btnSearch.setEnabled(true);
            btnNum.setEnabled(true);
          }
        });
      }

      @Override public void onReceiveData(byte[] data) {
        //ViewUtil.appendShow("info", tvShow);
        //LogUtils.v("onReceiveData:%s", ArrayUtils.bytes2HexString(data));
      }
    });
    showLoadingView("正在打开指纹模块...");
    fingerprintController.open();

    /* 指纹任务 */
    fingerPrintTask = new FingerprintController.FingerPrintTask(fingerprintController) {
      int count;

      @Override protected void onNoFinger() {
        runOnUi(() -> {
          count++;
          showLoadingView("感应器无指纹 " + count);
        });
      }

      @Override protected void onSuccess(boolean isAddMode, int fingerId, int score) {
        count = 0;
        runOnUi(() -> {
          dismissLoadingView();
          if (isAddMode) {
            ViewUtil.appendShow(String.format("添加成功，fingerID：%s", fingerId), tvShow);
          } else {
            ViewUtil.appendShow(String.format("搜索成功，fingerID：%s,    score:%s", fingerId, score),
                tvShow);
          }
          btnSearch.setEnabled(true);
          btnNum.setEnabled(true);
        });
      }

      @Override protected void onFailed(boolean isAddMode, int errorCode) {
        count = 0;
        runOnUi(() -> {
          dismissLoadingView();
          if (isAddMode) {
            ViewUtil.appendShow(String.format("添加失败，case:%s", errorCode), tvShow);
          } else {
            ViewUtil.appendShow(String.format("搜索失败，case:%s", errorCode), tvShow);
          }
          btnSearch.setEnabled(true);
          btnNum.setEnabled(true);
        });
      }
    };
  }

  @Override
  protected void onEnterPress() {
    if (fingerprintController.isSearch()) {
      fingerprintController.stopSearchFingerPrint();
    } else {
      fingerprintController.startSearchFingerPrint();
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
          ViewUtil.appendShow(String.format("生成指纹特征码成功"), tvShow);
        } else {
          ViewUtil.appendShow(String.format("生成指纹特征码失败，cause：%s", res), tvShow);
        }
        break;
      case KeyEvent.KEYCODE_2:
        ThreadUtil.execute(() -> {
          int[] resBuff = new int[2];
          int res1 = fingerprintController.operation.searchFinger2(resBuff);
          runOnUi(() -> {
            if (res1 == 0) {
              ViewUtil.appendShow(String.format("搜索成功，fingerID：%s,    score:%s",
                  resBuff[0], resBuff[1]), tvShow);
              byte[] bytes = new byte[512];
              fingerprintController.operation.getFingerFeature(resBuff[0], bytes);
            } else {
              ViewUtil.appendShow(String.format("搜索失败，case:%s", res1), tvShow);
            }
          });
        });
        break;
      case KeyEvent.KEYCODE_3:
        //new XPopup.Builder(this).inp
        new XPopup.Builder(this).asInputConfirm("添加指纹", "输入指纹ID", fingerId + "", "输入指纹ID",
            text -> {
              try {
                fingerId = Integer.parseInt(text);
              } catch (Exception e) {
                ToastUtils.showShort("请输入正整数");
                return;
              }

              ThreadUtil.execute(() -> {
                byte[] fingerFeature = new byte[512];
                int res1 = fingerprintController.operation.addFinger(fingerId, fingerFeature);
                long[] dbId = new long[] { -1L };
                if (res1 == 0) {
                  FingerBean fingerBean = new FingerBean(fingerId, fingerFeature);
                  dbId[0] = fingerPrintDbHelper.saveOrUpdate(fingerBean);
                }
                runOnUi(() -> {
                  if (res1 == 0) {
                    ViewUtil.appendShow(
                        String.format("添加成功，fingerId：%s，db Id：%s", fingerId, dbId[0]), tvShow);
                  } else {
                    ViewUtil.appendShow(String.format("添加失败，case:%s", res1), tvShow);
                  }
                });
              });
            }).show();
        break;
      case KeyEvent.KEYCODE_4:
        fingerprintController.operation.send(
            FingerPrintCmd.getCmdGetFingerFeature(FingerPrintCmd.BUFFER_ID_1));
        break;
      case KeyEvent.KEYCODE_5:
        fingerprintController.operation.send(
            FingerPrintCmd.getCmdGetFingerFeature(FingerPrintCmd.BUFFER_ID_2));
        break;
      case KeyEvent.KEYCODE_6:
        String str =
            "0301492D0000C0068002800200000000000000000000000200020002000200028002800280028002000000000000000000000000000000002B0FC91E6013279E2497C87E2A9A9EDE701FCFFE4423E7DE33B91A5E2E89C41F578DD03F0D95C9FF609BD0BF0E1D4A3F2D21455F55A1E6FF0E23C9BF292844FF4AABE99F2B31439F54BFD47F2307163C3E0901BD72134E150E2889DD378B05FB72170DFB0DACC97B0DB7473B4A8BD8D84E0E8EF8709A8EB8230B43D90E0DCA796B9A0F990DB048F318876CB611329D1014882C176D8D51170DB388B76089D2F5409D1CD33A9A9F906F06D68A160F492F451825AB000000000000000000000000000000000000000003014B290000C0068002800200000000000000000000000080008000800080008002800280028002000000000000000000000000000000002B0FC91E6013279E2497C87E2A9A9EDE701FCFFE33B91A5E2E89C41F410DC49F578DD03F0D95C9FF609BD0BF0E1D0A3F2D21455F55A1E6FF0E23C9BF422528DF292844FF4AABE99F2B31439F56BF947F2307163C0E0DCA7D4A8BD8DA4E0E8EFA3D8901BB378B05DB72170DFB18876CB87390CD72709A8EB814882C39230B43D972134E136B9A0F996D8D51176089D2F5148B41533E1CDCD3160F49316F06D6AE3D96E04B000000000000000000000000000000000000000000000000000000000000000000000000";
        byte[] fingerBuff = BytesUtil.hexString2Bytes(str);
        ThreadUtil.execute(() -> {
          int res1 = fingerprintController.operation.loadFinger(3, fingerBuff);
          runOnUi(() -> {
            if (res1 == 0) {
              ViewUtil.appendShow(String.format("加载指纹成功，fingerID：%s", 3), tvShow);
            } else {
              ViewUtil.appendShow(String.format("加载指纹失败，case:%s", res1), tvShow);
            }
          });
        });
        break;
      case KeyEvent.KEYCODE_9:
        btnSearch.setEnabled(false);
        btnNum.setEnabled(false);
        boolean clearSuccess = fingerprintController.clearFinger();
        LogUtils.d("清空指纹库 成功？:%s", clearSuccess);
        ViewUtil.appendShow(String.format("清空指纹库 成功？：%s", clearSuccess), tvShow);
        btnSearch.setEnabled(true);
        btnNum.setEnabled(true);
        break;
    }
    return super.onKeyDown(keyCode, event);
  }

  @Override
  protected void onDestroy() {
    fingerprintController.release();
    if (fingerPrintDbHelper != null) {
      fingerPrintDbHelper.close();
    }
    super.onDestroy();
  }
}
