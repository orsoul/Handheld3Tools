package com.fanfull.libhard.nfc;

public class RfidController implements IRfidOperation {
    private static final RfidController ourInstance = new RfidController(new RfidOperationRd());

    public static RfidController getInstance() {
        return ourInstance;
    }

    private RfidController(IRfidOperation uhfOperation) {
        this.operation = uhfOperation;
    }

    private IRfidOperation operation;

    @Override
    public boolean open() {
        return operation.open();
    }

    @Override
    public boolean isOpen() {
        return operation.isOpen();
    }

    @Override
    public boolean isScanning() {
        return operation.isScanning();
    }

    @Override
    public void release() {
        operation.release();
    }

    @Override
    public void setListener(IRfidListener listener) {
        operation.setListener(listener);
    }

    @Override
    public void findNfcAsync() {
        operation.findNfcAsync();
    }

    @Override
    public byte[] findM1() {
        return operation.findM1();
    }

    @Override
    public void findM1Async() {
        operation.findM1Async();
    }

    @Override
    public byte[] findNfcOrM1() {
        return operation.findNfcOrM1();
    }

    @Override
    public void findNfcOrM1Async() {
        operation.findNfcOrM1Async();
    }

    @Override
    public byte[] findNfc() {
        return operation.findNfc();
    }

    @Override
    public void readNfcAsync(int sa, int dataLen) {
        operation.readNfcAsync(sa, dataLen);
    }

    @Override
    public boolean readNfc(int sa, byte[] buff, int len) {
        return operation.readNfc(sa, buff, len);
    }

    @Override
    public boolean readNfc(int sa, byte[] buff) {
        return operation.readNfc(sa, buff);
    }
}
