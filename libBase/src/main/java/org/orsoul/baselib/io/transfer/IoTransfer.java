package org.orsoul.baselib.io.transfer;

public interface IoTransfer {

  boolean send(byte[] data, int offset, int len);

  boolean send(byte[] data);

  boolean send(byte[] data, int timeout, IoTransferListener listener);

  byte[] sendAndWaitReceive(byte[] data, int timeout);

  boolean isRunning();

  boolean startReceive();

  void stopReceive();

  void dispatchReceiveData(byte[] data);
}
