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

  @Override public boolean readEpc(int sa, byte[] buff) {
    return read(UhfCmd.MB_EPC, sa, buff, 500, 0, 0, null);
  }

  @Override public boolean readTid(int sa, byte[] buff) {
    return read(UhfCmd.MB_TID, sa, buff, 500, 0, 0, null);
  }

  @Override public boolean readUse(int sa, byte[] buff) {
    return read(UhfCmd.MB_USE, sa, buff, 500, 0, 0, null);
  }

  @Override public boolean writeEpc(int sa, byte[] data) {
    return write(UhfCmd.MB_EPC, sa, data, 500, 0, 0, null, null);
  }

  @Override public boolean writeUse(int sa, byte[] data) {
    return write(UhfCmd.MB_USE, sa, data, 500, 0, 0, null, null);
  }
}
