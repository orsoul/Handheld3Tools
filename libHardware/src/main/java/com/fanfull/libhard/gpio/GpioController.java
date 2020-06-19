package com.fanfull.libhard.gpio;

public abstract class GpioController implements IGpio {

    public static final int HIGH;
    public static final int LOW;

    protected boolean isInit;

    static {
        HIGH = 1;
        LOW = 0;
    }

    @Override
    public boolean isInit() {
        return isInit;
    }
}
