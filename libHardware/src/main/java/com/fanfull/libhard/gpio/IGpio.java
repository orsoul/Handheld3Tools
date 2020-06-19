package com.fanfull.libhard.gpio;

public interface IGpio {
    boolean init();

    boolean isInit();

    boolean close();

    boolean set(int index, boolean high);

    int getState(int index);

    boolean isHigh(int index);

    boolean isLow(int index);

    boolean setIO(int index, boolean input);

    boolean setMode(int index, int mode);
}
