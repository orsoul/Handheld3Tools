package com.fanfull.libhard.finger.impl;

import android.os.SystemClock;
import com.apkfuns.logutils.LogUtils;
import com.fanfull.libhard.finger.IFingerListener;
import com.fanfull.libhard.finger.IFingerOperation;

public class FingerprintController implements IFingerOperation {
  private IFingerOperation operation;
  private SearchFingerPrintThread searchThread;
  private boolean searchThreadRunning;

  private FingerprintController(IFingerOperation operation) {
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
      searchThread = new SearchFingerPrintThread();
      searchThread.start();
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

  class SearchFingerPrintThread extends Thread {
    private boolean stopped;

    public synchronized void stopSearch() {
      this.stopped = true;
    }

    public synchronized boolean isRunning() {
      return !stopped;
    }

    @Override
    public void run() {
      LogUtils.i("run start");

      while (!stopped) {
        send(FingerPrintCmd.CMD_GET_IMAGE);
        SystemClock.sleep(500);
      }// end while

      LogUtils.i("run end");
    }
  }
}
