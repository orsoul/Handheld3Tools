package com.fanfull.libhard.uhf;

import android.os.SystemClock;
import com.apkfuns.logutils.LogUtils;
import com.fanfull.libhard.gpio.impl.GpioController;
import com.fanfull.libhard.serialport.ISerialPortListener;
import com.fanfull.libhard.serialport.impl.SerialPortController;
import java.io.IOException;
import java.util.Arrays;

/**
 * 手持2、手持3 超高频读卡模块，通过串口控制.
 */
public class UhfOperationSerial extends AbsUhfOperation {
  private final String SERIAL_PORT_PATH = "/dev/ttyMT0";
  private final int BUADRATE = 115200;
  private SerialPortController serialPortController;
  private ISerialPortListener serialPortListener;

  public UhfOperationSerial() {
  }

  @Override
  public boolean open() throws SecurityException {
    if (isOpen) {
      if (uhfListener != null) {
        uhfListener.onOpen(true);
      }
      return true;
    }
    try {
      serialPortController = SerialPortController.newBuilder(SERIAL_PORT_PATH, BUADRATE).build();
      serialPortListener = new ISerialPortListener() {
        @Override public void onReceiveData(byte[] data) {
          if (uhfListener != null) {
            uhfListener.onReceiveData(data);
          }
        }
      };
      serialPortController.addSerialPortListener(serialPortListener);
      serialPortController.startReadThread();
      serialPortController.countUse(true);
    } catch (IOException e) {
      e.printStackTrace();
      if (uhfListener != null) {
        uhfListener.onOpen(false);
      }
      return false;
    }

    boolean init = GpioController.getInstance().init();
    LogUtils.tag(TAG).d("init:%s", init);
    if (init) {
      setGpioUhfMode();
    }
    isOpen = init;
    if (uhfListener != null) {
      uhfListener.onOpen(init);
    }
    return init;
  }

  @Override
  public void release() {
    serialPortController.removeSerialPortListener(serialPortListener);
    serialPortController.countUse(false);
    if (serialPortController.getUseCount() == 0) {
      serialPortController.close();
    }
    isOpen = false;
  }

  @Override public boolean send(byte[] data) {
    boolean isUhfMode = setGpioUhfMode();
    //        LogUtils.d("isUhfMode:%s", isUhfMode);
    return isUhfMode && serialPortController.send(data);
  }

  public byte[] sendAndWaitReceive(byte[] data, int timeout) {
    setGpioUhfMode();
    return serialPortController.sendAndWaitReceive(data, timeout);
  }

  public byte[] sendAndWaitReceive(byte[] data) {
    return sendAndWaitReceive(data, 500);
  }

  private boolean setGpioUhfMode() {
    boolean[] res = new boolean[6];
    res[0] = GpioController.getInstance().setMode(64, 0);
    res[1] = GpioController.getInstance().setMode(62, 0);
    res[2] = GpioController.getInstance().setIO(64, false);
    res[3] = GpioController.getInstance().setIO(62, false);
    res[4] = GpioController.getInstance().set(64, true);
    res[5] = GpioController.getInstance().set(62, false);

    if (SerialPortController.witchMode != SerialPortController.WITCH_MODE_UHF) {
      // 之前串口被其他模块 使用，休眠等待 切换生效
      SerialPortController.witchMode = SerialPortController.WITCH_MODE_UHF;
      SystemClock.sleep(SerialPortController.SWICHT_MODE_WAIT_TIME);
    }
    LogUtils.tag(TAG).v("%s", Arrays.toString(res));
    return true;
  }

  @Override public byte[] read(int mb, int sa, int readLen, byte[] filter, int mmb, int msa) {
    byte[] readCmd = UhfCmd.getReadCmd(mb, sa, readLen, filter, mmb, msa);
    byte[] rec = sendAndWaitReceive(readCmd);
    byte[] parseData = UhfCmd.parseData(rec);
    return parseData;
  }

  @Override public void readAsync(int mb, int sa, int readLen, byte[] filter, int mmb, int msa) {
    byte[] readCmd = UhfCmd.getReadCmd(mb, sa, readLen, filter, mmb, msa);
    send(readCmd);
  }

  @Override public byte[] fastEpc(int timeout) {
    byte[] fastReadEpcCmd = UhfCmd.getFastReadEpcCmd(timeout);
    byte[] rec = sendAndWaitReceive(fastReadEpcCmd, timeout + 50);
    byte[] parseData = UhfCmd.parseData(rec);
    return parseData;
  }

  @Override public byte[] fastTid(int sa, int len) {
    byte[] fastReadTidCmd = UhfCmd.getFastReadTidCmd(sa, len);
    byte[] rec = sendAndWaitReceive(fastReadTidCmd);
    byte[] parseData = UhfCmd.parseData(rec);
    return parseData;
  }

  @Override public boolean write(int mb, int sa, byte[] data, byte[] filter, int mmb, int msa) {
    byte[] cmd = UhfCmd.getWriteCmd(mb, sa, data, filter, mmb, msa);
    byte[] bytes = sendAndWaitReceive(cmd);
    byte[] parseData = UhfCmd.parseData(bytes);
    return parseData != null && parseData.length == 0;
  }

  @Override public void writeAsync(int mb, int sa, byte[] data, byte[] filter, int mmb, int msa) {
    byte[] cmd = UhfCmd.getWriteCmd(mb, sa, data, filter, mmb, msa);
    send(cmd);
  }

  @Override
  public boolean setPower(int readPower, int writePower, int id, boolean isSave, boolean isClosed) {
    byte[] powerCmd = UhfCmd.getSetPowerCmd(readPower, writePower, id, isSave, isClosed);
    byte[] bytes = sendAndWaitReceive(powerCmd);
    byte[] parseData = UhfCmd.parseData(bytes);
    return parseData != null
        && parseData.length == 1
        && parseData[0] == 1;
  }

  /**
   * 获取读写功率.
   *
   * @return 获取失败返回null；否则[0]为读功率，[1]为写功率，[2]天线id，[3]是否闭环：1是、0否
   */
  @Override public byte[] getPower() {
    byte[] powerCmd = UhfCmd.getPowerCmd();
    byte[] bytes = sendAndWaitReceive(powerCmd);
    byte[] parseData = UhfCmd.parseData(bytes);
    return parseData;
  }
}
