package com.fanfull.libhard.uhf;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.libhard.gpio.impl.GpioController;
import com.fanfull.libhard.serialport.ISerialPortListener;
import com.fanfull.libhard.serialport.impl.SerialPortController;
import java.io.IOException;
import java.util.Arrays;

public class UhfOperationRd extends AbsUhfOperation {
    private final String SERIAL_PORT_PATH = "/dev/ttyMT0";
    private final int BUADRATE = 115200;
    private SerialPortController serialPortController;
  private ISerialPortListener serialPortListener;

  public UhfOperationRd() {
  }

    @Override
    public boolean open() throws SecurityException {
      if (isOpen) {
        return true;
      }
        try {
            serialPortController = SerialPortController.newBuilder(SERIAL_PORT_PATH, BUADRATE).build();
          serialPortListener = data -> {
            if (uhfListener != null) {
              uhfListener.onReceiveData(data);
            }
          };
          serialPortController.addSerialPortListener(serialPortListener);
            serialPortController.startReadThread();
          serialPortController.addUseCount();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

      boolean init = GpioController.getInstance().init();
      LogUtils.d("init:%s", init);

        isOpen = true;
        if (uhfListener != null) {
            uhfListener.onOpen();
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
        boolean isUhfMode = setGpioUhfMode();
      //        LogUtils.d("isUhfMode:%s", isUhfMode);
        return isUhfMode && serialPortController.send(data);
    }

    private boolean setGpioUhfMode() {

      //        boolean reVal = GpioController.getInstance().init()
      //                && GpioController.getInstance().setMode(64, 0)
      //                && GpioController.getInstance().setMode(62, 0)
      //                && GpioController.getInstance().setIO(64, false)
      //                && GpioController.getInstance().setIO(62, false)
      //                && GpioController.getInstance().set(64, true)
      //                && GpioController.getInstance().set(62, false);

      boolean[] res = new boolean[6];
      //        res[0] = Platform.SetGpioMode(64, 0);
      //        res[1] = Platform.SetGpioMode(62, 0);
      //        res[2] = Platform.SetGpioOutput(64);
      //        res[3] = Platform.SetGpioOutput(62);
      //        res[4] = Platform.SetGpioDataHigh(64);
      //        res[5] = Platform.SetGpioDataLow(62);
      //        LogUtils.d("%s", Arrays.toString(res));

      res[0] = GpioController.getInstance().setMode(64, 0);
      res[1] = GpioController.getInstance().setMode(62, 0);
      res[2] = GpioController.getInstance().setIO(64, false);
      res[3] = GpioController.getInstance().setIO(62, false);
      res[4] = GpioController.getInstance().set(64, true);
      res[5] = GpioController.getInstance().set(62, false);
      LogUtils.d("%s", Arrays.toString(res));

      return true;
    }
}
