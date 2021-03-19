package com.fanfull.libhard.rfid;

import com.fanfull.libhard.EnumErrCode;
import com.fanfull.libhard.IOperation;

public interface IRfidOperation extends IOperation {

  void setListener(IRfidListener listener);

  /** 7字节的NFC. */
  byte[] findNfc();

  void findNfcAsync();

  /** 4字节的M1卡. */
  byte[] findM1();

  void findM1Async();

  /**
   * 高频卡 寻卡.
   *
   * @param uidBuff 4字节的M1卡 或 7字节的NFC
   */
  boolean findCard(byte[] uidBuff);

  /**
   * 高频卡 寻卡.
   *
   * @param uidBuff 4字节的M1卡 或 7字节的NFC
   */
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

  /**
   * @param block 待读的块区号.
   * @param dataBuff 16字节块区数据
   * @param uid 4字节的uid，若执行寻卡，用于存放寻到的数据；若不寻卡，则作为已寻到的数据
   * @param withFindCard true 执行寻卡操作，否则不寻卡.
   */
  boolean readM1(int block, byte[] dataBuff, byte[] uid, boolean withFindCard);

  boolean writeM1(int block, byte[] data16);
}
