package com.fanfull.libhard.serialport;

import com.apkfuns.logutils.LogUtils;

import org.orsoul.baselib.util.ArrayUtils;

import java.io.OutputStream;

public abstract class AbsSerialPort implements ISerialPort {

    protected String serialPortInfo;

    @Override
    public boolean send(byte[] data, int off, int len) {
        try {
            OutputStream out = getOutputStream();
            out.write(data, off, len);
            out.flush();
            LogUtils.d("%s", ArrayUtils.bytes2HexString(data, off, off + len));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean send(byte[] data) {
        if (data == null || data.length < 1) {
            return false;
        }
        return send(data, 0, data.length);
    }

    @Override
    public String getSerialPortInfo() {
        return serialPortInfo;
    }
}
