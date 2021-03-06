package com.fanfull.libhard.serialport.impl;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.libhard.serialport.AbsSerialPort;
import com.rd.io.SerialPort;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** 雨滴串口API. */
public class SerialPortRd extends AbsSerialPort {
  private SerialPort serialPort;

  public SerialPortRd(File device, int baudrate, int dataBits, int parity, int stopBits,
      int flags) throws SecurityException, IOException {
    serialPort = new SerialPort(device, baudrate, flags);
    serialPortInfo = device.getPath() + ":" + baudrate;
    LogUtils.tag(TAG).i("open %s:%s",
        SerialPortRd.class.getSimpleName(), serialPortInfo);
  }

  @Override
  public InputStream getInputStream() {
    return serialPort.getInputStream();
  }

  @Override
  public OutputStream getOutputStream() {
    return serialPort.getOutputStream();
  }

  @Override public void close() {
    serialPort.close();
    //try {
    //serialPort.getInputStream().close();
    //serialPort.getOutputStream().close();
    //  serialPort.close();
    //} catch (IOException e) {
    //  e.printStackTrace();
    //}
  }
}
