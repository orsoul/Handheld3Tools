package com.fanfull.libhard.gpio.impl;

import com.fanfull.libhard.gpio.IGpio;

public class GpioController implements IGpio {

    private IGpio gpio;

    private GpioController(IGpio gpio) {
        this.gpio = gpio;
    }

    @Override
    public boolean init() {
        return gpio.init();
    }

    @Override
    public boolean isInit() {
        return gpio.isInit();
    }

    @Override
    public boolean release() {
        return gpio.release();
    }

    @Override
    public boolean set(int index, boolean high) {
        return gpio.set(index, high);
    }

    @Override
    public int getState(int index) {
        return gpio.getState(index);
    }

    @Override
    public boolean isHigh(int index) {
        return gpio.isHigh(index);
    }

    @Override
    public boolean isLow(int index) {
        return gpio.isLow(index);
    }

    @Override
    public boolean setIO(int index, boolean input) {
        return gpio.setIO(index, input);
    }

    @Override
    public boolean setMode(int index, int mode) {
        return gpio.setMode(index, mode);
    }

    private static class SingletonHolder {
        private static final GpioController instance = new GpioController(new GpioRd());
    }

    public static GpioController getInstance() {
        return GpioController.SingletonHolder.instance;
    }
}
