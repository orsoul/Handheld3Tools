package com.fanfull.libhard.uhf;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.libhard.gpio.impl.GpioController;
import com.fanfull.libhard.serialport.ISerialPortListener;
import com.fanfull.libhard.serialport.impl.SerialPortController;
import java.io.IOException;
import java.util.Arrays;

public class UhfOperationRd extends AbsUhfOperation {
  private final String SERIAL_PORT_PATH = "/dev/ttyMT0";
  private final int BUADRATE = 115200;
  private SerialPortController serialPortController;
  private ISerialPortListener serialPortListener;

  public UhfOperationRd() {
  }

  @Override
  public boolean open() throws SecurityException {
    if (isOpen) {
      return true;
    }
    try {
      serialPortController = SerialPortController.newBuilder(SERIAL_PORT_PATH, BUADRATE).build();
      serialPortListener = data -> {
        if (uhfListener != null) {
          uhfListener.onReceiveData(data);
        }
      };
      serialPortController.addSerialPortListener(serialPortListener);
      serialPortController.startReadThread();
      serialPortController.addUseCount();
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }

    boolean init = GpioController.getInstance().init();
    LogUtils.d("init:%s", init);

    isOpen = true;
    if (uhfListener != null) {
      uhfListener.onOpen();
    }
    return true;
  }

  @Override
  public void release() {
    serialPortController.removeSerialPortListener(serialPortListener);
    serialPortController.minUseCount();
    if (serialPortController.getUseCount() == 0) {
      serialPortController.close();
    }
    isOpen = false;
  }

  @Override
  public boolean send(byte[] data) {
    boolean isUhfMode = setGpioUhfMode();
    //        LogUtils.d("isUhfMode:%s", isUhfMode);
    return isUhfMode && serialPortController.send(data);
  }

  private boolean setGpioUhfMode() {

    //        boolean reVal = GpioController.getInstance().init()
    //                && GpioController.getInstance().setMode(64, 0)
    //                && GpioController.getInstance().setMode(62, 0)
    //                && GpioController.getInstance().setIO(64, false)
    //                && GpioController.getInstance().setIO(62, false)
    //                && GpioController.getInstance().set(64, true)
    //                && GpioController.getInstance().set(62, false);

    boolean[] res = new boolean[6];
    //        res[0] = Platform.SetGpioMode(64, 0);
    //        res[1] = Platform.SetGpioMode(62, 0);
    //        res[2] = Platform.SetGpioOutput(64);
    //        res[3] = Platform.SetGpioOutput(62);
    //        res[4] = Platform.SetGpioDataHigh(64);
    //        res[5] = Platform.SetGpioDataLow(62);
    //        LogUtils.d("%s", Arrays.toString(res));

    res[0] = GpioController.getInstance().setMode(64, 0);
    res[1] = GpioController.getInstance().setMode(62, 0);
    res[2] = GpioController.getInstance().setIO(64, false);
    res[3] = GpioController.getInstance().setIO(62, false);
    res[4] = GpioController.getInstance().set(64, true);
    res[5] = GpioController.getInstance().set(62, false);
    LogUtils.v("%s", Arrays.toString(res));

    return true;
  }

  @Override public byte[] read(int mb, int sa, int readLen, byte[] filter, int mmb, int msa) {
    byte[] readCmd = UhfCmd.getReadCmd(mb, sa, readLen, filter, mmb, msa);
    byte[] bytes = serialPortController.sendAndWaitReceive(readCmd);
    return bytes;
  }

  @Override public void readAsync(int mb, int sa, int readLen, byte[] filter, int mmb, int msa) {
    byte[] readCmd = UhfCmd.getReadCmd(mb, sa, readLen, filter, mmb, msa);
    send(readCmd);
  }

  @Override public boolean write(byte[] data, int mb, int sa, byte[] filter, int mmb, int msa) {
    return false;
  }

  @Override public void writeAsync(byte[] data, int mb, int sa, byte[] filter, int mmb, int msa) {

  }

  @Override public byte[] readEpc(int timeout) {
    byte[] fastReadEpcCmd = UhfCmd.getFastReadEpcCmd(timeout);
    byte[] rec = serialPortController.sendAndWaitReceive(fastReadEpcCmd, timeout + 50);
    byte[] parseData = UhfCmd.parseData(rec);
    return parseData;
  }

  @Override public byte[] readTid(int sa, int len) {
    byte[] fastReadTidCmd = UhfCmd.getFastReadTidCmd(sa, len);
    byte[] rec = serialPortController.sendAndWaitReceive(fastReadTidCmd);
    byte[] parseData = UhfCmd.parseData(rec);
    return parseData;
  }

  @Override public byte[] readUse(int sa, int len) {
    byte[] readCmd = UhfCmd.getReadCmd(UhfCmd.MB_USE, sa, len);
    byte[] rec = serialPortController.sendAndWaitReceive(readCmd);
    byte[] parseData = UhfCmd.parseData(rec);
    return parseData;
  }

  @Override
  public boolean setPower(int readPower, int writePower, int id, boolean isSave, boolean isClosed) {
    byte[] powerCmd = UhfCmd.getSetPowerCmd(readPower, writePower, id, isSave, isClosed);
    byte[] bytes = serialPortController.sendAndWaitReceive(powerCmd, 1000);
    byte[] parseData = UhfCmd.parseData(bytes);
    return parseData != null
        && parseData.length == 1
        && parseData[0] == 1;
  }

  @Override public byte[] getPower() {
    byte[] powerCmd = UhfCmd.getPowerCmd();
    byte[] bytes = serialPortController.sendAndWaitReceive(powerCmd);
    byte[] parseData = UhfCmd.parseData(bytes);
    return parseData;
  }
}
