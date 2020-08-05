package com.fanfull.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import com.apkfuns.logutils.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.fanfull.base.BaseActivity;
import com.fanfull.contexts.MyContexts;
import com.fanfull.contexts.StaticString;
import com.fanfull.initbag3.R;
import com.fanfull.libhard.rfid.RfidController;
import com.fanfull.libhard.uhf.UhfController;
import com.fanfull.socket.ReplyParser;
import com.fanfull.socket.SocketConnet;
import com.fanfull.utils.DialogUtil;
import com.fanfull.utils.SPUtils;
import java.util.HashSet;
import org.orsoul.baselib.util.SoundUtils;
import org.orsoul.baselib.util.ThreadUtil;
import org.orsoul.baselib.util.ViewUtil;

public class ZhuangXiangActivity extends BaseActivity {
  private final static String TAG = ZhuangXiangActivity.class.getSimpleName();

  private int mInitNumber = 0;
  private Button mBtnOK;
  private Button mBtnCancel;
  private TextView mTvInitNumber;
  private TextView mTvInitRecoverNumber;
  private TextView mInCardInView;

  private final static int READ_RFID_SUCCESS = 2;
  private final static int READ_RFID_FAILED = 3;
  private byte mUid[];

  private final static int READ_EPC = 7;
  private final static int READ_EPC_SUCCESS = 8;
  private final static int READ_EPC_FAILED = 9;

  private final static int NET_INIT_NO_CONNET = 13;
  private final static int NET_INIT_SUCCESS = 14;
  private final static int NET_INIT_FAILED = 15;

  private final static int HAD_FILL_XIANG = 16;

  private boolean haveTaskRunning = false;// 记录当前界面是否有子线程在运行
  private byte[] mTID;

  private ReadRFIDTask mReadRFIDTask;
  private ReadEPCTask mReadEPCTask; // 读 EPC 任务

  private CheckBox mRfidBox, mEpCheckBox;

