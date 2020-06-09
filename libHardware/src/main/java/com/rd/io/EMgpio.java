package com.rd.io;

public class EMgpio {
    public static native boolean GPIOInit();

    public static native boolean GPIOUnInit();

    public static native boolean SetGpioInput(int gpio_index);

    public static native boolean SetGpioOutput(int gpio_index);

    public static native boolean SetGpioDataHigh(int gpio_index);

    public static native boolean SetGpioDataLow(int gpio_index);

    public static native int GetGpioState(int gpio_index);

    public static native boolean setGpioMode(int gpio_index, int mode);

    static {
        System.loadLibrary("rd_gpio_jni");
        System.loadLibrary("hyio_gpio_api");
    }
}
