package org.orsoul.baselib.lock3.task;

import android.os.SystemClock;
import android.text.TextUtils;
import com.apkfuns.logutils.LogUtils;
import org.orsoul.baselib.util.ThreadUtil;

/**
 * 获取登录卡ID任务.通过覆盖5个回调方法控制读卡过程
 */
public abstract class ReadLoginCardTask extends ThreadUtil.ThreadRunnable {
  /** 读卡次数，即 readLoginCard()方法执行的次数. */
  private int readTimes = 10;

  public void setReadTimes(int readTimes) {
    this.readTimes = readTimes;
  }

  @Override
  public void run() {
    int count = 0;
    stopped = false;
    boolean readSuccess = false;
    String cardNumber = null;
    while (!stopped) {
      if (readTimes < count++) {
        //onReadFailed();
        break;
      }
      onReading();

      cardNumber = readLoginCard();
      LogUtils.d("cardNumber:%s", cardNumber);
      readSuccess = !TextUtils.isEmpty(cardNumber) && !stopped;
      if (readSuccess) {
        break;
      }
      SystemClock.sleep(50);
    }
    if (readSuccess) {
      onReadSuccess(cardNumber);
    } else if (stopped) {
      onReadStop();
    } else {
      onReadFailed();
    }
    stopped = true;
    LogUtils.i("read stop");
  }

  /** 单次读卡逻辑. */
  protected abstract String readLoginCard();

  /** 读卡成功. */
  protected void onReadSuccess(String userId) {
  }

  /** 读卡成功. */
  protected void onReadSuccess(byte[] userId) {
  }

  /** 读卡中. */
  protected void onReading() {
  }

  /** 手动停止读卡. */
  protected void onReadStop() {
  }

  /** 读卡失败. */
  protected void onReadFailed() {
  }
}