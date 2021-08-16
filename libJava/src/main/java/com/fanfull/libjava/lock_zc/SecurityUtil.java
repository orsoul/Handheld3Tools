package com.fanfull.libjava.lock_zc;

import com.fanfull.libjava.util.BytesUtil;
import com.fanfull.libjava.util.DesUtil;
import com.fanfull.libjava.util.Logs;

import java.util.Arrays;

/**
 * 中钞锁 相关算法.
 */
public final class SecurityUtil {
  static void testDiversify() {
    byte[] mk = BytesUtil.hexString2Bytes("1233db4e05758ac9dcee55b702a9fb4b");
    byte[] data = BytesUtil.hexString2Bytes("000F466810000000");
    byte[] diversify = diversify(mk, data);
    Logs.out(BytesUtil.bytes2HexString(diversify));
  }

  static void testTk1() {
    byte[] key8 = BytesUtil.hexString2Bytes("91C537C8AA0B2018");
    byte[] factor8 = BytesUtil.hexString2Bytes("000F463110000000");
    byte[] data = BytesUtil.hexString2Bytes("101E12CD8EE41D19E39C96A1A09F74B4");
    //byte[] data = BytesUtil.hexString2Bytes("A502064527F34F725D4BE246AC409270");
    byte[] tk1 = tk1(key8, factor8, data);
    Logs.out(BytesUtil.bytes2HexString(tk1));
  }

  public static void main(String[] args) {
    //testDiversify();
    //testTk1();
  }

  /**
   * 子密钥分散算法.
   *
   * @param key16 16字节 主密钥
   * @param data8 8字节，分散因子
   * @return 16字节 子密钥
   */
  public static byte[] diversify(byte[] key16, byte[] data8) {
    if (key16 == null || data8 == null || key16.length != 16 || data8.length != 8) {
      return null;
    }
    try {
      byte[] key24 = new byte[24];
      System.arraycopy(key16, 0, key24, 0, 16);
      System.arraycopy(key16, 0, key24, 16, 8);

      byte[] dkLeft = DesUtil.cipherDoFinal(data8, key24, true,
          "DESede/ECB/NOPadding");
      Logs.out(BytesUtil.bytes2HexString(dkLeft));

      byte[] data8Non = Arrays.copyOf(data8, data8.length);
      for (int i = 0; i < data8Non.length; i++) {
        data8Non[i] = (byte) (data8Non[i] ^ 0xFF);
      }
      byte[] dkRight = DesUtil.cipherDoFinal(data8Non, key24, true,
          "DESede/ECB/NOPadding");
      Logs.out(BytesUtil.bytes2HexString(dkRight));
      return BytesUtil.concatArray(dkLeft, dkRight);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * tk1算法.
   *
   * @param key8 8字节 传输密钥
   * @param factor8 8字节 随机因子
   * @param data 交互指令密文
   * @return 交互指令 明文
   */
  public static byte[] tk1(byte[] key8, byte[] factor8, byte[] data) {
    if (key8 == null || factor8 == null || key8.length != 8 || factor8.length != 8) {
      return null;
    }
    try {
      byte[] keyNew = DesUtil.cipherDoFinal(factor8, key8, true,
          "DES/ECB/NOPadding");
      Logs.out(BytesUtil.bytes2HexString(keyNew));

      byte[] plainData = DesUtil.cipherDoFinal(data, keyNew, false,
          "DES/ECB/NOPadding");
      //Logs.out(BytesUtil.bytes2HexString(plainData));
      return plainData;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
