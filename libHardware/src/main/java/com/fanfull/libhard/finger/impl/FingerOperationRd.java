package com.fanfull.libhard.finger.impl;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.libhard.finger.AbsFingerOperation;
import com.fanfull.libhard.gpio.impl.GpioController;
import com.fanfull.libhard.serialport.ISerialPortListener;
import com.fanfull.libhard.serialport.impl.SerialPortController;
import java.io.IOException;
import java.util.Arrays;
import org.orsoul.baselib.util.BytesUtil;

public class FingerOperationRd extends AbsFingerOperation {
  private final String SERIAL_PORT_PATH = "/dev/ttyMT0";
  private final int BUADRATE = 115200;

  private SerialPortController serialPortController;
  private ISerialPortListener serialPortListener;

  public FingerOperationRd() {
  }

  @Override
  public boolean open() throws SecurityException {
    if (isOpen) {
      return true;
    }
    try {
      serialPortController = SerialPortController.newBuilder(SERIAL_PORT_PATH, BUADRATE).build();
      serialPortListener = new ISerialPortListener() {
        @Override public void onReceiveData(byte[] data) {
          if (fingerListener != null) {
            fingerListener.onReceiveData(data);
          }
        }
      };
      serialPortController.addSerialPortListener(serialPortListener);
      serialPortController.startReadThread();
      serialPortController.addUseCount();
    } catch (IOException e) {
      e.printStackTrace();
      if (fingerListener != null) {
        fingerListener.onOpen(false);
      }
      return false;
    }

    boolean init = GpioController.getInstance().init();
    LogUtils.d("init:%s", init);
    setGpioFingerMode();

    isOpen = true;
    if (fingerListener != null) {
      fingerListener.onOpen(true);
    }
    return true;
  }

  @Override
  public void release() {
    serialPortController.removeSerialPortListener(serialPortListener);
    serialPortController.minUseCount();
    if (serialPortController.getUseCount() == 0) {
      serialPortController.close();
    }
    isOpen = false;
  }

  @Override
  public boolean send(byte[] data) {
    boolean isUhfMode = setGpioFingerMode();
    boolean send = serialPortController.send(data);
    LogUtils.v("setFingerMode:%s", send);
    return send;
  }

  private boolean setGpioFingerMode() {

    boolean[] res = new boolean[6];
    //        res[0] = Platform.SetGpioMode(64, 0);
    //        res[1] = Platform.SetGpioMode(62, 0);
    //        res[2] = Platform.SetGpioOutput(64);
    //        res[3] = Platform.SetGpioOutput(62);
    //        res[4] = Platform.SetGpioDataHigh(64);
    //        res[5] = Platform.SetGpioDataHigh(62);
    //        LogUtils.d("%s", Arrays.toString(res));

    res[0] = GpioController.getInstance().setMode(64, 0);
    res[1] = GpioController.getInstance().setMode(62, 0);
    res[2] = GpioController.getInstance().setIO(64, false);
    res[3] = GpioController.getInstance().setIO(62, false);
    res[4] = GpioController.getInstance().set(64, true);
    res[5] = GpioController.getInstance().set(62, true);
    LogUtils.d("%s", Arrays.toString(res));
    return true;
  }

  /**
   * 执行生成图像指令、生成特征码指令.
   *
   * @param buffId 存放特征码的缓冲区号，只能为1或2
   * @return 添加指纹成功返回0，否则返回正整数确认码，<br/>
   * 确认码=00H 表示搜索到<br/>
   * 确认码=01H 表示收包有错<br/>
   * 确认码=02H 表示传感器上无手指<br/>
   * 确认码=03H 表示录入不成功<br/>
   *
   * 确认码=06H 表示指纹图像太乱而生不成特征<br/>
   * 确认码=07H 表示指纹图像正常，但特征点太少而生不成特征<br/>
   * 确认码=15H 表示图像缓冲区内没有有效原始图而生不成图像<br/>
   */
  public int genFingerFeature(int buffId) {
    byte[] getImageBuff = serialPortController.sendAndWaitReceive(FingerPrintCmd.CMD_GET_IMAGE);
    //确认码=00H 表示录入成功；
    //确认码=01H 表示收包有错；
    //确认码=02H 表示传感器上无手指；
    //确认码=03H 表示录入不成功；
    int res = FingerPrintCmd.getFingerRes(getImageBuff);

    if (0 == res) {
      byte[] genCharBuff =
          serialPortController.sendAndWaitReceive(FingerPrintCmd.getCmdGenChar(buffId), 900);
      //确认码=00H 表示生成特征成功；
      //确认码=01H 表示收包有错；
      //确认码=06H 表示指纹图像太乱而生不成特征；
      //确认码=07H 表示指纹图像正常，但特征点太少而生不成特征；
      //确认码=15H 表示图像缓冲区内没有有效原始图而生不成图像；
      res = FingerPrintCmd.getFingerRes(genCharBuff);
    }
    LogUtils.d("genFingerFeature:%s,  buffId:%s", res, buffId);
    return res;
  }

