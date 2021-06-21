package com.fanfull.handheldtools.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.apkfuns.logutils.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.fanfull.handheldtools.R;
import com.fanfull.handheldtools.preference.MyPreference;
import com.fanfull.handheldtools.ui.base.InitModuleActivity;
import com.fanfull.libhard.lock3.task.ScanLotUhfOrNfcTask;
import com.fanfull.libhard.rfid.IRfidListener;
import com.fanfull.libhard.rfid.RfidController;
import com.fanfull.libhard.uhf.IUhfListener;
import com.fanfull.libhard.uhf.UhfController;
import com.fanfull.libjava.io.netty.ClientNetty;
import com.fanfull.libjava.io.socketClient.Options;
import com.fanfull.libjava.util.BytesUtil;
import com.fanfull.libjava.util.ClockUtil;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.impl.LoadingPopupView;
import com.lxj.xpopup.interfaces.XPopupCallback;

import org.orsoul.baselib.util.HtmlUtil;
import org.orsoul.baselib.util.SoundHelper;
import org.orsoul.baselib.util.ViewUtil;
import org.orsoul.baselib.view.FullScreenPopupSetIp;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;

//import com.fanfull.handheldtools.ui.view.FullScreenPopupSetIp;

public class BagSearchActivity extends InitModuleActivity {

  private EditText etBagId;
  private TextView tvShow;
  private Button btnScan;
  private Button btnInput;
  private Switch switchUhf;

  private SearchBagTask allLockTask;
  private Set<String> bagIdList;

