package com.fanfull.libjava.util;

public class CrcUtil {

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

    CrcUtil crcUtil = CrcUtil.newBuilder(98, 2)
        .refIn(false)
        .refOut(false)
        .build();
  }

  /**
   * 计算 1个宽度的 crc.
   *
   * @param input 数据
   * @param poly 多项式
   * @param width 位宽，有效值 8、16、32、64
   * @return crc。
   */
  private static long crc(long input, long poly, int width, boolean refIn, boolean refOut) {
    long crc = input;

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

  private static void reverseBits(long bits, int width) {

  }

  private final long poly;
  private final int width;

  private long init;
  private long xorOut;

  private boolean refIn;
  private boolean refOut;

  private long[] table;

  public CrcUtil(long poly, int width, long init, long xorOut, boolean refIn, boolean refOut) {
    this.poly = poly;
    this.width = width;

    this.init = init;
    this.xorOut = xorOut;

    this.refIn = refIn;
    this.refOut = refOut;
  }

  public CrcUtil(long poly, int width) {
    this.poly = poly;
    this.width = width;
  }

  public void initTable() {

  }

  public long crc(byte[] input, int from, int to) {
    long crc = init;
    for (int i = from; i < to; i++) {
      crc ^= input[i];
      //crc = table[crc & 0xFF];
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

    public CrcUtil build() {
      CrcUtil crcUtil = new CrcUtil(poly, width, init, xorOut, refIn, refOut);
      return crcUtil;
    }
  }
}