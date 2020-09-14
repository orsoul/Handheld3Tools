package org.orsoul.baselib.util;

import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * DES、3DES、AES 加密/解密，只支持ECB加密模式.
 */
public class DesUtil {

  /** 算法名 AES. */
  public static final String ALGORITHM_NAME_AES = "AES";
  /** 算法名 DES. */
  public static final String ALGORITHM_NAME_DES = "DES";
  /** 算法名 DESede. */
  public static final String ALGORITHM_NAME_DESede = "DESede";

  /** 加密模式 ECB. */
  public static final String ALGORITHM_MODE_ECB = "ECB";
  /** 加密模式 CBC. */
  public static final String ALGORITHM_MODE_CBC = "CBC";

  /** 明文填充模式 PKCS5Padding. */
  public static final String PADDING_MODE_PKCS5Padding = "PKCS5Padding";
  /** 明文填充模式 NOPadding. */
  public static final String PADDING_MODE_NOPadding = "NOPadding";

  private static final String defaultCharset = "UTF-8";
  ///** 算法名. */
  //private static String defaultAlgorithmName = ALGORITHM_NAME_DES;
  ///** 加密模式. */
  //private static String defaultAlgorithmMode = ALGORITHM_MODE_ECB;
  ///** 填充模式. */
  //private static String defaultAlgorithmPadding = PADDING_MODE_PKCS5Padding;
  /** 加密完整参数: 算法名/加密模式/填充模式, DES/ECB/PKCS5Padding. */
  private static String defaultAlgorithmArgs =
      String.format("%s/%s/%s", ALGORITHM_NAME_DES, ALGORITHM_MODE_ECB,
          PADDING_MODE_PKCS5Padding);

  public static void setAlgorithmArgs(String algorithmArgs) {
    defaultAlgorithmArgs = algorithmArgs;
  }

  /**
   * 处理密码，将密码长度延长或截断至8的倍数.
   *
   * @param pwd 密码
   * @param genLen 新密长度
   */
  private static byte[] pwd2Key(byte[] pwd, int genLen) {
    if (pwd == null || genLen < 8 || genLen % 8 != 0) {
      return null;
    }
    if (pwd.length == genLen) {
      return pwd;
    }

    byte[] genKey = new byte[genLen];
    int len = Math.min(genLen, pwd.length);
    System.arraycopy(pwd, 0, genKey, 0, len);
    return genKey;
  }

  /**
   * 对于DES, 秘钥长度应为8byte, Java仅支持56位秘钥. 如果传入的秘钥长度超过8byte,截断; 若传入的秘钥长度 小于8byte, 补0.
   * 对于3DES(DESede), 秘钥长度应为24byte， 传入的秘钥长度超过24byte,截断; 若传入的秘钥长度小于24byte,
   * 补0.如果密码位数少于等于64位，加密结果与DES相同.
   * 对于AES, Java支持128位秘钥. 传入的秘钥长度超过16byte,截断; 若传入的秘钥长度小于16byte, 补0.
   *
   * @param data 明文/密文数据
   * @param pwd 密码
   * @param isEncrypt true 执行加密，否则解密
   * @param algorithmArgs 算法参数，AES/ECB/PKCS5Padding，支持DES、3DES、AES.
   */
  public static byte[] cipherDoFinal(byte[] data, byte[] pwd, boolean isEncrypt,
      String algorithmArgs) throws Exception {
    if (algorithmArgs == null) {
      return null;
    }
    String[] args = algorithmArgs.split("/");
    if (args.length != 3) {
      return null;
    }
    String algorithmName = args[0];

    SecretKey key;
    if (ALGORITHM_NAME_DES.equals(algorithmName)) {
      key = new SecretKeySpec(pwd2Key(pwd, 8), algorithmName);
    } else if (ALGORITHM_NAME_AES.equals(algorithmName)) {
      key = new SecretKeySpec(pwd2Key(pwd, 16), algorithmName);
    } else {
      key = new SecretKeySpec(pwd2Key(pwd, 24), algorithmName);
    }

    Cipher cipher = Cipher.getInstance(algorithmArgs);
    if (isEncrypt) {
      cipher.init(Cipher.ENCRYPT_MODE, key);
    } else {
      cipher.init(Cipher.DECRYPT_MODE, key);
    }

    return cipher.doFinal(data);
  }

