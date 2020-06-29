package com.fanfull.libhard.uhf;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.libhard.gpio.impl.GpioController;
import com.fanfull.libhard.serialport.ISerialPortListener;
import com.fanfull.libhard.serialport.impl.SerialPortController;

import java.io.IOException;

public class UhfOperationRd extends AbsUhfOperation {
    //    private final String SERIAL_PORT_PATH = "/dev/s3c2410_serial2"; /dev/s3c2410_serial2
    private final String SERIAL_PORT_PATH = "/dev/ttyMT0";
    private final int BUADRATE = 115200;
    private SerialPortController serialPortController;

    public UhfOperationRd() {

    }

    @Override
    public boolean open() throws SecurityException {
        try {
            serialPortController = SerialPortController.newBuilder(SERIAL_PORT_PATH, BUADRATE).build();
            serialPortController.setSerialPortListener(new ISerialPortListener() {
                @Override
                public void onReceiveData(byte[] data) {
                    if (uhfListener != null) {
                        uhfListener.onReceiveData(data);
                    }
                }
            });
            serialPortController.startReadThread();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        GpioController.getInstance().init();
        //        SystemClock.sleep(50);
        //        GpioController.getInstance().setMode(64, 0);
        //        GpioController.getInstance().setMode(62, 0);
        //        GpioController.getInstance().setIO(64, false);
        //        GpioController.getInstance().setIO(62, false);
        //        GpioController.getInstance().set(64, true);
        //        GpioController.getInstance().set(62, false);

        //        Platform.SetGpioMode(64, 0);
        //        Platform.SetGpioMode(62, 0);
        //        Platform.SetGpioOutput(64);
        //        Platform.SetGpioOutput(62);
        //        Platform.SetGpioDataHigh(64);
        //        Platform.SetGpioDataLow(62);

        isOpen = true;
        if (uhfListener != null) {
            uhfListener.onOpen();
        }
        return true;
    }

    @Override
    public void release() {
        serialPortController.close();
    }

    @Override
    public boolean send(byte[] data) {
        //        Platform.SetGpioMode(64, 0);
        //        Platform.SetGpioMode(62, 0);
        //        Platform.SetGpioOutput(64);
        //        Platform.SetGpioOutput(62);
        //        Platform.SetGpioDataHigh(64);
        //        Platform.SetGpioDataLow(62);

        //        GpioController.getInstance().setMode(64, 0);
        //        GpioController.getInstance().setMode(62, 0);
        //        GpioController.getInstance().setIO(64, false);
        //        GpioController.getInstance().setIO(62, false);
        //        GpioController.getInstance().set(64, true);
        //        GpioController.getInstance().set(62, false);
        boolean isUhfMode = setGpioUhfMode();
        LogUtils.d("isUhfMode:%s", isUhfMode);
        return isUhfMode && serialPortController.send(data);
    }

    private boolean setGpioUhfMode() {

        boolean reVal = GpioController.getInstance().init()
                && GpioController.getInstance().setMode(64, 0)
                && GpioController.getInstance().setMode(62, 0)
                && GpioController.getInstance().setIO(64, false)
                && GpioController.getInstance().setIO(62, false)
                && GpioController.getInstance().set(64, true)
                && GpioController.getInstance().set(62, false);

        return reVal;
    }
}
