package com.fanfull.handheldtools.ui;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.apkfuns.logutils.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.fanfull.handheldtools.R;
import com.fanfull.handheldtools.context.Contexts;
import com.fanfull.handheldtools.ui.base.InitModuleActivity;
import com.fanfull.libhard.rfid.APDUParser;
import com.fanfull.libhard.rfid.PSamCmd;
import com.fanfull.libjava.io.netty.ClientNetty;
import com.fanfull.libjava.io.netty.handler.ReconnectBeatHandler;
import com.fanfull.libjava.io.socketClient.Options;
import com.fanfull.libjava.lock_zc.SecurityUtil;
import com.fanfull.libjava.util.BytesUtil;
import com.fanfull.libjava.util.ClockUtil;
import com.fanfull.libjava.util.CrcUtil;

import org.orsoul.baselib.util.ViewUtil;
import org.orsoul.baselib.view.FullScreenPopupSetIp;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

public class NettyActivity extends InitModuleActivity {

  private TextView tvShow;
  private Button btnConnect;
  private Button btnSetIp;

  private ClientNetty clientNetty;
  private String serverIp = "192.168.11.246";
  private int serverPort = 23456;

  @Override protected void initView() {
    setContentView(R.layout.activity_netty);
    tvShow = findViewById(R.id.tv_netty_show);
    tvShow.setMovementMethod(ScrollingMovementMethod.getInstance());

    btnConnect = findViewById(R.id.btn_netty_connect);
    btnSetIp = findViewById(R.id.btn_netty_setIp);

    tvShow.setOnClickListener(this);
    btnConnect.setOnClickListener(this);
    btnSetIp.setOnClickListener(this);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    initModule(false, true);

    ViewUtil.appendShow(tvShow,
        "0 -> 重置Psam\n"
            + "1 -> 获取psam卡信息\n"
            + "2 -> 身份验证\n"
            + "3 -> 生成激活交互指令\n"
    );

    serverIp = SPUtils.getInstance().getString(Contexts.Key.KEY_SERVICE_IP, serverIp);
    serverPort = SPUtils.getInstance().getInt(Contexts.Key.KEY_SERVICE_PORT, serverPort);
    initTcp();

    onKeyDown(KeyEvent.KEYCODE_0, null);
  }

