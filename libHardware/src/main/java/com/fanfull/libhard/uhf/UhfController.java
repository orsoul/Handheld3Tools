package com.fanfull.libhard.uhf;

public class UhfController implements IUhfOperation {
  private static final int DEFAULT_TIMEOUT = 100;
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

  @Override public boolean read(int mb, int sa, byte[] dataBuff, int timeout, int mmb, int msa,
      byte[] filter) {
    return uhfOperation.read(mb, sa, dataBuff, timeout, mmb, msa, filter);
  }

  public boolean read(int mb, int sa, byte[] dataBuff, int mmb, int msa, byte[] filter) {
    return read(mb, sa, dataBuff, DEFAULT_TIMEOUT, mmb, msa, filter);
  }

  @Override public byte[] read(int mb, int sa, int readLen, byte[] filter, int mmb, int msa) {
    return uhfOperation.read(mb, sa, readLen, filter, mmb, msa);
  }

  @Override public void readAsync(int mb, int sa, int readLen, byte[] filter, int mmb, int msa) {
    uhfOperation.readAsync(mb, sa, readLen, filter, mmb, msa);
  }

  @Override
  public boolean write(int mb, int sa, byte[] data, int timeout, int mmb, int msa, byte[] filter) {
    return uhfOperation.write(mb, sa, data, timeout, mmb, msa, filter);
  }

  public boolean write(int mb, int sa, byte[] data, int mmb, int msa, byte[] filter) {
    return uhfOperation.write(mb, sa, data, DEFAULT_TIMEOUT, mmb, msa, filter);
  }

  @Override public void writeAsync(int mb, int sa, byte[] data, byte[] filter, int mmb, int msa) {
    uhfOperation.writeAsync(mb, sa, data, filter, mmb, msa);
  }

  @Override public boolean writeEpc(int sa, byte[] data) {
    return uhfOperation.writeEpc(sa, data);
  }

  @Override
  public boolean writeUse(int sa, byte[] data) {
    return uhfOperation.writeUse(sa, data);
  }

  @Override public byte[] readEpcWithTid(int timeout) {
    return uhfOperation.readEpcWithTid(timeout);
  }

  @Override public boolean fastTid(int sa, byte[] buff) {
    return uhfOperation.fastTid(sa, buff);
  }

  @Override public boolean readEpc(int sa, byte[] buff) {
    return uhfOperation.readEpc(sa, buff);
  }

  public boolean readEpc(byte[] buff) {
    return uhfOperation.readEpc(0x02, buff);
  }

  @Override public boolean readTid(int sa, byte[] buff) {
    return uhfOperation.readTid(sa, buff);
  }

  @Override public boolean readUse(int sa, byte[] buff) {
    return uhfOperation.readUse(sa, buff);
  }

  @Override
  public boolean setPower(int readPower, int writePower, int id, boolean isSave,
      boolean isClosed) {
    return uhfOperation.setPower(readPower, writePower, id, isSave, isClosed);
  }

  public boolean setPower(int readPower, int writePower) {
    return setPower(readPower, writePower, 0, true, false);
  }

  @Override public byte[] getPower() {
    return uhfOperation.getPower();
  }

  private static class SingletonHolder {
    private static final UhfController instance = new UhfController(new UhfOperationSerial());
  }

  public static UhfController getInstance() {
    return UhfController.SingletonHolder.instance;
  }
}
