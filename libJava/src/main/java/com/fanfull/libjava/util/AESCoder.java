package com.fanfull.libjava.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class AESCoder {

  /** 加解密封签事件码. */
  public static boolean myEncrypt(byte[] data, byte[] key, boolean isEncrypt) {
    if (null == data || null == key || 0 == data.length || 0 == key.length) {
      return false;
    }

    // 1, 将key平铺成 与data 一样长的 newKey
    byte[] newKey = new byte[data.length];
    int times = data.length / key.length;
    for (int i = 0; i < times; i++) {
      System.arraycopy(key, 0, newKey, i * key.length, key.length);
    }
    int sy = data.length % key.length;
    System.arraycopy(key, 0, newKey, times * key.length, sy);

    // 2, 对 newKey 进行AES加密， 得到 encryptKey
    byte[] encryptKey = null;
    try {
      encryptKey = DesUtil.cipherDoFinal(
          newKey, "$%&tF6&7G6R*&=[l", true, "AES/ECB/PKCS5Padding");
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }

    // 3, 开始对数据进行 加密\解密
    int t = 0;
    for (int i = 0; i < encryptKey.length; i++) {
      t ^= encryptKey[i];
    }
    if (isEncrypt) {
      // 加密， 与解密操作持续相反
      coderLink(data, t, isEncrypt);
      for (int i = 0; i < data.length; i++) {
        data[i] ^= encryptKey[i];
      }
    } else {
      // 解密， 与加密操作持续相反
      for (int i = 0; i < data.length; i++) {
        data[i] ^= encryptKey[i];
      }
      coderLink(data, t, isEncrypt);
    }
    return true;
  }

  private static void coderLink(byte[] plain, int key, boolean isEncrypt) {
    if (isEncrypt) {
      plain[0] ^= key;
      for (int i = 1; i < plain.length; i++) {
        plain[i] ^= plain[i - 1];
      }
      for (int i = plain.length - 2; 0 <= i; i--) {
        plain[i] ^= plain[i + 1];
      }
    } else {
      for (int i = 0; i < plain.length - 1; i++) {
        plain[i] ^= plain[i + 1];
      }
      for (int i = plain.length - 1; 0 < i; i--) {
        plain[i] ^= plain[i - 1];
      }
      plain[0] ^= key;
    }
  }

  /**
   * 生成袋id的校验值.
   *
   * @param bagId12 12字节锁片epc
   * @param tid12 12字节锁片tid
   */
  public static byte genBagIdCheck(byte[] bagId12, byte[] tid12) {
    if (bagId12 == null || bagId12.length != 12 || tid12 == null || tid12.length != 12) {
      return -1;
    }
    byte[] plain = BytesUtil.concatArray(Arrays.copyOfRange(bagId12, 0, bagId12.length - 1), tid12);
    byte[] key = Arrays.copyOfRange(tid12, 4, tid12.length);

    //if (plain.length % 8 != 0) {
    //  final int newLen = (plain.length / 8 + 1) * 8;
    //  plain = Arrays.copyOf(plain, newLen);
    //}
    //Logs.out("plain:%s", BytesUtil.bytes2HexString(plain));
    //Logs.out("key:%s", BytesUtil.bytes2HexString(key));

    //final DesUtil desUtil = new DesUtil("DES/ECB/PKCS5Padding");
    ////desUtil.setArgs(DesUtil.ALGORITHM_NAME_DES,
    ////    DesUtil.ALGORITHM_MODE_ECB,
    ////    DesUtil.ALGORITHM_PADDING_PKCS5Padding);
    //byte[] encrypt = desUtil.encrypt(plain, key);
    byte[] encrypt;
    try {
      encrypt = DesUtil.cipherDoFinal(
          plain, key, true, "DES/ECB/PKCS5Padding");
    } catch (Exception e) {
      e.printStackTrace();
      return -1;
    }

    //Logs.out("encrypt:%s", BytesUtil.bytes2HexString(encrypt));
    int n = 0;
    for (int i = 0; i < tid12.length; i++) {
      n ^= tid12[i];
    }

    n = n & 0xFF;
    n %= encrypt.length;

    //Logs.out("index:%s, res:0x%X", n, encrypt[n] & 0xFF);

    Logs.out("%s  %s  %02X  %s", BytesUtil.bytes2HexString(encrypt),
        n, encrypt[n] & 0xFF, BytesUtil.bytes2HexString(plain));

    return encrypt[n];
  }

  /**
   * 验证 袋id的校验值.验证通过返回true
   *
   * @param bagId12 12字节锁片epc
   * @param tid12 12字节锁片tid
   */
  public static boolean checkBagId6(byte[] bagId12, byte[] tid12) {
    final boolean reVal = bagId12[bagId12.length - 1] == genBagIdCheck(bagId12, tid12);
    return reVal;
  }

  /**
   * 验证 袋id的校验值.验证通过返回true
   *
   * @param bagId12 12字节锁片epc
   * @param tid12 12字节锁片tid
   */
  public static boolean checkBagId6(String bagId12, String tid12) {
    return checkBagId6(BytesUtil.hexString2Bytes(bagId12), BytesUtil.hexString2Bytes(tid12));
  }

  static void testString() throws Exception {
    final HashMap<String, String> map = new HashMap<>();
    //map.put("000000000000000000000000", "FFFFFFFFFFFFFFFF");
    //map.put("FFFFFFFFFFFFFFFFFFFFFFFF", "0000000000000000");

    //map.put("000000000000", "0000000000000000");
    //map.put("0000000000000000000000000000000000000000000000", "0000000000000000");
    //map.put("你好吗", "0000000000000000");
    //map.put("在线DES加密解密", "在线DES加密解密");
    map.put("3532333435363738393131313233343536373839313233", "3536373839313233");
    map.put("3132333435363738393131313233343536373839313233", "3536373839313233");

    final DesUtil desUtil = new DesUtil("DES/ECB/PKCS5Padding");

    final Set<Map.Entry<String, String>> entries = map.entrySet();
    for (Map.Entry<String, String> s : entries) {
      Logs.out("==== plain:%s ====", s);
      final byte[] epc = BytesUtil.hexString2Bytes(s.getKey());
      final byte[] tid = BytesUtil.hexString2Bytes(s.getValue());
      final byte[] encrypt = desUtil.encrypt(epc, tid);
      //final byte[] encrypt = desUtil.encrypt(s.getKey(), s.getValue());
      Logs.out("encrypt:%s", BytesUtil.bytes2HexString(encrypt));
    }
  }

  static void testBagIdCheck() {

    final LinkedHashMap<String, String> map = new LinkedHashMap<>();
    //map.put("000000000000000000000000", "FFFFFFFFFFFFFFFFFFFFFFFF");
    //map.put("FFFFFFFFFFFFFFFFFFFFFFFF", "000000000000000000000000");
    //map.put("05532103044A45D23E618072", "E28011402000247928471800");
    //map.put("0553210304594AD23E61806E", "E28011402000200B28771800");
    //map.put("05027101043B1222745A80D6", "E20034140123030179A13BB3");
    //map.put("E20034140123030179A13BB3", "05027101043B1222745A80D6");
    //map.put("050278012105140926440004", "050278012105140926440004");

    //map.put("0553210304594AD23E61806D", "E28011402000200B28771800");
    //map.put("05532103044A45D23E61809D", "E28011402000247928471800");
    //map.put("05FF0FF01122334455667737", "000000000FFFFFFFFFFF0000");
    //map.put("05554600FF77FF66223215B1", "E20034140123030179A13B65");
    //map.put("05027101043B1222745A80CA", "E20834140123030179A13B65");
    //map.put("06532101049F48D23E6180CD", "E280114020002578287E1800");
    //map.put("0653210304594AD23E61806D", "E28011402000200B28771800");
    //map.put("05532101049F48D23E6180CD", "E280114020002578287E1800");
    //map.put("0553210304594AD23E61806D", "E28011402000200B28771800");

    map.put("05000000000000000000003F", "000000000000000000000000");
    map.put("0510000000000000000000D2", "000000000000000000000000");
    map.put("05010000000000000000007B", "000000000000000000000000");
    map.put("050001000000000000000096", "000000000000000000000000");
    map.put("05001000000000000000007D", "000000000000000000000000");
    map.put("0500000000100000000000C4", "000000000000000000000000");
    map.put("0500000000010000000000F1", "000000000000000000000000");
    map.put("05000000000000000000013F", "000000000000000000000000");
    map.put("05000000000000000000103F", "000000000000000000000000");
    map.put("05FFFFFFFFFFFFFFFFFFFF5E", "FFFFFFFFFFFFFFFFFFFFFFFF");
    map.put("05F1FFFFFFFFFFFFFFFFFF04", "FFFFFFFFFFFFFFFFF1FFFFFF");
    map.put("05027101043B1222745A80EB", "E20034140123030179A13BB3");
    map.put("05FFFFFFFFFFFFFFFFFFFF32", "000000000000000000000000");
    map.put("05000000000000000000002C", "FFFFFFFFFFFFFFFFFFFFFFFF");
    map.put("0553210304594AD23E618001", "E28011402000200B28771800");
    map.put("05532103044A45D23E618002", "E28011402000247928471800");
    map.put("050278012105140926440070", "050278012105140926440004");
    map.put("0553210304594AD23E61806D", "E28011402000200B28771800");
    map.put("05027103047A47D23E61809D", "E28011402000247928471800");
    map.put("0553210304C060D23E6180F1", "E28011402000200B28771801");
    map.put("05532103044A45D23E61809D", "E28011402000247928471800");
    map.put("0553210104B749D23E618055", "E28011402000200B28771802");
    map.put("05532101049F48D23E61809D", "E28011402000247928471800");
    map.put("0553210104575BD23E6180FC", "E28011402000200B28771803");
    map.put("05532101043349D23E61809D", "E28011402000247928471800");

    final Set<Map.Entry<String, String>> entries = map.entrySet();
    for (Map.Entry<String, String> s : entries) {
      //Logs.out("------ plain:%s ------", s);
      final byte[] epc = BytesUtil.hexString2Bytes(s.getKey());
      final byte[] tid = BytesUtil.hexString2Bytes(s.getValue());
      final byte checkByte = genBagIdCheck(epc, tid);
      //Logs.out("res:%s", checkBagId6(epc, tid));
    }
  }

  static void testMyEncrypt() {
    String plain = "0502780121051409264500050278012105140926450007";
    String pwd = "050278012105140926460012";

    final byte[] plainBuff = plain.getBytes();
    final byte[] pwdBuff = pwd.getBytes();

    Logs.out("plainBuff:%s", BytesUtil.bytes2HexString(plainBuff));

    boolean myEncrypt = myEncrypt(plainBuff, pwdBuff, true);
    Logs.out("myEncrypt:%s - %s", BytesUtil.bytes2HexString(plainBuff), myEncrypt);
    myEncrypt = myEncrypt(plainBuff, pwdBuff, false);
    Logs.out("myDecrypt:%s - %s", BytesUtil.bytes2HexString(plainBuff), myEncrypt);

    //myEncrypt = myEncryptNew(plainBuff, pwdBuff, true);
    //Logs.out("myEncryptNew:%s - %s", BytesUtil.bytes2HexString(plainBuff), myEncrypt);
    //myEncrypt = myEncryptNew(plainBuff, pwdBuff, false);
    //Logs.out("myEncryptNew:%s - %s", BytesUtil.bytes2HexString(plainBuff), myEncrypt);
  }

  public static class TestThread extends Thread {
    @Override public void run() {
      for (int i = 0; i < 100; i++) {
        System.out.println("=================================子线程666：" + i);
      }
    }
  }

  public static void main(String[] args) throws Exception {

    //TestThread testThread = new TestThread();
    //testThread.start();
    //for (int i = 0; i < 100; i++) {
    //  System.out.println("主线程" + i + "：666");
    //}
    //testMyEncrypt();

    //testString();
    testBagIdCheck();
  }
}
