package com.fanfull.handheldtools.ui;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.apkfuns.logutils.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.fanfull.handheldtools.R;
import com.fanfull.handheldtools.preference.MyPreference;
import com.fanfull.handheldtools.ui.base.InitModuleActivity;
import com.fanfull.libhard.lock3.task.UhfReadTask;
import com.fanfull.libhard.lock_zc.LockZcBean;
import com.fanfull.libhard.lock_zc.PsamHelper;
import com.fanfull.libhard.lock_zc.SecurityUtil;
import com.fanfull.libhard.rfid.APDUParser;
import com.fanfull.libhard.rfid.PSamCmd;
import com.fanfull.libhard.uhf.UhfCmd;
import com.fanfull.libhard.uhf.UhfController;
import com.fanfull.libjava.io.netty.ClientNetty;
import com.fanfull.libjava.io.netty.handler.ReconnectBeatHandler;
import com.fanfull.libjava.io.socketClient.Options;
import com.fanfull.libjava.util.BytesUtil;
import com.fanfull.libjava.util.ClockUtil;
import com.fanfull.libjava.util.Crc8Util;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.InputConfirmPopupView;

import org.orsoul.baselib.util.SoundHelper;
import org.orsoul.baselib.util.ViewUtil;
import org.orsoul.baselib.view.FullScreenPopupSetIp;
import org.orsoul.baselib.view.MyInputPopupView;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

public class ZcLockActivity extends InitModuleActivity {

  private TextView tvShow;
  private Button btnConnect;
  private Button btnSetIp;

  private ClientNetty clientNetty;
  private String serverIp = "192.168.11.246";
  private int serverPort = 23456;

  private MyReadWriteTask readWriteTask;

  @Override protected void initView() {
    setContentView(R.layout.activity_zc);
    tvShow = findViewById(R.id.tv_netty_show);
    tvShow.setMovementMethod(ScrollingMovementMethod.getInstance());

    btnConnect = findViewById(R.id.btn_netty_connect);
    btnSetIp = findViewById(R.id.btn_netty_setIp);

    tvShow.setOnClickListener(this);
    btnConnect.setOnClickListener(this);
    btnSetIp.setOnClickListener(this);

    findViewById(R.id.btn_netty_setLog).setOnClickListener(this);
    findViewById(R.id.btn_netty_read_log).setOnClickListener(this);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    initModule(true, true);

    ViewUtil.appendShow(tvShow,
        "0 -> ????????????\n"
            + "1 -> ??????\n"
            + "2 -> ??????\n"
            + "3 -> ??????\n"
            + "4 -> ??????-??????\n"
            + "5 -> ??????-??????\n"
            + "6 -> ??????\n"
            + "7 -> ??????\n"
            + "8 -> ????????????\n"
            + "9 -> ??????Psam\n"
            + ". -> ????????????\n"
    );

    serverIp = MyPreference.SERVER_IP1.getValue(serverIp);

    serverPort = MyPreference.SERVER_PORT1.getValue(serverPort);
    initTcp();

    handlerApdu("reset");
    onKeyDown(KeyEvent.KEYCODE_0, null);

    readWriteTask = new MyReadWriteTask(uhfController);
    readWriteTask.setReadUse(true);
    readWriteTask.setUseSa(0x48);
    readWriteTask.setUseLen(112);
  }

