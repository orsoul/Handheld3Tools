package com.fanfull.libhard.serialport;

import java.io.InputStream;
import java.io.OutputStream;

public interface ISerialPort {

    boolean send(byte[] data, int off, int len);

    boolean send(byte[] data);

    String getSerialPortInfo();

    InputStream getInputStream();

    OutputStream getOutputStream();

    void close();
}
