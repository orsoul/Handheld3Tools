package com.fanfull.libhard.serialport.impl;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.libhard.serialport.ISerialPort;
import com.fanfull.libhard.serialport.ISerialPortListener;

import org.orsoul.baselib.util.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class SerialPortController implements ISerialPort {
    private ISerialPort serialPort;
    private ISerialPortListener serialPortListener;
    private SerialPostReadThread readThread;

    public SerialPortController(ISerialPort serialPort) {
        this.serialPort = serialPort;
    }

    public void setSerialPortListener(ISerialPortListener serialPortListener) {
        this.serialPortListener = serialPortListener;
    }

    public synchronized void startReadThread() {
        if (readThread == null || readThread.getState() == Thread.State.TERMINATED) {
            readThread = new SerialPostReadThread();
            readThread.start();
        }
    }

    public synchronized void stopReadThread() {
        if (readThread != null) {
            readThread.stopRead();
            readThread.interrupt();
            readThread = null;
        }
    }

    @Override
    public boolean send(byte[] data, int off, int len) {
        boolean send = serialPort.send(data, off, len);
        return send;
    }

    @Override
    public boolean send(byte[] data) {
        return serialPort.send(data);
    }

    @Override
    public String getSerialPortInfo() {
        return serialPort.getSerialPortInfo();
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
        stopReadThread();
        serialPort.close();
    }

    private class SerialPostReadThread extends Thread {
        private boolean stopped;

        public synchronized boolean isStop() {
            return stopped;
        }

        public synchronized void stopRead() {
            this.stopped = true;
        }

        @Override
        public void run() {
            LogUtils.w("run start");
            int len;
            byte[] buff = new byte[1024 * 16];
            InputStream in = serialPort.getInputStream();
            while (!isStop()) {
                try {
                    len = in.read(buff);
                    LogUtils.d("%s rec:%s", getSerialPortInfo(), ArrayUtils.bytes2HexString(buff, 0, len));
                    if (len < 1) {
                        break;
                    }
                    if (serialPortListener != null) {
                        serialPortListener.onReceiveData(Arrays.copyOf(buff, len));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            stopRead();
            LogUtils.w("run finish");
        }
    }

    public static SerialPortController.Builder newBuilder(File device, int baudrate) {
        return new SerialPortController.Builder(device, baudrate);
    }

    public static SerialPortController.Builder newBuilder(String devicePath, int baudrate) {
        return new SerialPortController.Builder(devicePath, baudrate);
    }

    public final static class Builder {

        private File device;
        private int baudrate;
        private int dataBits = 8;
        private int parity = 0;
        private int stopBits = 1;
        private int flags = 0;

        private Builder(File device, int baudrate) {
            this.device = device;
            this.baudrate = baudrate;
        }

        private Builder(String devicePath, int baudrate) {
            this(new File(devicePath), baudrate);
        }

        /**
         * 数据位
         * @param dataBits 默认8,可选值为5~8
         * @return
         */
        public SerialPortController.Builder dataBits(int dataBits) {
            this.dataBits = dataBits;
            return this;
        }

        /**
         * 校验位
         * @param parity 0:无校验位(NONE，默认)；1:奇校验位(ODD);2:偶校验位(EVEN)
         * @return
         */
        public SerialPortController.Builder parity(int parity) {
            this.parity = parity;
            return this;
        }

        /**
         * 停止位
         * @param stopBits 默认1；1:1位停止位；2:2位停止位
         * @return
         */
        public SerialPortController.Builder stopBits(int stopBits) {
            this.stopBits = stopBits;
            return this;
        }

        /**
         * 标志
         * @param flags 默认0
         * @return
         */
        public SerialPortController.Builder flags(int flags) {
            this.flags = flags;
            return this;
        }

        /**
         * 打开并返回串口
         * @return
         * @throws SecurityException
         * @throws IOException
         */
        public SerialPortController build() throws SecurityException, IOException {
            return new SerialPortController(new SerialPortRd(device, baudrate, dataBits, parity, stopBits, flags));
        }
    }
}
