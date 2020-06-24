package com.fanfull.libhard.gpio;

public abstract class AbsGpio implements IGpio {

    protected static final int HIGH = 1;
    protected static final int LOW = 0;
    protected boolean isInit;

    @Override
    public boolean isInit() {
        return isInit;
    }
}