  public int genFingerFeature() {
    return genFingerFeature(FingerPrintCmd.BUFFER_ID);
  }

  /**
   * 使用【合并特征（生成模板） PS_RegModel】指令添加指纹.
   *
   * @param pageId 指纹库位置号.
   * @return 添加指纹成功返回0，否则返回正整数确认码，<br/>
   * 确认码=00H 表示搜索到<br/>
   * 确认码=01H 表示收包有错<br/>
   * 确认码=02H 表示传感器上无手指<br/>
   * 确认码=03H 表示录入不成功<br/>
   *
   * 确认码=06H 表示指纹图像太乱而生不成特征<br/>
   * 确认码=07H 表示指纹图像正常，但特征点太少而生不成特征<br/>
   * 确认码=15H 表示图像缓冲区内没有有效原始图而生不成图像<br/>
   *
   * 确认码=0AH 表示合并失败（两枚指纹不属于同一手指）<br/>
   * 确认码=0bH 表示 PageID 超出指纹库范围<br/>
   * 确认码=18H 表示写 FLASH 出错<br/>
   */
  public int addFinger(int pageId, byte[] fingerFeatureBuff) {
    setGpioFingerMode();

    int res = genFingerFeature(FingerPrintCmd.BUFFER_ID_1);
    if (res == FingerPrintCmd.RES_CODE_SUCCESS) {
      res = genFingerFeature(FingerPrintCmd.BUFFER_ID_2);
    }

    byte[] buff;
    if (res == FingerPrintCmd.RES_CODE_SUCCESS) {
      buff = serialPortController.sendAndWaitReceive(FingerPrintCmd.CMD_REG_MODEL, 1000);
      res = FingerPrintCmd.getFingerRes(buff);
    }

    if (res == FingerPrintCmd.RES_CODE_SUCCESS) {
      res = getFingerFeature(fingerFeatureBuff);
    }

    if (res == FingerPrintCmd.RES_CODE_SUCCESS) {
      buff = serialPortController.sendAndWaitReceive(FingerPrintCmd.getCmdStoreChar(pageId));
      res = FingerPrintCmd.getFingerRes(buff);
    }

    LogUtils.d("addFinger to %s,res：%s", pageId, res);
    return res;
  }

  public int loadFinger(int pageId, byte[] fingerFeatureBuff) {

    if (fingerFeatureBuff == null
        || fingerFeatureBuff.length != FingerPrintCmd.FINGER_FEATURE_LEN) {
      return -2;
    }

    setGpioFingerMode();

    byte[] buff;
    buff = serialPortController.sendAndWaitReceive(FingerPrintCmd.getCmdSaveFingerFeature(), 1000);
    int res = FingerPrintCmd.getFingerRes(buff);

    if (res == FingerPrintCmd.RES_CODE_SUCCESS) {
      byte[] cmdBuff = FingerPrintCmd.convertFingerFeature2Cmd(fingerFeatureBuff);
      if (cmdBuff != null) {
        buff =
            serialPortController.sendAndWaitReceive(FingerPrintCmd.getCmdSaveFingerFeature(pageId));
        res = FingerPrintCmd.getFingerRes(buff);
      }
    }

    if (res == FingerPrintCmd.RES_CODE_SUCCESS) {
      buff = serialPortController.sendAndWaitReceive(FingerPrintCmd.getCmdStoreChar(pageId));
      res = FingerPrintCmd.getFingerRes(buff);
    }

    LogUtils.d("loadFinger to %s, res：%s", pageId, res);
    return res;
  }

