package com.fanfull.libjava.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class MessageDigestUtil {
  /** md5 算法，等于SHA-1. */
  public static final String ALGORITHM_MD5 = "md5";
  /** SHA 算法，等于SHA-1. */
  public static final String ALGORITHM_SHA = "sha";

  public static Charset charset = StandardCharsets.UTF_8;

  static void testMd5() {
    Map<String, String> map = new HashMap<>();
    map.put("123456", "e10adc3949ba59abbe56e057f20f883e");
    map.put("本站针对md5、sha1等全球通用公开的加密算法进行反向查询", "631a4e544578cbb6bddea063124642ca");

    for (Map.Entry<String, String> entry : map.entrySet()) {
      final byte[] plain = entry.getKey().getBytes(charset);
      final byte[] res = md5(plain);
      final String s = BytesUtil.bytes2HexString(res);
      Logs.out("%s-%s", s.equalsIgnoreCase(entry.getValue()), s);
    }
  }

  public static void main(String[] args) {
    testMd5();
  }

  public static byte[] md(byte[] input, String algorithm) {
    try {
      MessageDigest md = MessageDigest.getInstance(algorithm);
      return md.digest(input);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public static byte[] md5(byte[] input) {
    return md(input, ALGORITHM_MD5);
  }

  public static byte[] sha(byte[] input) {
    return md(input, ALGORITHM_SHA);
  }
}
