package com.fanfull.libjava.util;

public class Crc8Util {

  public static void main(String[] args) {
    //XLog.init();
    //Logs.out("test");
    //XLog.d("test");
    //Logs.out("MAX_VALUE:%s", Integer.MAX_VALUE);
    //Logs.out("MIN_VALUE:%s", Integer.MIN_VALUE);
    //Logs.out("1 << 31:%s", (1 << 31));
    //Logs.out("test:%s", (1 << 31) - 1);
    //byte b = -23;
    //int t = b;
    //Logs.out("byte:%s int:%s", b, t);

    testCrc8();
    Crc8Util crcUtil = Crc8Util.newBuilder(98, 2)
        .refIn(false)
        .refOut(false)
        .build();
  }

  static void testCrc8() {
    int poly = 0x07;
    byte[] data = BytesUtil.hexString2Bytes("F10123B9A19B9D26397D64EE1D1500D7");
    Logs.out("==== input:F10123B9A19B9D26397D64EE1D1500D7,poly:%02X ====", poly);
    byte b = crc8((byte) poly, data);
    Logs.out("crc:%02X == A0", b);
    b = crc8((byte) poly, (byte) 0, (byte) 0x55, data);
    Logs.out("crc-ITU:%02X == F5", b);

    poly = 0x8D;
    data = BytesUtil.hexString2Bytes("F101154A33B49D26397D64EE1D150031");
    Logs.out("==== input:F101154A33B49D26397D64EE1D150031,poly:%02X ====", poly);
    b = crc8((byte) poly, data);
    Logs.out("crc:%02X == 00", b);
    b = crc8(poly, 0x55, 0x00, data);
    Logs.out("crc 55 00:%02X == 57", b);

    poly = 0x8D;
    data =
        BytesUtil.hexString2Bytes("0C31460f00010000000000800f1089376D4D6C80FAEAEA4A2B048DB13A60");
    Logs.out(
        "==== input:0C31460f00010000000000800f1089376D4D6C80FAEAEA4A2B048DB13A60,poly:%02X ====",
        poly);
    b = crc8((byte) poly, data);
    Logs.out("crc:%02X == 6F", b);
    b = crc8(poly, 0x55, 0x77, data);
    Logs.out("crc 55 77:%02X == 3F", b);
  }

  /**
   * 计算 1个字节的 crc.
   *
   * @param poly 多项式
   * @param input 数据
   * @return crc。
   */
  private static byte crc8(int poly, byte input) {
    byte crc = input;

    //Logs.out("input:%s", Integer.toBinaryString(crc & 0xFF));
    for (int i = 0; i < 8; i++) {
      boolean is1 = (crc & 0x80) == 0x80;
      //Logs.out("is1:%s, crc:%02X", is1, crc & 0x80);
      crc <<= 1;
      //Logs.out("is1:%s, crc:%s", is1, Integer.toBinaryString(crc & 0xFF));
      if (is1) {
        crc ^= poly;
      }
    }
    //Logs.out("input:%02X,poly:%02X,crc:%02X,", input, poly, crc);
    return crc;
  }

  public static byte crc8(int poly, byte[] input) {
    return crc8(poly, 0, 0, input);
  }

  public static byte crc8(int poly, int init, int xorOut, byte[] input) {
    byte crc = (byte) init;
    for (int i = 0; i < input.length; i++) {
      crc ^= input[i];
      crc = crc8(poly, crc);
    }
    return (byte) (crc ^ xorOut);
  }

  public static byte crc8(int poly, int init, int xorOut, byte[] input, int from, int to) {
    byte crc = (byte) init;
    for (int i = from; i < to; i++) {
      crc ^= input[i];
      crc = crc8(poly, crc);
    }
    return (byte) (crc ^ xorOut);
  }

  private final long poly;
  private final int width;

  private long init;
  private long xorOut;

  private boolean refIn;
  private boolean refOut;

  private byte[] table;

  public Crc8Util(long poly, int width, long init, long xorOut, boolean refIn, boolean refOut) {
    this.poly = poly;
    this.width = width;

    this.init = init;
    this.xorOut = xorOut;

    this.refIn = refIn;
    this.refOut = refOut;
  }

  public Crc8Util(long poly, int width) {
    this.poly = poly;
    this.width = width;
  }

  public void initTable() {

  }

  public int crc(byte[] input, int from, int to) {
    byte crc = (byte) init;
    for (int i = from; i < to; i++) {
      crc ^= input[i];
      crc = table[crc & 0xFF];
    }
    return (byte) (crc ^ xorOut);
  }

  public static Builder newBuilder(long poly, int width) {
    Builder builder = new Builder(poly, width);
    return builder;
  }

  public static class Builder {
    private final long poly;
    private final int width;

    private long init;
    private long xorOut;

    private boolean refIn;
    private boolean refOut;

    public Builder(long poly, int width) {
      this.poly = poly;
      this.width = width;
    }

    public Builder init(long init) {
      this.init = init;
      return this;
    }

    public Builder xorOut(long xorOut) {
      this.xorOut = xorOut;
      return this;
    }

    public Builder refIn(boolean refIn) {
      this.refIn = refIn;
      return this;
    }

    public Builder refOut(boolean refOut) {
      this.refOut = refOut;
      return this;
    }

    public Crc8Util build() {
      Crc8Util crcUtil = new Crc8Util(poly, width, init, xorOut, refIn, refOut);
      return crcUtil;
    }
  }
}