  private HashSet<String> mHashSet = new HashSet<String>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void initView() {
    setContentView(R.layout.activity_init_zhuangxiang);
    mBtnOK = (Button) findViewById(R.id.btn_init_bag_ok);
    mBtnOK.setOnClickListener(this);

    ViewUtil.requestFocus(mBtnOK);
    mBtnCancel = (Button) findViewById(R.id.btn_init_bag_cancel);
    mBtnCancel.setOnClickListener(this);

    mInCardInView = (TextView) findViewById(R.id.cardinf);

    mTvInitNumber = (TextView) findViewById(R.id.tv_init_num);
    mTvInitRecoverNumber = (TextView) findViewById(R.id.tv_sum_recover);
    mTvInitRecoverNumber.setOnClickListener(this);

    mReadRFIDTask = new ReadRFIDTask();
    mReadEPCTask = new ReadEPCTask();

    mRfidBox = (CheckBox) findViewById(R.id.cb_v_setting_rfid);
    mRfidBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

      }
    });

    mEpCheckBox = (CheckBox) findViewById(R.id.cb_v_setting_epc);
    mRfidBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

      }
    });

    mBtnCancel.setOnLongClickListener(new View.OnLongClickListener() {

      @Override
      public boolean onLongClick(View v) {
        mInCardInView.setText("");
        return true;
      }
    });
  }

  ;

  @Override
  protected void initData() {
    super.initData();
    //恢复上一次已经扫描的数据
    mInitNumber = SPUtils.getInt(this, MyContexts.KEY_LAST_ZX_NUMBER, 0);
    mTvInitNumber.setText(mInitNumber + "");

    ThreadUtil.execute(new Runnable() {

      @Override
      public void run() {
        if (!SocketConnet.getInstance().isConnect()) {
          mHandler.sendEmptyMessage(NET_INIT_NO_CONNET);
        } else {
          SocketConnet.getInstance().communication(790);//start
        }
      }
    });
  }

  @Override
  protected void initEvent() {
    super.initEvent();
  }

  @Override
  public void onClick(View v) {
    super.onClick(v);
    switch (v.getId()) {
      case R.id.btn_init_bag_ok:
        if (!haveTaskRunning) {

          if (mEpCheckBox.isChecked()) {
            mBtnOK.setText("进行中..");
            mBtnOK.setEnabled(false);
            mBtnCancel.setText("取消");
            ThreadUtil.execute(mReadEPCTask);
            return;
          } else if (mRfidBox.isChecked()) {
            mBtnOK.setText("进行中..");
            mBtnOK.setEnabled(false);
            mBtnCancel.setText("取消");
            ThreadUtil.execute(mReadRFIDTask);
          } else {
            ToastUtils.showShort("请勾选袋码或者标签码");
          }
        }

        break;
      case R.id.btn_init_bag_cancel:
        if (!haveTaskRunning) {
          finish();
        } else {
          haveTaskRunning = false;
          mReadEPCTask.stop();
          mReadEPCTask.stop();
          ToastUtils.showShort("已经结束扫描，再次点击退出");
        }

        break;
      case R.id.tv_sum_recover:
        mInitNumber = 0;
        mTvInitNumber.setText(mInitNumber + "");
        SPUtils.putInt(ZhuangXiangActivity.this, MyContexts.KEY_LAST_ZX_NUMBER, mInitNumber);
        return;
      default:
        break;
    }
  }

  @Override
  protected void onDestroy() {

    super.onDestroy();
    mHashSet.clear();
    ThreadUtil.execute(new Runnable() {
      @Override
      public void run() {
        if (!SocketConnet.getInstance().isConnect()) {

        } else {
          SocketConnet.getInstance().communication(792);
        }
      }
    });
  }

  private Handler mHandler = new Handler() {
    public void handleMessage(android.os.Message msg) {
      switch (msg.what) {
        case READ_RFID_FAILED:
          mBtnOK.setText("重试");
          mBtnOK.setEnabled(true);
          haveTaskRunning = false;
          mBtnCancel.setText("返回");
          SoundUtils.playFailedSound();
          break;
        case READ_RFID_SUCCESS:
          haveTaskRunning = false;
          mBtnCancel.setText("返回");

          StaticString.bagid = String.valueOf(msg.obj);
          if (!"05".equals(StaticString.bagid.substring(0, 2))) {
            ToastUtils.showShort("该袋尚未初始化");
          } else {
            if (mHashSet.contains(StaticString.bagid)) {
              mHandler.sendEmptyMessage(HAD_FILL_XIANG);
              return;
            }
            mInCardInView.append("袋ID:" + StaticString.bagid + "\n");
            ThreadUtil.execute(new Runnable() {

              @Override
              public void run() {
                if (!SocketConnet.getInstance().isConnect()) {
                  mHandler.sendEmptyMessage(NET_INIT_NO_CONNET);
                } else {
                  LogUtils.d("id:" + StaticString.bagid);
                  SocketConnet.getInstance().communication(791);
                  if (ReplyParser.waitReply() && StaticString.information.startsWith("*oa ok")) {
                    mHashSet.add(StaticString.bagid);
                    mHandler.sendEmptyMessage(NET_INIT_SUCCESS);
                  } else {
                    mHandler.sendEmptyMessage(NET_INIT_FAILED);
                  }
                }
              }
            });
          }
          break;

        case HAD_FILL_XIANG:
          mBtnOK.setText("重试");
          mBtnOK.setEnabled(true);
          haveTaskRunning = false;
          mBtnCancel.setText("返回");
          ToastUtils.showShort("该袋已经装箱");
          SoundUtils.playFailedSound();
          break;
        case READ_EPC_FAILED:
          mBtnOK.setText("重试");
          mBtnOK.setEnabled(true);
          haveTaskRunning = false;
          mBtnCancel.setText("返回");
          SoundUtils.playFailedSound();
          break;
        case READ_EPC_SUCCESS:
        case NET_INIT_SUCCESS:
          mBtnOK.setEnabled(true);
          mInitNumber++;

          SPUtils.putInt(ZhuangXiangActivity.this, MyContexts.KEY_LAST_INIT_NUMBER, mInitNumber);
          mTvInitNumber.setText(mInitNumber + "");
          SoundUtils.playNumber(mInitNumber);
          mBtnOK.setText("继续");
          haveTaskRunning = false;
          mBtnCancel.setText("返回");
          break;
        case NET_INIT_FAILED:
          mBtnOK.setText("重试");
          mBtnOK.setEnabled(true);
          //                mToast.cancel();
          SoundUtils.playFailedSound();
          haveTaskRunning = false;
          mBtnCancel.setText("返回");
          new DialogUtil(ZhuangXiangActivity.this).
              showNegativeReplyDialog("服务器或者网络异常，重新扫描", MyContexts.TEXT_OK);
          break;
        case NET_INIT_NO_CONNET:
          haveTaskRunning = false;
          mBtnCancel.setText("返回");
          mBtnOK.setText("开始");
          mBtnOK.setEnabled(true);
          new DialogUtil(ZhuangXiangActivity.this).
              showNegativeReplyDialog("网络没有连接", MyContexts.TEXT_OK);
          break;
        default:
          break;
      }
    }

    ;
  };

  private class ReadRFIDTask implements Runnable {
    private boolean stoped;

    public void stop() {
      stoped = true;
    }

    @Override
    public void run() {
      LogUtils.tag(TAG).d("mReadRFIDTask run");
      haveTaskRunning = true;
      int count = 0;
      byte[] tmpID = null;
      while (null == (tmpID = RfidController.getInstance().findNfc())) {
        if (40 < ++count) {
          LogUtils.i("获取袋ID失败");
          break;
        }
        SystemClock.sleep(100);
      }

      if (null == tmpID) {
        LogUtils.i("没有寻到卡");
        mHandler.sendEmptyMessage(READ_RFID_FAILED);
        return;
      } else if (tmpID.length == 7) {
        mUid = tmpID;
        LogUtils.i("扫描袋锁nfc卡成功");
        if (tmpID != null) {
          mHandler.sendEmptyMessage(READ_RFID_SUCCESS);
        } else {
          mHandler.sendEmptyMessage(READ_RFID_FAILED);
        }
      } else {
        for (int i = 0; i < tmpID.length; i++) {
          mUid[i] = tmpID[i];
        }
        LogUtils.i("扫描袋锁m1卡成功");
        mHandler.sendEmptyMessage(READ_RFID_SUCCESS);
      }
      LogUtils.tag(TAG).d("mReadRFIDTask end");
    }
  }

  ;

  /**
   * 超高频读取EPC线程
   */
  class ReadEPCTask implements Runnable {
    private boolean stoped;

    private void stop() {
      stoped = true;
    }

    @Override
    public void run() {
      LogUtils.d("Read EPC task run");
      stoped = false;
      int count = 0;
      final int TIMES = 200;// 读取 EPC 的次数
      final int GAS = 20; // 读取 EPC 间隔,单位 毫秒

      haveTaskRunning = true;
      while (!stoped) {
        // TIMES * GAS 毫秒后停止 读取 EPC
        if (TIMES < ++count) {
          mHandler.sendEmptyMessage(READ_EPC_FAILED);
          break;
        }
        if (null == UhfController.getInstance().fastEpc(500)) {// 单次读取EPC
          SystemClock.sleep(GAS << 1);
        }
        SystemClock.sleep(GAS);
      } // end while()
    }// end run()
  }
}
