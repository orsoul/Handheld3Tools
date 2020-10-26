package com.fanfull.handheldtools.ui;

import android.os.Bundle;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.apkfuns.logutils.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.fanfull.handheldtools.R;
import com.fanfull.handheldtools.base.InitModuleActivity;
import com.fanfull.libhard.uhf.IUhfListener;
import com.fanfull.libhard.uhf.UhfCmd;
import com.fanfull.libhard.uhf.UhfController;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnInputConfirmListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Random;
import org.orsoul.baselib.util.BytesUtil;
import org.orsoul.baselib.util.ClockUtil;
import org.orsoul.baselib.util.ViewUtil;

public class UhfActivity extends InitModuleActivity {

  private TextView tvShow;
  private Button btnReadEpc;
  private Button btnReadTid;
  private Button btnReadUse;
  private Button btnGetPower;

  private UhfController uhfController;
  private SocketServiceDemo socketService;

  private byte[] readBuff;
  private boolean fastIdOn;
  private boolean readingLot;
  private int readLotCount;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void initView() {
    setContentView(R.layout.activity_uhf);
    tvShow = findViewById(R.id.tv_barcode_show);
    tvShow.setMovementMethod(ScrollingMovementMethod.getInstance());
    btnReadEpc = findViewById(R.id.btn_uhf_read_epc);
    btnReadTid = findViewById(R.id.btn_uhf_read_tid);
    btnReadUse = findViewById(R.id.btn_uhf_read_use);
    btnGetPower = findViewById(R.id.btn_uhf_get_power);

    tvShow.setOnClickListener(this);
    btnReadEpc.setOnClickListener(this);
    btnReadTid.setOnClickListener(this);
    btnReadUse.setOnClickListener(this);
    btnGetPower.setOnClickListener(this);

    btnReadEpc.setEnabled(false);
    btnReadTid.setEnabled(false);
    btnReadUse.setEnabled(false);
    btnGetPower.setEnabled(false);

    ViewUtil.requestFocus(tvShow);
  }

