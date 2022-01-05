package org.orsoul.baselib.tcp.msg;

/**
 * 前置下 设备通讯 通用协议
 *
 * 发起端：$1002 xx ip func data num#
 * xx：01手持，02天线柜
 * ip：设备ip，发起端可任意设置，具体内容由前置转发时设置
 * func：功能号
 * data：数据段
 *
 * 回复端：`*1002 xx ip func data num#`
 * ip：设备ip，回复端
 */
public abstract class BaseSocketMessage4qz1002<T> extends BaseSocketMessage4qz<T> {
  public final static int FRAME = FUNC_TC2;
  public final static String DEVICE_TYPE = "01";
  public final static String IP = "0";

  protected String deviceType;

  public BaseSocketMessage4qz1002() {
  }

  public BaseSocketMessage4qz1002(BaseSocketMessage4qz msg) {
    super(msg.getFunc(), msg.getSplit(), msg.getMsgNum());
  }

  public BaseSocketMessage4qz1002(String deviceType, int func, String[] split, int num) {
    super(func, split, num);
    this.deviceType = deviceType;
  }

  public String getDeviceType() {
    return deviceType;
  }

  public void setDeviceType(String deviceType) {
    this.deviceType = deviceType;
  }
}
