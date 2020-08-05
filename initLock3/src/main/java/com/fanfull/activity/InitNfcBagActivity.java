package com.fanfull.activity;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.apkfuns.logutils.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.fanfull.base.BaseActivity;
import com.fanfull.contexts.MyContexts;
import com.fanfull.contexts.StaticString;
import com.fanfull.initbag3.R;
import com.fanfull.libhard.lock3.InitBagTask;
import com.fanfull.libhard.rfid.RfidController;
import com.fanfull.libhard.uhf.UhfController;
import com.fanfull.room.Bag3Entity;
import com.fanfull.socket.SocketConnet;
import com.fanfull.utils.DialogUtil;
import com.fanfull.utils.SPUtils;
import com.fanfull.view.CoverBagItemView;
import com.fanfull.view.SetIpFullScreenPopup;
import com.orsoul.view.IPEditText;
import com.orsoul.view.SpinerAdapter;
import com.orsoul.view.SpinerPopWindow;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.orsoul.baselib.util.ArrayUtils;
import org.orsoul.baselib.util.ClockUtil;
import org.orsoul.baselib.util.SoundUtils;
import org.orsoul.baselib.util.ThreadUtil;
import org.orsoul.baselib.util.ViewUtil;
import org.orsoul.baselib.util.lock.BagIdParser;
import org.orsoul.baselib.util.lock.EnumBagType;
import org.orsoul.baselib.util.lock.EnumCity;
import org.orsoul.baselib.util.lock.EnumMoneyType;

public class InitNfcBagActivity extends BaseActivity implements OnClickListener {

  public static final int MSG_CHECK_RES = 1242354;

  private class CheckBagIdTask implements Runnable {
    private BagIdParser bagIdParser;

    public CheckBagIdTask(BagIdParser bagIdParser) {
      this.bagIdParser = bagIdParser;
    }

    @Override
    public void run() {
      haveTaskRunning = true;
      //String bagID = mNfcBagOp.getBagID();
      Message msg = mHandler.obtainMessage();
      msg.what = MSG_CHECK_RES;

      byte[] bagIdBuff = new byte[12];
      boolean readNfc = rfidController.readNfc(0x04, bagIdBuff, true);
      if (!readNfc) {
        mHandler.sendMessage(msg);
        haveTaskRunning = false;
        return;
      }

      String bagID;
      bagID = ArrayUtils.bytes2HexString(bagIdBuff);
      LogUtils.tag(TAG).d("CheckBagIdTask bagID:" + bagID);
      BagIdParser idParser = BagIdParser.parseBagId(bagID);

      //            int res = 0;
      boolean[] res = null;
      if (idParser != null) {
        res = new boolean[4];
        res[0] = bagIdParser.getVersion().equals(idParser.getVersion());
        res[1] = bagIdParser.getCityCode().equals(idParser.getCityCode());
        res[2] = bagIdParser.getMoneyType().equals(idParser.getMoneyType());
        res[3] = bagIdParser.getBagType().equals(idParser.getBagType());
      }
      LogUtils.tag(TAG).d("CheckBagIdTask res:" + Arrays.toString(res));

      msg.obj = idParser;
      mHandler.sendMessage(msg);
      haveTaskRunning = false;
    }
  }

  private TextView mQuTView;
  private TextView mCanTView;

  private TextView mDaiTView;
  private SpinerAdapter mQuAdapter;
  private SpinerAdapter mCanAdapter;
  private SpinerAdapter mDaiAdapter;
  // 设置PopWindow
  private SpinerPopWindow mQuSpinerPopWindow;
  private SpinerPopWindow mCanSpinerPopWindow;
  private SpinerPopWindow mDaiSpinerPopWindow;

  private TextView tvBagId;
  private CoverBagItemView vShow1;
  private CoverBagItemView vShow2;
  private CoverBagItemView vShow3;
  private CoverBagItemView vShow4;
  private Button mBtnOK;
  private Button mBtnCancel;
  private Button mBtnUpload;
  private TextView mTvInitNumber;
  private TextView mTvInitRecoverNumber;

  private byte mUid[];
  // 任务进度
  private final int STEP_READ_EPC = 1;

  private final int STEP_INIT_EPC = 2;
  private final int STEP_READ_RFID = 3;
  private final int STEP_WRITE_RFID = 4;

  private final int STEP_FINISH = 5;
  private final static int GET_BAG_ID = 1;
  private final static int READ_RFID_SUCCESS = 2;

  private final static int READ_RFID_FAILED = 3;
  private final static int INIT_EPC = 4;
  private final static int INIT_EPC_SUCCESS = 5;

  private final static int INIT_EPC_FAILED = 6;
  private final static int READ_EPC = 7;
  private final static int READ_EPC_SUCCESS = 8;

  private final static int READ_EPC_FAILED = 9;
  private final static int WRITE_RFID = 10;

  private final static int WRITHE_RFID_SUCCESS = 11;
  private final static int WRITHE_RFID_FAILED = 12;
  private final static int NET_INIT_START = 13;

