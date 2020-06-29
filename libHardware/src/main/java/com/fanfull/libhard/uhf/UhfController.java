package com.fanfull.libhard.uhf;

public class UhfController implements IUhfOperation {
    private IUhfOperation uhfOperation;

    public UhfController(IUhfOperation uhfOperation) {
        this.uhfOperation = uhfOperation;
    }

    @Override
    public boolean open() {
        return uhfOperation.open();
    }

    @Override
    public boolean isOpen() {
        return uhfOperation.isOpen();
    }

    @Override
    public void release() {
        uhfOperation.release();
    }

    @Override
    public boolean send(byte[] data) {
        return uhfOperation.send(data);
    }

    @Override
    public void setListener(IUhfListener listener) {
        uhfOperation.setListener(listener);
    }

    private static class SingletonHolder {
        private static final UhfController instance = new UhfController(new UhfOperationRd());
    }

    public static UhfController getInstance() {
        return UhfController.SingletonHolder.instance;
    }
}
