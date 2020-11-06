package com.fanfull.libhard.serialport.impl;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.libhard.serialport.AbsSerialPort;
import com.rd.io.SerialPort;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPortRd extends AbsSerialPort {
  private SerialPort serialPort;

  public SerialPortRd(File device, int baudrate, int dataBits, int parity, int stopBits,
      int flags) throws SecurityException, IOException {
    serialPort = new SerialPort(device, baudrate, flags);
    serialPortInfo = device.getPath() + ":" + baudrate;
    LogUtils.tag(TAG).i("open SerialPort:%s", serialPortInfo);
  }

  @Override
  public InputStream getInputStream() {
    return serialPort.getInputStream();
  }

  @Override
  public OutputStream getOutputStream() {
    return serialPort.getOutputStream();
  }

  @Override
  public void close() {
    try {
      serialPort.getInputStream().close();
      serialPort.getOutputStream().close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    serialPort.close();
  }
}
