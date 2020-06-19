package com.fanfull.libhard.serialport;

import com.rd.io.SerialPort;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPortRd implements ISerialPort {
    private SerialPort serialPort;

    protected SerialPortRd(File device, int baudrate, int flags) throws SecurityException, IOException {
        serialPort = new SerialPort(device, baudrate, flags);
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
        serialPort.close();
    }
}
