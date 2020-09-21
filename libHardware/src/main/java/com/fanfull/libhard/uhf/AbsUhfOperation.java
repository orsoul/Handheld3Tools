package com.fanfull.libhard.uhf;

public abstract class AbsUhfOperation implements IUhfOperation {
  protected String TAG = this.getClass().getSimpleName();
  protected IUhfListener uhfListener;

  protected boolean isOpen;
  protected boolean isScanning;

  @Override public boolean isOpen() {
    return isOpen;
  }

  @Override public boolean isScanning() {
    return isScanning;
  }

  @Override public void setListener(IUhfListener listener) {
    uhfListener = listener;
  }

  @Override public boolean send(byte[] data) {
    return false;
  }

  @Override public byte[] fastEpc(int timeout) {
    return null;
  }

  @Override public byte[] fastTid(int sa, int len) {
    return null;
  }

  @Override public byte[] readEpc(int sa, int len) {
    return read(UhfCmd.MB_EPC, sa, len, null, 0, 0);
  }

  @Override public byte[] readTid(int sa, int len) {
    return read(UhfCmd.MB_TID, sa, len, null, 0, 0);
  }

  @Override public byte[] readUse(int sa, int len) {
    return read(UhfCmd.MB_USE, sa, len, null, 0, 0);
  }

  @Override public boolean writeEpc(int sa, byte[] data) {
    return write(UhfCmd.MB_EPC, sa, data, null, 0, 0);
  }

  @Override public boolean writeUse(int sa, byte[] data) {
    return write(UhfCmd.MB_USE, sa, data, null, 0, 0);
  }
}