  /**
   * 使用【自动注册模板 PS_Enroll】指令添加指纹.
   *
   * @param fingerIdBuff 添加指纹成功时 用来保存所添加指纹的指纹ID
   * @return 添加指纹成功返回0，指纹模块回复异常返回-1，否则返回正整数确认码：<br/>
   * 确认码=00H 表示搜索到<br/>
   * 确认码=01H 表示收包有错<br/>
   * 确认码=02H 表示传感器上无手指<br/>
   * 确认码=09H 表示没搜索到<br/>
   */
  @Override public int addFinger(int[] fingerIdBuff) {
    setGpioFingerMode();
    byte[] buff;
    buff = serialPortController.sendAndWaitReceive(FingerPrintCmd.CMD_ADD_FINGER, 1000);
    int res = FingerPrintCmd.getFingerRes(buff);
    LogUtils.d("addFinger:%s", res);

    if (res == FingerPrintCmd.RES_CODE_SUCCESS && fingerIdBuff != null && 0 < fingerIdBuff.length) {
      fingerIdBuff[0] = (buff[10] << 8) | (buff[11] & 0xFF);
      LogUtils.d("fingerId:%s", fingerIdBuff[0]);
    }
    return res;
  }

  @Override public int addFinger(int[] fingerIdBuff, byte[] fingerFeatureBuff) {
    int res = addFinger(fingerIdBuff);
    if (fingerFeatureBuff != null) {
      res = getFingerFeature(fingerIdBuff[0], fingerFeatureBuff);
    }
    return res;
  }

  /**
   * 8. 上传特征或模板 PS_UpChar.获取特征码 指令，使用 bufferID1.
   *
   * 收到的数据 由5条指令拼接而成，第1条指令长12字节，说明执行情况；
   * 后面4条每条长139字节，其中[9-137]为128字节的部分指纹特征码，4条部分特征码合并成512字节的完整特征码.
   *
   * @param fingerFeatureBuff 接收指纹特征码，长度必须大于或等于512，否则返回-2
   * @return 获取成功返回 0
   */
  private int getFingerFeature(byte[] fingerFeatureBuff) {

    if (fingerFeatureBuff == null
        || fingerFeatureBuff.length < FingerPrintCmd.FINGER_FEATURE_LEN) {
      return -2;
    }

    byte[] buff = serialPortController.sendAndWaitReceive(
        FingerPrintCmd.getCmdGetFingerFeature(), 1500);

    int res = -1;
    if (buff != null && buff.length == FingerPrintCmd.CMD_FINGER_FEATURE_LEN) {
      res = FingerPrintCmd.getFingerRes(buff);
    }
    LogUtils.d("getFingerFeature:%s", res);
    if (res != 0) {
      // 未获取 指纹特征码
      return res;
    }

    // 到这里已获得 512字节指纹特征码
    // 收到的数据 由5条指令拼接而成，第1条指令长12字节，说明执行情况；
    // 后面4条每条长139字节，其中[9-137]为128字节的部分指纹特征码，4条部分特征码合并成512字节的完整特征码.
    for (int i = 0; i < 4; i++) {
      int srcPos = 12 + i * 139 + 9;
      int destPos = i * 128;
      System.arraycopy(buff, srcPos, fingerFeatureBuff, destPos, 128);
    }

    LogUtils.d("finger Feature:%s", BytesUtil.bytes2HexString(fingerFeatureBuff));
    //LogUtils.d("finger Feature:%s", Base64.encodeToString(fingerFeatureBuff, Base64.DEFAULT));
    return 0;
  }

  /**
   * 7. 读出模板 PS_LoadChar,读出指定位置指纹的特征码 指令，使用 bufferID1.<br/>
   * 8. 上传特征或模板 PS_UpChar.获取特征码 指令，使用 bufferID1.<br/>
   *
   * 收到的数据 由5条指令拼接而成，第1条指令长12字节，说明执行情况；
   * 后面4条每条长139字节，其中[9-137]为128字节的部分指纹特征码，4条部分特征码合并成512字节的完整特征码.
   *
   * @param fingerFeatureBuff 接收指纹特征码，长度必须大于或等于512，否则返回-2
   * @return 获取成功返回 0
   */
  public int getFingerFeature(int fingerId, byte[] fingerFeatureBuff) {
    setGpioFingerMode();

    byte[] buff = serialPortController.sendAndWaitReceive(
        FingerPrintCmd.getCmdLoadFingerFeature(fingerId), 1000);
    int res = FingerPrintCmd.getFingerRes(buff);
    if (res == FingerPrintCmd.RES_CODE_SUCCESS) {
      res = getFingerFeature(fingerFeatureBuff);
    }
    return res;
  }

