package com.fanfull.handheldtools.ui;

import android.os.Bundle;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.apkfuns.logutils.LogUtils;
import com.fanfull.handheldtools.R;
import com.fanfull.handheldtools.base.InitModuleActivity;
import com.fanfull.libhard.Lock3Operation;
import com.fanfull.libhard.nfc.IRfidListener;
import com.fanfull.libhard.nfc.RfidController;
import com.fanfull.libhard.uhf.IUhfListener;
import com.fanfull.libhard.uhf.UhfCmd;
import com.fanfull.libhard.uhf.UhfController;
import java.util.Arrays;
import java.util.Random;
import org.orsoul.baselib.util.ArrayUtils;
import org.orsoul.baselib.util.ClickUtil;
import org.orsoul.baselib.util.ThreadUtil;
import org.orsoul.baselib.util.ViewUtil;
import org.orsoul.baselib.util.lock.BagIdParser;
import org.orsoul.baselib.util.lock.Lock3Bean;
import org.orsoul.baselib.util.lock.Lock3Util;

public class InitBag3Activity extends InitModuleActivity {

  private TextView tvShow;
  private Button btnOk;

  private UhfController uhfController;
  private RfidController rfidController;
  private Lock3Operation lock3Operation;

  private InitBagTask initBagTask;
  private BagIdParser bagIdParser;
  private Lock3Bean initLock3Bean;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    bagIdParser = new BagIdParser();
    bagIdParser.setCityCode("532");
    bagIdParser.setMoneyType("1");
    bagIdParser.setBagType("03");
  }

  @Override protected void initModule() {
    uhfController = UhfController.getInstance();
    rfidController = RfidController.getInstance();

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
          ViewUtil.appendShow("超高频初始成功.\n正在初始化高频...\n", tvShow);
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

    rfidController.setListener(new IRfidListener() {
      @Override
      public void onOpen() {
        runOnUi(() -> {
          dismissLoadingView();
          //tvShow.setText("初始化成功");
          ViewUtil.appendShow("高频模块初始成功", tvShow);
          lock3Operation = Lock3Operation.getInstance();
          initBagTask = new InitBagTask();
          btnOk.setEnabled(true);
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

    showLoadingView("正在初始化模块...");
    uhfController.open();
  }

  @Override protected void onEnterPress() {
    super.onEnterPress();
  }

  @Override public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_init_bag3_ok:
        if (initBagTask.isStopped()) {
          ThreadUtil.executeInSingleThread(initBagTask);
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

    btnOk = findViewById(R.id.btn_init_bag3_ok);
    btnOk.setOnClickListener(this);
    btnOk.setEnabled(false);
  }

  @Override protected void onDestroy() {
    uhfController.release();
    rfidController.release();
    super.onDestroy();
  }

  class InitBagTask implements Runnable {

    public byte[] uid = new byte[7];
    public byte[] epc = new byte[12];
    public byte[] tid = new byte[6];

    private boolean readSuccess;
    private boolean stopped = true;

    public boolean isStopped() {
      return stopped;
    }

    public synchronized void stop() {
      this.stopped = true;
    }

    public boolean isReadSuccess() {
      return readSuccess;
    }

    @Override public void run() {
      stopped = false;
      /* 1、 读uid、epc、tid */
      readSuccess = false;
      int readRes = -1;
      ClickUtil.resetRunTime();
      while (!stopped && ClickUtil.runTime() < 8000) {
        if (0 == (readRes = lock3Operation.readUidEpcTid(uid, epc, tid))) {
          readSuccess = true;
          break;
        } else {
          SystemClock.sleep(50);
        }
      } // end while()

      if (!readSuccess) {
        onReadFailed(readRes);
        stopped = true;
        return;
      }

      /* 2、 生成袋id、密钥编号，写入nfc */
      initLock3Bean = new Lock3Bean();
      initLock3Bean.addInitBagSa();

      int keyNum = new Random().nextInt(10); // 加密编号
      int statusEncrypt = Lock3Util.getStatus(1, keyNum, uid, true);
      int statusPlain = Lock3Util.getStatus(statusEncrypt, keyNum, uid, false);
      LogUtils.d("Encrypt=Plain: %s = %s", statusEncrypt, statusPlain);
      byte[] sa10 = new byte[12];
      sa10[0] = (byte) statusEncrypt;
      byte[] sa14 = new byte[4];
      sa14[0] = (byte) (keyNum | 0xA0);
      sa10[3] = sa14[0]; //

      bagIdParser.setUid(uid);
      byte[] sa4 = bagIdParser.genBagIdBuff();
      initLock3Bean.getInfoUnit(Lock3Bean.SA_BAG_ID).buff = sa4;
      initLock3Bean.getInfoUnit(Lock3Bean.SA_STATUS).buff = sa10;
      initLock3Bean.getInfoUnit(Lock3Bean.SA_KEY_NUM).buff = sa14;

      boolean writeLockNfc = rfidController.writeLockNfc(initLock3Bean, false);
      if (!writeLockNfc) {
        onReadFailed(4);
        stopped = true;
        return;
      }

      /* 3、 袋id 写入超高频卡epc区 */
      boolean writeEpd = uhfController.write(UhfCmd.MB_EPC, 0x02, sa4, tid, UhfCmd.MB_TID, 0x03);
      if (!writeEpd) {
        onReadFailed(5);
        stopped = true;
        return;
      }

      Lock3Bean readLock3Bean = new Lock3Bean();
      readLock3Bean.addInitBagSa();
      boolean checkNfcWrite = rfidController.readLockNfc(initLock3Bean, false);
      if (!checkNfcWrite) {
        onReadFailed(6);
        stopped = true;
        return;
      }
      boolean dataEquael = readLock3Bean.dataEquals(initLock3Bean);
      LogUtils.i("dataEquael:%s", dataEquael);
      byte[] readEpc = uhfController.read(UhfCmd.MB_EPC, 0x02, 12, tid, UhfCmd.MB_TID, 0x03);
      boolean epcEquals = Arrays.equals(sa4, readEpc);
      if (!epcEquals) {
        onReadFailed(7);
        stopped = true;
        return;
      }

      onReadSuccess(uid, epc, tid);
      stopped = true;
    } // end run()

    protected void onReadSuccess(byte[] uid, byte[] epc, byte[] tid) {
      LogUtils.i("onSuccess:%s", bagIdParser.getBagId());
    }

    protected void onReadFailed(int readRes) {
      LogUtils.w("onReadFailed:%s", readRes);
    }
  }
}