  @Override
  protected void initModule() {
    uhfController = UhfController.getInstance();
    uhfController.setListener(new IUhfListener() {
      @Override public void onOpen(boolean openSuccess) {
        runOnUi(() -> {
          dismissLoadingView();
          if (!openSuccess) {
            tvShow.setText("初始化失败");
            return;
          }
          tvShow.setText("打开成功.\n"
              + "3连接击->清空\n"
              + "Enter->开始/停止 连续扫描\n"
              + "按键1->随机写EPC 12byte\n"
              + "按键2->随机写USE 32byte\n"
              + "按键4->读TID 12byte\n"
              + "按键5->读EPC 12byte\n"
              + "按键6->读USE 32byte\n"
              + "按键7->设置功率\n"
              + "按键8->开启 EPC、TID同读\n"
              + "按键9->关闭 EPC、TID同读\n\n");
          btnReadEpc.setEnabled(true);
          btnReadTid.setEnabled(true);
          btnReadUse.setEnabled(true);
          btnGetPower.setEnabled(true);
        });
        uhfController.send(UhfCmd.CMD_GET_DEVICE_VERSION);
        SystemClock.sleep(50);
        uhfController.send(UhfCmd.CMD_GET_DEVICE_ID);
        SystemClock.sleep(50);
        uhfController.send(UhfCmd.CMD_GET_FAST_ID);
        socketService = new SocketServiceDemo();
        socketService.start();
      }

      @Override
      public void onReceiveData(byte[] data) {
        byte[] parseData = UhfCmd.parseData(data);
        if (parseData == null) {
          LogUtils.i("parseData failed:%s", BytesUtil.bytes2HexString(data));
          return;
        }
        int cmdType = data[4] & 0xFF;
        Object info = null;
        switch (cmdType) {
          case UhfCmd.RECEIVE_TYPE_GET_DEVICE_VERSION:
            info = String.format("设备版本：v%s.%s.%s", parseData[0], parseData[1], parseData[2]);
            break;
          case UhfCmd.RECEIVE_TYPE_GET_DEVICE_ID:
            info = String.format("设备Id：%s", BytesUtil.bytes2HexString(parseData));
            break;
          case UhfCmd.RECEIVE_TYPE_GET_FAST_ID:
            if (parseData[0] == 1) {
              info = "EPC、TID同时读取 开启";
            } else {
              info = "EPC、TID同时读取 关闭";
            }
            break;
          case UhfCmd.RECEIVE_TYPE_SET_FAST_ID:
            // 1:成功， 0：失败
            if (parseData[0] == 1) {
              if (fastIdOn) {
                info = "EPC、TID同读设为 开启";
              } else {
                info = "EPC、TID同读设为 关闭";
              }
            } else {
              info = "设置 EPC、TID同读 失败";
            }
            break;
          case UhfCmd.RECEIVE_TYPE_READ_LOT:
            readLotCount++;
            if (parseData.length == 24) {
              info = String.format("%s epc:%s\n%s tid:%s",
                  readLotCount, BytesUtil.bytes2HexString(parseData, 0, 12),
                  readLotCount, BytesUtil.bytes2HexString(parseData, 12, parseData.length));
            } else if (parseData.length == 1) {
              // 续卡停止
              readingLot = false;
              info = "连续续卡停止";
            } else {
              info =
                  String.format("%s epc:%s", readLotCount, BytesUtil.bytes2HexString(parseData));
            }
            break;
          case UhfCmd.RECEIVE_TYPE_READ:
            info =
                String.format("read:%s", BytesUtil.bytes2HexString(parseData));
            break;
          case UhfCmd.RECEIVE_TYPE_WRITE:
            if (parseData.length == 0) {
              info = "写成功";
            } else {
              info = String.format("写失败,cause:%X", parseData[0]);
            }
            break;
          default:
            LogUtils.v("parseData:%s", BytesUtil.bytes2HexString(parseData));
        }
        if (info != null) {
          Object obj = info;
          runOnUiThread(() -> ViewUtil.appendShow(obj, tvShow));
          if (socketService != null) {
            socketService.send(String.valueOf(obj));
          }
        }
      }
    });
    showLoadingView("正在打开超高频读头...");
    uhfController.open();
  }

  @Override
  public void onClick(View v) {
    v.setEnabled(false);
    Object info;
    switch (v.getId()) {
      case R.id.btn_uhf_read_epc:
        readBuff = uhfController.fastEpc(500);
        if (readBuff == null) {
          info = "读epc失败";
        } else if (readBuff.length == 24) {
          info = String.format("epc:%s\ntid:%s",
              BytesUtil.bytes2HexString(readBuff, 0, 12),
              BytesUtil.bytes2HexString(readBuff, 12, readBuff.length));
        } else {
          info = String.format("epc:%s", BytesUtil.bytes2HexString(readBuff));
        }
        ViewUtil.appendShow(info, tvShow);
        break;
      case R.id.btn_uhf_read_tid:
        readBuff = uhfController.fastTid(0, 12);
        if (readBuff == null) {
          info = "读tid失败";
        } else {
          info = String.format("tid:%s", BytesUtil.bytes2HexString(readBuff));
        }
        ViewUtil.appendShow(info, tvShow);
        break;
      case R.id.btn_uhf_read_use:
        readBuff = uhfController.readUse(0x4, 32);
        if (readBuff == null) {
          info = "读use失败";
        } else {
          info = String.format("use:%s", BytesUtil.bytes2HexString(readBuff));
        }
        ViewUtil.appendShow(info, tvShow);
        break;
      case R.id.btn_uhf_get_power:
        readBuff = uhfController.getPower();
        if (readBuff == null) {
          info = "获取功率失败";
        } else {
          info = String.format("读/写功率: %s/%s | 天线号:%s | %s",
              readBuff[0], readBuff[1], readBuff[2], readBuff[3] == 0 ? "开环" : "闭环");
        }
        ViewUtil.appendShow(info, tvShow);
        break;
      case R.id.tv_barcode_show:
        if (3 == ClockUtil.fastClickTimes()) {
          ViewUtil.appendShow(null, tvShow);
        }
        break;
    }
    v.setEnabled(true);
  }

