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
    uhfController = UhfController.getInstance();
    uhfController.setListener(new IUhfListener() {
      @Override public void onOpen(boolean openSuccess) {
        runOnUiThread(() -> {
          dismissLoadingView();
          if (!openSuccess) {
            ViewUtil.appendShow("超高频 初始化失败！！", tvShow);
          } else {
            ViewUtil.appendShow("超高频 初始化成功", tvShow);
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
          case UhfCmd.RECEIVE_TYPE_SET_FAST_ID:
            // 1:成功， 0：失败
            if (parseData[0] == 1) {
              info = "EPC、TID同读设为 开启";
              info = "EPC、TID同读设为 关闭";
            } else {
              info = "设置 EPC、TID同读 失败";
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
              info = "写成功";
            } else {
              info = String.format("写失败,cause:%X", parseData[0]);
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
            tvShow.setText("模块初始化失败！！！");
          } else {
            tvShow.setText("初始化成功\n"
                + "按键1 -> 生成特征码\n"
                + "按键2 -> 搜索指纹2\n"
                + "按键3 -> 添加指纹到指定位置\n"
                + "按键4 -> 删除指纹\n"
                + "按键5 -> 查看数据库指纹\n"
                + "按键6 -> 读Epc\n"
                + "按键7 -> 数据库指纹---导入--->指纹库\n"
                + "按键8 -> 清空指纹数据库\n"
                + "按键9 -> 清空指纹库\n");
            btnSearch.setEnabled(true);
            btnNum.setEnabled(true);

            showLoadingView("正在打开超高频读头...");
            uhfController.open();
          }
        });
      }

      @Override public void onReceiveData(byte[] data) {
        //ViewUtil.appendShow("info", tvShow);
        //LogUtils.v("onReceiveData:%s", ArrayUtils.bytes2HexString(data));
      }
    });
    showLoadingView("正在打开指纹模块...");
    fingerprintController.init(this);
    //fingerprintController.open();
    //FingerPrintSQLiteHelper.init(this);
    fingerPrintDbHelper = FingerPrintSQLiteHelper.getInstance();

    /* 指纹任务 */
    fingerPrintTask = new FingerPrintTask(new FingerPrintTask.FingerSearchListener() {
      int count;

      @Override public void onNoFinger() {
        runOnUiThread(() -> {
          count++;
          showLoadingView("感应器无指纹 " + count);
        });
      }

      @Override public void onAddSuccess(FingerBean fingerBean, boolean isSaveInDB) {
        count = 0;
        runOnUiThread(() -> {
          ViewUtil.appendShow(String.format("添加成功，FingerIndex：%s, %s",
              fingerBean.getFingerIndex(), isSaveInDB ? "在数据库中" : "不在数据库中"), tvShow);
          btnSearch.setEnabled(true);
          btnNum.setEnabled(true);
          dismissLoadingView();
        });
      }

      @Override public void onSearchSuccess(FingerBean fingerBean, boolean isSaveInDB) {
        count = 0;
        runOnUiThread(() -> {
          ViewUtil.appendShow(String.format("搜索成功，FingerIndex：%s, %s",
              fingerBean.getFingerIndex(), isSaveInDB ? "在数据库中" : "不在数据库中"), tvShow);
          if (isSaveInDB) {
            ViewUtil.appendShow(String.format("FingerId：%s, FingerVersion：%s",
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
          ViewUtil.appendShow("未匹配到指纹", tvShow);
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
            ViewUtil.appendShow(String.format("添加失败，case:%s", errorCode), tvShow);
          } else {
            ViewUtil.appendShow(String.format("搜索失败，case:%s", errorCode), tvShow);
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
          ViewUtil.appendShow(String.format("生成指纹特征码成功"), tvShow);
        } else {
          ViewUtil.appendShow(String.format("生成指纹特征码失败，cause：%s", res), tvShow);
        }
        break;
      case KeyEvent.KEYCODE_2:
        ThreadUtil.execute(() -> {
          int[] resBuff = new int[2];
          int res1 = fingerprintController.operation.searchFinger2(resBuff);
          runOnUiThread(() -> {
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
                int res1 = fingerprintController.operation.addFinger2(fingerId, fingerFeature);
                long[] dbId = new long[] { -1L };
                if (res1 == 0) {
                  FingerBean fingerBean = new FingerBean(fingerId, fingerFeature);
                  dbId[0] = fingerPrintDbHelper.saveOrUpdate(fingerBean);
                }
                runOnUiThread(() -> {
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
        btnSearch.setEnabled(false);
        btnNum.setEnabled(false);
        boolean deleteSuccess = fingerprintController.deleteFinger(fingerId);
        ViewUtil.appendShow(String.format("删除指纹 %s 成功？：%s", fingerId, deleteSuccess), tvShow);
        LogUtils.d("删除指纹 %s 成功？：%s", fingerId, deleteSuccess);
        btnSearch.setEnabled(true);
        btnNum.setEnabled(true);
        break;
      case KeyEvent.KEYCODE_5:
        List<FingerBean> fingerBeanList = fingerPrintDbHelper.queryAllFinger();
        int[] resBuff = new int[1];
        res = fingerprintController.getFingerNum(resBuff);
        if (res == 0) {
          ViewUtil.appendShow(String.format("数据库指纹数量：%s，指纹库：%s",
              fingerBeanList.size(), resBuff[0]), tvShow);
        } else {
          ViewUtil.appendShow(String.format("数据库指纹数量：%s，获取指纹库数量失败", fingerBeanList.size()), tvShow);
        }
        for (int i = 0; i < fingerBeanList.size(); i++) {
          ViewUtil.appendShow(String.format("====== %s ======\n%s",
              i, fingerBeanList.get(i).toInfo()), tvShow);
        }
        break;
      case KeyEvent.KEYCODE_6:
        byte[] fastEpc = uhfController.readEpcWithTid(800);
        ViewUtil.appendShow(String.format("readEpcWithTid：%s", BytesUtil.bytes2HexString(fastEpc)),
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
            ViewUtil.appendShow(String.format("从数据库加载数量：%s / %s", res1, fingerBeans.size()),
                tvShow);
            dismissLoadingView();
          });
        });
        break;
      case KeyEvent.KEYCODE_8:
        List<Integer> indexList = fingerPrintDbHelper.queryAllFingerIndex();
        LogUtils.d("indexList:%s", indexList);
        fingerPrintDbHelper.clearAll();
        ViewUtil.appendShow(String.format("清空指纹数据库,num: %s", indexList.size()), tvShow);
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
