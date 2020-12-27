package org.orsoul.baselib.util;

import androidx.annotation.IntRange;
import java.math.BigInteger;

public abstract class BytesUtil {

  private static final char[] HEX_CHAR = {
      '0', '1', '2', '3', '4', '5', '6',
      '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
  };

  /**
   * 字节数组转16进制字符串.
   *
   * @param isLowerCase true 返回字符串小写，否则 返回字符串大写
   */
  public static String bytes2HexString(byte[] bArray, int start, int end, boolean isLowerCase) {
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
      sb.append(HEX_CHAR[h]).append(HEX_CHAR[l]);
    }

    if (isLowerCase) {
      return sb.toString().toLowerCase();
    }
    return sb.toString();
  }

  /** 字节数组转16进制字符串,返回字符串大写. */
  public static String bytes2HexString(byte[] bArray, int start, int end) {
    return bytes2HexString(bArray, start, end, false);
  }

  /** 字节数组转16进制字符串,返回字符串大写. */
  public static String bytes2HexString(byte[] bArray, int len) {
    if (null == bArray) {
      return null;
    }
    return bytes2HexString(bArray, 0, len);
  }

  /** 字节数组转16进制字符串,返回字符串大写. */
  public static String bytes2HexString(byte[] bArray) {
    if (null == bArray) {
      return null;
    } else if (bArray.length == 0) {
      return "";
    }
    return bytes2HexString(bArray, 0, bArray.length);
  }

  public static byte[] hexString2Bytes(String hexString) {
    if (hexString == null || 0 == hexString.length() || hexString.length() % 2 != 0) {
      return null;
    }
    char[] hexChars = hexString.toCharArray();

    byte[] reVal = new byte[hexChars.length / 2];

    //for (int i = reVal.length - 1, j = hexChars.length - 1; 0 < j; i--, j -= 2) {
    //  reVal[i] = (byte) (Character.digit(hexChars[j - 1], 16) << 4
    //      | Character.digit(hexChars[j], 16));
    //}
    for (int i = 0; i < reVal.length; i++) {
      int h = Character.digit(hexChars[2 * i], 16) << 4;
      int l = Character.digit(hexChars[2 * i + 1], 16);
      if (h < 0 || l < 0) {
        return null;
      }
      reVal[i] = (byte) (h | l);
    }
    return reVal;
  }

  public static byte[] string2Bytes(String str, int radix) {

    return null;
  }

  /** 拼接数组.为null的数组会被忽略 */
  public static byte[] concatArray(byte[]... args) {
    if (args == null) {
      return null;
    }
    int len = 0;
    for (byte[] arg : args) {
      if (arg != null) {
        len += arg.length;
      }
    }

    byte[] reVal = new byte[len];
    int offset = 0;
    for (byte[] arg : args) {
      if (arg == null) {
        continue;
      }
      System.arraycopy(arg, 0, reVal, offset, arg.length);
      offset += arg.length;
    }
    return reVal;
  }

  public static void reverse(byte[] data) {
    if (data == null) {
      return;
    }
    for (int start = 0, end = data.length - 1; start < end; start++, end--) {
      byte temp = data[start];
      data[start] = data[end];
      data[end] = temp;
    }
  }

  private static boolean checkBounds(byte[] data, int beginIndex, int len) {
    if (data == null || data.length == 0) {
      return false;
    }
    if (beginIndex < 0 || data.length <= beginIndex) {
      return false;
    }

    if (len < 0 || data.length < beginIndex + len) {
      return false;
    }
    return true;
  }

  /**
   * 将整数转成 byte[]。例：0xABCDEF -> {0xAB,0xCD,0xEF}.
   *
   * @param byteCount 字节数，1~8 的整数
   */
  public static byte[] long2Bytes(long n, @IntRange(from = 1, to = 8) int byteCount) {
    if (8 < byteCount || byteCount <= 0) {
      return null;
    }
    byte[] reVal = new byte[byteCount];
    for (int i = byteCount - 1; 0 <= i; i--) {
      reVal[i] = (byte) (n & 0xFF);
      n >>= 8;
    }
    return reVal;
  }

  /**
   * 截取字节数组，并组成long。例：{0xAB,0xCD,0xEF} -> 0xABCDEF
   *
   * @param len 截取的长度
   * @return 成功截取返回Long，否则返回 null
   */
  public static Long bytes2Long(byte[] data, int beginIndex, @IntRange(from = 1, to = 8) int len) {

    if (!checkBounds(data, beginIndex, len)) {
      return null;
    }
    if (8 < len) {
      return null;
    }

    long res = data[beginIndex] & 0xFF;
    for (int i = beginIndex + 1; i < beginIndex + len; i++) {
      res = (res << 8) | (data[i] & 0xFF);
    }

    return res;
  }

  /**
   * 将数据V 转为LnV结构. L为V的长度，n为L所占的字节数.
   *
   * @param n 数据长度L所占的字节数
   * @param v 数据
   * @return Ln + v
   */
  public static byte[] bytes2LnVData(int n, byte[] v) {
    if (n < 1 || v == null) {
      return null;
    }
    byte[] ln = long2Bytes(v.length, n);
    return concatArray(ln, v);
  }

  final static char[] digits = {
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
      'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B',
      'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
      'V', 'W', 'X', 'Y', 'Z', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '`', '-', '=', '[',
      ']', ',', '.', '/', '~', '_', '+', '{', '}', '|', ':', '"', '<', '>', '?', ';', '\'', '\\',
  };

  public static String bytes2String(byte[] data, int radix) {
    if (data == null) {
      return null;
    } else if (data.length == 0) {
      return "";
    }

    int startPoint = data.length - 1;
    int co = 0;
    StringBuilder sb = new StringBuilder();
    for (int i = startPoint; 0 <= i; i--) {
      int n = (0xFF & data[i]) + co * 256;
      co = handleOneByte(n, radix, sb);
      System.out.println(sb);
    }
    if (0 < co) {
      sb.append(digits[co]);
    }
    return sb.reverse().toString();
  }

  private static int handleOneByte(int n, int radix, StringBuilder sb) {
    if (n < radix) {
      sb.append(digits[n]);
      return 0;
    }
    while (radix <= n) {
      sb.append(digits[n % radix]);
      n /= radix;
    }
    return n;
  }

  static void test() {
    int r = 256;
    int q = 94;

    r = 32;
    q = 12;
    int res = 1;
    for (int i = 1; i < q + 4; i++) {
      res = res * r % q;
      System.out.printf("%s^%s %% %s = %s\n", r, i, q, res);
    }
  }

  static void test2() {
    int radix = 26;
    StringBuilder sb = new StringBuilder();
    int i = 0x7F7F;
    int i1 = handleOneByte(i, radix, sb);
    System.out.println("api:" + Integer.toString(i, radix));
    System.out.println("my:" + i1 + sb.reverse().toString());

    byte[] data = { (byte) 0x7F, (byte) 0x7F };
    BigInteger bigInteger = new BigInteger(data);
    System.out.println(bigInteger);
    String s = bytes2String(data, radix);
    System.out.println("bytes2String:" + s);
    System.out.println("api:" + Integer.toString(bigInteger.intValue(), radix));
  }

  public static void main(String[] args) {
    test();
  }
}