  private Options options;
  private ClientNetty clientNetty;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    LoadingPopupView loadingPopupView = new XPopup.Builder(this).setPopupCallback(
        new XPopupCallback() {
          @Override public void onCreated(BasePopupView popupView) {

          }

          @Override public void beforeShow(BasePopupView popupView) {

          }

          @Override public void onShow(BasePopupView popupView) {

          }

          @Override public void onDismiss(BasePopupView popupView) {
            if (allLockTask != null) {
              allLockTask.stopThread();
            }
          }

          @Override public void beforeDismiss(BasePopupView popupView) {

          }

          @Override public boolean onBackPressed(BasePopupView popupView) {
            return false;
          }

          @Override public void onKeyBoardStateChanged(BasePopupView popupView, int height) {

          }

          @Override
          public void onDrag(BasePopupView popupView, int value, float percent, boolean upOrLeft) {

          }
        })
        .asLoading();
    dialogUtil.setLoadingPopupView(loadingPopupView);
    bagIdList = Collections.synchronizedSet(new HashSet<>());
    //bagIdList.add("0553210104C35ED23E6180E2");
  }

  private void initTcp() {
    if (options != null) {
      return;
    }

    options = new Options();
    //options.serverIp = "192.168.11.246";
    //options.serverPort = 23579;
    options.serverIp = MyPreference.SERVER_IP1.getValue("192.168.11.246");
    options.serverPort = MyPreference.SERVER_PORT1.getValue(23579);
    options.reconnectEnable = true;
    options.heartBeatEnable = false;
    clientNetty = new ClientNetty(options);

    ChannelInitializer<SocketChannel> channelInitializer =
        new ChannelInitializer<SocketChannel>() { // 指定Handler
          @Override protected void initChannel(SocketChannel socketChannel) {
            ChannelPipeline pipeline = socketChannel.pipeline();
            //pipeline.addLast(new StringDecoder(StandardCharsets.UTF_8));
            pipeline.addLast(new StringDecoder());

            pipeline.addLast(new SimpleChannelInboundHandler<String>() {
              @Override protected void channelRead0(ChannelHandlerContext ctx, String bb)
                  throws Exception {
                LogUtils.d("rec:%s", bb);

                runOnUiThread(new Runnable() {
                  @Override public void run() {
                    formatDataView(bb);
                  }
                });
              }
            });
          }
        };

    clientNetty.init(channelInitializer, false);
  }

  @Override protected void initView() {
    setContentView(R.layout.activity_bag_search);
    tvShow = findViewById(R.id.tv_bag_search_show);
    tvShow.setMovementMethod(ScrollingMovementMethod.getInstance());
    tvShow.setOnClickListener(this);

    etBagId = findViewById(R.id.et_bag_search_show);
    etBagId.setOnClickListener(this);

    btnScan = findViewById(R.id.btn_bag_search_scan);
    btnScan.setOnClickListener(this);

    btnInput = findViewById(R.id.btn_bag_search_input);
    btnInput.setOnClickListener(this);
    btnInput.setOnLongClickListener(view -> {
      formatDataView(null);
      ToastUtils.showShort("已更新扫描列表");
      return true;
    });

    switchUhf = findViewById(R.id.switch_bag_search_nfc_mode);
    switchUhf.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          allLockTask.setReadNfc(isChecked);
        });

    btnScan.setEnabled(false);
    //switchUhf.setEnabled(true);
  }

  @Override protected void initModule() {
    uhfController = UhfController.getInstance();
    rfidController = RfidController.getInstance();

    uhfController.setListener(new IUhfListener() {
      @Override public void onOpen(boolean openSuccess) {
        runOnUiThread(() -> {
          if (!openSuccess) {
            dismissLoadingView();
            //ViewUtil.appendShow("超高频初始失败!!!", tvShow);
            tvShow.setText("超高频初始失败!!!");
            return;
          }
          //ViewUtil.appendShow("超高频初始成功", tvShow);
          //tvShow.setText("超高频初始成功\n");
          rfidController.open();
        });
      }

      @Override public void onReceiveData(byte[] data) {
      }
    });

    rfidController.setListener(new IRfidListener() {
      @Override public void onOpen(boolean openSuccess) {
        runOnUiThread(() -> {
          dismissLoadingView();
          if (!openSuccess) {
            //ViewUtil.appendShow("高频模块初始失败!!!", tvShow);
            tvShow.setText("高频模块初始失败!!!");
            return;
          }
          //tvShow.setText("初始成功\n");
          ViewUtil.appendShow(
              "按键1 -> 连接Tcp\n"
                  + "按键7 -> 设置功率\n"
                  + "长按【输入】按钮，格式并更新袋列表", tvShow);
          allLockTask = new SearchBagTask(uhfController, rfidController);
          allLockTask.setReadNfc(switchUhf.isChecked());
          //initBagTask = new InitBag3Activity.MyInitBagTask();
          //initSpinner();
          btnScan.setEnabled(true);
          switchUhf.setEnabled(true);
          //switchUhf.setChecked(false);
        });
      }

      @Override public void onReceiveData(byte[] data) {
        LogUtils.d("recNfc:%s", BytesUtil.bytes2HexString(data));
      }
    });

    showLoadingView("正在初始化模块...");
    //ViewUtil.appendShow("", tvShow);
    uhfController.open();
  }

  @Override protected void onEnterPress() {
    btnScan.performClick();
  }

  @Override public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_bag_search_scan:
        if (allLockTask.isRunning()) {
          allLockTask.stopThread();
          return;
        }

        if (bagIdList.isEmpty() && !formatDataView(null)) {
          ToastUtils.showShort("搜索数据为空");
          return;
        }

        LogUtils.d("bags:%s", bagIdList);

        if (!allLockTask.startThread()) {
          ToastUtils.showShort("扫描线程启动失败！！！");
        }
        break;
      case R.id.btn_bag_search_input:
        switchView();
        break;
      case R.id.tv_bag_search_show:
        if (3 == ClockUtil.fastClickTimes()) {
          tvShow.setText("");
        }
        break;
      case R.id.et_bag_search_show:
        if (2 == ClockUtil.fastClickTimes()) {
          etBagId.setText(null);
          bagIdList.clear();
          ToastUtils.showShort("袋列表已清空");
        }
        break;
      default:
    }
  }

  boolean isInput;

  private void switchView() {
    isInput = !isInput;
    if (isInput) {
      etBagId.setVisibility(View.VISIBLE);
      tvShow.setVisibility(View.GONE);
      btnInput.setText("结果");
      ViewUtil.requestFocus(etBagId);
    } else {
      tvShow.setVisibility(View.VISIBLE);
      etBagId.setVisibility(View.GONE);
      btnInput.setText("输入");
    }
  }

  private boolean formatDataView(String data) {
    if (TextUtils.isEmpty(data)) {
      final Editable text = etBagId.getText();
      if (TextUtils.isEmpty(text)) {
        return false;
      }
      data = text.toString();
    }

    //final String[] split = text.toString().split("[\\s\\\\.]");
    final String[] split = data.split("[\\W]");
    bagIdList.clear();
    StringBuilder sb = new StringBuilder(split.length);
    for (int i = 0; i < split.length; i++) {
      if (!TextUtils.isEmpty(split[i]) && bagIdList.add(split[i])) {
        sb.append(split[i]).append("\n");
      }
    }
    etBagId.setText(sb.toString());
    //bagIdList.addAll(Arrays.asList(split));
    LogUtils.d("formatDataView:%s", bagIdList);
    return true;
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_1) {
      initTcp();
      FullScreenPopupSetIp.showIpPortSetting(this, options.serverIp, options.serverPort,
          (ip, port, settingNoChange) -> {
            if (settingNoChange) {
              if (clientNetty.isConnected()) {
                ToastUtils.showShort("已连接");
                return true;
              }
            } else {
              MyPreference.SERVER_IP1.put(ip);
              MyPreference.SERVER_PORT1.put(port);
            }

            options.serverIp = ip;
            options.serverPort = port;
            //clientNetty.setOptions(options);
            clientNetty.disconnect();
            clientNetty.connect();
            return true;
          });
    }
    return super.onKeyDown(keyCode, event);
  }

  @Override protected void onDestroy() {
    if (clientNetty != null) {
      clientNetty.shutdown();
    }
    super.onDestroy();
  }

  private class SearchBagTask extends ScanLotUhfOrNfcTask {

    public SearchBagTask(UhfController uhfController,
        RfidController rfidController) {
      super(uhfController, rfidController);
    }

    @Override protected boolean onScanSuccess(ScanLotBean data) {
      resetStartTime();
      final String bagId = data.getBagId();
      LogUtils.d("scan:%s", bagId);
      if (bagIdList.contains(bagId)) {
        playDelay(SoundHelper.TONE_SUCCESS, bagId);
      } else {
        playDelay(SoundHelper.TONE_DROP, bagId);
      }
      return false;
    }

    @Override protected boolean onScanFailed() {
      playDelay(SoundHelper.TONE_DIDA);
      return false;
    }

    private void playDelay(int soundId) {
      playDelay(soundId, null);
    }

    private void playDelay(int soundId, String bagId) {
      final long clock = ClockUtil.runTime();
      if (clock < 400) {
        return;
      }

      SoundHelper.playSound(soundId);
      ClockUtil.runTime(true);

      Spanned colorSpanned = null;
      if (soundId == SoundHelper.TONE_SUCCESS) {
        colorSpanned = HtmlUtil.getColorSpanned(bagId, 0x0000FF);
      } else if (soundId == SoundHelper.TONE_DROP) {
        colorSpanned = HtmlUtil.getColorSpanned(bagId, 0xFF0000);
      }
      if (colorSpanned != null) {
        Spanned finalColorSpanned = colorSpanned;
        runOnUiThread(new Runnable() {
          @Override public void run() {
            ViewUtil.appendShow(finalColorSpanned, tvShow);
          }
        });
      }
    }

    @Override protected void onTaskBefore() {
      runOnUiThread(() -> {
        //showLoadingView("正在扫描...");
        //btnScan.setEnabled(false);
        btnScan.setText("停止");
      });
    }

    @Override protected void onStop() {
      super.onStop();
    }

    @Override protected void onTaskFinish() {
      runOnUiThread(() -> {
        //dismissLoadingView();
        //btnScan.setEnabled(true);
        btnScan.setText("扫描");
      });
    }
  }
}