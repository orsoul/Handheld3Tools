package com.fanfull.libhard.serialport.impl;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.libhard.serialport.ISerialPort;
import com.fanfull.libhard.serialport.ISerialPortListener;
import com.fanfull.libjava.util.BytesUtil;
import com.fanfull.libjava.util.ClockUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SerialPortController implements ISerialPort {
  public static final long SWICHT_MODE_WAIT_TIME = 50;
  public static final int WITCH_MODE_UHF = 1;
  public static final int WITCH_MODE_FINGER = 2;
  public static int witchMode = 0;

  protected String TAG = this.getClass().getSimpleName();

  private static Map<String, SerialPortController> sControllerMap;
  private ISerialPort serialPort;
  private SerialPostReadThread readThread;
  private Set<ISerialPortListener> listenerSet;
  private ISerialPortListener onceListener;
  //private byte[] onceRecBuff;
  private int useCount;
  private String key;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public ISerialPortListener getOnceListener() {
    return onceListener;
  }

  public void setOnceListener(ISerialPortListener onceListener) {
    this.onceListener = onceListener;
  }

  public SerialPortController(ISerialPort serialPort) {
    this.serialPort = serialPort;
  }

  public boolean addSerialPortListener(ISerialPortListener serialPortListener) {
    if (serialPortListener == null) {
      return false;
    }
    if (listenerSet == null) {
      listenerSet = new HashSet<>();
    }
    return listenerSet.add(serialPortListener);
  }

  public boolean removeSerialPortListener(ISerialPortListener serialPortListener) {
    if (serialPortListener == null) {
      return false;
    }
    if (listenerSet == null) {
      return false;
    }
    return listenerSet.remove(serialPortListener);
  }

  public synchronized void startReadThread() {
    if (readThread == null || readThread.getState() == Thread.State.TERMINATED) {
      readThread = new SerialPostReadThread();
      readThread.start();
    }
  }

  public synchronized void stopReadThread() {
    if (readThread != null) {
      readThread.stopRead();
      readThread.interrupt();
      readThread = null;
    }
  }

  @Override
  public boolean send(byte[] data, int off, int len) {
    boolean send = serialPort.send(data, off, len);
    LogUtils.tag(TAG).d("send %s:%s", send, BytesUtil.bytes2HexString(data, 0, len));
    return send;
  }

  @Override
  public boolean send(byte[] data) {
    return send(data, 0, data != null ? data.length : 0);
  }

  /**
   * @return 发送失败返回-1，回复超时返回-2
   */
  public int sendAndWaitReceive(byte[] data, long timeout, byte[] recBuff) {
    final int[] reVal = {-2};
    ISerialPortListener listener = new ISerialPortListener() {
      @Override public void onReceiveData(byte[] recData, int len) {
        if (recBuff != null) {
          reVal[0] = Math.min(recBuff.length, len);
          System.arraycopy(recData, 0, recBuff, 0, reVal[0]);
        }
      }
    };
    boolean send = send(data);
    if (!send) {
      return -1;
    }

    synchronized (listener) {
      try {
        ClockUtil.clock();
        onceListener = listener;
        onceListener.wait(timeout);
      } catch (InterruptedException e) {
        LogUtils.tag(TAG).i("InterruptedException");
      }
    }
    onceListener = null;
    LogUtils.tag(TAG).d("wait time: %s / %s, rec len:%s",
        ClockUtil.clock(), timeout, reVal[0]);
    return reVal[0];
  }

  public byte[] sendAndWaitReceive(byte[] data, long timeout) {
    final byte[][] reVal = {null};
    //onceListener = (buff, len) -> onceRecBuff = Arrays.copyOf(buff, len);
    ISerialPortListener listener = new ISerialPortListener() {
      @Override public void onReceiveData(byte[] data, int len) {
        reVal[0] = Arrays.copyOf(data, len);
      }
    };
    boolean send = send(data);
    if (!send) {
      return null;
    }

    synchronized (listener) {
      try {
        ClockUtil.clock();
        //LogUtils.w("listener wait:%s ======", listener.hashCode());
        onceListener = listener;
        onceListener.wait(timeout);
      } catch (InterruptedException e) {
        LogUtils.tag(TAG).i("InterruptedException");
      }
    }
    //LogUtils.w("listener endw:%s", Objects.hashCode(onceListener));
    if (reVal[0] == null) {
      LogUtils.tag(TAG).w("timeout----");
    }
    onceListener = null;
    LogUtils.tag(TAG).d("wait time: %s / %s", ClockUtil.clock(), timeout);
    return reVal[0];
  }

  public byte[] sendAndWaitReceive(byte[] data) {
    return sendAndWaitReceive(data, 500);
  }

  @Override
  public String getSerialPortInfo() {
    return serialPort.getSerialPortInfo();
  }

  @Override
  public InputStream getInputStream() {
    return serialPort.getInputStream();
  }

  @Override
  public OutputStream getOutputStream() {
    return serialPort.getOutputStream();
  }

  @Override
  public void close() {
    LogUtils.tag(TAG).d("close");
    listenerSet.clear();
    stopReadThread();
    sControllerMap.remove(getKey());
    serialPort.close();
  }

  public synchronized int getUseCount() {
    return useCount;
  }

  /**
   * 记录使用串口的模式数量.
   *
   * @param isAdd true 计数器加一，否则计数器减一
   */
  public synchronized void countUse(boolean isAdd) {
    if (isAdd) {
      this.useCount++;
    } else {
      this.useCount--;
    }
    LogUtils.tag(TAG).d("useCount:%s", useCount);
  }

  private class SerialPostReadThread extends Thread {
    private boolean stopped;

    public synchronized boolean isStop() {
      return stopped;
    }

    public synchronized void stopRead() {
      this.stopped = true;
    }

    @Override
    public void run() {
      LogUtils.tag(TAG).i("run start");
      int len;
      byte[] buff = new byte[1024 * 16];
      InputStream in = serialPort.getInputStream();
      stopped = false;
      while (!isStop()) {
        try {
          len = in.read(buff);
          LogUtils.tag(TAG).d("rec:%s, %s",
              BytesUtil.bytes2HexString(buff, 0, len), getSerialPortInfo());
          if (len < 1) {
            LogUtils.tag(TAG).w("rec:%s", len);
            break;
          }
          LogUtils.tag(TAG).v("onceListener:%s", onceListener);
          if (onceListener != null) {
            ISerialPortListener listener = onceListener;
            listener.onReceiveData(buff, len);
            synchronized (listener) {
              //LogUtils.w("listener notify:%s", Objects.hashCode(listener));
              listener.notifyAll();
            }
            //onceListener = null;
          }
          if (listenerSet != null) {
            for (ISerialPortListener listener : listenerSet) {
              listener.onReceiveData(Arrays.copyOf(buff, len));
            }
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      stopRead();
      LogUtils.tag(TAG).i("run finish");
    }
  }

  public static SerialPortController.Builder newBuilder(File device, int baudrate) {
    return new SerialPortController.Builder(device, baudrate);
  }

  public static SerialPortController.Builder newBuilder(String devicePath, int baudrate) {
    return new SerialPortController.Builder(devicePath, baudrate);
  }

  public static final class Builder {

    private File device;
    private int baudrate;
    private int dataBits = 8;
    private int parity = 0;
    private int stopBits = 1;
    private int flags = 0;

    private Builder(File device, int baudrate) {
      this.device = device;
      this.baudrate = baudrate;
    }

    private Builder(String devicePath, int baudrate) {
      this(new File(devicePath), baudrate);
    }

    /**
     * 数据位
     *
     * @param dataBits 默认8,可选值为5~8
     */
    public SerialPortController.Builder dataBits(int dataBits) {
      this.dataBits = dataBits;
      return this;
    }

    /**
     * 校验位
     *
     * @param parity 0:无校验位(NONE，默认)；1:奇校验位(ODD);2:偶校验位(EVEN)
     */
    public SerialPortController.Builder parity(int parity) {
      this.parity = parity;
      return this;
    }

    /**
     * 停止位
     *
     * @param stopBits 默认1；1:1位停止位；2:2位停止位
     */
    public SerialPortController.Builder stopBits(int stopBits) {
      this.stopBits = stopBits;
      return this;
    }

    /**
     * 标志
     *
     * @param flags 默认0
     */
    public SerialPortController.Builder flags(int flags) {
      this.flags = flags;
      return this;
    }

    /**
     * 打开并返回串口
     *
     * @throws SecurityException
     * @throws IOException
     */
    public SerialPortController build() throws SecurityException, IOException {
      String path = device.getPath();
      if (sControllerMap == null) {
        sControllerMap = new HashMap<>();
      }
      if (sControllerMap.containsKey(path)) {
        return sControllerMap.get(path);
      }

      SerialPortController controller = new SerialPortController(
          // TODO: 2020-11-10 串口实现类
          //new SerialPortGo(device, baudrate, dataBits, parity, stopBits, flags));
          new SerialPortRd(device, baudrate, dataBits, parity, stopBits, flags));
      controller.setKey(path);
      sControllerMap.put(path, controller);
      return controller;
    }
  }
}