  private final static int NET_INIT_SUCCESS = 14;
  private final static int NET_INIT_FAILED = 15;
  private final static int UPLOAD_INIT_SUCCESS = 16;
  private final static int UPLOAD_INIT_FAILED = 17;

  private final static int SQL_INIT_SUCCESS = 18;
  private final static int SQL_INIT_FAILED = 19;

  private final static int SQL_NET_INIT_SAME = 20;
  protected static final int CONNECT_FAILED = 21;
  protected static final int CONNECT_SUCCESS = 22;
  protected static final int CONNECT_ING = 23;
  protected static final int CONNECT_FAILED_FIRST = 24;

  private final static int NET_RP_SUCCESS = 25;

  private final static int NET_RP_FAILED = 26;

  private final static int MSG_NET_SUCCESS = 27;
  private final static int MSG_NET_FAILED = 28;
  private final static int MSG_NET_INIT_SAME = 29;
  private final static int MSG_TIMEOUT = 30;

  private boolean haveTaskRunning;// 记录当前界面是否有子线程在运行
  private byte[] mTID;

  public byte[] tempEPC = null;// 保存EPC
  private byte[] mNewBagID = null;
  private int mStep = STEP_READ_EPC;

  private byte[] mIndexData = new byte[] {
      (byte) 0x04, (byte) 0x03,
      (byte) 0x01, (byte) 0x10, (byte) 0x01, (byte) 0x01, (byte) 0x30,
      (byte) 0x0f, (byte) 0x00, (byte) 0x40, (byte) 0x01, (byte) 0x00
  };
  private byte[] mInit12_16 = new byte[] {
      (byte) 0x00, (byte) 0x00,
      (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
      (byte) 0x00, (byte) 0xA1, (byte) 0x00, (byte) 0x00, (byte) 0x00,
      (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
      (byte) 0x00, (byte) 0x00, (byte) 0x00,
  };

  private int mOffCount = 0;

  private int mFailedCount = 0;

  private SocketConnet mSocketConn;

  private int mInitNumber = 0;

  private Button mZhuangBtn;

  Handler mHandler = new Handler(new MyHandlerCallback());

  private void connetFailue() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setIcon(R.drawable.dialog_title_alarm_48);
    builder.setTitle("提示");
    final LayoutInflater inflater = LayoutInflater
        .from(InitNfcBagActivity.this);
    View v = inflater.inflate(R.layout.dilaog_ip, null);
    final com.orsoul.view.IPEditText ipEditText = (IPEditText) v
        .findViewById(R.id.v_setting_ip_set_ip1);
    ipEditText.setIp(StaticString.IP);
    builder.setView(v);
    builder.setMessage("连接失败，请检查IP是否正确或者电脑接收程序是否打开？");
    builder.setPositiveButton("重连", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        haveTaskRunning = true;
        mBtnCancel.setText("离线模式");
        int connCount = 0;
        while (false == mSocketConn.connect(2000)) { // 连接失败
          if (15 < ++connCount) {
            mHandler.sendEmptyMessage(CONNECT_FAILED);
            return;
          }
          LogUtils.d("socket conntect faile : " + connCount);
        }
        ToastUtils.showShort("网络连接成功！");
        mHandler.sendEmptyMessage(CONNECT_SUCCESS);
        dialog.dismiss();
      }
    });
    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        mHandler.sendEmptyMessage(CONNECT_FAILED);
        dialog.dismiss();
      }
    });
    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

      @Override
      public void onCancel(DialogInterface dialog) {
        mHandler.sendEmptyMessage(CONNECT_FAILED);
      }
    });
    builder.show();
  }

  /**
   * 界面过程显示初始化，恢复到最开始状态
   */
  private void resetShowItem() {
    vShow1.setChecked(false);
    vShow2.setChecked(false);
    vShow3.setChecked(false);
    vShow4.setChecked(false);
  }

  @Override
  public void onBackPressed() {
    if (!initBagTask.isStopped()) {
      ToastUtils.showShort("正在初始袋锁，请稍等...");
      return;
    } else {
      super.onBackPressed();
    }
  }

  private boolean goCheckMode;
  private boolean isCheckMode;
  private int checkNumber;

  private void switchMode() {
    tvBagId.setText(null);
    if (!isCheckMode) {
      //tvBagId.setVisibility(View.VISIBLE);
      vShow1.setText(getString(R.string.show1_check));
      vShow2.setText(getString(R.string.show2_check));
      vShow3.setText(getString(R.string.show3_check));
      vShow4.setVisibility(View.INVISIBLE);
      if (!goCheckMode) {
        checkNumber = 0;
      }
      mTvInitNumber.setText(checkNumber + "");
      mTvInitRecoverNumber.setText("袋锁检测");
      mTvInitRecoverNumber.setClickable(false);
    } else {
      goCheckMode = false;
      //tvBagId.setVisibility(View.GONE);
      vShow1.setText(getString(R.string.show1_init));
      vShow2.setText(getString(R.string.show2_init));
      vShow3.setText(getString(R.string.show3_init));
      vShow4.setVisibility(View.VISIBLE);
      mTvInitNumber.setText(mInitNumber + "");
      mTvInitRecoverNumber.setText("重新计数");
      mTvInitRecoverNumber.setClickable(true);
    }
    resetShowItem();
    isCheckMode = !isCheckMode;
  }

  //BagIdParser desParser;

  private void startCheckBagId() {
    CheckBagIdTask checkBagIdTask = new CheckBagIdTask(initBagTask.getBagIdParser());
    resetShowItem();
    vShow1.setDoing(true);
    vShow2.setDoing(true);
    vShow3.setDoing(true);
    mBtnOK.setEnabled(false);
    //ThreadPoolFactory.getNormalPool().execute(checkBagIdTask);
    ThreadUtil.executeInSingleThread(checkBagIdTask);
  }

  @Override
  public void onClick(View v) {
    if (!initBagTask.isStopped()) {
      ToastUtils.showShort("正在初始袋锁，请稍等...");
      return;
    }

    switch (v.getId()) {
      case R.id.btn_init_bag_ok:
        if (isCheckMode) {
          startCheckBagId();
          return;
        }

        if (!isOfflineMode && !isConnected) {
          checkNet();
          return;
        }

        boolean startRun = initBagTask.startRun();
        if (startRun) {
          ClockUtil.resetRunTime();
          resetShowItem();
          vShow1.setDoing(true);
          mBtnOK.setEnabled(false);
        }
        break;
      case R.id.bt_dropdown_diqu:
        showQuSpinWindow();
        return;
      case R.id.bt_dropdown_cai:
        showCanSpinWindow();
        return;
      case R.id.bt_dropdown_dai:
        showDaiSpinWindow();
        return;
      case R.id.tv_sum_recover:// 恢复计数
        mInitNumber = 0;
        mTvInitNumber.setText(mInitNumber + "");
        return;
      case R.id.btn_init_bag_cancel:
        onBackPressed();
        return;
      case R.id.btn_init_bag_zhuangxiang:
        Intent intent = new Intent(InitNfcBagActivity.this,
            ZhuangXiangActivity.class);
        startActivity(intent);

        break;
    }
  }

  private RfidController rfidController;
  private UhfController uhfController;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_nfc_init);
    TAG = "myLog";
    mQuTView = (TextView) findViewById(R.id.tv_value_diqu);
    mCanTView = (TextView) findViewById(R.id.tv_value_can);
    mDaiTView = (TextView) findViewById(R.id.tv_value_dai);

    tvBagId = (TextView) findViewById(R.id.tv_bagId);

    vShow1 = (CoverBagItemView) findViewById(R.id.v_init_bag_init_epc_show);
    vShow2 = (CoverBagItemView) findViewById(R.id.v_init_bag_read_rfid_show);
    vShow3 = (CoverBagItemView) findViewById(R.id.v_init_bag_init_first_show);
    vShow4 = (CoverBagItemView) findViewById(R.id.v_init_bag_write_rfid_show);

    mBtnOK = (Button) findViewById(R.id.btn_init_bag_ok);
    mBtnOK.setOnClickListener(this);
    mBtnOK.setOnLongClickListener(new OnLongClickListener() {

      @Override
      public boolean onLongClick(View v) {
        Intent intent = new Intent(InitNfcBagActivity.this,
            SettingIPActivity.class);
        startActivity(intent);
        return true;
      }
    });

    mZhuangBtn = (Button) findViewById(R.id.btn_init_bag_zhuangxiang);
    mZhuangBtn.setOnClickListener(this);

    mBtnCancel = (Button) findViewById(R.id.btn_init_bag_cancel);
    mBtnCancel.setOnClickListener(this);

    mTvInitNumber = (TextView) findViewById(R.id.tv_init_num);
    mTvInitRecoverNumber = (TextView) findViewById(R.id.tv_sum_recover);
    mTvInitRecoverNumber.setOnClickListener(this);

    // 加载 声音文件, 供以后使用
    //SoundUtils.loadSounds(getApplicationContext());
    ViewUtil.requestFocus(mBtnOK);

    mTvInitNumber.setOnLongClickListener(v -> {
      if (!initBagTask.isStopped()) {
        ToastUtils.showShort("正在初始袋锁，请稍等...");
      } else {
        switchMode();
      }
      return true;
    });

    initModule();
  }

  private boolean isOfflineMode;
  private boolean isConnected;
  private MyInitBagTask initBagTask;

  private void initModule() {
    dialogUtil.showProgressDialog("正在初始化读卡模块...");
    // TODO: 2020/7/23 init
    //mBagOp = new BagOperation();
    //mNfcBagOp = mBagOp.getNfcBagOperation();
    rfidController = RfidController.getInstance();
    boolean open = rfidController.open();
    if (!open) {
      dialogUtil.dismissProgressDialog();
      dialogUtil.showDialogFinishActivity("高频模块初始化失败！");
      return;
    }
    uhfController = UhfController.getInstance();
    open = uhfController.open();
    if (!open) {
      dialogUtil.dismissProgressDialog();
      dialogUtil.showDialogFinishActivity("超高频模块初始化失败！");
      return;
    }

    dialogUtil.dismissProgressDialog();
    initBagTask = new MyInitBagTask();

    initBagIdSelect();
    mUid = new byte[7];
    checkNet();
    mBtnOK.setEnabled(true);
  }

  private void checkNet() {
    if (!NetworkUtils.isConnected()) {
      dialogUtil.showWifiNotConnect((dialog, which) -> {
        isOfflineMode = true;
        vShow4.setVisibility(View.INVISIBLE);
      });
      return;
    }

    dialogUtil.showProgressDialog("正在连接服务器...");
    ThreadUtil.executeInSingleThread(() -> {
      boolean connect = SocketConnet.getInstance().connect(2000);
      dialogUtil.dismissProgressDialog();
      if (!connect) {
        dialogUtil.showDialog2ButtonOnUiThread("连接失败，请检查ip地址是否正确或者服务器是否打开", "设置ip", "离线模式",
            new DialogInterface.OnClickListener() {
              @Override public void onClick(DialogInterface dialog, int which) {
                dialogUtil.showSetIpPopup(new SetIpFullScreenPopup.SetIpCallback() {
                  @Override public void onIpSet(String ip) {
                    checkNet();
                  }
                });
              }
            }, (dialog, which) -> {
              isOfflineMode = true;
              vShow4.setVisibility(View.INVISIBLE);
            });
        return;
      }
      isConnected = true;
      mSocketConn = SocketConnet.getInstance();
    });
  }

  private void initBagIdSelect() {
    // 初始化地区数据
    //initCityMap();

    // 恢复上一次已经扫描的数据
    mInitNumber = SPUtils.getInt(this, MyContexts.KEY_LAST_INIT_NUMBER, 0);
    mTvInitNumber.setText(mInitNumber + "");

    int lastSelectPos;
    // 1, 初始化地区
    List<String> cityNameList = EnumCity.getNames();
    //List<String> cityNameList = new ArrayList<>();
    //Map<String, String> cityMap = new HashMap<>();
    //for (EnumCity enumCity : EnumCity.values()) {
    //  cityNameList.add(enumCity.getName());
    //  cityMap.put(enumCity.getName(), enumCity.getCode());
    //}
    mQuAdapter = new SpinerAdapter(this, cityNameList);
    //mQuAdapter = new SpinerAdapter(this, mCityNameList);
    mQuSpinerPopWindow = new SpinerPopWindow(this);
    mQuSpinerPopWindow.setAdatper(mQuAdapter);
    mQuSpinerPopWindow.setItemListener(pos -> {
      String name = cityNameList.get(pos);
      mQuTView.setText(name);
      String type = EnumCity.getCodeByName(name);
      initBagTask.setCityCode(type);
      //bagIdParser.setCityCode(type);
      LogUtils.i("%s:%s", type, name);
      SPUtils.putInt(InitNfcBagActivity.this,
          MyContexts.KEY_LAST_DIQU, pos);
    });
    lastSelectPos = SPUtils.getInt(this, MyContexts.KEY_LAST_DIQU, 0);
    mQuSpinerPopWindow.setSelection(lastSelectPos);
    //mQuAdapter.refreshData(cityNameList, lastSelectPos);

    // 2, 初始化残损
    List<String> moneyTypeList = new ArrayList<>();
    moneyTypeList.add(EnumMoneyType.WZ.getName());
    moneyTypeList.add(EnumMoneyType.CS.getName());
    //cityMap.put(EnumMoneyType.WZ.getName(), EnumMoneyType.WZ.getType());
    //cityMap.put(EnumMoneyType.CS.getName(), EnumMoneyType.CS.getType());
    mCanAdapter = new SpinerAdapter(this, moneyTypeList);
    //mCanAdapter = new SpinerAdapter(this, mCanListType);
    mCanSpinerPopWindow = new SpinerPopWindow(this);
    mCanSpinerPopWindow.setAdatper(mCanAdapter);
    mCanSpinerPopWindow.setItemListener(pos -> {
      String name = moneyTypeList.get(pos);
      mCanTView.setText(name);
      String type = EnumMoneyType.getTypeByName(name);
      initBagTask.setMoneyType(type);
      //bagIdParser.setMoneyType(type);
      LogUtils.i("%s:%s", type, name);
      SPUtils.putInt(InitNfcBagActivity.this,
          MyContexts.KEY_LAST_CAN, pos);
    });
    lastSelectPos = SPUtils.getInt(this, MyContexts.KEY_LAST_CAN, 0);
    mCanSpinerPopWindow.setSelection(lastSelectPos);
    //mCanAdapter.refreshData(moneyTypeList, lastSelectPos);

    // 3, 初始化袋型号PopWindow
    List<String> bagTypeList = EnumBagType.getNames();
    mDaiSpinerPopWindow = new SpinerPopWindow(this);
    //mDaiAdapter = new SpinerAdapter(this, bagTypeList);
    //mDaiSpinerPopWindow.setAdatper(mDaiAdapter);
    mDaiSpinerPopWindow.setAdatper(new SpinerAdapter(this, bagTypeList));
    mDaiSpinerPopWindow.setItemListener(pos -> {
      String name = bagTypeList.get(pos);
      mDaiTView.setText(name);
      String type = EnumBagType.getTypeByName(name);
      initBagTask.setBagType(type);
      //bagIdParser.setBagType(type);
      LogUtils.i("%s:%s", type, name);
      SPUtils.putInt(InitNfcBagActivity.this,
          MyContexts.KEY_LAST_BAG_TYPE, pos);
    });
    lastSelectPos = SPUtils.getInt(this, MyContexts.KEY_LAST_BAG_TYPE, 0);
    mDaiSpinerPopWindow.setSelection(lastSelectPos);
    //mDaiAdapter.refreshData(bagTypeList, lastSelectPos);
  }

  private void addInitCount() {
    mInitNumber++;
    mTvInitNumber.setText(String.format("%d", mInitNumber));
    SoundUtils.playNumber(mInitNumber);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    return onOptionsItemSelected(id);
  }

  private boolean onOptionsItemSelected(int itemId) {
    switch (itemId) {
      case R.id.menu_old_bag:
        startActivity(new Intent(this, CheckOldBagActivity.class));
        break;
      case R.id.menu_setting:
        break;
      case R.id.menu_setting_ip:
        Intent intent = new Intent(this, SettingIPActivity.class);
        startActivityForResult(intent, 1025);
        break;
      case R.id.menu_about:
        startActivity(new Intent(this, AboutActivity.class));
        break;
      default:
        return false;
    }
    return true;
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    switch (keyCode) {
      case KeyEvent.KEYCODE_7:
        onOptionsItemSelected(R.id.menu_setting_ip);
        break;
      case KeyEvent.KEYCODE_8:
        onOptionsItemSelected(R.id.menu_old_bag);
        break;
      case KeyEvent.KEYCODE_9:
        onOptionsItemSelected(R.id.menu_setting);
        break;
      case KeyEvent.KEYCODE_0:
        onOptionsItemSelected(R.id.menu_about);
        break;
      case KeyEvent.KEYCODE_SHIFT_LEFT:
      case KeyEvent.KEYCODE_F2:
        openOptionsMenu();
        break;
      default:
        return super.onKeyDown(keyCode, event);
    }
    return true;
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    LogUtils.i("requestCode:%s, resultCode:%s, data:%s", requestCode, resultCode, data);
    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override protected void onNewIntent(Intent intent) {
    LogUtils.i(intent);
    super.onNewIntent(intent);
  }

  @Override protected void onDestroy() {
    SPUtils.putInt(InitNfcBagActivity.this,
        MyContexts.KEY_LAST_INIT_NUMBER, mInitNumber);

    initBagTask.stop();

    if (rfidController != null) {
      rfidController.release();
    }
    if (uhfController != null) {
      uhfController.release();
    }

    if (mSocketConn != null) {
      mSocketConn.close();
      mSocketConn = null;
    }

    super.onDestroy();
    android.os.Process.killProcess(android.os.Process.myPid());
    System.exit(0);
  }

  /**
   * 显示残损下拉框
   */
  private void showCanSpinWindow() {
    mCanSpinerPopWindow.setWidth(mCanTView.getWidth());
    mCanSpinerPopWindow.showAsDropDown(mCanTView);
  }

  /**
   * 显示袋型号下拉框
   */
  private void showDaiSpinWindow() {
    mDaiSpinerPopWindow.setWidth(mDaiTView.getWidth());
    mDaiSpinerPopWindow.showAsDropDown(mDaiTView);
  }

  /**
   * 显示地区下拉框
   */
  private void showQuSpinWindow() {
    mQuSpinerPopWindow.setWidth(mQuTView.getWidth());
    mQuSpinerPopWindow.showAsDropDown(mQuTView);
  }

  private class MyInitBagTask extends InitBagTask {

    private Bag3Entity bag3Entity = new Bag3Entity();

    public Bag3Entity getBag3Entity() {
      return bag3Entity;
    }

    @Override protected void onSuccess(BagIdParser bagIdParser) {
      super.onSuccess(bagIdParser);
      ToastUtils.showShort("用时：%.2f秒", ClockUtil.runTime() / 1000.0);

      bag3Entity.insert(bagIdParser.getBagId(), ArrayUtils.bytes2HexString(tid));

      runOnUiThread(() -> {
        tvBagId.setText(bagIdParser.getFormatBagId());
        if (isOfflineMode) {
          /* 离线模式 */
          addInitCount();
          mBtnOK.setEnabled(true);
          return;
        }

        boolean send = mSocketConn.printBagId(bagIdParser.getBagId(), new MyReceiveListener());
        if (!send) {
          Message msg = mHandler.obtainMessage();
          msg.what = MSG_NET_FAILED;
          msg.obj = "刻码指令发送失败";
          mHandler.sendMessage(msg);
          return;
        }
        dialogUtil.showProgressDialog("正在刻码通讯...");
      });
    }

    @Override protected void onProgress(int progress) {
      super.onProgress(progress);
      runOnUiThread(() -> {
        switch (progress) {
          case RES_READ_TID:
            LogUtils.d("读锁成功");
            vShow1.setChecked(true);
            vShow2.setDoing(true);
            break;
          case RES_WRITE_NFC:
            LogUtils.d("写NFC成功");
            break;
          case RES_WRITE_EPC:
            LogUtils.d("写EPC成功");
            vShow2.setChecked(true);
            vShow3.setDoing(true);
            break;
          case RES_READ_NFC:
            LogUtils.d("读NFC成功");
            break;
          case RES_READ_EPC:
            LogUtils.d("读EPC成功");
            vShow3.setChecked(true);
            //vShow4.setDoing(true);
            break;
        }
      });
    }

    @Override protected void onFailed(int readRes, String info) {
      super.onFailed(readRes, info);
      SoundUtils.playFailedSound();
      runOnUiThread(() -> {
        mBtnOK.setEnabled(true);
        switch (readRes) {
          case RES_FIND_NFC:
          case RES_READ_TID:
            vShow1.setDoing(false, info);
            break;
          case RES_GEN_DATA:
          case RES_WRITE_NFC:
          case RES_WRITE_EPC:
            vShow2.setDoing(false, info);
            break;
          case RES_READ_NFC:
          case RES_NFC_DATA_NOT_EQUALS:
          case RES_READ_EPC:
          case RES_EPC_DATA_NOT_EQUALS:
            vShow3.setDoing(false, info);
            break;
        }
      });
    }

    public boolean startRun() {
      if (!isStopped()) {
        return false;
      }

      ThreadUtil.execute(this);
      return true;
    }
  }

  private class MyHandlerCallback implements Handler.Callback {

    @Override public boolean handleMessage(Message msg) {
      switch (msg.what) {
        case MSG_CHECK_RES:
          mBtnOK.setEnabled(true);
          if (!(msg.obj instanceof BagIdParser)) {
            SoundUtils.playFailedSound();
            vShow1.setDoing(false, "未读到袋锁");
            vShow2.setDoing(false, "未读到袋锁");
            vShow3.setDoing(false, "未读到袋锁");
            ToastUtils.showShort("未读到袋锁");
            return true;
          }

          BagIdParser idParser = (BagIdParser) msg.obj;
          tvBagId.setText(idParser.getFormatBagId());
          boolean[] checkRes = new boolean[4];
          if (idParser != null) {
            BagIdParser desParser = initBagTask.getBagIdParser();
            checkRes[0] = desParser.getVersion().equals(idParser.getVersion());
            checkRes[1] = desParser.getCityCode().equals(idParser.getCityCode());
            checkRes[2] = desParser.getMoneyType().equals(idParser.getMoneyType());
            checkRes[3] = desParser.getBagType().equals(idParser.getBagType());
          }

          if (checkRes[1]) {
            vShow1.setChecked(true);
          } else {
            vShow1.setDoing(false, "地区不匹配");
          }
          if (checkRes[2]) {
            vShow2.setChecked(true);
          } else {
            vShow2.setDoing(false, "币种不匹配");
          }
          if (checkRes[3]) {
            vShow3.setChecked(true);
          } else {
            vShow3.setDoing(false, "券别不匹配");
          }
          if (!checkRes[0]) {
            SoundUtils.playFailedSound();
            ToastUtils.showShort("该袋未初始化");
            return true;
          }
          if (!checkRes[3]) {
            SoundUtils.playFailedSound();
          } else {
            checkNumber++;
            mTvInitNumber.setText(String.valueOf(checkNumber));
            SoundUtils.playInitSuccessSound();
            SoundUtils.playNumber(checkNumber);
          }
          return true;
        case CONNECT_FAILED_FIRST:
          connetFailue();
          break;
        case CONNECT_FAILED:
        case CONNECT_SUCCESS:
          /** 根据网络的连接状态，动态的控制底部控件的显示 */
          if (SocketConnet.getInstance().isConnect()) {
            SPUtils.putString(InitNfcBagActivity.this,
                MyContexts.Key_IP1, StaticString.IP);
            SPUtils.putInt(InitNfcBagActivity.this,
                MyContexts.KEY_PORT1, StaticString.PORT_SECOND);
            haveTaskRunning = false;
            mBtnCancel.setText("退出");
            mBtnOK.setEnabled(true);
            // mBtnUpload.setVisibility(View.VISIBLE);
          } else {
            AlertDialog.Builder builder = new Builder(
                InitNfcBagActivity.this);
            builder.setIcon(R.drawable.dialog_title_alarm_48);
            builder.setTitle(MyContexts.TEXT_DIALOG_TITLE);
            builder.setMessage("当前网络不可用，是否进行离线操作？");
            builder.setPositiveButton("是",
                new DialogInterface.OnClickListener() {

                  @Override
                  public void onClick(DialogInterface dialog,
                      int which) {
                    //
                    mBtnOK.setEnabled(true);
                    haveTaskRunning = false;
                    mBtnCancel.setText("退出");
                  }
                });
            builder.setNegativeButton("否",
                new DialogInterface.OnClickListener() {

                  @Override
                  public void onClick(DialogInterface dialog,
                      int which) {
                    //
                    finish();
                  }
                });
            builder.show();
          }
          break;

        //com.apkfuns.logutils.LogUtils.tag(TAG).d(
        /** 开始处理标签信息 */
        case READ_EPC:
          LogUtils.tag(TAG).d("----read epc----");
          vShow1.setDoing(true);
          haveTaskRunning = true;
          mBtnCancel.setText("取消");
          mBtnOK.setText("读标签");
          break;
        case READ_EPC_SUCCESS:
          // 更新控件
          vShow1.setChecked(true);
          haveTaskRunning = false;
          mBtnCancel.setText("退出");
          mBtnOK.setText("读袋码");// 将袋ID内容处理后写入EPC区
          // 进入 下一个 阶段:更新标签
          mStep = STEP_READ_RFID;
          mHandler.sendEmptyMessage(GET_BAG_ID);
          break;
        case READ_EPC_FAILED:
          // 更新控件
          vShow1.setDoing(false);
          SoundUtils.playFailedSound();
          haveTaskRunning = false;
          mBtnCancel.setText("退出");
          break;

        /** 开始处理读锁片信息 */
        case GET_BAG_ID: // 读袋ID
          vShow2.setDoing(true);
          haveTaskRunning = true;
          mBtnCancel.setText("取消");
          break;
        case READ_RFID_SUCCESS:
          vShow2.setChecked(true);
          mBtnOK.setText("写标签");
          haveTaskRunning = false;
          mBtnCancel.setText("退出");
          mStep = STEP_INIT_EPC;
          mHandler.sendEmptyMessage(INIT_EPC);
          break;
        case READ_RFID_FAILED:// 23写入信息失败
          haveTaskRunning = false;
          mBtnCancel.setText("退出");
          if (mStep != STEP_READ_RFID) {
            return true;
          }
          SoundUtils.playFailedSound();
          vShow2.setDoing(false);
          break;

        case INIT_EPC:// 初始化 EPC
          LogUtils.d("INIT_EPC");
          vShow3.setDoing(true);
          haveTaskRunning = true;
          mBtnCancel.setText("取消");
          break;
        case INIT_EPC_SUCCESS: // 初始化 EPC 成功
          LogUtils.d("INIT_EPC_SUCCESS");
          // 标记 无子线程运行
          haveTaskRunning = false;
          mBtnCancel.setText("退出");
          // 更新控件
          vShow3.setChecked(true);
          mBtnOK.setText("写袋锁");

          // 进入 初始化第二个 EPC 阶段
          mStep = STEP_WRITE_RFID;
          mHandler.sendEmptyMessage(WRITE_RFID);
          break;
        case INIT_EPC_FAILED: // 初始化 EPC 失败
          // 标记 无子线程运行
          haveTaskRunning = false;
          mBtnCancel.setText("退出");
          SoundUtils.playFailedSound();
          vShow3.setDoing(false);
          break;

        case WRITE_RFID:
          vShow4.setDoing(true);
          haveTaskRunning = true;
          mBtnCancel.setText("取消");
          break;
        case WRITHE_RFID_SUCCESS:
          vShow4.setChecked(true);
          SystemClock.sleep(100);
          if (goCheckMode) {
            ToastUtils.showShort("重新初始化成功");
            switchMode();
          } else {
            mHandler.sendEmptyMessage(NET_INIT_START);
          }
          break;
        case WRITHE_RFID_FAILED:
          vShow4.setDoing(false);
          SoundUtils.playFailedSound();
          haveTaskRunning = false;
          mBtnCancel.setText("退出");
          break;

        case NET_INIT_START:
          break;
        case NET_INIT_SUCCESS:
          // mToast.cancel();
          mInitNumber++;
          mTvInitNumber.setText(mInitNumber + "");
          // SoundUtils.playInitSuccessSound();
          SoundUtils.playNumber(mInitNumber);
          mBtnOK.setText("继续");
          mBtnCancel.setText("退出");
          mStep = STEP_READ_EPC;// 重新开始
          haveTaskRunning = false;
          mBtnCancel.setText("退出");
          break;
        case NET_INIT_FAILED:
          // mToast.cancel();
          SoundUtils.playFailedSound();
          haveTaskRunning = false;
          mBtnCancel.setText("退出");
          mStep = STEP_READ_EPC;
          mBtnOK.setText("重新开始");
          dialogUtil.showNegativeReplyDialog("服务器或者网络异常，重新扫描",
              MyContexts.TEXT_OK);
          break;

        case NET_RP_FAILED:
          SoundUtils.playFailedSound();
          haveTaskRunning = false;
          mBtnCancel.setText("退出");
          mStep = STEP_READ_EPC;
          mBtnOK.setText("重新开始");
          new DialogUtil(InitNfcBagActivity.this)
              .showNegativeReplyDialog("该功能需要先连接网络，重新扫描",
                  MyContexts.TEXT_OK);
          break;

        case UPLOAD_INIT_SUCCESS:
          SoundUtils.playInitSuccessSound();
          mTvInitNumber.setText(mOffCount + "");
          mOffCount++;
          break;
        case UPLOAD_INIT_FAILED:
          SoundUtils.playFailedSound();
          mOffCount++;
          mFailedCount++;
          ToastUtils.showShort("失败" + mFailedCount + "个");
          break;
        case SQL_INIT_SUCCESS:
          mHandler.sendEmptyMessage(NET_INIT_SUCCESS);
          break;
        case SQL_INIT_FAILED:
          SoundUtils.playFailedSound();
          haveTaskRunning = false;
          mBtnCancel.setText("退出");

          new DialogUtil(InitNfcBagActivity.this)
              .showNegativeReplyDialog("数据异常，重新扫描",
                  MyContexts.TEXT_OK);

          mStep = STEP_READ_EPC;// 重新开始
          mBtnOK.setText("重新开始");
          break;
        case SQL_NET_INIT_SAME:
          SoundUtils.playFailedSound();
          haveTaskRunning = false;
          mBtnCancel.setText("退出");
          // DialogUtil dialogUtil = new
          // DialogUtil(InitNfcBagActivity.this);
          // dialogUtil.showNegativeReplyDialog("重复初始化基金袋,是否重新打印？",MyContexts.TEXT_OK);
          mStep = STEP_READ_EPC;// 重新开始
          mBtnOK.setText("重新开始");
          break;
        case MSG_NET_SUCCESS:
          dialogUtil.dismissProgressDialog();
          addInitCount();
          vShow4.setChecked(true);
          mBtnOK.setEnabled(true);
          break;
        case MSG_NET_FAILED:
          dialogUtil.dismissProgressDialog();
          ToastUtils.showShort(String.valueOf(msg.obj));
          vShow4.setDoing(false, String.valueOf(msg.obj));
          mBtnOK.setEnabled(true);
          SoundUtils.playFailedSound();
          break;
        case MSG_NET_INIT_SAME:
          dialogUtil.dismissProgressDialog();
          dialogUtil.showDialog2Button("重复初始化基金袋!!是否需要打印？", "打印", "取消",
              (dialog, which) -> {
                boolean send = mSocketConn.printBagIdRep(initBagTask.getBagIdParser().getBagId(),
                    new MyReceiveListener());
                if (!send) {
                  Message msg1 = mHandler.obtainMessage();
                  msg1.what = MSG_NET_FAILED;
                  msg1.obj = "重复刻码指令发送失败";
                  mHandler.sendMessage(msg1);
                  return;
                } else {
                  dialogUtil.showProgressDialog("重复刻码通讯...");
                }
              }, (dialog, which) -> {
                vShow4.setDoing(false);
                mBtnOK.setEnabled(true);
              });
          break;
        default:
          break;
      }
      return false;
    }
  }

  private class MyReceiveListener implements SocketConnet.ReceiveListener {
    @Override public void onReceive(String recString) {
      String[] split = recString.split(" ");
      if (split == null || split.length < 3) {
        Message msg = mHandler.obtainMessage();
        msg.what = MSG_NET_FAILED;
        msg.obj = "未知指令:" + recString;
        mHandler.sendMessage(msg);
        return;
      }

      switch (split[0]) {
        case "*ib":
          if ("Ok".equals(split[1])) {
            Bag3Entity bag3Entity = initBagTask.getBag3Entity();
            bag3Entity.update(Bag3Entity.STATUS_UPLOAD);
            mHandler.sendEmptyMessage(MSG_NET_SUCCESS);
          } else if ("01".equals(split[1])) {
            mHandler.sendEmptyMessage(MSG_NET_INIT_SAME);
          } else {
            Message msg = mHandler.obtainMessage();
            msg.what = MSG_NET_FAILED;
            msg.obj = "刻码指令回复异常";
            mHandler.sendMessage(msg);
            return;
          }
          break;
        case "*rp":
          Bag3Entity bag3Entity = initBagTask.getBag3Entity();
          bag3Entity.update(Bag3Entity.STATUS_UPLOAD_AGAIN);
          mHandler.sendEmptyMessage(MSG_NET_SUCCESS);
          //if (BagIdParser.isBagId(split[1])) {
          //  mHandler.sendEmptyMessage(MSG_NET_SUCCESS);
          //} else {
          //  Message msg = mHandler.obtainMessage();
          //  msg.what = MSG_NET_FAILED;
          //  msg.obj = "重复刻码指令回复异常";
          //  mHandler.sendMessage(msg);
          //}
          break;
        default:
          Message msg = mHandler.obtainMessage();
          msg.what = MSG_NET_FAILED;
          msg.obj = "未知指令:" + recString;
          mHandler.sendMessage(msg);
      }
    }

    @Override public void onReceiveTimeout() {
      //mHandler.sendEmptyMessage(MSG_TIMEOUT);
      Message msg = mHandler.obtainMessage();
      msg.what = MSG_NET_FAILED;
      msg.obj = getString(R.string.text_receive_timeout);
      mHandler.sendMessage(msg);
    }
  }
}