  @Override
  protected void onEnterPress() {
    //        LogUtils.d("onEnterPress");
    if (readingLot) {
      return;
    }
    if (uhfController.isOpen()) {
      readLotCount = 0;
      readingLot = uhfController.send(UhfCmd.getReadLotCmd(20));
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
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
        byte[] writeBuff = readBuff;
        if (writeBuff == null) {
          writeBuff = new byte[12];
          Arrays.fill(writeBuff, (byte) new Random().nextInt(256));
        }
        uhfController.writeAsync(UhfCmd.MB_EPC, 0x02, writeBuff, null, 0, 0);
        break;
      case KeyEvent.KEYCODE_2:
        writeBuff = readBuff;
        if (writeBuff == null) {
          writeBuff = new byte[32];
          Arrays.fill(writeBuff, (byte) new Random().nextInt(256));
        }
        uhfController.writeAsync(UhfCmd.MB_USE, 0x4, writeBuff, null, 0, 0);
        break;
      case KeyEvent.KEYCODE_3:
        uhfController.readEpc(0x02, 12);
        break;
      case KeyEvent.KEYCODE_4:
        uhfController.send(UhfCmd.getReadCmd(UhfCmd.MB_TID, 0x00, 12));
        break;
      case KeyEvent.KEYCODE_5:
        uhfController.send(UhfCmd.getReadCmd(UhfCmd.MB_EPC, 0x02, 12));
        break;
      case KeyEvent.KEYCODE_6:
        uhfController.send(UhfCmd.getReadCmd(UhfCmd.MB_USE, 0x00, 32));
        break;
      case KeyEvent.KEYCODE_7:
        new XPopup.Builder(this).asInputConfirm(
            "输入功率", "功率范围：5 ~ 25", "读功率.写功率", new OnInputConfirmListener() {
              @Override public void onConfirm(String text) {
                if (text == null) {
                  return;
                }

                if (!text.matches("\\d+\\.\\d+")) {
                  ToastUtils.showShort("输入格式不合法，正确格式：6.12");
                  return;
                }

                String[] split = text.split("\\.");
                int r = Integer.parseInt(split[0]);
                int w = Integer.parseInt(split[1]);
                if (UhfCmd.MAX_POWER < r || r < UhfCmd.MIN_POWER ||
                    UhfCmd.MAX_POWER < w || w < UhfCmd.MIN_POWER) {
                  ToastUtils.showShort("功率超出允许范围");
                  return;
                }

                boolean b = uhfController.setPower(r, w, 0, true, false);
                String res;
                if (b) {
                  res = String.format("设置读/写功率成功：%s / %s", r, w);
                } else {
                  res = "设置功率失败";
                }
                ToastUtils.showShort(res);
                ViewUtil.appendShow(res, tvShow);
              }
            }).show();
        break;
      case KeyEvent.KEYCODE_8:
        //byte[] setPwdCmd = UhfCmd.getSetPwdCmd(0x00000000, null, 0, 0, 0x0FC2A0);
        //byte[] setPwdCmd = UhfCmd.getSetPwdCmd(0x00000000, null, 0, 0, 0b11111100000000000000);
        //byte[] setPwdCmd = UhfCmd.getSetPwdCmd(0x00000000, null, 0, 0, 0B00000000001111111111);
        //byte[] setPwdCmd = UhfCmd.getSetPwdCmd(0x00000000, null, 0, 0, 0B11111111111111111111);
        byte[] setPwdCmd = UhfCmd.getSetPwdCmd(0x00000000, null, 0, 0, 0B11111111110000000000);
        uhfController.send(setPwdCmd);
        //fastIdOn = true;
        //uhfController.send(UhfCmd.getSetFastIdCmd(fastIdOn));
        break;
      case KeyEvent.KEYCODE_9:
        fastIdOn = false;
        uhfController.send(UhfCmd.getSetFastIdCmd(fastIdOn));
        break;
      default:
        break;
    }

    return super.onKeyDown(keyCode, event);
  }