  @Override public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_netty_connect:
        if (clientNetty.isConnected()) {
          clientNetty.disconnect();
        } else {
          showLoadingView("正在连接...");
          clientNetty.connectChannelFuture();
        }
        break;
      case R.id.tv_netty_show:
        if (ClockUtil.fastClickTimes() == 3) {
          tvShow.setText("");
        }
        break;
      case R.id.btn_netty_setIp:
        showSetIp();
        break;
    }
  }

  private void showSetIp() {
    FullScreenPopupSetIp.showIpPortSetting(this, serverIp, serverPort,
        (ip, port, settingNoChange) -> {
          if (settingNoChange) {
            ToastUtils.showShort("设置未变化");
            return true;
          }
          serverIp = ip;
          serverPort = port;
          if (clientNetty.isConnected()) {
            clientNetty.disconnect();
          } else {
            clientNetty.connectChannelFuture();
          }

          LogUtils.d("ip:%s:%s", ip, port);
          ToastUtils.showShort("ip:%s:%s", ip, port);
          return true;
        });
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    byte[] apduCmd = null;
    switch (keyCode) {
      case KeyEvent.KEYCODE_0:
        //boolean resetPSam = rfidController.resetPSam();
        //if (resetPSam) {
        //  ViewUtil.appendShow("重置Psam成功", tvShow);
        //} else {
        //  ViewUtil.appendShow("重置Psam失败！！！", tvShow);
        //}
        handlerApdu("reset");
        break;
      case KeyEvent.KEYCODE_1:
        apduCmd = PSamCmd.genCmdGetInfo(1, 0);
        break;
      case KeyEvent.KEYCODE_2:
        apduCmd = PSamCmd.getCmdVerifyUser();
        break;
      case KeyEvent.KEYCODE_3:
        byte[] epc = new byte[]{0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77,
            (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC};
        byte[] data = new byte[14];
        data[0] = 0x03;
        apduCmd = PSamCmd.getCmdGenElsCmd(1, epc, data);
        break;
      default:
        return super.onKeyDown(keyCode, event);
    }
    if (apduCmd != null) {
      LogUtils.d("sendCmd:%s", BytesUtil.bytes2HexString(apduCmd));
      byte[] recCmd = rfidController.send2PSam(apduCmd);
      LogUtils.d("recCmd:%s", BytesUtil.bytes2HexString(recCmd));
    }
    return true;
  }

  public void initTcp() {
    Options options = new Options();
    //sOptions.serverIp = "192.168.11.246";
    options.serverIp = serverIp;
    options.serverPort = serverPort;
    options.reconnectEnable = true;
    options.heartBeatEnable = false;
    options.connectTimeout = 5000;

    LogUtils.wtf("tcp options:%s", options);
    clientNetty = new ClientNetty(options);
    clientNetty.init(new MyChannelInitializer(), false);
  }

  @Override protected void onDestroy() {
    if (clientNetty != null) {
      clientNetty.shutdown();
    }
    super.onDestroy();
  }

  private void handlerApdu(String cmd) {
    LogUtils.d("handlerApdu:%s", cmd);
    if (cmd == null) {
      return;
    }
    String[] sendInfo = new String[1];
    if (cmd.equalsIgnoreCase("reset")) {
      boolean resetPSam = rfidController.resetPSam();
      if (resetPSam) {
        sendInfo[0] = "重置Psam成功";
      } else {
        sendInfo[0] = "重置Psam失败！！！";
      }
      runOnUiThread(() -> {
        ViewUtil.appendShow(sendInfo[0], tvShow);
      });
      clientNetty.send(sendInfo[0]);
      return;
    }

    String[] split = cmd.split(" ");
    if (split.length < 4 || 7 < split.length) {
      clientNetty.send("非apdu：" + cmd);
      return;
    }

    byte[] data = null;
    byte[] apduCmd = null;
    try {
      int cla = Integer.parseInt(split[0], 16);
      int ins = Integer.parseInt(split[1], 16);
      int p1 = Integer.parseInt(split[2], 16);
      int p2 = Integer.parseInt(split[3], 16);
      switch (split.length) {
        case 7:
          // lc = split[4]
          data = BytesUtil.hexString2Bytes(split[5]);
          int le = Integer.parseInt(split[6], 16);
          apduCmd = APDUParser.genCmd(cla, ins, p1, p2, data, le);
          break;
        case 6:
          // lc = split[4]
          data = BytesUtil.hexString2Bytes(split[5]);
          apduCmd = APDUParser.genCmd(cla, ins, p1, p2, data);
          break;
        case 5:
          le = Integer.parseInt(split[4], 16);
          apduCmd = APDUParser.genCmd(cla, ins, p1, p2, le);
          break;
        case 4:
          apduCmd = APDUParser.genCmd(cla, ins, p1, p2);
          break;
      }
    } catch (Exception e) {
      LogUtils.wtf("%s", e.getMessage());
      clientNetty.send(e.getMessage());
      e.printStackTrace();
    }

    if (apduCmd != null) {
      byte[] buff = new byte[512];
      LogUtils.v("sendCmd:%s", BytesUtil.bytes2HexString(apduCmd));
      LogUtils.d("sendCmd:%s", APDUParser.cmd2String(apduCmd));
      int len = rfidController.send2PSam(apduCmd, buff, false);
      sendInfo[0] = BytesUtil.bytes2HexString(buff, len);
      sendInfo[0] = String.format("rec %s:%s", len, sendInfo[0]);
      LogUtils.i(sendInfo[0]);
      clientNetty.send(sendInfo[0]);

      if (split[1].equals("30") && APDUParser.checkReply(buff, len)) {
        byte[] key8 = BytesUtil.hexString2Bytes("91C537C8AA0B2018");
        byte[] factor8 = Arrays.copyOfRange(data, 1, 9);
        byte[] recCmd = Arrays.copyOf(buff, len - 2);
        byte[] tk1 = SecurityUtil.tk1(key8, factor8, recCmd);
        String format = String.format("传输密钥:%s\n随机因子:%s\n交互密文:%s\n交互明文:%s,%02X = crc",
            BytesUtil.bytes2HexString(key8),
            BytesUtil.bytes2HexString(factor8),
            BytesUtil.bytes2HexString(recCmd),
            BytesUtil.bytes2HexString(tk1),
            CrcUtil.crc8(0x8D, 0, 0, tk1, 0, tk1.length - 1));
        LogUtils.wtf(format);
        sendInfo[0] = format;
        clientNetty.send(sendInfo[0]);
      }
    }
    runOnUiThread(() -> {
      ViewUtil.appendShow(sendInfo[0], tvShow);
    });
  }

  class MyChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override protected void initChannel(SocketChannel socketChannel) {
      ChannelPipeline pipeline = socketChannel.pipeline();
      // 心跳
      Options options = clientNetty.getOptions();
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
          handlerApdu(recStr);
        }
      });

      // 编码器，为发送字符串数据 加上头尾标识
      pipeline.addLast(new StringEncoder(StandardCharsets.UTF_8));
      //pipeline.addLast(new ByteArrayEncoder());
    }
  }

  class MyReconnectBeatHandler extends ReconnectBeatHandler {
    public MyReconnectBeatHandler(ClientNetty clientNetty) {
      super(clientNetty);
    }

    boolean isConnectFailed = true;

    @Override public Object getHeartBeatMsg() {
      return "HeartBeat";
    }

    @Override public void onHeartBeatOut() {
      LogUtils.wtf("心跳回复超时");
      ToastUtils.showShort("心跳回复超时");
      //clientNetty.disconnect();
      clientNetty.getOptions().disconnectCount *= 1.5;
    }

    @Override public void channelActive(ChannelHandlerContext ctx) throws Exception {
      final Options opt = clientNetty.getOptions();
      LogUtils.wtf("连接成功 %s", opt);
      String format = String.format("连接成功 %s:%s", opt.serverIp, opt.serverPort);
      ToastUtils.showShort(format);
      runOnUiThread(() -> {
        ViewUtil.appendShow(format, tvShow);
        btnConnect.setText("断开");
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
        btnConnect.setText("连接");
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