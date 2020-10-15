package com.fanfull.libhard.finger.impl;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.libhard.finger.IFingerListener;
import com.fanfull.libhard.finger.IFingerOperation;
import org.orsoul.baselib.util.ClockUtil;
import org.orsoul.baselib.util.ThreadUtil;

public class FingerprintController implements IFingerOperation {
  //private IFingerOperation operation;
  public FingerOperationRd operation;
  private FingerPrintTask searchThread;
  private boolean searchThreadRunning;

  //private FingerprintController(IFingerOperation operation) {
  //  this.operation = operation;
  //}
  private FingerprintController(FingerOperationRd operation) {
    this.operation = operation;
  }

  @Override
  public boolean open() {
    return operation.open();
  }

  @Override
  public boolean isOpen() {
    return operation.isOpen();
  }

  @Override
  public boolean isScanning() {
    return operation.isScanning();
  }

  @Override
  public void release() {
    stopSearchFingerPrint();
    operation.release();
  }

  @Override
  public boolean send(byte[] data) {
    return operation.send(data);
  }

  @Override public int addFinger(int[] fingerIdBuff) {
    return operation.addFinger(fingerIdBuff);
  }

  @Override public int searchFinger(int[] fingerIdBuff) {
    return operation.searchFinger(fingerIdBuff);
  }

  @Override public int getFingerNum(int[] fingerNumBuff) {
    return operation.getFingerNum(fingerNumBuff);
  }

  @Override public boolean clearFinger() {
    return operation.clearFinger();
  }

  @Override
  public void setListener(IFingerListener listener) {
    operation.setListener(listener);
  }

  private static class SingletonHolder {
    private static final FingerprintController instance =
        new FingerprintController(new FingerOperationRd());
  }

  public static FingerprintController getInstance() {
    return FingerprintController.SingletonHolder.instance;
  }

  public synchronized void startSearchFingerPrint() {
    if (searchThread == null) {
      searchThread = new FingerPrintTask(this);
      searchThread.startRun();
    }
  }

  public synchronized void stopSearchFingerPrint() {
    if (searchThread != null) {
      searchThread.stopSearch();
      searchThread = null;
    }
  }

  public boolean isSearch() {
    return searchThread != null && searchThread.isRunning();
  }

  public static class FingerPrintTask implements Runnable {
    private FingerprintController fingerprintController;
    private boolean stopped = true;
    private boolean isAddMode;
    private long runTime = 5000L;

    public FingerPrintTask(FingerprintController fingerprintController) {
      this.fingerprintController = fingerprintController;
    }

    public void setAddMode(boolean addMode) {
      isAddMode = addMode;
    }

    public boolean isAddMode() {
      return isAddMode;
    }

    public void setRunTime(long runTime) {
      this.runTime = runTime;
    }

    public synchronized void stopSearch() {
      this.stopped = true;
    }

    public synchronized boolean isRunning() {
      return !stopped;
    }

    public synchronized boolean startRun() {
      if (isRunning()) {
        return false;
      }
      ThreadUtil.execute(this);
      return true;
    }

    @Override
    public void run() {
      LogUtils.i("run start");

      stopped = false;
      int[] fingerIdBuff = new int[2];
      ClockUtil.resetRunTime();
      while (!stopped) {
        if (runTime <= ClockUtil.runTime()) {
          onFailed(isAddMode, -2);
          break;
        }

        int res;
        if (isAddMode) {
          res = fingerprintController.addFinger(fingerIdBuff);
        } else {
          res = fingerprintController.searchFinger(fingerIdBuff);
        }

        if (res == FingerPrintCmd.RES_CODE_NO_FINGER) {
          onNoFinger();
        } else if (res == FingerPrintCmd.RES_CODE_SUCCESS) {
          onSuccess(isAddMode, fingerIdBuff[0], fingerIdBuff[1]);
          break;
        } else {
          onFailed(isAddMode, res);
          break;
        }
      } // end while
      LogUtils.i("run end");
      stopped = true;
    }

    /** 感应器无指纹时 回调. */
    protected void onNoFinger() {
    }

    /** 添加或匹配指纹成功时 回调. */
    protected void onSuccess(boolean isAddMode, int fingerId, int score) {
    }

    /** 添加或匹配指纹失败时 回调. */
    protected void onFailed(boolean isAddMode, int errorCode) {
    }
  }
}
