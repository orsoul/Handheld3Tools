package com.fanfull.libhard.gpio;

public class GpioFactary {
    private static GpioController instance = new GpioControllerRd();

    public static GpioController getGpioController() {
        return instance;
    }
}
