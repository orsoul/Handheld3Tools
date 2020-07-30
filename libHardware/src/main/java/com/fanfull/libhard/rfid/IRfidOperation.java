package com.fanfull.libhard.rfid;

import com.fanfull.libhard.IOperation;

public interface IRfidOperation extends IOperation {

  void setListener(IRfidListener listener);

  byte[] findNfc();

  void findNfcAsync();

  byte[] findM1();

  void findM1Async();

  boolean findCard(byte[] uidBuff);

  boolean readNfc4Byte(int sa, byte[] buff);

  boolean readNfc(int sa, byte[] buff, boolean withFindCard);

  void readNfcAsync(int sa, int dataLen, boolean withFindCard);

  boolean writeNfc4Byte(int sa, byte[] buff);

  boolean writeNfc(int sa, byte[] buff, boolean withFindCard);

  byte[] readM1(int block);

  boolean writeM1(int block, byte[] data16);
}
