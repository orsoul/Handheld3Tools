package com.fanfull.libhard.serialport.impl;

import android.serialport.SerialPort;
import com.apkfuns.logutils.LogUtils;
import com.fanfull.libhard.serialport.AbsSerialPort;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPortGo extends AbsSerialPort {
  private SerialPort serialPort;

  public SerialPortGo(File device, int baudrate, int dataBits, int parity, int stopBits,
      int flags) throws SecurityException, IOException {
    //serialPort = new SerialPort(device, baudrate, flags);
    serialPort = SerialPort
        .newBuilder(device, baudrate)
        .flags(flags)
        .parity('n')
        .build();
    serialPortInfo = device.getPath() + ":" + baudrate;
    LogUtils.tag(TAG).i("open %s:%s",
        SerialPortGo.class.getSimpleName(), serialPortInfo);
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
