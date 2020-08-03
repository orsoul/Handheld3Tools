package com.fanfull.socket;

import android.text.TextUtils;
import com.apkfuns.logutils.LogUtils;
import com.fanfull.contexts.MyContexts;
import com.fanfull.contexts.StaticString;
import com.fanfull.factory.ThreadPoolFactory;
import com.fanfull.utils.ArrayUtils;
import com.fanfull.utils.SPUtils;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import org.orsoul.baselib.util.ClockUtil;
import org.orsoul.baselib.util.ThreadUtil;

/**
 * @author Keung
 * @ClassName: SocketConnet
 * @Description: socket连接
 * @date 2014-8-15 上午09:12:56
 */
public class SocketConnet implements Runnable {
  private final static String TAG = SocketConnet.class.getSimpleName();

  private DataOutputStream dout = null;
  private InputStream in = null;
  private OutputStream out = null;
  private Socket sSocket = null;

  private int count = 0;

  private SendTask mSendTask = null;
  private int communication_id = 0;

  private int mConnNum = -1;

  private Thread recieveThread;
  private static final SocketConnet sSocketConnet = new SocketConnet();

  private SocketConnet() {

  }

  public static SocketConnet getInstance() {

    return sSocketConnet;
  }

  public boolean isConnect() {
    return -1 != mConnNum;
  }

  private TimeoutTask timeoutTask = new TimeoutTask();
  private ReceiveListener recListener;
  private ReceiveListener onceListener;

  public void setReceiveListener(ReceiveListener recListener) {
    this.recListener = recListener;
  }

  /**
   * @return 返回 当前 连接 的天线系统, 0 == 1号门, 1 == 2号门, 2 == 3号门, -1 == 未创建连接
   */
  public int getConnectedDoorNum() {
    return mConnNum;
  }

  /**
   * @Description: socket通信.在此方法中创建socket, 并开启一个子线程线程接收 服务器 的发来的 信息
   */
  public boolean connect(int timeout) {
    if (isConnect()) {
      close();
    }
    try {
      sSocket = new Socket();
      String ip = SPUtils.getString(MyContexts.Key_IP1, StaticString.IP);
      int port = SPUtils.getInt(MyContexts.KEY_PORT1, StaticString.PORT);
      LogUtils.tag(TAG).i("start connect: %s:%s", ip, port);
      sSocket.connect(new InetSocketAddress(ip, port), timeout);
      out = sSocket.getOutputStream();
      in = sSocket.getInputStream();
      mConnNum = 1;
      //dout = new DataOutputStream(out);
      //mSendTask = new SendTask(dout);
      recieveThread = new Thread(this);
      recieveThread.start();
      if (recListener != null) {
        recListener.onConnect(ip, port);
      }
      return true;
    } catch (IOException e) {
      LogUtils.tag(TAG).w("%s", e.getMessage());
      e.printStackTrace();
      if (recListener != null) {
        recListener.onConnectFailed(e);
      }
      close();
      return false;
    }
  }

