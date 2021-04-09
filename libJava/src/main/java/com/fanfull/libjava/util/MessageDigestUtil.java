package com.fanfull.libjava.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MessageDigestUtil {
  public static void main(String[] args) {
    byte[] epc = BytesUtil.hexString2Bytes("05532103047B47D23E618041");
    byte[] tid = BytesUtil.hexString2Bytes("E28011402000218228661800");

    checkBagId(epc, tid, false);

    System.out.println(BytesUtil.bytes2HexString(epc));

    System.out.println(checkBagId(epc, tid, true));
  }

  static boolean checkBagId(byte[] epc12, byte[] tid12, boolean isCheck) {
    if (epc12 == null || epc12.length != 12 || tid12 == null) {
      return false;
    }

    // epc前11byte 与 tid 拼接
    int bagDataLen = epc12.length - 1;
    byte[] data = new byte[bagDataLen + tid12.length];
    System.arraycopy(epc12, 0, data, 0, bagDataLen);
    System.arraycopy(tid12, 0, data, bagDataLen, tid12.length);

    // 对拼接后的数据 进行md5摘要
    byte[] md5 = md5(data);
    if (md5 == null) {
      return false;
    }

    if (isCheck) {
      return epc12[bagDataLen] == md5[0];
    }

    // md5数据的第一个字节作为 校验位
    epc12[bagDataLen] = md5[0];
    return true;
  }

  public static byte[] md5(byte[] input) {
    try {
      MessageDigest md5;
      md5 = MessageDigest.getInstance("MD5");
      md5.update(input);
      return md5.digest();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      return null;
    }
  }
}
