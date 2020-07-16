package com.fanfull.libhard.uhf;

public class UhfController implements IUhfOperation {
  private IUhfOperation uhfOperation;

  private UhfController(IUhfOperation uhfOperation) {
    this.uhfOperation = uhfOperation;
  }

  @Override
  public boolean open() {
    return uhfOperation.open();
  }

  @Override
  public boolean isOpen() {
    return uhfOperation.isOpen();
  }

  @Override
  public boolean isScanning() {
    return uhfOperation.isScanning();
  }

  @Override
  public void release() {
    uhfOperation.release();
  }

  @Override public boolean send(byte[] data) {
    return uhfOperation.send(data);
  }

  @Override
  public void setListener(IUhfListener listener) {
    uhfOperation.setListener(listener);
  }

  @Override public byte[] read(int mb, int sa, int readLen, byte[] filter, int mmb, int msa) {
    byte[] read = uhfOperation.read(mb, sa, readLen, filter, mmb, msa);
    byte[] parseData = UhfCmd.parseData(read);
    return parseData;
  }

  @Override public void readAsync(int mb, int sa, int readLen, byte[] filter, int mmb, int msa) {
    uhfOperation.readAsync(mb, sa, readLen, filter, mmb, msa);
  }

  @Override public boolean write(byte[] data, int mb, int sa, byte[] filter, int mmb, int msa) {
    return false;
  }

  @Override public void writeAsync(byte[] data, int mb, int sa, byte[] filter, int mmb, int msa) {

  }

  @Override public byte[] readEpc(int timeout) {
    return uhfOperation.readEpc(timeout);
  }

  @Override public byte[] readTid(int sa, int len) {
    return uhfOperation.readTid(sa, len);
  }

  @Override public byte[] readUse(int sa, int len) {
    return uhfOperation.readUse(sa, len);
  }

  @Override
  public boolean setPower(int readPower, int writePower, int id, boolean isSave,
      boolean isClosed) {
    return uhfOperation.setPower(readPower, writePower, id, isSave, isClosed);
  }

  @Override public byte[] getPower() {
    return uhfOperation.getPower();
  }

  private static class SingletonHolder {
    private static final UhfController instance = new UhfController(new UhfOperationRd());
  }

  public static UhfController getInstance() {
    return UhfController.SingletonHolder.instance;
  }
}
