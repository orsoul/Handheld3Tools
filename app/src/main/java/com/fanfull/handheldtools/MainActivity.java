package com.fanfull.handheldtools;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apkfuns.logutils.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.entity.node.BaseNode;
import com.fanfull.handheldtools.main.FuncNode;
import com.fanfull.handheldtools.main.NodeAdapter;
import com.fanfull.handheldtools.main.RootNode;
import com.fanfull.handheldtools.preference.MyPreference;
import com.fanfull.handheldtools.ui.AboutActivity;
import com.fanfull.handheldtools.ui.BagCheckActivity;
import com.fanfull.handheldtools.ui.BagSearchActivity;
import com.fanfull.handheldtools.ui.BarcodeActivity;
import com.fanfull.handheldtools.ui.CoverBagActivity;
import com.fanfull.handheldtools.ui.FingerActivity;
import com.fanfull.handheldtools.ui.InitBag3Activity;
import com.fanfull.handheldtools.ui.NettyActivity;
import com.fanfull.handheldtools.ui.NfcActivity;
import com.fanfull.handheldtools.ui.OldBagActivity;
import com.fanfull.handheldtools.ui.SocketActivity;
import com.fanfull.handheldtools.ui.SoundActivity;
import com.fanfull.handheldtools.ui.UhfActivity;
import com.fanfull.handheldtools.ui.UhfLotScanActivity;
import com.fanfull.handheldtools.ui.ZcLockActivity;
import com.fanfull.handheldtools.ui.base.BaseActivity;
import com.fanfull.handheldtools.ui.view.SetIpPortHelper;
import com.fanfull.libhard.rfid.RfidController;
import com.fanfull.libhard.uhf.UhfController;
import com.google.gson.Gson;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.InputConfirmPopupView;