  public boolean send(byte[] dataBuff) {
    if (dataBuff == null || dataBuff.length == 0 || !isConnect()) {
      return false;
    }
    try {
      out.write(dataBuff);
      LogUtils.tag(TAG).d("send:%s", ArrayUtils.bytesToHexString(dataBuff));
      return true;
    } catch (IOException e) {
      LogUtils.tag(TAG).w("%s", e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  public boolean send(String data) {
    if (TextUtils.isEmpty(data)) {
      return true;
    }
    return send(data.getBytes());
  }

  public boolean send(String cmd, ReceiveListener listener, int timeout) {
    LogUtils.tag(TAG).d("set:%s, old:%s", listener, onceListener);
    if (onceListener != null) {
      return false;
    }

    boolean send = send(cmd);
    if (!send) {
      return false;
    }
    if (listener != null) {
      onceListener = listener;
      timeoutTask.startClock(timeout);
    }
    return true;
  }

  public boolean send(String cmd, ReceiveListener listener) {
    return send(cmd, listener, 4000);
  }

  public boolean printBagId(String bagId, ReceiveListener listener) {
    return send(getCmd("ib", bagId, ++count), listener);
  }

  public boolean printBagIdRep(String bagId, ReceiveListener listener) {
    return send(getCmd("rp", bagId, ++count), listener);
  }

  public String getCmd(Object... args) {
    if (args == null || args.length == 0) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    sb.append("$");
    for (int i = 0; i < args.length; i++) {
      if (args[i] != null) {
        sb.append(args[i]).append(' ');
      }
    }
    sb.setLength(sb.length() - 1);
    sb.append("#");
    LogUtils.tag(TAG).v("getCmd:%s", sb);
    return sb.toString();
  }

  /**
   * @description: socket通信, 在新线程中 向服务器发送数据
   */
  public void communication(int taskid) {
    if (null == mSendTask) {
      return;
    }
    communication_id = taskid;
    count++;
    mSendTask.setProperty(taskid, getIntString(count));// 设置S的值
    StaticString.information = null;
    ThreadPoolFactory.getNormalPool().execute(mSendTask);
  }

  public void communication() {
    communication(communication_id);
  }

  /**
   * @return void 返回类型
   * @throws
   * @Description: 关闭网络连接
   */
  public void close() {
    mConnNum = -1;
    try {
      if (null != recieveThread) {
        recieveThread.interrupt();
      }
      if (in != null) {
        in.close();
      }
      if (dout != null) {
        dout.close();
      }
      if (sSocket != null) {
        sSocket.close();
      }
    } catch (IOException e) {
      LogUtils.e("socket close exception");
      e.printStackTrace();
    } finally {
      try {
        if (in != null) {
          in.close();
        }
        if (dout != null) {
          dout.close();
        }
        if (sSocket != null) {
          sSocket.close();
        }
      } catch (IOException e) {
      }
    }
    LogUtils.tag(TAG).i("socket close");
  }

  /**
   * @return 返回 num 的字符串形式:num不满3位的,在前面补0;num为负数,原样返回
   */
  public String getIntString(int num) {
    String reVal = null;
    reVal = String.valueOf(num);
    if (num < 0) {
      return reVal;
    }

    switch (reVal.length()) {
      case 1:
        reVal = "00" + reVal;
        break;
      case 2:
        reVal = "0" + reVal;
        break;
      default:
        break;
    }

    // if (num >= 0 && num <= 9) {
    // reVal = "00" + String.valueOf(num);
    // } else if (num > 9 && num <= 99) {
    // reVal = "0" + String.valueOf(num);
    // } else if (num > 99 && num <= 999) {
    // reVal = String.valueOf(num);
    // } else {
    // reVal = String.valueOf(num);
    // }
    return reVal;
  }

  @Override public void run() {
    LogUtils.tag(TAG).i("receiveThread run");
    try {
      byte[] data = new byte[1024 << 4];
      int len;
      while (isConnect() && 0 < (len = in.read(data))) {
        //LogUtils.tag(TAG).d(":%s",ArrayUtils.bytesToHexString(data, 0, len));
        String str = new String(data, 0, len);
        StaticString.information = str;
        LogUtils.tag(TAG).i("receive: %s", str);
        if (onceListener != null) {
          LogUtils.tag(TAG).d("onceListener: %s", onceListener);
          onceListener.onReceive(str);
          timeoutTask.stopClock();
        }
        if (null != recListener) {
          LogUtils.tag(TAG).d("recListener: %s", recListener);
          recListener.onReceive(str);
        }
      } // end while()
    } catch (Exception e) {
      LogUtils.tag(TAG).w(":%s", e.getMessage());
      e.printStackTrace();
      if (null != recListener) {
        recListener.onDisconnect();
      }
    }
    LogUtils.tag(TAG).i("receiveThread end");
  }

  private class TimeoutTask implements Runnable {
    private static final int minTimeout = 500;
    private static final int maxTimeout = 30_000;
    private int timeout;

    public void startClock(int timeout) {
      if (timeout < minTimeout) {
        timeout = minTimeout;
      } else if (maxTimeout < timeout) {
        timeout = maxTimeout;
      }
      this.timeout = timeout;
      ThreadUtil.execute(this);
    }

    public void stopClock() {
      synchronized (this) {
        onceListener = null;
        this.notifyAll();
      }
    }

    @Override public void run() {
      synchronized (this) {
        try {
          LogUtils.tag(TAG).d("click1:%s", ClockUtil.clock());
          this.wait(timeout);
          if (onceListener != null) {
            LogUtils.tag(TAG).d("timeout:%s", onceListener);
            onceListener.onReceiveTimeout();
          }
        } catch (InterruptedException e) {
          LogUtils.tag(TAG).i("InterruptedException");
        }
        LogUtils.tag(TAG).d("click2:%s", ClockUtil.clock());
        onceListener = null;
      }
    }
  }

  public interface ReceiveListener {
    default void onConnect(String ip, int port) {
      LogUtils.tag(TAG).i("onConnect: %s:%s", ip, port);
    }

    default void onConnectFailed(Exception e) {
      LogUtils.tag(TAG).i("onConnectFailed: %s", e.getMessage());
    }

    default void onDisconnect() {
    }

    default void onReceive(byte[] data) {
    }

    default void onReceive(String recString) {
    }

    default void onReceiveTimeout() {
    }
  }
}
