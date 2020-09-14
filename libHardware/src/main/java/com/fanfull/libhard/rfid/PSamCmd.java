package com.fanfull.libhard.rfid;

public abstract class PSamCmd {
  public static final int COS_RES_CARD_LEN = 12;

  public static final byte[] COS_RES_0 = new byte[] { (byte) 0x90, 0x00 };
  public static final byte[] COS_RES_3 = new byte[] { (byte) 0x91, 0x00 };

  public static final byte[] CMD_COS_0 = new byte[] {
      (byte) 0x80, (byte) 0x20, (byte) 0x07, (byte) 0x00, (byte) 0x08, (byte) 0x49, (byte) 0x43,
      (byte) 0x46, (byte) 0x43, (byte) 0x43, (byte) 0x4B, (byte) 0x46, (byte) 0x33,
  };
  public static final byte[] CMD_COS_1 = new byte[] {
      (byte) 0x80, (byte) 0x20, (byte) 0x06, (byte) 0x00, (byte) 0x08, (byte) 0x31, (byte) 0x32,
      (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38
  };
  public static final byte[] CMD_COS_2 = new byte[] {
      (byte) 0x80, (byte) 0x20, (byte) 0x02, (byte) 0x00, (byte) 0x08, (byte) 0x43, (byte) 0x46,
      (byte) 0x43, (byte) 0x43, (byte) 0x41, (byte) 0x43, (byte) 0x30, (byte) 0x32
  };
  public static final byte[] CMD_COS_3 = new byte[] {
      (byte) 0x80, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0xAA, (byte) 0x01,
  };
  public static final byte[] CMD_COS_4 =
      new byte[] { (byte) 0x80, (byte) 0xB2, (byte) 0x00, (byte) 0x00, (byte) 0x0A, };

  public static final byte[] CMD_PSAM_GET_RANDOM_NUM =
      { (byte) 0x00, (byte) 0x84, (byte) 0x00, (byte) 0x00, (byte) 0x04 };

  public static boolean checkRecData(byte[] successBuff, byte[] recData, int recLen) {
    if (recData == null
        || successBuff == null
        || recLen != successBuff.length
        || recData.length < recLen) {
      return false;
    }
    for (int i = 0; i < successBuff.length; i++) {
      if (successBuff[i] != recData[i]) {
        return false;
      }
    }
    return true;
  }

  public static boolean dealSuccess(byte[] data, int dataLen) {
    return dataLen == 2
        && data[0] == COS_RES_0[0]
        && data[1] == COS_RES_0[1];
  }
}
