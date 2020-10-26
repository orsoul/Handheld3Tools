package com.fanfull.libhard.serialport;

import com.apkfuns.logutils.LogUtils;
import java.io.OutputStream;
import org.orsoul.baselib.util.BytesUtil;

public abstract class AbsSerialPort implements ISerialPort {
  protected String TAG = this.getClass().getSimpleName();
  protected String serialPortInfo;

  @Override
  public boolean send(byte[] data, int off, int len) {
    try {
      OutputStream out = getOutputStream();
      out.write(data, off, len);
      out.flush();
      LogUtils.tag(TAG).i("send:%s", BytesUtil.bytes2HexString(data, off, off + len));
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