  private void initSocketService() {
  }

  @Override
  protected void onDestroy() {
    if (socketService != null) {
      socketService.closeService();
    }
    uhfController.release();
    super.onDestroy();
  }

  static class SocketServiceDemo extends Thread {
    private ServerSocket serverSocket;
    private Socket client;
    private boolean canRun;

    public void init() {
      try {
        serverSocket = new ServerSocket(12345, 3);
        String format = String.format("server %s run", serverSocket.getLocalSocketAddress());
        LogUtils.d(format);
        client = serverSocket.accept();
        format = String.format("%s connected", client.getInetAddress());
        ToastUtils.showShort(format);
        LogUtils.d(format);
      } catch (IOException e) {
        e.printStackTrace();
        return;
      }
    }

    public void closeService() {
      canRun = false;

      try {
        if (client != null) {
          client.close();
        }
        if (serverSocket != null) {
          serverSocket.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      client = null;
      serverSocket = null;
    }

    public boolean send(Socket client, byte[] data) throws IOException {
      if (client == null || !client.isConnected() || data == null) {
        return false;
      }
      OutputStream out = client.getOutputStream();
      out.write(data);
      out.flush();
      return true;
      //LogUtils.v("socket send:%s", ArrayUtils.bytes2HexString(data));
    }

    public boolean send(String info) {
      try {
        boolean send = send(client, info.getBytes("gbk"));
        LogUtils.d("socket send:%s-%s", send, info);
        return send;
      } catch (Exception e) {
        e.printStackTrace();
      }
      return false;
    }

    @Override
    public void run() {
      try {
        serverSocket = new ServerSocket(12345, 3);
        String format = String.format("server %s run", serverSocket.getLocalSocketAddress());
        LogUtils.d(format);

        byte[] buff = new byte[1024 * 16];
        canRun = true;
        while (canRun) {
          if (client == null) {
            client = serverSocket.accept();
            format = String.format("%s connected", client.getInetAddress());
            ToastUtils.showShort(format);
            LogUtils.d(format);
          }
          InputStream in = client.getInputStream();
          int len = in.read(buff);
          if (len <= 0) {
            format = String.format("%s disconnected", client.getInetAddress());
            ToastUtils.showShort(format);
            LogUtils.d(format);
            client.close();
            client = null;
          } else {
            handlerRec(buff, len);
          }
        }
      } catch (IOException e) {
        closeService();
        e.printStackTrace();
      }
      LogUtils.i("rnd end");
    }

    private void handlerRec(byte[] buff, int len) {
      String rec;
      try {
        rec = new String(buff, 0, len, "gbk");
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
        return;
      }
      String[] s = rec.split(" ");
      if (s.length < 3) {
        return;
      }
      switch (s[0]) {
        case "r":
          int mb = Integer.parseInt(s[1]);
          int sa = Integer.parseInt(s[2]);
          int dl = Integer.parseInt(s[3]);
          UhfController.getInstance().send(UhfCmd.getReadCmd(mb, sa, dl));
          break;
        case "w":
          mb = Integer.parseInt(s[1]);
          sa = Integer.parseInt(s[2]);
          byte[] data = BytesUtil.hexString2Bytes(s[3]);
          UhfController.getInstance().send(UhfCmd.getWriteCmd(mb, sa, data));
          break;
        default:
          LogUtils.d(("Unexpected value: " + rec));
      }
    }
  }
}
