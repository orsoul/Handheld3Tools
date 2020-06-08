package com.fanfull.libhard.nfc;

import com.apkfuns.logutils.LogUtils;
import java.util.concurrent.ExecutorService;

public abstract class AbsRfidOperation implements IRfidOperation {

  protected IRfidListener nfcListener;
  protected boolean isOpen;
  protected boolean isScanning;
  protected ExecutorService executor;

  @Override
  public boolean isOpen() {
    return isOpen;
  }

  protected synchronized void setScanning(boolean scanning) {
    isScanning = scanning;
    LogUtils.d("isScanning:%s", isScanning);
    if (nfcListener != null) {
      if (scanning) {
        nfcListener.onScan();
      } else {
        nfcListener.onStopScan();
      }
    }
  }

  @Override
  public synchronized boolean isScanning() {
    return isScanning;
  }

  @Override
  public void setListener(IRfidListener listener) {
    nfcListener = listener;
  }

  @Override
  public void findNfcAsync() {
    if (isScanning()) {
      return;
    }

    //        setScanning(true);
    executor.execute(() -> {
      byte[] uid = findNfc();
      if (uid != null && nfcListener != null) {
        nfcListener.onReceiveData(uid);
      }
      //            setScanning(false);
    });
  }

  @Override
  public void findM1Async() {
    if (isScanning()) {
      return;
    }

    //        setScanning(true);
    executor.execute(() -> {
      byte[] uid = findM1();
      if (uid != null && nfcListener != null) {
        nfcListener.onReceiveData(uid);
      }
      //            setScanning(false);
    });
  }

  @Override
  public void readNfcAsync(int sa, int dataLen, boolean withFindCard) {
    if (sa < 0 || dataLen < 1) {
      LogUtils.w("check failed sa:%s len:%s", sa, dataLen);
      return;
    }

    if (isScanning()) {
      return;
    }

    //        setScanning(true);
    executor.execute(() -> {
      byte[] buff = new byte[dataLen];
      boolean readSuccess = readNfc(sa, buff, withFindCard);
      if (readSuccess && nfcListener != null) {
        nfcListener.onReceiveData(buff);
      }
      //            setScanning(false);
    });
  }

}
