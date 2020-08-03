package com.fanfull.utils;

import android.util.Log;
import com.apkfuns.logutils.LogUtils;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class ArrayUtils {

  public static byte flagData[] = new byte[] {
      (byte) 0x23, (byte) 0x5f, (byte) 0x8e, (byte) 0x41, (byte) 0x4d,
      (byte) 0x8c, (byte) 0x3d, (byte) 0x6a, (byte) 0x23, (byte) 0x9c,
      (byte) 0x95, (byte) 0x3c, (byte) 0x4b, (byte) 0x11, (byte) 0x73,
      (byte) 0x1c, (byte) 0x3e, (byte) 0x22, (byte) 0x49, (byte) 0x83,
      (byte) 0x36, (byte) 0x47, (byte) 0x88, (byte) 0x26, (byte) 0x32,
      (byte) 0x28, (byte) 0x3d, (byte) 0x6f, (byte) 0x78, (byte) 0x7a,
      (byte) 0x99, (byte) 0x2d, (byte) 0x6c, (byte) 0x24, (byte) 0xa3,
      (byte) 0x8c, (byte) 0x7f, (byte) 0x9d, (byte) 0x36, (byte) 0xb2,
      (byte) 0x25, (byte) 0x39, (byte) 0x48, (byte) 0x76, (byte) 0xea,
      (byte) 0x2c, (byte) 0x36, (byte) 0x47, (byte) 0x79, (byte) 0x29,
  };

  /**
   * @param @param bArray
   * @param @return 设定文件
   * @return String 返回类型
   * @Title: bytesToHexString
   * @Description:把字节数组转换成十六进制字符串
   */
  public static final String bytesToHexString(byte[] bArray) {
    if (bArray == null) {
      return null;
    }
    return bytesToHexString(bArray, 0, bArray.length);

    // StringBuilder sb = new StringBuilder(bArray.length);
    // String sTemp;
    // for (int i = 0; i < bArray.length; i++) {
    // sTemp = Integer.toHexString(0xFF & bArray[i]);
    // if (sTemp.length() < 2)
    // sb.append(0);
    // sb.append(sTemp.toUpperCase());
    // }
    // return sb.toString();
  }

  public static byte getFlagData(byte[] uid, int n) {
    byte tmp = (byte) 0x0;
    if (null == uid || (null != uid && uid.length != 7)) {
      Log.d("UID", "uid 错误");
      return tmp;
    }
    switch (n) {
      case 0:
        tmp = (byte) (uid[3] + uid[4] + uid[1]);
        LogUtils.d("Flag0", ArrayUtils.bytesToHexString(new byte[] { tmp }));
        return tmp;
      case 1:
        tmp = (byte) (tmp ^ uid[1]);
        tmp = (byte) (tmp ^ uid[3]);
        tmp = (byte) (tmp ^ uid[5]);
        LogUtils.d("Flag1", ArrayUtils.bytesToHexString(new byte[] { tmp }));
        return tmp;
      case 2:
        tmp = (byte) (tmp ^ uid[4]);
        tmp = (byte) (tmp ^ uid[5]);
        tmp = (byte) (tmp ^ uid[6]);
        LogUtils.d("Flag2", ArrayUtils.bytesToHexString(new byte[] { tmp }));
        return tmp;
      case 3:
        tmp = (byte) (uid[3] + uid[2] + uid[1]);
        LogUtils.d("Flag3", ArrayUtils.bytesToHexString(new byte[] { tmp }));
        return tmp;
      case 4:
        tmp = (byte) (uid[6] + 1);
        LogUtils.d("Flag4", ArrayUtils.bytesToHexString(new byte[] { tmp }));
        return tmp;
      case 5:
        tmp = (byte) (uid[1] + uid[4] + uid[6]);
        LogUtils.d("Flag5", ArrayUtils.bytesToHexString(new byte[] { tmp }));
        return tmp;
      case 6:
        tmp = (byte) (uid[1] ^ uid[3]);
        tmp = (byte) (tmp + uid[5]);
        LogUtils.d("Flag6", ArrayUtils.bytesToHexString(new byte[] { tmp }));
        return tmp;
      case 7:
        tmp = (byte) (uid[4] + uid[6]);
        tmp = (byte) (tmp ^ uid[2]);
        LogUtils.d("Flag7", ArrayUtils.bytesToHexString(new byte[] { tmp }));
        return tmp;
      case 8:
        tmp = (byte) (uid[2] + uid[4]);
        LogUtils.d("Flag8", ArrayUtils.bytesToHexString(new byte[] { tmp }));
        return tmp;
      case 9:
        tmp = (byte) (uid[3] + uid[5]);
        tmp = (byte) (tmp ^ uid[4]);
        LogUtils.d("Flag9", ArrayUtils.bytesToHexString(new byte[] { tmp }));
        return tmp;
      default:
        return tmp;
    }
  }

  public static final String bytesToHexString(byte[] bArray, int start,
      int end) {
    if ((bArray == null) || (end <= start) || (bArray.length <= start)) {
      return null;
    }

    if (bArray.length < end) {
      end = bArray.length;
    }

    StringBuilder sb = new StringBuilder(end - start);
    for (int i = start; i < end; i++) {
      int h = (bArray[i] & 0xF0) >> 4;
      int l = (bArray[i] & 0x0F);
      sb.append(byte2Char(h)).append(byte2Char(l));
    }
    return sb.toString().toUpperCase(Locale.getDefault());
  }

  /**
   * @param @param String
   * @param @return 设定文件
   * @return byte[]
   * @Title: bytesToByte
   * @Description:把字符串转化为byte【】
   */
  public static final List<byte[]> bytesToByte(String index) {
    if (index.length() == 58) {
      List<byte[]> list = new ArrayList<byte[]>();

      byte[] totalData = hexStringToBytes(index);
      byte[] a = new byte[16];
      byte[] b = new byte[13];
      a = Arrays.copyOfRange(totalData, 0, 16);
      b = Arrays.copyOfRange(totalData, 16, 23);
      list.add(a);
      list.add(b);
      System.err.println("barcode1:" + bytesToHexString(a));
      System.err.println("barcode2:" + bytesToHexString(b));
      //            byte[] barcode1 = new byte[16];
      //            for (int i = 0; i < 16; i++) {
      ////                barcode1[i] = (byte) (Integer.valueOf(index.charAt(i * 2) - 48) * 16 + Integer
      ////                        .valueOf(index.charAt(i * 2 + 1) - 48));
      //                barcode1[i] = (byte) (more65(index.charAt(i * 2)) * 16 + more65(index.charAt(i * 2 + 1)));
      //            }
      //            System.err.println("barcode1:"+bytesToHexString(barcode1));
      //            list.add(barcode1);
      //            byte[] barcode2 = new byte[16];
      //            for (int i = 0; i < 13; i++) {
      ////                barcode2[i] = (byte) (Integer.valueOf(index
      ////                        .charAt((i + 16) * 2) - 48) * 16 + Integer
      ////                        .valueOf(index.charAt((i + 16) * 2 + 1) - 48));
      //                barcode2[i] = (byte) (more65(index
      //                        .charAt((i + 16) * 2)) * 16 + more65(index.charAt((i + 16) * 2 + 1)));
      //            }
      //            for (int i = 13; i < 16; i++) {
      //                barcode2[i] = 0;
      //            }
      //            System.err.println("barcode2:"+bytesToHexString(barcode2));
      //            list.add(barcode2);
      return list;
    } else {
      return null;
    }
  }

  /**
   * @param @param String
   * @param @return 设定文件
   * @return byte[]
   * @Title: bytesToByte
   * @Description:把字符串转化为byte【】
   */
  public static final List<byte[]> bytesToByte(String index, byte tKey[]) {
    if (index.length() == 50) {
      List<byte[]> list = new ArrayList<byte[]>();

      byte[] totalData = hexStringToBytes(index);
      byte[] a = new byte[16];
      byte[] b = new byte[13];
      a = Arrays.copyOfRange(totalData, 0, 16);
      b = Arrays.copyOfRange(totalData, 16, 25);
      list.add(encryption(a, tKey));
      list.add(encryption(b, tKey));
      //            byte[] barcode1 = new byte[16];
      //            for (int i = 0; i < 16; i++) {
      ////                barcode1[i] = (byte) (Integer.valueOf(index.charAt(i * 2) - 48) * 16 + Integer
      ////                        .valueOf(index.charAt(i * 2 + 1) - 48));
      //                barcode1[i] = (byte) (more65(index.charAt(i * 2)) * 16 + more65(index.charAt(i * 2 + 1)));
      //            }
      //            System.err.println("barcode1:"+bytesToHexString(barcode1));
      //            list.add(barcode1);
      //            byte[] barcode2 = new byte[16];
      //            for (int i = 0; i < 13; i++) {
      ////                barcode2[i] = (byte) (Integer.valueOf(index
      ////                        .charAt((i + 16) * 2) - 48) * 16 + Integer
      ////                        .valueOf(index.charAt((i + 16) * 2 + 1) - 48));
      //                barcode2[i] = (byte) (more65(index
      //                        .charAt((i + 16) * 2)) * 16 + more65(index.charAt((i + 16) * 2 + 1)));
      //            }
      //            for (int i = 13; i < 16; i++) {
      //                barcode2[i] = 0;
      //            }
      //            System.err.println("barcode2:"+bytesToHexString(barcode2));
      //            list.add(barcode2);
      return list;
    } else {
      return null;
    }
  }

  public static int more65(char tmp) {
    if (Integer.valueOf(tmp) > 64) {
      return Integer.valueOf(tmp) - 65;
    } else {
      return Integer.valueOf(tmp) - 48;
    }
  }

  /**
   * @param @param String
   * @param @return 设定文件
   * @return byte[]
   * @Title: bytesToByte
   * @Description:把字符串转化为byte【】
   */
  public static final byte[] traceToByte(String index) {
    if (index.length() == 30) {
      byte[] barcode = new byte[16];
      for (int i = 0; i < 15; i++) {
        barcode[i] = (byte) (Integer.parseInt(
            index.substring(i * 2, (i + 1) * 2), 16));
      }
      barcode[15] = (byte) 0x00;
      for (int i = 0; i < 15; i++) {
        barcode[15] ^= barcode[i];
      }
      System.err.println(bytesToHexString(barcode));
      return barcode;
    } else {
      return null;
    }
  }

  /**
   * @param @param oldString
   * @param @param replaceString
   * @param @return 设定文件
   * @return String 返回类型
   * @throws
   * @Title: charReplace
   * @Description: 字符替换 123456，17返回123417
   */
  public static final String charReplace(String oldString,
      String replaceString) {
    oldString = oldString.substring(0,
        oldString.length() - replaceString.length());
    oldString = oldString + replaceString;
    return oldString;
  }

  public static final byte[] traceToByte1(String index) {
    if (index.length() == 24) {
      byte[] barcode = new byte[12];
      for (int i = 0; i < 12; i++) {
        barcode[i] = (byte) (Integer.parseInt(
            index.substring(i * 2, (i + 1) * 2), 10));
      }
      /*
       * barcode[15] = (byte) 0x00; for (int i = 0; i < 15; i++) {
       * barcode[15] ^= barcode[i]; }
       */
      System.err.println(bytesToHexString(barcode));
      return barcode;
    } else {
      return null;
    }
  }

  public static byte[] hexStringToBytes(String hexString) {
    if (hexString == null || hexString.equals("")) {
      return null;
    }
    hexString = hexString.toUpperCase();
    if (hexString.length() % 2 == 1) {
      hexString = "0" + hexString;
    }
    int length = hexString.length() / 2;
    char[] hexChars = hexString.toCharArray();
    byte[] d = new byte[length];
    for (int i = 0; i < length; i++) {
      int pos = i * 2;
      d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
    }
    return d;
  }

  private static byte charToByte(char c) {
    return (byte) "0123456789ABCDEF".indexOf(c);
  }

  private static char byte2Char(int n) {
    return "0123456789ABCDEF".charAt(n);
  }

  /**
   * @description 将38个字符长度的barcode分成两个 16字节长度的byte[],为 后续写入RFID的两个区 做准备
   */
  public static byte[][] get2Data(String barcode) {
    if ((null == barcode) || (50 != barcode.length())) {
      return null;
    }
    byte[] totalData = hexStringToBytes(barcode);
    byte[][] reVal = new byte[2][];
    reVal[0] = Arrays.copyOfRange(totalData, 0, 16);
    reVal[1] = new byte[16];

    byte[] tmp = Arrays.copyOfRange(totalData, 16, 23);
    for (int i = 0; i < tmp.length; i++) {
      reVal[1][i] = tmp[i];
    }
    return reVal;
  }

  /**
   * @return 返回 拼接好后的新 字节数组
   * @description 拼接两个字节数组
   */
  public static byte[] concatArray(byte[] arr1, byte[] arr2) {
    // 处理 特殊情况
    if (null == arr1 & null == arr2) {
      return null;
    } else if (null == arr1) {
      return arr2;
    } else if (null == arr2) {
      return arr1;
    }

    byte[] reVal = new byte[arr1.length + arr2.length];
    int i = 0;
    for (int j = 0; j < arr1.length; j++) {
      reVal[i++] = arr1[j];
    }
    for (int j = 0; j < arr2.length; j++) {
      reVal[i++] = arr2[j];
    }
    return reVal;
  }

  /**
   * @description 判断两个基本类型数组是否相等
   */
  public static boolean arrayIsEqulse(Object obj1, Object obj2) {
    if (null == obj1 || null == obj2) {
      return false;
    } else if (obj1 == obj2) {
      return true;
    }

    if (obj1 instanceof int[] && obj2 instanceof int[]) {
      int[] arr1 = (int[]) obj1;
      int[] arr2 = (int[]) obj2;

      if (arr1.length != arr2.length) {
        return false;
      }

      for (int i = 0; i < arr2.length; i++) {
        if (arr1[i] != arr2[i]) {
          return false;
        }
      }
      return true;
    } else if (obj1 instanceof byte[] && obj2 instanceof byte[]) {
      byte[] arr1 = (byte[]) obj1;
      byte[] arr2 = (byte[]) obj2;

      if (arr1.length != arr2.length) {
        return false;
      }

      for (int i = 0; i < arr2.length; i++) {
        if (arr1[i] != arr2[i]) {
          return false;
        }
      }
      return true;
    } else if (obj1 instanceof long[] && obj2 instanceof long[]) {
      long[] arr1 = (long[]) obj1;
      long[] arr2 = (long[]) obj2;

      if (arr1.length != arr2.length) {
        return false;
      }

      for (int i = 0; i < arr2.length; i++) {
        if (arr1[i] != arr2[i]) {
          return false;
        }
      }
      return true;
    } else if (obj1 instanceof char[] && obj2 instanceof char[]) {
      char[] arr1 = (char[]) obj1;
      char[] arr2 = (char[]) obj2;

      if (arr1.length != arr2.length) {
        return false;
      }

      for (int i = 0; i < arr2.length; i++) {
        if (arr1[i] != arr2[i]) {
          return false;
        }
      }
      return true;
    } else if (obj1 instanceof short[] && obj2 instanceof short[]) {
      short[] arr1 = (short[]) obj1;
      short[] arr2 = (short[]) obj2;

      if (arr1.length != arr2.length) {
        return false;
      }

      for (int i = 0; i < arr2.length; i++) {
        if (arr1[i] != arr2[i]) {
          return false;
        }
      }
      return true;
    } else if (obj1 instanceof double[] && obj2 instanceof double[]) {
      double[] arr1 = (double[]) obj1;
      double[] arr2 = (double[]) obj2;

      if (arr1.length != arr2.length) {
        return false;
      }

      for (int i = 0; i < arr2.length; i++) {
        if (arr1[i] != arr2[i]) {
          return false;
        }
      }
      return true;
    } else if (obj1 instanceof float[] && obj2 instanceof float[]) {
      float[] arr1 = (float[]) obj1;
      float[] arr2 = (float[]) obj2;

      if (arr1.length != arr2.length) {
        return false;
      }

      for (int i = 0; i < arr2.length; i++) {
        if (arr1[i] != arr2[i]) {
          return false;
        }
      }
      return true;
    } else if (obj1 instanceof boolean[] && obj2 instanceof boolean[]) {
      boolean[] arr1 = (boolean[]) obj1;
      boolean[] arr2 = (boolean[]) obj2;

      if (arr1.length != arr2.length) {
        return false;
      }

      for (int i = 0; i < arr2.length; i++) {
        if (arr1[i] != arr2[i]) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * @return 已上锁的 EPC号
   * @description 根据 未上锁EPC号 生成 已上锁的 EPC号
   */
  public static byte[] genLockEPC(byte[] unlockEPC) {

    if (null == unlockEPC || 12 != unlockEPC.length) {
      return null;
    }

    byte[] reVal = null;
    reVal = Arrays.copyOf(unlockEPC, unlockEPC.length);

    // 最后一个byte按位取反
    int end = reVal.length - 1;
    reVal[end] = (byte) (((int) reVal[end]) & 0xFF ^ 0xFF);
    return reVal;
  }

  /**
   * @param preEPC 未插锁片读到的 EPC号
   * @param postEPC 已插插锁片读到的 EPC号
   * @return 返回 true 表示 锁片 已经插上
   * @description 通过比较 两次读到的epc号，判断 条码卡片 是否 已经插上
   */
  public static boolean compareEPC(byte[] preEPC, byte[] postEPC) {
    if (null == preEPC || null == postEPC) {
      return false;
    }

    LogUtils.i("preEPC:" + bytesToHexString(preEPC));
    LogUtils.i("postEPC:" + bytesToHexString(postEPC));

    if (preEPC.length != postEPC.length) {
      return false;
    }

    // 判断 其余 位
    // int end = postEPC.length - 1;
    // for (int i = 0; i < end; i++) {
    // if (preEPC[i] != postEPC[i]) {
    // return false;
    // }
    // }
    // 判断最后一个字节 是否为 异或 关系
    // int t1 = preEPC[preEPC.length - 1] & 0xFF;
    // int t2 = postEPC[postEPC.length - 1] & 0xFF;
    // if ((t1 ^ t2) != 0xFF) {
    // return false;
    // }

    if ((postEPC[postEPC.length - 1] & 0xFF) != 0xFF) {
      return false;
    }

    return true;
  }

  /**
   * @description DEC 加密
   */
  public static byte[] encrypt(byte[] datasource, byte[] password) {
    try {
      password = buildKey(password);
      SecureRandom random = new SecureRandom();
      DESKeySpec desKey = new DESKeySpec(password);// password.getBytes()
      // 创建一个密匙工厂，然后用它把DESKeySpec转换成
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
      SecretKey securekey = keyFactory.generateSecret(desKey);
      // Cipher对象实际完成加密操作
      Cipher cipher = Cipher.getInstance("DES/ECB/NOPadding");// DES/ECB/
      // 用密匙初始化Cipher对象
      cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
      // 现在，获取数据并加密
      // 正式执行加密操作
      return cipher.doFinal(datasource);
    } catch (Throwable e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * @description DEC 解密
   */
  public static byte[] decrypt(byte[] datasource, byte[] password) {
    try {
      password = buildKey(password);
      SecureRandom random = new SecureRandom();
      DESKeySpec desKey = new DESKeySpec(password);// password.getBytes()
      // 创建一个密匙工厂，然后用它把DESKeySpec转换成
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
      SecretKey securekey = keyFactory.generateSecret(desKey);
      // Cipher对象实际完成加密操作
      Cipher cipher = Cipher.getInstance("DES/ECB/NOPadding");// DES/ECB/
      // 用密匙初始化Cipher对象
      cipher.init(Cipher.DECRYPT_MODE, securekey, random);
      // 正式执行解密操作
      return cipher.doFinal(datasource);
    } catch (Throwable e) {
      e.printStackTrace();
    }
    return null;
  }

  public static int yh(byte[] arr) {

    if (null == arr || arr.length == 0) {
      return -1;
    }
    int reVal = 0;
    for (int i = 0; i < arr.length; i++) {
      reVal ^= arr[i];
    }
    return reVal;
  }

  /**
   * 加密
   */
  public static byte[] encryption(byte[] src, byte[] netkey) {
    if (src == null || netkey == null) {
      return src;
    }
    int len = src.length;
    byte[] des = new byte[len];
    byte[] tmp = new byte[len];
    if (src.length >= netkey.length) {
      for (int i = 0; i < netkey.length; i++) {
        tmp[i] = netkey[i];
      }
      for (int i = 0; i < (src.length - netkey.length); i++) {
        tmp[netkey.length + i] = netkey[i % netkey.length]; //补全密钥长度
      }
    } else {
      for (int i = 0; i < src.length; i++) {
        tmp[i] = netkey[i]; //缩短密钥长度
      }
    }
    /**开始异或加密*/
    for (int i = 0; i < tmp.length; i++) {
      des[i] = (byte) ((src[i]) ^ (tmp[i]));
    }
    /**前后三位错位处理*/
    if (null != swapEnAndDec(des)) {
      return des;
    } else {
      return null;
    }
  }

  private static byte[] swapEnAndDec(byte[] src) {
    byte k = (byte) 0x0;
    if (null == src || src.length == 0) {
      return null;
    } else if (src.length == 1) {
      return src;
    } else if (src.length == 2 || src.length == 3) {
      k = src[0];
      src[0] = src[src.length - 1];
      src[src.length - 1] = k;
    } else if (src.length == 4 || src.length == 5) {
      k = src[1];
      src[1] = src[src.length - 2];
      src[src.length - 2] = k;
    } else {
      k = src[2];
      src[2] = src[src.length - 3];
      src[src.length - 3] = k;
    }
    return src;
  }

  /**
   * 解密
   *
   * @param src 需要解密的数据
   * @param netkey 密钥
   * @return 返回解完的数据
   */
  public static byte[] deciphering(byte[] src, byte[] netkey) {
    if (src == null || netkey == null) {
      return src;
    }
    int len = src.length;
    byte[] des = new byte[len];
    byte[] tmp = new byte[len];
    if (src.length >= netkey.length) {
      for (int i = 0; i < netkey.length; i++) {
        tmp[i] = netkey[i];
      }
      for (int i = 0; i < (src.length - netkey.length); i++) {
        tmp[netkey.length + i] = netkey[i % netkey.length]; //补全密钥长度
      }
    } else {
      for (int i = 0; i < src.length; i++) {
        tmp[i] = netkey[i]; //缩短密钥长度
      }
    }
    /**恢复错位*/
    if (null == swapEnAndDec(src)) {
      return null;
    }
    /**开始异或加密*/
    for (int i = 0; i < tmp.length; i++) {
      des[i] = (byte) ((src[i]) ^ (tmp[i]));
    }

    return des;
  }

  public static byte[] buildKey(byte[] src) {
    byte[] tmp = new byte[8];
    if (null == src) {
      return null;
    } else if (src.length == 4) {
      for (int i = 0; i < src.length; i++) {
        tmp[i] = src[i];
        tmp[7 - i] = src[i];
      }
    } else if (src.length == 6) {
      for (int i = 0; i < src.length; i++) {
        tmp[i] = src[i];
      }
      tmp[6] = src[3];
      tmp[7] = src[4];
    } else if (src.length == 7) {
      for (int i = 0; i < src.length; i++) {
        tmp[i] = src[i];
      }
      tmp[7] = src[4];
    }
    return tmp;
  }
}
