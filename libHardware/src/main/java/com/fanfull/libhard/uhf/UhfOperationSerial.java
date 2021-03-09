package com.fanfull.libhard.uhf;

import android.os.SystemClock;

import androidx.annotation.NonNull;

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
      setStatus(false);
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
    setListener(null);
    if (serialPortController != null) {
      serialPortController.removeSerialPortListener(serialPortListener);
      serialPortController.countUse(false);
      if (serialPortController.getUseCount() == 0) {
        serialPortController.close();
      }
    }
    isOpen = false;
  }

  @Override public boolean send(byte[] data) {
    boolean isUhfMode = setGpioUhfMode();
    //        LogUtils.d("isUhfMode:%s", isUhfMode);
    return isUhfMode && serialPortController.send(data);
  }

  /**
   * @return 发送失败返回-1，回复超时返回-2
   */
  public int sendAndWaitReceive(byte[] data, int timeout, byte[] recBuff) {
    setGpioUhfMode();
    return serialPortController.sendAndWaitReceive(data, timeout, recBuff);
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

  @Override public void setStatus(boolean pause) {
    if (pause) {
      GpioController.getInstance().set(64, false);
      GpioController.getInstance().set(62, true);
    } else {
      GpioController.getInstance().set(64, true);
      GpioController.getInstance().set(62, false);
    }
  }

  /**
   * 接收模块返回的数据. 帧头、帧尾等格式内容长度 占12字节，
   * 读取TID或EPC数据最大16字节；读取use区数据大于16时，会进行扩容.
   */
  private byte[] uhfBuff = new byte[12 + 16];

  /**
   * 检查通过read()方法接收的指令，如果成功获取到数据 且 数据长度与目标数组长度一致，拷贝数据到目标数组.
   *
   * @param recCmd 接收到的数据
   * @param dest 目标数组
   */
  private boolean checkAndCopy(@NonNull byte[] recCmd, @NonNull byte[] dest) {
    if (recCmd[5] == 0x01 && recCmd[6] == 0x00) {
      // A55A 0019 81 3000CFCFCFCFCFCFCFCFCFCFCFCFFE3F01 68 0D0A
      int dataLen = recCmd[7] & 0xFF;
      dataLen <<= 8;
      dataLen |= recCmd[8] & 0xFF;
      dataLen <<= 1;
      if (dataLen == dest.length) {
        System.arraycopy(uhfBuff, 9, dest, 0, dataLen);
        return true;
      }
    }
    return false;
  }

  @Override public boolean read(int mb, int sa, byte[] buff, int timeout, int mmb, int msa,
      byte[] filter) {
    return 0 < readCode(mb, sa, buff, timeout, mmb, msa, filter);
  }

  private int readCode(int mb, int sa, byte[] buff, int timeout, int mmb, int msa,
      byte[] filter) {
    if (buff == null || buff.length == 0) {
      return -3;
    }

    if (mb == UhfCmd.MB_USE && uhfBuff.length < buff.length + 12) {
      // 读取use区，原缓存区空间不足，进行扩容
      uhfBuff = new byte[buff.length + 12];
    }

    byte[] readCmd = UhfCmd.getReadCmd(mb, sa, buff.length, filter, mmb, msa);
    int len = sendAndWaitReceive(readCmd, timeout, uhfBuff);
    if (len < 1) {
      return len;
    }
    if (!UhfCmd.isUhfCmd(uhfBuff, len)) {
      return -4;
    }

    if (checkAndCopy(uhfBuff, buff)) {
      return len;
    }

    return -5;
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

  @Override public byte[] readEpcWithTid(int timeout) {
    byte[] fastReadEpcCmd = UhfCmd.getFastReadEpcCmd(timeout);
    byte[] rec = sendAndWaitReceive(fastReadEpcCmd, timeout + 50);
    byte[] parseData = UhfCmd.parseData(rec);
    return parseData;
  }

  /**
   * 快速读取tid，效率比通过read()获取高，部分芯片或读卡器可能不支持.
   */
  public boolean fastTid(int sa, byte[] buff) {
    return 0 < fastTidCode(sa, buff);
  }

  private int fastTidCode(int sa, byte[] buff) {
    if (buff == null || buff.length == 0) {
      return -3;
    }

    byte[] fastReadTidCmd = UhfCmd.getFastReadTidCmd(sa, buff.length);
    int len = sendAndWaitReceive(fastReadTidCmd, 100, uhfBuff);
    if (len < 1) {
      return len;
    }
    if (!UhfCmd.isUhfCmd(uhfBuff, len)) {
      return -4;
    }

    if (checkAndCopy(uhfBuff, buff)) {
      return len;
    }
    return -5;
  }

  @Override
  public boolean write(int mb, int sa, byte[] data, int timeout, int mmb, int msa, byte[] filter) {
    if (data == null || data.length == 0) {
      return false;
    }
    byte[] cmd = UhfCmd.getWriteCmd(mb, sa, data, mmb, msa, filter);
    //byte[] bytes = sendAndWaitReceive(cmd, timeout, uhfBuff);
    int len = sendAndWaitReceive(cmd, timeout, uhfBuff);
    if (!UhfCmd.isUhfCmd(uhfBuff, len)) {
      return false;
    }

    if (uhfBuff[5] == 0x01 && uhfBuff[6] == 0x00) {
      return true;
    } else {
      return false;
    }
    //return parseData != null && parseData.length == 0;
  }

  @Override public void writeAsync(int mb, int sa, byte[] data, byte[] filter, int mmb, int msa) {
    byte[] cmd = UhfCmd.getWriteCmd(mb, sa, data, mmb, msa, filter);
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
