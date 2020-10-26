package com.fanfull.libhard.finger.impl;

import com.fanfull.libhard.finger.IFingerListener;

public class FingerprintListener implements IFingerListener {
  private boolean stopped;

  public void stopSearch() {
    this.stopped = true;
  }

  @Override public void onOpen(boolean openSuccess) {

  }

  @Override
  public void onReceiveData(byte[] data) {

  }
}
