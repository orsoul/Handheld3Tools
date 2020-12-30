package com.fanfull.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.fanfull.utils.SPUtils;
import java.util.ArrayList;
import java.util.List;
import org.orsoul.baselib.lock3.bean.BagIdParser;
import org.orsoul.baselib.lock3.bean.Lock3Bean;
import org.orsoul.baselib.util.BytesUtil;
import org.orsoul.baselib.util.ClockUtil;
import org.orsoul.baselib.util.SoundUtils;
import org.orsoul.baselib.util.ThreadUtil;
import org.orsoul.baselib.util.ViewUtil;

public class ZhuangXiangActivity extends BaseActivity {
  private final static String TAG = ZhuangXiangActivity.class.getSimpleName();

  private int mInitNumber = 0;
  private Button mBtnOK;
  private TextView mTvInitNumber;
  private TextView mTvInitRecoverNumber;
  private TextView mInCardInView;

  private final static int READ_RFID_SUCCESS = 2;
  private final static int READ_EPC_SUCCESS = 8;
  private final static int READ_EPC_FAILED = 9;

  private final static int NET_INIT_NO_CONNET = 13;
  private final static int NET_INIT_SUCCESS = 14;
  private final static int NET_INIT_FAILED = 15;

  private final static int HAD_FILL_XIANG = 16;

  private CheckBox cbNfc;
  private CheckBox cbEpc;

  private List<String> bagIdList = new ArrayList<>();
  private ReadBagIdTask readBagIdTask;

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

    mInCardInView = (TextView) findViewById(R.id.cardinf);

    mTvInitNumber = (TextView) findViewById(R.id.tv_init_num);
    mTvInitRecoverNumber = (TextView) findViewById(R.id.tv_sum_recover);
    mTvInitRecoverNumber.setOnClickListener(this);

