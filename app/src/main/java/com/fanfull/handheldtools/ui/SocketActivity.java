package com.fanfull.handheldtools.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.apkfuns.logutils.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.fanfull.handheldtools.R;
import com.fanfull.handheldtools.context.Contexts;
import com.fanfull.handheldtools.ui.base.InitModuleActivity;
import com.fanfull.libhard.rfid.RfidController;
import com.fanfull.libhard.uhf.UhfCmd;
import com.fanfull.libhard.uhf.UhfController;
import com.fanfull.libjava.io.socketClient.Options;
import com.fanfull.libjava.io.socketClient.impl.BaseSocketClient;
import com.fanfull.libjava.io.socketClient.interf.ISocketClientListener;
import com.fanfull.libjava.io.transfer.IoTransferListener;
import com.fanfull.libjava.util.BytesUtil;
import com.fanfull.libjava.util.ClockUtil;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.InputConfirmPopupView;
import com.lxj.xpopup.interfaces.OnInputConfirmListener;

import org.orsoul.baselib.util.ViewUtil;

import java.io.UnsupportedEncodingException;
import java.util.Objects;

public class SocketActivity extends InitModuleActivity {
  private TextView tvShow;
  private Button btnConnect;
  private Button btnDisconnect;
  private Button btnSend;

  BaseSocketClient socketClient;
  ISocketClientListener clientListener;
  private String serverIp = "192.168.11.197";
  private int serverPort = 23456;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override protected void initModule() {

    String ip = NetworkUtils.getIPAddress(true);
    serverIp = SPUtils.getInstance().getString(Contexts.Key.KEY_SERVICE_IP, ip);
    serverPort = SPUtils.getInstance().getInt(Contexts.Key.KEY_SERVICE_PORT, serverPort);
    ViewUtil.appendShow(String.format("本机IP：%s\n目标地址：%s:%s", ip, serverIp, serverPort), tvShow);
    ViewUtil.appendShow("r|w mb sa len|hexStr [mmb msa hexStr]", tvShow);
    ViewUtil.appendShow("r nfc 4 12：读nfc,起始地址4(字长4字节)，读12字节", tvShow);
    ViewUtil.appendShow("r epc 2 12：读epc 起始地址2(字长2字节)，读12字节", tvShow);
    ViewUtil.appendShow("w epc 2 ACED tid 3 F378：写epc ACED，tid过滤F378", tvShow);

    Options opt = new Options();
    opt.serverIp = serverIp;
    opt.serverPort = serverPort;
    opt.autoReconnect = true;
    socketClient = new BaseSocketClient(opt);
    clientListener = new ISocketClientListener() {
      @Override public void onConnect(String serverIp, int serverPort) {
        runOnUiThread(() -> {
          dismissLoadingView();
          ViewUtil.appendShow(String.format("已连接：%s:%s", serverIp, serverPort), tvShow);
          btnConnect.setEnabled(false);
        });
      }

      @Override public void onConnectFailed(Throwable e) {
        runOnUiThread(() -> {
          dismissLoadingView();
          ViewUtil.appendShow(String.format("连接失败：%s", e.getMessage()), tvShow);
          btnConnect.setEnabled(true);
        });
      }

      @Override public void onDisconnect(String serverIp, int serverPort) {
        runOnUiThread(() -> {
          ViewUtil.appendShow(String.format("断开连接：%s:%s", serverIp, serverPort), tvShow);
          btnConnect.setEnabled(true);
        });
      }

      @Override public boolean onReceive(byte[] data) {
        String s = new String(data);
        String s1 = BytesUtil.bytes2HexString(data);
        LogUtils.d("str:%s\nhex:%s", s, s1);
        runOnUiThread(() -> {
          ViewUtil.appendShow(
              String.format("rec: %s", s), tvShow);
        });
        String info = handlerCmd(s);
        boolean send = false;
        try {
          send = socketClient.send(info.getBytes("gbk"));
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
        }
        String format = String.format("send %s：%s", send, info);
        LogUtils.d(format);
        runOnUiThread(() -> {
          ViewUtil.appendShow(format, tvShow);
        });
        return true;
      }

      @Override public void onSend(boolean isSuccess, byte[] data, int offset, int len) {
        String info;
        if (isSuccess) {
          info = String.format("send:%s", new String(data, 0, len));
        } else {
          info = String.format("发送失败:%s", new String(data, 0, len));
        }
        runOnUiThread(() -> {
          ViewUtil.appendShow(info, tvShow);
        });
      }
    };
  }

  @Override protected void initView() {
    setContentView(R.layout.activity_socket);

    tvShow = findViewById(R.id.tv_socket_show);
    tvShow.setMovementMethod(ScrollingMovementMethod.getInstance());

    btnConnect = findViewById(R.id.btn_socket_connect);
    btnDisconnect = findViewById(R.id.btn_socket_disconnect);
    btnSend = findViewById(R.id.btn_socket_send);

    tvShow.setOnClickListener(this);
    btnConnect.setOnClickListener(this);
    btnDisconnect.setOnClickListener(this);
    btnSend.setOnClickListener(this);
    findViewById(R.id.btn_socket_setIp).setOnClickListener(this);
  }

  InputConfirmPopupView inputConfirm;

