package com.fanfull.libhard.gpio;

import com.rd.io.EMgpio;

public class GpioControllerRd extends GpioController {

    @Override
    public boolean init() {
        isInit = EMgpio.GPIOInit();
        return isInit;
    }


    @Override
    public boolean close() {
        boolean unInit = EMgpio.GPIOUnInit();
        if (unInit) {
            isInit = false;
        }
        return unInit;
    }

    @Override
    public boolean set(int index, boolean high) {
        if (high) {
            return EMgpio.SetGpioDataHigh(index);
        } else {
            return EMgpio.SetGpioDataLow(index);
        }
    }

    @Override
    public int getState(int index) {
        return EMgpio.GetGpioState(index);
    }

    @Override
    public boolean isHigh(int index) {
        return getState(index) == HIGH;
    }

    @Override
    public boolean isLow(int index) {
        return getState(index) == LOW;
    }

    @Override
    public boolean setIO(int index, boolean input) {
        if (input) {
            return EMgpio.SetGpioInput(index);
        } else {
            return EMgpio.SetGpioOutput(index);
        }
    }

    @Override
    public boolean setMode(int index, int mode) {
        return EMgpio.setGpioMode(index, mode);
    }
}
