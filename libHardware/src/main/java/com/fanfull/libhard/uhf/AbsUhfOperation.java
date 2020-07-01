package com.fanfull.libhard.uhf;

public abstract class AbsUhfOperation implements IUhfOperation {

    protected IUhfListener uhfListener;

    protected boolean isOpen;
    protected boolean isScanning;

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public boolean isScanning() {
        return isScanning;
    }

    @Override
    public void setListener(IUhfListener listener) {
        uhfListener = listener;
    }
}
