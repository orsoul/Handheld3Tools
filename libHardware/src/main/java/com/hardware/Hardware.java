package com.hardware;

public class Hardware {
    /* oled */
    public native int openGPIO();

    public native int openSPI();

    public native int transferString(int fd_spi, char len, char XSpos,
                                     char YSpos, char CharWidth, char CharHeight, long BackColor,
                                     long TextColor, byte[] buffer);

    public native int clearScreen(int fd_spi, char XSpos, char XCpos,
                                  char YSpos, char YCpos);

    public native int fillScreen(int fd_spi, char XSpos, char XCpos,
                                 char YSpos, char YCpos, long FillColor);

    public native int copyRect(int fd_spi, char XSpos, char XCpos, char YSpos,
                               char YCpos, char NewXSpos, char NewYSpos);

    public native int drawRect(int fd_spi, char XSpos, char XCpos, char YSpos,
                               char YCpos, long LineColor, long FillColor);

    public native int drawLine(int fd_spi, char XSpos, char XCpos, char YSpos,
                               char YCpos, long LineColor);

    /* I2C */
    public native int openI2CDevice();

    public native int writeByteDataToI2C(int fd, int pos, byte byteData);

    public native int readByteDataFromI2C(int fd, int pos, byte[] data);

    public native int sleep(int fd, int cmd);

    public native int setGPIO(int state, int port);

    public native int reset();

    public native int closeGPIO();

    public native int closeSPI();

    public native int openSerialPort(String devName, long baud, int dataBits,
                                     int stopBits);

    public native int write(int fd, byte[] data);

    public native int read(int fd, byte[] buf, int len);

    /**
     * @param fd
     * @param sec  秒
     * @param usec 毫秒
     * @description 设定 取读头数据的频率, 时间隔由 第2、3参数决定
     */
    public native int select(int fd, int sec, int usec);

    public native void close(int fd);

    public native void DecodeBarcode(byte[] data);

    public native String unimplementedStringFromJNI();

    private static Hardware mHardware = new Hardware();

    static {
        System.loadLibrary("hardware_jni");
    }

    private Hardware() {

    }

    public static Hardware getInstance() {
        return mHardware;
    }

    public static void decodeBarcode(byte[] data) {
        if (null != mHardware) {
            mHardware.DecodeBarcode(data);
        }
    }
}