import org.orsoul.baselib.util.AppUtil;
import org.orsoul.baselib.util.DeviceInfoUtils;
import org.orsoul.baselib.util.NetworkChangeReceiver;
import org.orsoul.baselib.util.SoundHelper;
import org.orsoul.baselib.view.MyInputPopupView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //setContentView(R.layout.activity_main);
    //findViewById(R.id.btn_uhf).setOnClickListener(this);
    //findViewById(R.id.btn_nfc).setOnClickListener(this);
    //findViewById(R.id.btn_init_bag3).setOnClickListener(this);
    //findViewById(R.id.btn_check_bag).setOnClickListener(this);
    //findViewById(R.id.btn_main_socket).setOnClickListener(this);
    //findViewById(R.id.btn_barcode).setOnClickListener(this);
    //findViewById(R.id.btn_finger).setOnClickListener(this);
    //findViewById(R.id.btn_old_bag).setOnClickListener(this);
    //findViewById(R.id.btn_sound).setOnClickListener(this);
    //findViewById(R.id.btn_main_cover_bag).setOnClickListener(this);
    //findViewById(R.id.btn_main_bag_search).setOnClickListener(this);
    //findViewById(R.id.btn_main_apdu).setOnClickListener(this);
    //findViewById(R.id.btn_main_lot_scan).setOnClickListener(this);
    //findViewById(R.id.btn_main_netty).setOnClickListener(this);

    initView();
    initData();

    //SoundUtils.loadSounds(MyApplication.getInstance());
    SoundHelper.loadSounds(MyApplication.getInstance());
    //SoundHelper.getInstance().loadTone(getApplicationContext());
    //SoundHelper.getInstance().loadNum(getApplicationContext());

    ConnectivityManager connMgr =
        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo wifiNetInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    NetworkInfo.State state = wifiNetInfo.getState();
    LogUtils.d("NetworkInfo.State:%s", state);

    NetworkChangeReceiver.register(this, new NetworkChangeReceiver.NetChangeObserver() {
      @Override public void onNetChange(ConnectivityManager connMgr) {
        NetworkChangeReceiver.NetChangeObserver.super.onNetChange(connMgr);
      }

      @Override public void onDisconnect() {

      }
    });
  }

  RecyclerView recycler;
  NodeAdapter funcAdapter;
  List<BaseNode> rootNodeList;
  List<BaseNode> allFuncList;
  //List<Integer> useList;
  RootNode hisRootNode;

  @Override protected void initView() {
    setContentView(R.layout.activity_main);
    recycler = findViewById(R.id.layout_main_recycler);
    recycler.setLayoutManager(new GridLayoutManager(this, 3));
    funcAdapter = new NodeAdapter();
    recycler.setAdapter(funcAdapter);
  }

  private List<BaseNode> genFuncData() {
    rootNodeList = new ArrayList<>();
    allFuncList = new ArrayList<>();

    List<BaseNode> moduleList = FuncNode.genModuleList();
    RootNode module = new RootNode(moduleList, "模块");
    allFuncList.addAll(moduleList);

    List<BaseNode> businessList = FuncNode.genBusinessList();
    RootNode business = new RootNode(businessList, "业务");
    allFuncList.addAll(businessList);

    List<BaseNode> otherList = FuncNode.genOtherList();
    RootNode other = new RootNode(otherList, "其他");
    allFuncList.addAll(otherList);

    String funcJson = MyPreference.FUNC_MAIN.getValue();
    LogUtils.d("get FUNC_MAIN:%s", funcJson);
    List<BaseNode> hisList = new ArrayList<>();
    if (funcJson != null) {
      int[] useList = new Gson().fromJson(funcJson, int[].class);
      for (Integer integer : useList) {
        BaseNode funcBean = allFuncList.get(integer);
        hisList.add(funcBean);
      }
    }

    hisRootNode = new RootNode(hisList, "最近使用");
    if (hisRootNode.haveChildNode()) {
      hisRootNode.setExpanded(true);
      rootNodeList.add(hisRootNode);
    }
    rootNodeList.add(module);
    rootNodeList.add(business);
    rootNodeList.add(other);

    return rootNodeList;
  }

  private void initData() {
    genFuncData();
    funcAdapter.setNewInstance(rootNodeList);
    funcAdapter.setOnFuncClickListener(funcBean -> {
      //ToastUtils.showShort("click func " + funcBean.getName());

      List<BaseNode> childNode = hisRootNode.getChildNode();
      int i = childNode.indexOf(funcBean);
      if (i == 0) {
      } else {
        if (0 < i) {
          childNode.remove(i);
          //childNode.add(0, funcBean);
        } else if (i < 0) {
        }
        childNode.add(0, funcBean);
        if (rootNodeList.size() < 4) {
          rootNodeList.add(0, hisRootNode);
        }
        funcAdapter.setNewInstance(rootNodeList);
        saveUseList();
      }

      if (funcBean.getActivityClass() != null) {
        startActivity(new Intent(MainActivity.this, funcBean.getActivityClass()));
      }
    });
  }

  private void saveUseList() {
    List<BaseNode> childNode = hisRootNode.getChildNode();
    if (!childNode.isEmpty()) {
      int[] func = new int[hisRootNode.getChildNode().size()];
      for (int i = 0; i < childNode.size(); i++) {
        int funcIndex = allFuncList.indexOf(childNode.get(i));
        func[i] = funcIndex;
      }
      String json = new Gson().toJson(func);
      LogUtils.d("put FUNC_MAIN:%s", json);
      MyPreference.FUNC_MAIN.put(json);
    }
  }

  @Override public void onNetworkChange(boolean isConnected) {
    LogUtils.d("网络已连接?:%s", isConnected);
    if (isConnected) {
      ToastUtils.showShort("网络已连接");
    } else {
      ToastUtils.showShort("网络断开");
    }
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    LogUtils.v("%s:  RepeatCount:%s Action:%s long:%s shift:%s meta:%X",
        KeyEvent.keyCodeToString(keyCode),
        event.getRepeatCount(),
        event.getAction(),
        event.isLongPress(),
        event.isShiftPressed(),
        event.getMetaState()
    );
    switch (keyCode) {
      case KeyEvent.KEYCODE_1:
        SetIpPortHelper.showIpPortSetting(this, (ip, port, settingNoChange) -> {
          LogUtils.d("ip:%s:%s", ip, port);
          if (!settingNoChange) {
            ToastUtils.showShort("已保存设置，%s:%s", ip, port);
            MyPreference.SERVER_IP1.put(ip);
            MyPreference.SERVER_PORT1.put(port);
          } else {
            ToastUtils.showShort("设置未改变，%s:%s", ip, port);
          }
          return false;
        });
        break;
      case KeyEvent.KEYCODE_3:
        //startActivity(new Intent(this, ZcLockActivity.class));
        startActivity(new Intent(this, NettyActivity.class));
        break;
      case KeyEvent.KEYCODE_4:
        startActivity(new Intent(this, UhfLotScanActivity.class));
        break;
      case KeyEvent.KEYCODE_5:
        startActivity(new Intent(this, MainActivity2.class));
        break;
      case KeyEvent.KEYCODE_6:
        //CrashLogUtil.logException(new RuntimeException("test log exception"));

        InputConfirmPopupView popupView = new MyInputPopupView(this, 0);
        popupView.setTitleContent("title", "content", "hint");
        popupView.inputContent = "inputContent";
        popupView.setTitleContent("设置托盘容量", null, null);
        //popupView.inputContent = "inputContent";
        popupView.setListener(text -> {
        }, null);
        new XPopup.Builder(this)
            //.isDarkTheme(true)
            .hasShadowBg(true)
            .autoOpenSoftInput(false)
            .asCustom(popupView)
            .show();
        break;
      case KeyEvent.KEYCODE_7:
        DeviceInfoUtils.shutdown(true);
        //DeviceInfoUtils.reboot(this);
        //throw new RuntimeException("test crash");
      case KeyEvent.KEYCODE_9:
        break;
      case KeyEvent.KEYCODE_SHIFT_LEFT:
      case KeyEvent.KEYCODE_F2:
        startActivity(new Intent(this, AboutActivity.class));
        break;
      default:
        return super.onKeyDown(keyCode, event);
    }

    return true;
  }

  @Override protected void onDestroy() {
    if (UhfController.getInstance().isOpen()) {
      UhfController.getInstance().release();
    }
    if (RfidController.getInstance().isOpen()) {
      RfidController.getInstance().release();
    }
    LogUtils.getLog2FileConfig().flushAsync();

    NetworkChangeReceiver.unregister(this);
    super.onDestroy();

    AppUtil.killProcess();
  }

  @Override public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_uhf:
        startActivity(new Intent(this, UhfActivity.class));
        break;
      case R.id.btn_nfc:
        startActivity(new Intent(this, NfcActivity.class));
        break;
      case R.id.btn_init_bag3:
        startActivity(new Intent(this, InitBag3Activity.class));
        break;
      case R.id.btn_main_socket:
        startActivity(new Intent(this, SocketActivity.class));
        break;
      case R.id.btn_barcode:
        startActivity(new Intent(this, BarcodeActivity.class));
        break;
      case R.id.btn_finger:
        startActivity(new Intent(this, FingerActivity.class));
        break;
      case R.id.btn_old_bag:
        startActivity(new Intent(this, OldBagActivity.class));
        break;
      case R.id.btn_check_bag:
        startActivity(new Intent(this, BagCheckActivity.class));
        break;
      case R.id.btn_sound:
        startActivity(new Intent(this, SoundActivity.class));
        break;
      case R.id.btn_main_cover_bag:
        startActivity(new Intent(this, CoverBagActivity.class));
        break;
      case R.id.btn_main_bag_search:
        startActivity(new Intent(this, BagSearchActivity.class));
        break;
      case R.id.btn_main_apdu:
        startActivity(new Intent(this, ZcLockActivity.class));
        break;
      case R.id.btn_main_lot_scan:
        startActivity(new Intent(this, UhfLotScanActivity.class));
        break;
      case R.id.btn_main_netty:
        startActivity(new Intent(this, NettyActivity.class));
        break;
    }
  }
}
