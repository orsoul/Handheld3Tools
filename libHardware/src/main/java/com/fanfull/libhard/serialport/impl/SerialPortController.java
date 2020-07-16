package com.fanfull.libhard.serialport.impl;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.libhard.serialport.ISerialPort;
import com.fanfull.libhard.serialport.ISerialPortListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.orsoul.baselib.util.ArrayUtils;
import org.orsoul.baselib.util.ClickUtil;

public class SerialPortController implements ISerialPort {
  private static Map<String, SerialPortController> sControllerMap;
  private ISerialPort serialPort;
  private SerialPostReadThread readThread;
  private Set<ISerialPortListener> listenerSet;
  private ISerialPortListener onceListener;
  private byte[] onceRecBuff;
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
    if (listenerSet != null) {
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
    LogUtils.d("send:%s", ArrayUtils.bytes2HexString(data, off, off + len));
    return send;
  }

  @Override
  public boolean send(byte[] data) {
    return serialPort.send(data);
  }

  public byte[] sendAndWaitReceive(byte[] data, long timeout) {
    onceRecBuff = null;
    onceListener = data1 -> onceRecBuff = data1;
    boolean send = send(data);
    if (!send) {
      return null;
    }

    synchronized (onceListener) {
      try {
        LogUtils.v("click1:%s", ClickUtil.click());
        onceListener.wait(timeout);
      } catch (InterruptedException e) {
        LogUtils.d("InterruptedException");
      }
    }
    onceListener = null;
    LogUtils.v("click2:%s", ClickUtil.click());
    return onceRecBuff;
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
    stopReadThread();
    sControllerMap.remove(getKey());
    serialPort.close();
  }

  public synchronized int getUseCount() {
    return useCount;
  }

  public synchronized void addUseCount() {
    ++this.useCount;
    LogUtils.d("useCount:%s", useCount);
  }

  public synchronized void minUseCount() {
    --this.useCount;
    LogUtils.d("useCount:%s", useCount);
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
      LogUtils.i("run start");
      int len;
      byte[] buff = new byte[1024 * 16];
      InputStream in = serialPort.getInputStream();
      while (!isStop()) {
        try {
          len = in.read(buff);
          LogUtils.d("%s rec:%s", getSerialPortInfo(), ArrayUtils.bytes2HexString(buff, 0, len));
          if (len < 1) {
            break;
          }
          LogUtils.v("onceListener:%s", onceListener);
          if (onceListener != null) {
            onceListener.onReceiveData(Arrays.copyOf(buff, len));
            synchronized (onceListener) {
              onceListener.notifyAll();
            }
            onceListener = null;
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
      LogUtils.i("run finish");
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
          new SerialPortRd(device, baudrate, dataBits, parity, stopBits, flags));
      controller.setKey(path);
      sControllerMap.put(path, controller);
      return controller;
    }
  }
}