  @Override public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_netty_connect:
        if (clientNetty.isConnected()) {
          clientNetty.disconnect();
        } else {
          showLoadingView("????????????...");
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
      case R.id.btn_netty_setLog:
        showInputLog();
        break;
      case R.id.btn_netty_read_log:
        byte[] buff = new byte[128];
        boolean b = uhfController.readUse(0, buff);
        StringBuilder sb = new StringBuilder();
        if (b) {
          sb.append("?????????????????????\n");
          for (int i = 0; i < buff.length; i += 16) {
            sb.append((i / 16 + 1) + ":")
                .append(BytesUtil.bytes2HexString(buff, i, i + 16))
                .append("\n");
          }
          SoundHelper.playToneSuccess();
        } else {
          sb.append("??????????????? ???????????????");
          SoundHelper.playToneFailed();
        }
        ViewUtil.appendShow(tvShow, sb.toString());
        break;
    }
  }

  private void showSetIp() {
    FullScreenPopupSetIp.showIpPortSetting(this, serverIp, serverPort,
        (ip, port, settingNoChange) -> {
          if (settingNoChange) {
            ToastUtils.showShort("???????????????");
            return true;
          }
          serverIp = ip;
          serverPort = port;
          clientNetty.setIpPort(ip, port);
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
      case KeyEvent.KEYCODE_ENTER:
        readWriteTask.setOnlyRead(true);
        break;
      case KeyEvent.KEYCODE_0:
        apduCmd = PSamCmd.getCmdVerifyUser();
        break;
      case KeyEvent.KEYCODE_1:
        readWriteTask.setOnlyRead(false);
        readWriteTask.setType(PSamCmd.CMD_ELS_TYPE_ACTIVE);
        break;
      case KeyEvent.KEYCODE_2:
        readWriteTask.setOnlyRead(false);
        readWriteTask.setType(PSamCmd.CMD_ELS_TYPE_CLOSE);
        break;
      case KeyEvent.KEYCODE_3:
        readWriteTask.setOnlyRead(false);
        readWriteTask.setType(PSamCmd.CMD_ELS_TYPE_OPEN);
        break;
      case KeyEvent.KEYCODE_4:
        readWriteTask.setOnlyRead(false);
        readWriteTask.setType(PSamCmd.CMD_ELS_TYPE_CLOSE_WRITE);
        break;
      case KeyEvent.KEYCODE_5:
        readWriteTask.setOnlyRead(false);
        readWriteTask.setType(PSamCmd.CMD_ELS_TYPE_OPEN_WRITE);
        break;
      case KeyEvent.KEYCODE_6:
        readWriteTask.setOnlyRead(false);
        readWriteTask.setType(PSamCmd.CMD_ELS_TYPE_READ_LOG);
        break;
      case KeyEvent.KEYCODE_7:
        readWriteTask.setOnlyRead(false);
        readWriteTask.setType(PSamCmd.CMD_ELS_TYPE_RECOVERY);
        break;
      case KeyEvent.KEYCODE_8:
        readWriteTask.setOnlyRead(false);
        readWriteTask.setType(PSamCmd.CMD_ELS_TYPE_CLEAR);
        break;
      case KeyEvent.KEYCODE_9:
        handlerApdu("reset");
        return true;
      default:
        return super.onKeyDown(keyCode, event);
    }
    if (apduCmd != null) {
      LogUtils.d("sendCmd:%s", BytesUtil.bytes2HexString(apduCmd));
      byte[] recCmd = rfidController.send2PSam(apduCmd);
      LogUtils.d("recCmd:%s", BytesUtil.bytes2HexString(recCmd));
      if (APDUParser.checkReply(recCmd)) {
        ViewUtil.appendShow("??????????????????", tvShow);
      } else {
        ViewUtil.appendShow("??????????????????", tvShow);
      }
    } else {
      boolean b = readWriteTask.startThread();
      if (!b) {
        readWriteTask.stopThread();
      }
    }
    return true;
  }

  public void initTcp() {
    Options options = new Options();
    //sOptions.serverIp = "192.168.11.246";
    options.serverIp = serverIp;
    options.serverPort = serverPort;
    options.reconnectEnable = false;
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

  private String handlerWrite(String cmd) {
    String[] split = cmd.split(" ");
    if (split.length < 3) {
      return "??????????????????";
    }
    byte[] pwd = BytesUtil.hexString2Bytes(split[1]);
    byte[] data = BytesUtil.hexString2Bytes(split[2]);
    if (pwd == null || data == null) {
      return "??????????????????";
    }
    byte[] head = new byte[2];
    head[0] = 0x77;
    head[1] = (byte) data.length;
    byte[] write = BytesUtil.concatArray(head, data);
    boolean success = false;
    for (int i = 0; i < 5; i++) {
      //success = uhfController.writeUse(0x4C, write);
      success =
          uhfController.write(UhfCmd.MB_USE, 0x4C, write, 500, UhfCmd.MB_USE, 0, null, pwd);
      if (success) {
        break;
      }
    }
    if (success) {
      return "??????USE??????";
    }
    return "??????USE??????";
  }

  private void handlerApdu(String cmd) {
    LogUtils.d("handlerApdu:%s", cmd);
    if (cmd == null) {
      return;
    }

    if (cmd.startsWith("w ") || cmd.startsWith("r ")) {
      String s = SocketActivity.handlerCmd(cmd);
      runOnUiThread(() -> {
        ViewUtil.appendShow(s, tvShow);
      });
      clientNetty.send(s);
      return;
    }
    String[] sendInfo = new String[1];
    if (cmd.equalsIgnoreCase("reset")) {
      boolean resetPSam = rfidController.resetPSam();
      if (resetPSam) {
        sendInfo[0] = "??????Psam??????";
      } else {
        sendInfo[0] = "??????Psam???????????????";
      }
      runOnUiThread(() -> {
        ViewUtil.appendShow(sendInfo[0], tvShow);
      });
      clientNetty.send(sendInfo[0]);
      return;
    } else if (cmd.startsWith("wu ")) {
      sendInfo[0] = handlerWrite(cmd);
    }
    if (sendInfo[0] != null) {
      runOnUiThread(() -> {
        ViewUtil.appendShow(sendInfo[0], tvShow);
      });
      clientNetty.send(sendInfo[0]);
      return;
    }

    String[] split = cmd.split(" ");
    if (split.length < 4 || 7 < split.length) {
      clientNetty.send("???apdu???" + cmd);
      return;
    }

    byte[] data = null;
    byte[] apduCmd = null;
    int cla;
    int ins;
    int p1 = 0;
    int p2;
    try {
      cla = Integer.parseInt(split[0], 16);
      ins = Integer.parseInt(split[1], 16);
      p1 = Integer.parseInt(split[2], 16);
      p2 = Integer.parseInt(split[3], 16);
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
      byte[] buff = new byte[256];
      LogUtils.v("sendCmd:%s", BytesUtil.bytes2HexString(apduCmd));
      LogUtils.d("sendCmd:%s", APDUParser.cmd2String(apduCmd));
      int len = rfidController.send2PSam(apduCmd, buff, false);
      sendInfo[0] = BytesUtil.bytes2HexString(buff, len);
      sendInfo[0] = String.format("rec %s:%s", len, sendInfo[0]);
      LogUtils.i(sendInfo[0]);
      clientNetty.send(sendInfo[0]);

      if (split[1].equals("30") && APDUParser.checkReply(buff, len)) {
        byte[] key8;
        if (p1 == 1) {
          key8 = BytesUtil.hexString2Bytes("91C537C8AA0B2018");
        } else {
          key8 = BytesUtil.hexString2Bytes("B4F208F6752F89DA");
        }
        //byte[] factor8 = Arrays.copyOfRange(data, 1, 9);
        byte[] factor8 = SecurityUtil.genRandom(Arrays.copyOfRange(data, 1, 13));
        byte[] recCmd = Arrays.copyOf(buff, len - 2);
        byte[] tk1 = null;
        tk1 = SecurityUtil.tk1(key8, factor8, recCmd, false);
        String format = String.format("??????????????????" +
                "\n??????:%s\n????????????:%s\n????????????:%s\n????????????:%s,%02X = crc",
            BytesUtil.bytes2HexString(key8),
            BytesUtil.bytes2HexString(factor8),
            BytesUtil.bytes2HexString(recCmd),
            BytesUtil.bytes2HexString(tk1),
            Crc8Util.crc8(0x8D, 0, 0, tk1, 0, tk1.length - 1));
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
      // ??????
      Options options = clientNetty.getOptions();
      pipeline.addLast(new IdleStateHandler(0,
          options.heartBeatInterval,
          0));

      // ???????????????
      pipeline.addLast(new MyReconnectBeatHandler(clientNetty));

      // ???????????????
      //pipeline.addLast(new HeadEndDecoder(true));
      // ????????????????????????????????? ????????? ?????????
      //pipeline.addLast(new StringDecoder(Charset.forName("utf-8")));
      pipeline.addLast(new StringDecoder(StandardCharsets.UTF_8));

      pipeline.addLast(new SimpleChannelInboundHandler<String>() {
        @Override protected void channelRead0(ChannelHandlerContext ctx, String recStr)
            throws Exception {
          handlerApdu(recStr);
        }
      });

      // ???????????????????????????????????? ??????????????????
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
      LogUtils.wtf("??????????????????");
      ToastUtils.showShort("??????????????????");
      //clientNetty.disconnect();
      clientNetty.getOptions().disconnectCount *= 1.5;
    }

    @Override public void channelActive(ChannelHandlerContext ctx) throws Exception {
      final Options opt = clientNetty.getOptions();
      LogUtils.wtf("???????????? %s", opt);
      String format = String.format("???????????? %s:%s", opt.serverIp, opt.serverPort);
      ToastUtils.showShort(format);
      runOnUiThread(() -> {
        ViewUtil.appendShow(format, tvShow);
        btnConnect.setText("??????");
        dismissLoadingView();
      });
    }

    @Override public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
      if (isConnectFailed) {
        final Options opt = clientNetty.getOptions();
        String format = String.format("?????????????????? %s:%s", opt.serverIp, opt.serverPort);
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
      //ToastUtils.showLong("?????????????????? %s:%s", opt.serverIp, opt.serverPort);
      final Options opt = clientNetty.getOptions();
      String format = String.format("???????????? %s:%s", opt.serverIp, opt.serverPort);
      LogUtils.wtf(format);
      ToastUtils.showShort(format);
      runOnUiThread(() -> {
        ViewUtil.appendShow(format, tvShow);
        btnConnect.setText("??????");
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

  int log = 12;

  private void showInputLog() {
    InputConfirmPopupView popupView = new MyInputPopupView(this, 0);
    popupView.setTitleContent("??????", "?????????????????????????????????????????????", "hint");
    popupView.inputContent = "" + log;
    popupView.setListener(text -> {
      log = Integer.parseInt(text);
    }, null);
    new XPopup.Builder(this)
        //.isDarkTheme(true)
        .hasShadowBg(true)
        .autoOpenSoftInput(false)
        .asCustom(popupView)
        .show();
  }

  class MyReadWriteTask extends UhfReadTask {
    int type;
    boolean onlyRead;

    public MyReadWriteTask(UhfController uhfController) {
      super(uhfController);
    }

    public void setType(int type) {
      this.type = type;
    }

    public void setOnlyRead(boolean onlyRead) {
      this.onlyRead = onlyRead;
    }

    private void handler(byte[] epcBuff, byte[] tidBuff) {
      byte[] recData = null;
      byte[] els;
      //els = BytesUtil.hexString2Bytes("4E201000000120000002001122443000300000000000");
      try {
        switch (type) {
          case PSamCmd.CMD_ELS_TYPE_ACTIVE:
            //??????????????????????????? 80 30 xx 00 lc lv-epc-lv-data le
            //80 30 01 00 1C 0C000F4631100000000000800F0E0100000000000000000000000000 00
            els = new byte[14];
            els[0] = 0x01; // ??????
            recData = PsamHelper.sendGenElsCmd(PSamCmd.CMD_ELS_TYPE_ACTIVE, epcBuff, els);
            break;
          case PSamCmd.CMD_ELS_TYPE_CLOSE:
          case PSamCmd.CMD_ELS_TYPE_OPEN:
          case PSamCmd.CMD_ELS_TYPE_CLOSE_WRITE:
          case PSamCmd.CMD_ELS_TYPE_OPEN_WRITE:
          case PSamCmd.CMD_ELS_TYPE_RECOVERY:
            //els = BytesUtil.hexString2Bytes("4E201000000120000002001122443000300000000000");
            els = PSamCmd.getElsData(2_0000, 0xAAAAAAAA, 0xBBBBBBBB, 0xCCCCCCCC);
            recData = PsamHelper.sendGenElsCmd(type, epcBuff, els);
            break;
          case PSamCmd.CMD_ELS_TYPE_READ_LOG:
            //???????????? 2 byte
            //???????????? 1 byte
            //???????????? 1 byte 0- ?????? ??? 0 ?????????

            recData = PsamHelper.sendReadLog(epcBuff, log / 10, log % 10, true);
            break;
          case PSamCmd.CMD_ELS_TYPE_CLEAR:
            els = PSamCmd.getElsData(0xAAAAAAAA, 0xBBBBBBBB, 0xCCCCCCCC);
            recData = PsamHelper.sendGenElsCmd(type, epcBuff, els);
            break;
        }
      } catch (Exception e) {
        LogUtils.wtf("%s", e.getMessage());
        e.printStackTrace();
      }

      byte[] pwd = null;
      if (recData == null) {
        //runOnUiThread(() -> {
        //  dismissLoadingView();
        //  showDialog("????????????????????????");
        //});
        //return true;
        //ToastUtils.showShort("?????????????????????????????????????????????");
        runOnUiThread(() -> {
          String info;
          info = "????????????????????????";
          SoundHelper.playToneFailed();
          showDialog(info);
          ViewUtil.appendShow(tvShow, info);
        });
        //recData = BytesUtil.hexString2Bytes("9E182D1F663763D254FB868608FD72DF");
      } else {
        pwd = PsamHelper.sendGetPwd(epcBuff);
        LogUtils.d("pwd:%s", BytesUtil.bytes2HexString(pwd));
      }

      byte[] toWrite = new byte[]{0x66, (byte) recData.length};
      toWrite = BytesUtil.concatArray(toWrite, recData);
      LogUtils.d("write:%s", BytesUtil.bytes2HexString(toWrite));

      boolean write = false;
      for (int i = 0; i < TRY_TIMES; i++) {
        write = uhfController.write(
            UhfCmd.MB_USE, 0x4C, toWrite, 0, UhfCmd.MB_TID, 0x00, tidBuff, pwd);
        if (write) {
          break;
        }
      }

      boolean finalWrite = write;
      runOnUiThread(() -> {
        String info;
        if (finalWrite) {
          info = "????????????????????????";
          SoundHelper.playToneSuccess();
          ToastUtils.showShort(info);
        } else {
          info = "???????????????????????????";
          SoundHelper.playToneFailed();
          showDialog(info);
        }
      });
    }

    @Override protected boolean onScanSuccess(UhfReadBean bean) {
      LogUtils.wtf("scanUhf:%s", bean);
      byte[] epcBuff = bean.getEpcBuff();
      byte[] tidBuff = bean.getTidBuff();
      byte[] useBuff = bean.getUseBuff();

      boolean verifyEpc = PsamHelper.sendVerifyEpc(epcBuff, tidBuff);

      LockZcBean zcBean = LockZcBean.parse(epcBuff, useBuff);
      LogUtils.wtf("%s", zcBean);

      //int epcState = epcBuff[11] & 0xFF;
      //int useLabel = useBuff[8] & 0xFF;
      //int useElsLen = useBuff[9] & 0xFF;
      //String info = String.format("EPC???%s\nEPC?????????%02X\nUSE?????????%s",
      //    BytesUtil.bytes2HexString(epcBuff), epcState, useLabel);
      String info;
      if (zcBean == null) {
        info = "????????????????????????";
      } else {
        info = String.format("EPC???%s\nEPC?????????%s,EPC?????????%02X???USE?????????%s\n%s",
            BytesUtil.bytes2HexString(epcBuff), verifyEpc ? "??????" : "??????", zcBean.getStatusEpc(),
            zcBean.getStatusUse(),
            zcBean);
      }
      LogUtils.wtf("?????????%s", info);
      String finalInfo = info;
      runOnUiThread(new Runnable() {
                      @Override public void run() {
                        ViewUtil.appendShow(tvShow, finalInfo);
                      }
                    }
      );

      if (zcBean == null) {
        return true;
      }

      if (!onlyRead) {
        handler(epcBuff, tidBuff);
      } else if (type == PSamCmd.CMD_ELS_TYPE_READ_LOG) {
        //byte[] elsEncrypt = Arrays.copyOfRange(useBuff, 10, useElsLen);
        byte[] res = PsamHelper.sendDecryptEls(epcBuff, zcBean.getCmd());
        LogUtils.d("%s", BytesUtil.bytes2HexString(res));
        String msg;
        if (res != null) {
          List<LockZcBean.CmdBean> logList = LockZcBean.parse(res);
          msg = String.format("??????????????????%s:%s", logList.size(), logList);
        } else {
          msg = String.format("??????????????????");
        }
        LogUtils.wtf("%s", msg);
        runOnUiThread(new Runnable() {
          @Override public void run() {
            ViewUtil.appendShow(tvShow, msg);
          }
        });
      }
      return true;
    }

    private final int TRY_TIMES = 5;
    private int failedTimes;

    @Override protected boolean onScanFailed(int errorCode) {
      long goingTime = getGoingTime();
      long runTime = getRunTime();
      runOnUiThread(() -> {
        switch (errorCode) {
          case 1:
            showLoadingView("????????? tid, " + goingTime / 1000);
            break;
          case 2:
            showLoadingView("????????? epc " + goingTime / 1000);
            break;
          case 3:
            showLoadingView("????????? use " + goingTime / 1000);
            break;
        }
      });
      return false;
    }

    @Override protected void onTimeout(long runTime, int total) {
      super.onTimeout(runTime, total);
      ToastUtils.showLong("???????????????????????????");
      //runOnUiThread(() -> {
      //  dismissLoadingView();
      //  showDialog("???????????????????????????");
      //});
    }

    @Override protected void onTaskBefore() {
      super.onTaskBefore();
      runOnUiThread(() -> {
        showLoadingView("??????????????????...");
      });
    }

    @Override protected void onTaskFinish() {
      super.onTaskFinish();
      runOnUiThread(() -> {
        dismissLoadingView();
      });
    }
  }
}