  /**
   * 对于DES, 秘钥长度应为8byte, Java仅支持56位秘钥. 如果传入的秘钥长度超过8byte,截断; 若传入的秘钥长度 小于8byte, 补0.
   * 对于3DES(DESede), 秘钥长度应为24byte， 传入的秘钥长度超过24byte,截断; 若传入的秘钥长度小于24byte,
   * 补0.如果密码位数少于等于64位，加密结果与DES相同.
   * 对于AES, Java支持128位秘钥. 传入的秘钥长度超过16byte,截断; 若传入的秘钥长度小于16byte, 补0.
   *
   * @param data 明文/密文数据
   * @param pwd 密码
   * @param isEncrypt true 执行加密，否则解密
   * @param algorithmArgs 算法参数，AES/ECB/PKCS5Padding，支持DES、3DES、AES.
   */
  public static byte[] cipherDoFinal(String data, String pwd, boolean isEncrypt,
      String algorithmArgs) throws Exception {
    return cipherDoFinal(
        data.getBytes(defaultCharset),
        pwd.getBytes(defaultCharset),
        isEncrypt,
        algorithmArgs);
  }

  /**
   * 对于DES, 秘钥长度应为8byte, Java仅支持56位秘钥. 如果传入的秘钥长度超过8byte,截断; 若传入的秘钥长度 小于8byte, 补0.
   * 对于3DES(DESede), 秘钥长度应为24byte， 传入的秘钥长度超过24byte,截断; 若传入的秘钥长度小于24byte,
   * 补0.如果密码位数少于等于64位，加密结果与DES相同.
   * 对于AES, Java支持128位秘钥. 传入的秘钥长度超过16byte,截断; 若传入的秘钥长度小于16byte, 补0.
   *
   * @param data 明文/密文数据
   * @param pwd 密码
   * @param isEncrypt true 执行加密，否则解密
   * @param algorithmArgs 算法参数，AES/ECB/PKCS5Padding，支持DES、3DES、AES.
   */
  public static byte[] cipherDoFinal(byte[] data, String pwd, boolean isEncrypt,
      String algorithmArgs) throws Exception {
    return cipherDoFinal(
        data,
        pwd.getBytes(defaultCharset),
        isEncrypt,
        algorithmArgs);
  }

  /**
   * 对于DES, 秘钥长度应为8byte, Java仅支持56位秘钥. 如果传入的秘钥长度超过8byte,截断; 若传入的秘钥长度 小于8byte, 补0.
   * 对于3DES(DESede), 秘钥长度应为24byte， 传入的秘钥长度超过24byte,截断; 若传入的秘钥长度小于24byte,
   * 补0.如果密码位数少于等于64位，加密结果与DES相同.
   * 对于AES, Java支持128位秘钥. 传入的秘钥长度超过16byte,截断; 若传入的秘钥长度小于16byte, 补0.
   *
   * @param data 明文/密文数据
   * @param pwd 密码
   * @param isEncrypt true 执行加密，否则解密
   * @param algorithmArgs 算法参数，AES/ECB/PKCS5Padding，支持DES、3DES、AES.
   */
  public static byte[] cipherDoFinal(String data, byte[] pwd, boolean isEncrypt,
      String algorithmArgs) throws Exception {
    return cipherDoFinal(
        data.getBytes(defaultCharset),
        pwd,
        isEncrypt,
        algorithmArgs);
  }

  /** 算法参数，支持DES、3DES、AES，支持ECB. 例：DES/ECB/PKCS5Padding */
  private String algorithmArgs;

