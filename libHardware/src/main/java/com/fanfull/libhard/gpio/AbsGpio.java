package com.fanfull.libhard.gpio;

public abstract class AbsGpio implements IGpio {

  public static final int HIGH = 1;
  public static final int LOW = 0;
    protected boolean isInit;

    @Override
    public boolean isInit() {
        return isInit;
    }
}
