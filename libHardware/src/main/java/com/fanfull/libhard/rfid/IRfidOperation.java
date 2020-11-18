package com.fanfull.libhard.rfid;

import com.fanfull.libhard.EnumErrCode;
import com.fanfull.libhard.IOperation;

public interface IRfidOperation extends IOperation {

  void setListener(IRfidListener listener);

  byte[] findNfc();

  void findNfcAsync();

  byte[] findM1();

  void findM1Async();

  boolean findCard(byte[] uidBuff);

  EnumErrCode findCardRes(byte[] uidBuff);

  boolean readNfc4Byte(int sa, byte[] buff);

  boolean readNfc(int sa, byte[] buff, boolean withFindCard);

  /**
   * 读NFC.
   *
   * @param uid 7字节数组 执行寻卡操作，null不寻卡
   */
  EnumErrCode readNfc(int sa, byte[] data, byte[] uid);

  void readNfcAsync(int sa, int dataLen, boolean withFindCard);

  boolean writeNfc4Byte(int sa, byte[] buff);

  boolean writeNfc(int sa, byte[] buff, boolean withFindCard);

  boolean readM1(int block, byte[] dataBuff);

  boolean writeM1(int block, byte[] data16);
}