  /**
   * 使用【自动验证指纹 PS_Identify】指令 搜索指纹.
   *
   * @param fingerIdBuff 搜索指纹成功时 用来保存结果，长度为2，resBuff[0]为指纹所在指纹库的pageId，resBuff[1]为指纹得分.
   * @return 搜索指纹成功返回0，否则返回正整数确认码，<br/>
   * 确认码=00H 表示搜索到<br/>
   * 确认码=01H 表示收包有错<br/>
   * 确认码=02H 表示传感器上无手指<br/>
   * 确认码=09H 表示没搜索到<br/>
   */
  @Override public int searchFinger(int[] fingerIdBuff) {
    if (fingerIdBuff == null || fingerIdBuff.length < 2) {
      return -2;
    }

    setGpioFingerMode();
    byte[] buff;
    buff = serialPortController.sendAndWaitReceive(FingerPrintCmd.CMD_SEARCH_IDENTIFY, 1500);
    int res = FingerPrintCmd.getFingerRes(buff);
    LogUtils.d("searchFinger:%s", res);

    if (res == FingerPrintCmd.RES_CODE_SUCCESS) {
      fingerIdBuff[0] = (buff[10] << 8) | (buff[11] & 0xFF);
      fingerIdBuff[1] = (buff[12] << 8) | (buff[13] & 0xFF);
      LogUtils.d("pageId:%s, score:%s", fingerIdBuff[0], fingerIdBuff[1]);
    }
    return res;
  }

  @Override public int searchFinger(int[] fingerIdBuff, byte[] featureBuff) {
    int res = searchFinger(fingerIdBuff);
    if (res == FingerPrintCmd.RES_CODE_SUCCESS) {
      res = getFingerFeature(fingerIdBuff[0], featureBuff);
    }
    return res;
  }

  @Override public int getFingerNum(int[] fingerNumBuff) {
    setGpioFingerMode();
    byte[] buff;
    buff = serialPortController.sendAndWaitReceive(FingerPrintCmd.CMD_FINGER_NUM);
    int res = FingerPrintCmd.getFingerRes(buff);
    LogUtils.d("getFingerNum res:%s", res);

    if (res == FingerPrintCmd.RES_CODE_SUCCESS
        && fingerNumBuff != null
        && 1 <= fingerNumBuff.length) {
      fingerNumBuff[0] = (buff[10] << 8) | (buff[11] & 0xFF);
      LogUtils.d("getFingerNum num：%s", fingerNumBuff[0]);
    }
    return res;
  }

  /**
   * 搜索指纹.
   *
   * @param fingerIdBuff 保存结果，长度为2，resBuff[0]为指纹所在指纹库的pageId，resBuff[1]为指纹得分.
   * @return 非指纹指令返回-1；否则返回确认码，即 cmd[9]，返回0即为 搜索到结果
   */
  public int searchFinger2(int[] fingerIdBuff) {
    setGpioFingerMode();
    int res = genFingerFeature();
    if (res == FingerPrintCmd.RES_CODE_SUCCESS) {
      byte[] buff = serialPortController.sendAndWaitReceive(FingerPrintCmd.getCmdSearch(), 1500);
      res = FingerPrintCmd.getFingerRes(buff);
      LogUtils.d("searchFinger2:%s", res);

      if (res == FingerPrintCmd.RES_CODE_SUCCESS
          && fingerIdBuff != null
          && 2 <= fingerIdBuff.length) {
        fingerIdBuff[0] = (buff[10] << 8) | (buff[11] & 0xFF);
        fingerIdBuff[1] = (buff[12] << 8) | (buff[13] & 0xFF);
        LogUtils.d("fingerId:%s, score:%s", fingerIdBuff[0], fingerIdBuff[1]);
      }
    }
    return res;
  }

  @Override public boolean clearFinger() {
    setGpioFingerMode();
    int res;
    byte[] buff = serialPortController.sendAndWaitReceive(FingerPrintCmd.CMD_CLEAR_FINGER);
    res = FingerPrintCmd.getFingerRes(buff);
    LogUtils.d("clearFinger:%s", res);
    return res == FingerPrintCmd.RES_CODE_SUCCESS;
  }
}
