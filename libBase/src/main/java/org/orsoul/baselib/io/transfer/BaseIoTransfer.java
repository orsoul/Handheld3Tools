package org.orsoul.baselib.io.transfer;

import com.apkfuns.logutils.LogUtils;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.orsoul.baselib.util.ClockUtil;
import org.orsoul.baselib.util.ThreadUtil;

/**
 * io流 数据收发.
 */
public abstract class BaseIoTransfer implements IoTransfer, Runnable {
  protected String TAG = this.getClass().getSimpleName();

  protected InputStream inputStream;
  protected OutputStream outputStream;

  /** 消息接收线程 */
  private Thread receiverThread;
  /** 控制消息接收线程是否 运行 */
  private boolean isRunning;

  private IoTransferListener ioTransferListener;
  private Set<TimeoutTask> timeoutTaskSet;

  public BaseIoTransfer() {
    timeoutTaskSet = new HashSet<>();
  }

  public BaseIoTransfer(InputStream inputStream, OutputStream outputStream) {
    super();
    this.inputStream = inputStream;
    this.outputStream = outputStream;
  }

  public void setInputStream(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  public void setOutputStream(OutputStream outputStream) {
    this.outputStream = outputStream;
  }

  public void setIoTransferListener(IoTransferListener listener) {
    this.ioTransferListener = listener;
  }

  @Override public synchronized boolean startReceive() {
    if (isRunning()) {
      return true;
    }

    receiverThread = new Thread(this, TAG + "Thread");
    receiverThread.start();
    return true;
  }

  @Override public boolean send(byte[] data, int offset, int len) {
    if (!isRunning()) {
      return false;
    }

    boolean reVal;
    try {
      synchronized (outputStream) {
        outputStream.write(data, offset, len);
      }
      reVal = true;
    } catch (Exception e) {
      e.printStackTrace();
      reVal = false;
    }
    if (ioTransferListener != null) {
      ioTransferListener.onSend(reVal, data, offset, len);
    }
    return reVal;
  }

  @Override public boolean send(byte[] bytes) {
    if (bytes == null || 0 == bytes.length) {
      return false;
    }
    return send(bytes, 0, bytes.length);
  }

  @Override public boolean send(byte[] data, int timeout, IoTransferListener listener) {
    boolean send = send(data);
    if (!send) {
      return false;
    }
    TimeoutTask timeoutTask = new TimeoutTask(timeout, listener);
    timeoutTaskSet.add(timeoutTask);
    timeoutTask.startClock();
    return true;
  }

  @Override public byte[] sendAndWaitReceive(byte[] data, int timeout) {
    boolean send = send(data);
    if (!send) {
      return null;
    }
    TimeoutTask timeoutTask = new TimeoutTask(timeout, null);
    timeoutTaskSet.add(timeoutTask);
    timeoutTask.run();

    return timeoutTask.getRecData();
  }

  @Override public boolean isRunning() {
    return isRunning;
  }

  @Override public void stopReceive() {
    isRunning = false;
    ioTransferListener = null;
    timeoutTaskSet.clear();
    if (receiverThread != null) {
      receiverThread.interrupt();
    }
  }

  @Override public void dispatchReceiveData(byte[] recData) {
    if (!timeoutTaskSet.isEmpty()) {
      for (TimeoutTask timeoutTask : new ArrayList<>(timeoutTaskSet)) {
        timeoutTask.stopClock(recData);
      }
    }
    if (ioTransferListener != null) {
      ioTransferListener.onReceive(recData);
    }
  }

  public void run() {
    System.out.println(Thread.currentThread().getName() + " run");
    isRunning = true;
    byte[] recBuff = new byte[1024 * 16];
    while (isRunning) {
      try {
        int recLen = inputStream.read(recBuff);
        if (recLen < 1) { // 流被关闭
          break;
        }
        dispatchReceiveData(Arrays.copyOf(recBuff, recLen));
      } catch (Exception e) {
        e.printStackTrace();
      }
    } // end while()
    isRunning = false;

    if (ioTransferListener != null) {
      ioTransferListener.onStopReceive();
    }
    System.out.println(Thread.currentThread().getName() + " end");
  }

  /**
   * 计时任务.
   * 大致逻辑：
   * 发送数据成功后 计时线程waite()，数据接收线程收到数据后 唤醒计时线程，
   * 计时线程被唤醒后 执行 onReceive()回调方法，
   * 计时线程超时未被唤醒，则执行 onTimeout()回调方法。
   */
  private class TimeoutTask implements Runnable {
    private static final int minTimeout = 500;
    private static final int maxTimeout = 30_000;
    private int timeout;
    private IoTransferListener listener;
    private byte[] recData;

    public TimeoutTask(int timeout, IoTransferListener listener) {
      if (timeout < minTimeout) {
        timeout = minTimeout;
      } else if (maxTimeout < timeout) {
        timeout = maxTimeout;
      }
      this.timeout = timeout;
      this.listener = listener;
    }

    /** 开启新线程 进行计时，成功发送数据后调用. */
    public void startClock() {
      ThreadUtil.execute(this);
    }

    /** 唤醒 计时线程，接收线程收到数据后调用. */
    public void stopClock(byte[] recData) {
      this.recData = recData;
      synchronized (this) {
        this.notifyAll();
      }
    }

    public byte[] getRecData() {
      return recData;
    }

    @Override public void run() {
      try {
        // 1，开始计时
        LogUtils.tag(TAG).d("click1:%s", ClockUtil.clock());
        synchronized (this) {
          this.wait(timeout);
        }
      } catch (InterruptedException e) {
        LogUtils.tag(TAG).i("InterruptedException");
      }
      LogUtils.tag(TAG).d("click2:%s", ClockUtil.clock());

      if (listener != null) {
        LogUtils.tag(TAG).d("timeout:%s", listener);
        if (recData == null) { // 超时
          listener.onReceiveTimeout();
        } else {
          // 计时终止
          boolean deal = listener.onReceive(recData);
          if (deal) {
            timeoutTaskSet.remove(this);
          }
        }
        listener = null;
      }
    } // end run()
  }
}
