package com.fanfull.libhard.lock_zc;

import com.fanfull.libjava.util.BytesUtil;
import com.fanfull.libjava.util.DesUtil;
import com.fanfull.libjava.util.Logs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

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
    byte[] tk1 = tk1(key8, factor8, data, false);
    Logs.out(BytesUtil.bytes2HexString(tk1));

    //data = BytesUtil.hexString2Bytes("24F79127718668745F3BD46C6296BC63");
    //tk1 = tk1(key8, factor8, data, false);
    //Logs.out(BytesUtil.bytes2HexString(tk1));

    tk1 = tk1(key8, factor8, tk1, true);
    Logs.out(BytesUtil.bytes2HexString(tk1));
  }

  public static void main(String[] args) throws Exception {
    //testDiversify();
    //testTk1();
    //verifyEpcData();
    //byte[] epc = BytesUtil.hexString2Bytes("000F4631110000A7f9f99F01");
    //genRandom(epc);

    testZip();
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
   * 生成 8个字节的 随机因子
   */
  public static byte[] genRandom(byte[] epc) {
    byte[] random = Arrays.copyOf(epc, 8);
    int count = (epc[9] >> 4) & 0x0F;
    Logs.out("9:%X", count);
    count = (0xFF & epc[8] << 4) | count;
    //Logs.out("8:%X", (0x0F & epc[8]) << 4);
    //Logs.out("8:%X", (0xFF & epc[8] << 4));
    Logs.out("%X", count);
    for (int i = 0; i < random.length; i++) {
      random[i] += count;
    }
    Logs.out("random:%s", BytesUtil.bytes2HexString(random));
    return random;
  }

  /**
   * tk1算法.加解密 交互指令
   *
   * @param key 8字节 传输密钥
   * @param factor 8字节 随机因子 或 12个字节 epc
   * @param elsData 交互指令密文\明文
   */
  public static byte[] tk1(byte[] key, byte[] factor, byte[] elsData, boolean isEncrypt) {
    if (key == null || factor == null || key.length != 8) {
      return null;
    }

    if (factor.length == 12) {
      // 根据epc 计算随机因子
      factor = SecurityUtil.genRandom(factor);
    } else if (factor.length != 8) {
      return null;
    }

    try {
      byte[] keyNew = DesUtil.cipherDoFinal(factor, key, true,
          "DES/ECB/NOPadding");
      //Logs.out(BytesUtil.bytes2HexString(keyNew));

      byte[] plainData = DesUtil.cipherDoFinal(elsData, keyNew, isEncrypt,
          "DES/ECB/NOPadding");
      //Logs.out(BytesUtil.bytes2HexString(plainData));
      return plainData;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static byte[] decryptDes(byte[] data, byte[] key8) {
    try {
      return DesUtil.cipherDoFinal(data, key8, false,
          "DES/ECB/NOPadding");
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /** 验证epc */
  private static byte[] verifyEpcData() {
    //EPC 关键数据的校验码计算:
    //- 取 EPC 的 1-5 字段，8 byte，表示：A
    //- 取 EPC 的 6 字段右补 0 至 8 byte，表示：B
    //- 取 TID（12 byte）的低 8 byte，表示：C
    //- 取 C 对 A 和 B 依次异或，计算结果表示：D, D = C ^ A ^B
    //- MCU 调用业务处理子密钥（8 byte）对 D 进行单 des 加密运算
    //- 得出结果取低位 12 bit
    byte[] epc = BytesUtil.hexString2Bytes("000f46311000000024600000");
    byte[] tid = BytesUtil.hexString2Bytes("e2801140200024479a0a0009");

    byte[] a = Arrays.copyOf(epc, 8);
    byte[] b = new byte[8];
    b[0] = epc[8];
    b[1] = (byte) (epc[9] & 0xF0);
    byte[] c = Arrays.copyOfRange(tid, 4, tid.length);
    Logs.out("a:%s", BytesUtil.bytes2HexString(a));
    Logs.out("b:%s", BytesUtil.bytes2HexString(b));
    Logs.out("c:%s", BytesUtil.bytes2HexString(c));

    for (int i = 0; i < a.length; i++) {
      a[i] = (byte) (c[i] ^ a[i] ^ b[i]);
    }
    Logs.out("res:%s", BytesUtil.bytes2HexString(a));

    byte[] key = BytesUtil.hexString2Bytes("66DDEB966F50294A"); // 业务子密钥
    byte[] des = new byte[0];
    try {
      des = DesUtil.cipherDoFinal(a, key, true,
          "DES/ECB/NOPadding");
    } catch (Exception e) {
      e.printStackTrace();
    }
    Logs.out("des:%s", BytesUtil.bytes2HexString(des));
    Logs.out("epcOld:%s", BytesUtil.bytes2HexString(epc));
    epc[9] = (byte) (epc[9] & 0xF0 | 0x0F & des[6]);
    epc[10] = des[7];
    Logs.out("epcNew:%s", BytesUtil.bytes2HexString(epc));
    return epc;
  }

  static void testZip() throws IOException {
    Logs.out("==== 开始 ====");
    DesUtil desUtil = new DesUtil("DES/ECB/NOPadding");
    byte[] key = new byte[8];
    new Random().nextBytes(key);
    Logs.out("key:%s", BytesUtil.bytes2HexString(key));

    String path = "C:\\Users\\Administrator\\Desktop\\data";
    String des = "C:\\Users\\Administrator\\Desktop\\dataEncrypt";

    BufferedInputStream in = new BufferedInputStream(new FileInputStream(path));
    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(des));
    byte[] buff = new byte[1024];
    int len;
    while (0 < (len = in.read(buff))) {
      byte[] encrypt = desUtil.encrypt(buff, key);
      out.write(encrypt, 0, len);
    }
    //new ZipInputStream()

    in.close();
    out.close();
    Logs.out("==== 完成 ====");
  }
}
