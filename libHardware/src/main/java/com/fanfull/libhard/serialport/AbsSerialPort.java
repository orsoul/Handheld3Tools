package com.fanfull.libhard.serialport;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.libjava.util.BytesUtil;

import java.io.OutputStream;

public abstract class AbsSerialPort implements ISerialPort {
  protected String TAG = this.getClass().getSimpleName();
  protected String serialPortInfo;

  @Override
  public boolean send(byte[] data, int off, int len) {
    try {
      OutputStream out = getOutputStream();
      out.write(data, off, len);
      out.flush();
      LogUtils.tag(TAG).d("send:%s", BytesUtil.bytes2HexString(data, off, off + len));
      return true;
    } catch (Exception e) {
      LogUtils.tag(TAG).w("%s", e.getMessage());
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
