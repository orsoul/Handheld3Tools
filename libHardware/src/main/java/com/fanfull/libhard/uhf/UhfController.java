package com.fanfull.libhard.uhf;

public class UhfController implements IUhfOperation {
  private static final int DEFAULT_TIMEOUT = 300;
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
  public boolean write(int mb, int sa, byte[] data, int timeout, int mmb, int msa, byte[] filter,
      byte[] pwd) {
    return uhfOperation.write(mb, sa, data, timeout, mmb, msa, filter, pwd);
  }

  public boolean write(int mb, int sa, byte[] data, int timeout, int mmb, int msa, byte[] filter) {
    return write(mb, sa, data, timeout, mmb, msa, filter, null);
  }

  public boolean write(int mb, int sa, byte[] data, int mmb, int msa, byte[] filter) {
    return write(mb, sa, data, DEFAULT_TIMEOUT, mmb, msa, filter, null);
  }

  @Override public void writeAsync(int mb, int sa, byte[] data, byte[] filter, int mmb, int msa) {
    uhfOperation.writeAsync(mb, sa, data, filter, mmb, msa);
  }

  @Override public boolean writeEpc(int sa, byte[] data) {
    return uhfOperation.writeEpc(sa, data);
  }

  public boolean writeEpc(byte[] data) {
    return uhfOperation.writeEpc(0x02, data);
  }

  /**
   * ???epc??????tid?????????.
   *
   * @param sa ??? 0x02 ??????
   * @param epcDate epc??????????????????????????????2~12??????.
   * @param msa ??? 0x00 ??????
   * @param tidDataFilter tid????????????????????????????????????2~12??????.
   */
  public boolean writeEpcFilterTid(int sa, byte[] epcDate, int msa, byte[] tidDataFilter) {
    return write(UhfCmd.MB_EPC, sa, epcDate,
        UhfCmd.MB_TID, msa, tidDataFilter);
  }

  /**
   * ???epc??????tid?????????.epc???tid??????12??????
   *
   * @param epcDate epc???????????????????????????12??????.
   * @param tidDataFilter tid?????????????????????????????????12??????.
   */
  public boolean writeEpcFilterTid(byte[] epcDate, byte[] tidDataFilter) {
    return writeEpcFilterTid(0x02, epcDate, 0x00, tidDataFilter);
  }

  @Override public boolean writeUse(int sa, byte[] data) {
    //if (data != null && 32 < data.length) {
    //}
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

  /**
   * ???epc??????tid?????????.
   *
   * @param sa ??? 0x02 ??????
   * @param epcDate epc??????????????????????????????2~12??????.
   * @param msa ??? 0x00 ??????
   * @param tidDataFilter tid????????????????????????????????????2~12??????.
   */
  public boolean readEpcFilterTid(int sa, byte[] epcDate, int msa, byte[] tidDataFilter) {
    return read(UhfCmd.MB_EPC, sa, epcDate,
        UhfCmd.MB_TID, msa, tidDataFilter);
  }

  /**
   * ???epc??????tid?????????.epc???tid??????12??????
   *
   * @param epcDate epc???????????????????????????12??????.
   * @param tidDataFilter tid?????????????????????????????????12??????.
   */
  public boolean readEpcFilterTid(byte[] epcDate, byte[] tidDataFilter) {
    return readEpcFilterTid(0x02, epcDate, 0x00, tidDataFilter);
  }

  @Override public boolean readTid(int sa, byte[] buff) {
    return uhfOperation.readTid(sa, buff);
  }

  /** ???????????????12??????tid. */
  public boolean readTid(byte[] buff) {
    return uhfOperation.readTid(0x00, buff);
  }

  /** ???????????????12??????tid???????????????tid????????????12??????epc. */
  public boolean readTidAndEpc(byte[] tidBuff, byte[] epcBuff) {
    if (readTid(tidBuff)) {
      return readEpcFilterTid(epcBuff, tidBuff);
    }
    return false;
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

  @Override public void setStatus(boolean pause) {
    uhfOperation.setStatus(pause);
  }

  private static class SingletonHolder {
    private static final UhfController instance = new UhfController(new UhfOperationSerial());
  }

  public static UhfController getInstance() {
    return UhfController.SingletonHolder.instance;
  }
}
