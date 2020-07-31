package com.fanfull.handheldtools.ui;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.apkfuns.logutils.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.fanfull.handheldtools.R;
import com.fanfull.handheldtools.base.InitModuleActivity;
import org.orsoul.baselib.io.socketClient.Options;
import org.orsoul.baselib.io.socketClient.impl.BaseSocketClient;
import org.orsoul.baselib.io.socketClient.interf.ISocketClientListener;
import org.orsoul.baselib.io.transfer.IoTransferListener;
import org.orsoul.baselib.util.ArrayUtils;
import org.orsoul.baselib.util.ClockUtil;
import org.orsoul.baselib.util.ViewUtil;

public class SocketActivity extends InitModuleActivity {
  private TextView tvShow;
  private Button btnConnect;
  private Button btnDisconnect;
  private Button btnSend;

  BaseSocketClient socketClient;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    String ip = NetworkUtils.getIPAddress(true);
    ViewUtil.appendShow(String.format("本机ip：%s", ip), tvShow);
  }

  @Override protected void initModule() {
    Options opt = new Options();
    opt.serverIp = "192.168.11.197";
    opt.serverPort = 23456;
    opt.autoReconnect = true;
    socketClient = new BaseSocketClient(opt);
    socketClient.addSocketClientListener(new ISocketClientListener() {
      @Override public void onConnect(String serverIp, int serverPort) {
        runOnUi(() -> {
          dismissLoadingView();
          ViewUtil.appendShow(String.format("已连接：%s:%s", serverIp, serverPort), tvShow);
          btnConnect.setEnabled(false);
        });
      }

      @Override public void onConnectFailed(Throwable e) {
        dismissLoadingView();
        ViewUtil.appendShow(String.format("连接失败：%s", e.getMessage()), tvShow);
        btnConnect.setEnabled(true);
      }

      @Override public void onDisconnect(String serverIp, int serverPort) {
        runOnUi(() -> {
          ViewUtil.appendShow(String.format("断开连接：%s:%s", serverIp, serverPort), tvShow);
          btnConnect.setEnabled(true);
        });
      }

      @Override public boolean onReceive(byte[] data) {
        runOnUi(() -> {
          ViewUtil.appendShow(
              String.format("rec:%s", ArrayUtils.bytes2HexString(data)), tvShow);
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
        runOnUi(() -> {
          ViewUtil.appendShow(info, tvShow);
        });
      }
    });
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
  }

  @Override public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_socket_connect:
        showLoadingView("正在连接...");
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
            runOnUi(() -> {
              dismissLoadingView();
              ViewUtil.appendShow(String.format("收到：%s", new String(data)), tvShow);
            });
            return true;
          }

          @Override public void onReceiveTimeout() {
            runOnUi(() -> {
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
    }
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    return super.onKeyDown(keyCode, event);
  }

  @Override protected void onDestroy() {
    socketClient.disconnect();
    super.onDestroy();
  }
}