    cbNfc = (CheckBox) findViewById(R.id.cb_v_setting_rfid);
    cbEpc = (CheckBox) findViewById(R.id.cb_v_setting_epc);
  }

  @Override
  protected void initData() {
    super.initData();
    //恢复上一次已经扫描的数据
    mInitNumber = SPUtils.getInt(this, MyContexts.KEY_LAST_ZX_NUMBER, 0);
    mTvInitNumber.setText(mInitNumber + "");

    readBagIdTask = new ReadBagIdTask();
    readBagIdTask.setReadNfc(cbNfc.isChecked());
    readBagIdTask.setReadEpc(cbEpc.isChecked());
  }

  @Override
  protected void initEvent() {
    OnCheckedChangeListener onCheckedChangeListener = (buttonView, isChecked) -> {
      if (!cbEpc.isChecked() && !cbNfc.isChecked()) {
        ToastUtils.showShort("至少读取一种芯片");
        buttonView.setChecked(true);
        return;
      }
      if (buttonView == cbNfc) {
        readBagIdTask.setReadNfc(isChecked);
      } else if (buttonView == cbEpc) {
        readBagIdTask.setReadEpc(isChecked);
      }
    };
    cbNfc.setOnCheckedChangeListener(onCheckedChangeListener);
    cbEpc.setOnCheckedChangeListener(onCheckedChangeListener);
  }

  @Override
  public void onClick(View v) {
    super.onClick(v);
    switch (v.getId()) {
      case R.id.btn_init_bag_ok:
        if (!readBagIdTask.isRunning()) {
          readBagIdTask.startRun();
          mBtnOK.setEnabled(false);
        }
        break;
      case R.id.tv_sum_recover:
        mInitNumber = 0;
        mTvInitNumber.setText(mInitNumber + "");
        SPUtils.putInt(ZhuangXiangActivity.this, MyContexts.KEY_LAST_ZX_NUMBER, mInitNumber);
        bagIdList.clear();
        return;
      default:
        break;
    }
  }

  @Override public void onBackPressed() {
    if (readBagIdTask.isRunning) {
      ToastUtils.showShort("正在扫描请稍后...");
    } else {
      finish();
    }
  }

  @Override
  protected void onDestroy() {
    SPUtils.putInt(ZhuangXiangActivity.this, MyContexts.KEY_LAST_INIT_NUMBER, mInitNumber);
    bagIdList.clear();
    readBagIdTask.stop();
    super.onDestroy();
  }

  private Handler mHandler = new Handler() {
    public void handleMessage(android.os.Message msg) {
      switch (msg.what) {
        case READ_RFID_SUCCESS:
          StaticString.bagid = String.valueOf(msg.obj);
          if (!"05".equals(StaticString.bagid.substring(0, 2))) {
            ToastUtils.showShort("该袋尚未初始化");
          } else {
            if (bagIdList.contains(StaticString.bagid)) {
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
                    bagIdList.add(StaticString.bagid);
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
          ToastUtils.showShort("该袋已经装箱");
          SoundUtils.playToneFailed();
          break;
        case READ_EPC_FAILED:
          mBtnOK.setText("重试");
          mBtnOK.setEnabled(true);
          SoundUtils.playToneFailed();
          break;
        case READ_EPC_SUCCESS:
        case NET_INIT_SUCCESS:
          mBtnOK.setEnabled(true);
          mInitNumber++;
          SPUtils.putInt(ZhuangXiangActivity.this, MyContexts.KEY_LAST_INIT_NUMBER, mInitNumber);
          mTvInitNumber.setText(mInitNumber + "");
          SoundUtils.playNumber(mInitNumber);
          break;
        default:
          break;
      }
    }

    ;
  };

  private class ReadBagIdTask implements Runnable {
    private boolean isReadEpc = true;
    private boolean isReadNfc;
    private int runTime = 4000;
    private boolean isRunning;

    public void setReadNfc(boolean readNfc) {
      isReadNfc = readNfc;
    }

    public void setReadEpc(boolean readEpc) {
      isReadEpc = readEpc;
    }

    public void setRunTime(int runTime) {
      this.runTime = runTime;
    }

    public boolean isRunning() {
      return isRunning;
    }

    public void stop() {
      isRunning = false;
    }

    public void startRun() {
      ThreadUtil.execute(this);
    }

    @Override public void run() {
      boolean isReadNfc = this.isReadNfc;
      boolean isReadEpc = this.isReadEpc;
      if (!isReadEpc && !isReadNfc) {
        onReadFailed();
        isRunning = false;
        return;
      }
      ClockUtil.runTime(true);
      isRunning = true;
      boolean readSuccess = false;
      while (isRunning && ClockUtil.runTime() < runTime) {
        byte[] epc = new byte[12];
        byte[] nfc = null;
        if (isReadEpc) {
          // 从epc获取 袋id
          readSuccess = UhfController.getInstance().readEpc(epc);
        }

        if (!readSuccess && isReadNfc) {
          // 从NFC获取 袋id
          if (nfc == null) {
            nfc = new byte[12];
          }
          readSuccess = RfidController.getInstance().readNfc(Lock3Bean.SA_BAG_ID, nfc, true);
        }

        if (readSuccess) {
          String bagId;
          boolean dataFromEpc = epc != null;
          if (dataFromEpc) {
            bagId = BytesUtil.bytes2HexString(epc);
          } else {
            bagId = BytesUtil.bytes2HexString(nfc);
          }
          onReadSuccess(bagId, dataFromEpc);
          isRunning = false;
          return;
        }
        SystemClock.sleep(50);
      } // end while()
      onReadFailed();
      isRunning = false;
    }

    protected void onReadSuccess(String bagId, boolean dataFromEpc) {
      LogUtils.d("dataFromEpc %s:%s", dataFromEpc, bagId);
      if (!BagIdParser.isBagId(bagId)) {
        SoundUtils.playToneFailed();
        ToastUtils.showShort("该袋未初始化");
        runOnUiThread(() -> mBtnOK.setEnabled(true));
        return;
      } else if (bagIdList.contains(bagId)) {
        SoundUtils.playToneFailed();
        ToastUtils.showShort("该袋已装箱");
        runOnUiThread(() -> mBtnOK.setEnabled(true));
        return;
      }
      bagIdList.add(bagId);
      mInitNumber++;
      SoundUtils.playNumber(mInitNumber);
      runOnUiThread(() -> {
        mBtnOK.setEnabled(true);
        mTvInitNumber.setText(mInitNumber + "");
        mInCardInView.append("袋ID:" + bagId + "\n");
      });

      //dialogUtil.showProgressDialog("正在通讯...");
      //SocketConnet.getInstance().communication(123);
    }

    protected void onReadFailed() {
      SoundUtils.playToneFailed();
      ToastUtils.showShort("读锁失败");
      runOnUiThread(() -> {
        mBtnOK.setEnabled(true);
      });
    }
  }
}
