package com.fanfull.libhard.serialport;

import java.io.InputStream;
import java.io.OutputStream;

public interface ISerialPort {

    public abstract InputStream getInputStream();

    public abstract OutputStream getOutputStream();

    public abstract void close();
}
