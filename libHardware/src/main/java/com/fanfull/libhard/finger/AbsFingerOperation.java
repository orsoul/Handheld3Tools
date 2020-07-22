package com.fanfull.libhard.finger;

public abstract class AbsFingerOperation implements IFingerOperation {

  protected IFingerListener fingerListener;

  protected boolean isOpen;
  protected boolean isScanning;

  @Override
  public boolean isOpen() {
    return isOpen;
  }

  @Override
  public boolean isScanning() {
    return isScanning;
  }

  @Override
  public void setListener(IFingerListener listener) {
    fingerListener = listener;
  }
}