  private void showSetIp() {
    if (inputConfirm == null) {

      String ip = serverIp + ":" + serverPort;
      inputConfirm = new XPopup.Builder(this).asInputConfirm("设置服务器地址", "当前服务器地址", ip, ip,
          new OnInputConfirmListener() {
            @Override public void onConfirm(String text) {
              if (TextUtils.isEmpty(text)) {

              } else {
                String[] split = text.split(":");
                switch (split.length) {
                  case 2:
                    if (!RegexUtils.isMatch("\\d+", split[1])) {
                      ToastUtils.showShort("端口格式错误");
                      return;
                    } else if (!Objects.equals(String.valueOf(serverPort), split[1])) {
                      serverPort = Integer.parseInt(split[1]);
                      SPUtils.getInstance().put(Contexts.Key.KEY_SERVICE_PORT, serverPort);
                    }
                  case 1:
                    if (!RegexUtils.isIP(split[0])) {
                      ToastUtils.showShort("IP地址格式错误");
                      return;
                    } else if (!Objects.equals(serverIp, split[0])) {
                      serverIp = split[0];
                      SPUtils.getInstance().put(Contexts.Key.KEY_SERVICE_IP, serverIp);
                    }
                    break;
                  default:
                    ToastUtils.showShort("IP、端口格式错误");
                    return;
                }
                socketClient.setIpPort(serverIp, serverPort);
              } // end if
            }
          });
    }
    inputConfirm.show();
  }

  @Override public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_socket_connect:
        showLoadingView("正在连接...");
        socketClient.addSocketClientListener(clientListener);
        socketClient.connectOnNewThread();
        btnConnect.setEnabled(false);
        break;
      case R.id.btn_socket_disconnect:
        socketClient.disconnect();
        ViewUtil.appendShow("连接已断开", tvShow);
        btnConnect.setEnabled(true);
        break;
      case R.id.btn_socket_send:
        byte[] data = "hello".getBytes();
        //boolean send = socketClient.send(data);
        boolean send = socketClient.send(data, 2000, new IoTransferListener() {
          @Override public boolean onReceive(byte[] data) {
            runOnUiThread(() -> {
              dismissLoadingView();
              ViewUtil.appendShow(String.format("收到：%s", new String(data)), tvShow);
            });
            return true;
          }

          @Override public void onReceiveTimeout() {
            runOnUiThread(() -> {
              dismissLoadingView();
              ViewUtil.appendShow("回复超时", tvShow);
              ToastUtils.showShort("回复超时");
            });
          }

          @Override public void onStopReceive() {

          }
        });
        if (send) {
          showLoadingView("等待回复...");
        }
        LogUtils.d("send success:%s", send);
        break;
      case R.id.tv_socket_show:
        if (ClockUtil.fastClickTimes() == 3) {
          tvShow.setText("");
        }
        break;
      case R.id.btn_socket_setIp:
        showSetIp();
        break;
    }
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    return super.onKeyDown(keyCode, event);
  }

  @Override protected void onDestroy() {
    socketClient.disconnect();
    if (UhfController.getInstance().isOpen()) {
      UhfController.getInstance().release();
    }
    if (RfidController.getInstance().isOpen()) {
      RfidController.getInstance().release();
    }
    super.onDestroy();
  }

  private static int parseMb(String mb) {
    if (mb == null) {
      return -1;
    }
    switch (mb.toLowerCase()) {
      case "epc":
      case "1":
        return UhfCmd.MB_EPC;
      case "tid":
      case "2":
        return UhfCmd.MB_TID;
      case "use":
      case "3":
        return UhfCmd.MB_USE;
      case "nfc":
      case "4":
        return 4;
      default:
        return -1;
    }
  }

  public static String handlerCmd(String cmd) {
    if (cmd == null) {
      return null;
    }
    String[] s = cmd.split(" ");
    if (s.length < 4) {
      return "命令长度错误";
    }

    boolean isRead = true;
    boolean isInit = false;
    switch (s[0]) {
      case "r":
      case "read":
        //isRead = true;
        break;
      case "w":
      case "write":
        isRead = false;
        break;
      case "init":
        isInit = true;
        break;
      default:
        return "不支持的命令：" + s[0];
    }

    boolean isUhf;
    int mb = parseMb(s[1]);
    if (mb <= 0) {
      return "错误的mb号：" + s[0];
    }
    isUhf = mb <= 3;

    if (isInit) {
      boolean open;
      if (isUhf) {
        open = UhfController.getInstance().open();
      } else {
        open = RfidController.getInstance().open();
      }
      if (open) {
        return "初始化 成功";
      } else {
        return "初始化 失败";
      }
    }

    if (isUhf) {
      if (!UhfController.getInstance().open()) {
        return "未能初始化 超高频，请执行：init epc";
      }
    } else {
      if (!RfidController.getInstance().open()) {
        return "未能初始化 高频，请执行：init nfc";
      }
    }

    // 解析 过滤数据 参数
    int mmb = 0;
    int msa = 0;
    byte[] dataFilter = null;
    if (isUhf && s.length == 7) {
      mmb = parseMb(s[4]);
      msa = Integer.parseInt(s[5]);
      dataFilter = BytesUtil.hexString2Bytes(s[6]);
    }

    int sa = Integer.parseInt(s[2]);
    byte[] data;
    boolean res = false;
    if (isRead) { // ============ read ============
      int dl = Integer.parseInt(s[3]);
      data = new byte[dl];
      if (isUhf) {
        res = UhfController.getInstance().read(mb, sa, data, 300, mmb, msa, dataFilter);
      } else {
        res = RfidController.getInstance().readNfc(sa, data, true);
      }
    } else { // ============ write ============
      data = BytesUtil.hexString2Bytes(s[3]);
      if (isUhf) {
        res = UhfController.getInstance().write(mb, sa, data, 300, mmb, msa, dataFilter);
      } else {
        res = RfidController.getInstance().writeNfc(sa, data, true);
      }
    }

    String reVal = null;
    if (res) {
      if (isRead) {
        reVal = String.format("success,read %s ：%s", s[1], BytesUtil.bytes2HexString(data));
      } else {
        reVal = String.format("success,write %s ：%s", s[1], s[3]);
      }
    } else {
      reVal = "命令执行失败";
    }
    return reVal;
  }
}