  private DesUtil(String algorithmArgs) {
    this.algorithmArgs = algorithmArgs;
  }

  public byte[] encrypt(byte[] data, byte[] pwd) {
    try {
      return cipherDoFinal(data, pwd, true, algorithmArgs);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public byte[] encrypt(String data, byte[] pwd) {
    try {
      return cipherDoFinal(data, pwd, true, algorithmArgs);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public byte[] encrypt(byte[] data, String pwd) {
    try {
      return cipherDoFinal(data, pwd, true, algorithmArgs);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public byte[] encrypt(String data, String pwd) {
    try {
      return cipherDoFinal(data, pwd, true, algorithmArgs);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public byte[] decrypt(byte[] data, byte[] pwd) {
    try {
      return cipherDoFinal(data, pwd, false, algorithmArgs);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public byte[] decrypt(String data, byte[] pwd) {
    try {
      return cipherDoFinal(data, pwd, false, algorithmArgs);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public byte[] decrypt(byte[] data, String pwd) {
    try {
      return cipherDoFinal(data, pwd, false, algorithmArgs);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public byte[] decrypt(String data, String pwd) {
    try {
      return cipherDoFinal(data, pwd, false, algorithmArgs);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private static void testKey(byte[] keyBytes) {
    try {
      //第一种，Factory
      DESKeySpec keySpec = new DESKeySpec(keyBytes);
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
      SecretKey key1 = keyFactory.generateSecret(keySpec);

      //第二种, Generator
      KeyGenerator keyGenerator = KeyGenerator.getInstance("DES");
      keyGenerator.init(56, new SecureRandom(keyBytes));//key为8个字节，实际用了56位； 后面随机数用key作为种子seed生成
      SecretKey key2 = keyGenerator.generateKey();

      //第三种， SecretKeySpec
      SecretKey key3 = new SecretKeySpec(keyBytes, "DES");//SecretKeySpec类同时实现了Key和KeySpec接口

      //打印
      System.out.println("key1：" + ArrayUtils.bytes2HexString(key1.getEncoded()));
      System.out.println("key2：" + ArrayUtils.bytes2HexString(key2.getEncoded()));
      System.out.println("key3：" + ArrayUtils.bytes2HexString(key3.getEncoded()));
    } catch (Exception e) {
      System.out.println(e.toString());
    }
  }

  public static void main(String[] args) throws Exception {

    testDes();
    //int i = 2;
    //System.out.println(String.format("%d,%d,%d,%d,", i++, ++i, i, i++));
    //String s = "char *s=%c%s%c;%cprintf(s,34,s,34,10);";
    //System.out.printf(s, 34, s, 34, 10);
  }

  static void testDes() throws Exception {
    String text = "哈喽12ab_#";
    String pwd = "00000000";
    byte[] dataEncrypt;
    byte[] dataDecrypt;

    text = "12345678";
    //text = "12345678123456_#";
    dataEncrypt = cipherDoFinal(text, pwd, true, "DES/ECB/PKCS5Padding");
    //dataDecrypt = cipherDoFinal(dataEncrypt, pwd, false, "DES/ECB/PKCS5Padding");
    System.out.println(String.format("%s, PKCS5Padding", ArrayUtils.bytes2HexString(dataEncrypt)));

    dataEncrypt = cipherDoFinal(text, pwd, true, "DES/ECB/NoPadding");
    //dataDecrypt = cipherDoFinal(dataEncrypt, pwd, false, "DES/ECB/NOPadding");
    System.out.println(String.format("%s, NOPadding", ArrayUtils.bytes2HexString(dataEncrypt)));

    dataEncrypt = cipherDoFinal(text, pwd, true, "DES/ECB/ZeroPadding");
    dataDecrypt = cipherDoFinal(dataEncrypt, pwd, false, "DES/ECB/ZeroPadding");
    System.out.println(String.format("%s, ZeroPadding", ArrayUtils.bytes2HexString(dataEncrypt)));
  }
}
