package org.orsoul.baselib.util;

import android.text.TextUtils;
import androidx.annotation.IntRange;
import java.util.Locale;

public abstract class ArrayUtils {

  private static final char[] HEX_CHAR = {
      '0', '1', '2', '3', '4', '5', '6',
      '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
  };

  public static String bytes2HexString(byte[] bArray, int start, int end) {
    if ((bArray == null) || (end < start) || (bArray.length < start)) {
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
    return sb.toString().toUpperCase(Locale.getDefault());
  }

  public static String bytes2HexString(byte[] bArray) {
    if (null == bArray) {
      return null;
    }
    return bytes2HexString(bArray, 0, bArray.length);
  }

  public static byte[] hexString2Bytes(String hexString) {
    if (hexString == null || 0 == hexString.length()) {
      return null;
    }
    char[] hexChars = hexString.toUpperCase().toCharArray();

    byte[] reVal = new byte[(hexChars.length + 1) / 2];

    for (int i = reVal.length - 1, j = hexChars.length - 1; 0 < j; i--, j -= 2) {
      reVal[i] = (byte) (Character.digit(hexChars[j - 1], 16) << 4 | Character
          .digit(hexChars[j], 16));
    }
    if (0 != hexChars.length % 2) {
      reVal[0] = (byte) Character.digit(hexChars[0], 16);
    }
    return reVal;
  }

  /**
   * @return 返回 拼接好后的新 字节数组
   * @description 拼接两个字节数组
   */
  public static byte[] concatArray(byte[] arr1, byte[] arr2) {
    // 处理 特殊情况
    if (null == arr1 && null == arr2) {
      return null;
    } else if (null == arr1) {
      return arr2;
    } else if (null == arr2) {
      return arr1;
    }

    byte[] reVal = new byte[arr1.length + arr2.length];
    System.arraycopy(arr1, 0, reVal, 0, arr1.length);
    System.arraycopy(arr2, 0, reVal, arr1.length, arr2.length);
    return reVal;
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

  /**
   * 将基金袋上的袋id通过每两位异或得到检验位，合成新的芯片的袋id
   */
  public static String bagIdConvert(String originalId) throws NumberFormatException {
    if (TextUtils.isEmpty(originalId)) {
      return "";
    }
    if (originalId.length() % 2 == 1) {
      originalId = "0" + originalId;
    }
    int checkBit = 0;
    for (int i = 0; i < originalId.length() / 2; i++) {
      checkBit ^= Integer.parseInt(originalId.substring(i * 2, i * 2 + 2), 16);
    }
    return (originalId + String.format("%2s", Integer.toHexString(checkBit))
        .replace(' ', '0')).toUpperCase();
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

  /**
   * 将整数转成 byte[]。例：0xABCDEF -> {0xAB,0xCD,0xEF}
   *
   * @param byteCount 1~8 的整数
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
}
