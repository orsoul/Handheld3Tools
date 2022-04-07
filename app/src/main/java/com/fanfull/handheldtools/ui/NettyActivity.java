package com.fanfull.handheldtools.ui;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.apkfuns.logutils.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.fanfull.handheldtools.R;
import com.fanfull.handheldtools.preference.MyPreference;
import com.fanfull.handheldtools.ui.base.BaseActivity;
import com.fanfull.libjava.io.netty.ClientNetty;
import com.fanfull.libjava.io.netty.handler.ReconnectBeatHandler;
import com.fanfull.libjava.io.socketClient.Options;
import com.fanfull.libjava.util.ClockUtil;
import com.google.gson.Gson;

import org.orsoul.baselib.util.ViewUtil;
import org.orsoul.baselib.view.FullScreenPopupSetIp2;

import java.nio.charset.StandardCharsets;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

public class NettyActivity extends BaseActivity {

  private TextView tvShow;
  private Button btnConnect;
  private Button btnSetIp;

  private ClientNetty clientNetty;
  private Options options = new Options();

  private String serverIp = "192.168.11.246";
  private int serverPort = 23456;
  //private boolean reconnectEnable = false;
  //private boolean heartBeatEnable = false;
  //private int connectTimeout = 5000;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    String value = MyPreference.NETTY_OP.getValue();
    if (value != null) {
      options = new Gson().fromJson(value, Options.class);
    } else {
      options = new Options();
      options.serverIp = serverIp;
      options.serverPort = serverPort;
      //options.reconnectEnable = true;
      //options.heartBeatEnable = true;
      //options.connectTimeout = 5000;
    }
    LogUtils.d("tcp:%s", options);
    ViewUtil.appendShow(getTcpOptionString(), tvShow);
  }

  @Override protected void initView() {
    setContentView(R.layout.activity_netty);
    tvShow = findViewById(R.id.tv_netty_show);
    btnConnect = findViewById(R.id.btn_netty_connect);

    findViewById(R.id.tv_netty_show).setOnClickListener(this);
    findViewById(R.id.btn_netty_connect).setOnClickListener(this);
    findViewById(R.id.btn_netty_disconnect).setOnClickListener(this);
    findViewById(R.id.btn_netty_setting).setOnClickListener(this);
    findViewById(R.id.btn_netty_help).setOnClickListener(this);
  }

  private String getTcpOptionString() {
    return String.format("IP：%s:%s\n断线重连：%s\n心跳：%s",
        options.serverIp, options.serverPort,
        options.reconnectEnable ? options.reconnectInterval + "毫秒" : "未启用",
        options.heartBeatEnable ? options.heartBeatInterval + "秒" : "未启用");
  }

  @Override public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.tv_netty_show) {
      if (ClockUtil.isFastDoubleClick()) {
        tvShow.setText(null);
      } else {
        ToastUtils.showShort("再次点击清除内容");
      }
    } else if (id == R.id.btn_netty_connect) {
      connect();
    } else if (id == R.id.btn_netty_disconnect) {
      disconnect();
    } else if (id == R.id.btn_netty_setting) {
      FullScreenPopupSetIp2.showIpPortSetting(this, options,
          (ip, port, reconnect, reconnectGap, heartBeat, beatGap) -> {
            options.serverIp = ip;
            options.serverPort = port;
            options.reconnectEnable = reconnect;
            options.reconnectInterval = reconnectGap;
            options.heartBeatEnable = heartBeat;
            options.heartBeatInterval = beatGap;
            MyPreference.NETTY_OP.put(new Gson().toJson(options));
            shutdownTcp();
            return true;
          });
    } else if (id == R.id.btn_netty_help) {

    }
  }

  private void connect() {
    if (clientNetty == null) {
      showLoadingView("正在连接...");
      initTcp();
    } else if (clientNetty.isConnected()) {
      ToastUtils.showShort("TCP已连接");
    } else {
      showLoadingView("正在连接...");
      clientNetty.connect();
    }
  }

  private void disconnect() {
    if (clientNetty != null && clientNetty.isConnected()) {
      clientNetty.disconnect();
    } else {
      ToastUtils.showShort("TCP未连接");
      btnConnect.setEnabled(true);
    }
  }

  private void initTcp() {
    LogUtils.wtf("tcp options:%s", options);
    clientNetty = new ClientNetty(options) {
      @Override public boolean send(Object msg) {
        boolean send = super.send(msg);
        runOnUiThread(() ->
            ViewUtil.appendShow("send " + send +
                ":" + msg, tvShow));
        return send;
      }
    };
    clientNetty.init(new MyChannelInitializer(), true);
  }

  private void shutdownTcp() {
    if (clientNetty != null) {
      clientNetty.shutdown();
      clientNetty = null;
    }
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    return super.onKeyDown(keyCode, event);
  }

  @Override protected void onDestroy() {
    shutdownTcp();
    super.onDestroy();
  }

  class MyChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override protected void initChannel(SocketChannel socketChannel) {
      ChannelPipeline pipeline = socketChannel.pipeline();
      Options options = clientNetty.getOptions();

      // 心跳
      pipeline.addLast(new IdleStateHandler(0,
          options.heartBeatInterval,
          0));

      // 心跳、重连
      pipeline.addLast(new MyReconnectBeatHandler(clientNetty));

      // 粘包处理器
      //pipeline.addLast(new HeadEndDecoder(true));
      // 粘包处理之后的字节数据 转换为 字符串
      //pipeline.addLast(new StringDecoder(Charset.forName("utf-8")));
      pipeline.addLast(new StringDecoder(StandardCharsets.UTF_8));

      pipeline.addLast(new SimpleChannelInboundHandler<String>() {
        @Override protected void channelRead0(ChannelHandlerContext ctx, String recStr)
            throws Exception {
          runOnUiThread(new Runnable() {
            @Override public void run() {
              ViewUtil.appendShow("rec:" + recStr, tvShow);
            }
          });
        }
      });

      pipeline.addLast(new ChannelOutboundHandlerAdapter() {
        @Override public void read(ChannelHandlerContext ctx) throws Exception {
          super.read(ctx);
          //LogUtils.d("ChannelOutboundHandlerAdapter read");
        }

        @Override public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
            throws Exception {
          super.write(ctx, msg, promise);
          //LogUtils.d("ChannelOutboundHandlerAdapter write: %s", msg);
        }
      });
      // 编码器
      pipeline.addLast(new StringEncoder(StandardCharsets.UTF_8));
      pipeline.addLast(new ByteArrayEncoder());
    }
  }

  class MyReconnectBeatHandler extends ReconnectBeatHandler {
    public MyReconnectBeatHandler(ClientNetty clientNetty) {
      super(clientNetty);
    }

    boolean isConnectFailed = true;

    int sendCount;

    @Override public Object getHeartBeatMsg() {
      return "HeartBeat - " + sendCount++;
    }

    @Override public void onHeartBeatOut() {
      LogUtils.wtf("心跳回复超时");
      //ToastUtils.showShort("心跳回复超时");
      //clientNetty.disconnect();
      clientNetty.getOptions().disconnectCount *= 1.5;
    }

    @Override public void channelActive(ChannelHandlerContext ctx) throws Exception {
      final Options opt = clientNetty.getOptions();
      LogUtils.wtf("连接成功 %s", opt);
      String format = String.format("连接成功 %s:%s", opt.serverIp, opt.serverPort);
      ToastUtils.showShort(format);
      runOnUiThread(() -> {
        ViewUtil.appendShow("连接成功 " + getTcpOptionString(), tvShow);
        btnConnect.setEnabled(false);
        //btnConnect.setText("断开");
        dismissLoadingView();
      });
    }

    @Override public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
      if (isConnectFailed) {
        final Options opt = clientNetty.getOptions();
        String format = String.format("建立连接失败 %s:%s", opt.serverIp, opt.serverPort);
        LogUtils.wtf(format);
        ToastUtils.showShort(format);
        runOnUiThread(() -> {
          ViewUtil.appendShow(format, tvShow);
          dismissLoadingView();
        });
      }
      isConnectFailed = true;
      super.channelUnregistered(ctx);
    }

    @Override public void channelInactive(ChannelHandlerContext ctx) throws Exception {
      super.channelInactive(ctx);
      //final Options opt = clientNetty.getOptions();
      //ToastUtils.showLong("网络连接断开 %s:%s", opt.serverIp, opt.serverPort);
      final Options opt = clientNetty.getOptions();
      String format = String.format("连接断开 %s:%s", opt.serverIp, opt.serverPort);
      LogUtils.wtf(format);
      ToastUtils.showShort(format);
      runOnUiThread(() -> {
        ViewUtil.appendShow(format, tvShow);
        //btnConnect.setText("连接");
        btnConnect.setEnabled(true);
      });
      isConnectFailed = false;
    }

    @Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
        throws Exception {
      super.exceptionCaught(ctx, cause);
      String format = String.format("exceptionCaught:%s", cause.getMessage());
      LogUtils.wtf(format);
      ToastUtils.showShort(format);
      runOnUiThread(() -> {
        ViewUtil.appendShow(format, tvShow);
        dismissLoadingView();
      });
    }
  }
}