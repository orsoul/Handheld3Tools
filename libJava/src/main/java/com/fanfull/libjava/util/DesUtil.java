package com.fanfull.libjava.util;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 加密/解密工具，基于java原生API封装. <br/>
 * <br/>
 * 支持3种加密算法：DES、3DES、AES，<br/>
 * 支持加密模式：ECB、CBC.<br/>
 * 支持填充方式：PKCS5Padding、NOPadding<br/>
 */
public class DesUtil {

  /** 算法名 AES. */
  public static final String ALGORITHM_NAME_AES = "AES";
  /** 算法名 DES. */
  public static final String ALGORITHM_NAME_DES = "DES";
  /** 算法名 3DES(DESede). */
  public static final String ALGORITHM_NAME_3DES = "DESede";

  /** 加密模式 ECB. */
  public static final String ALGORITHM_MODE_ECB = "ECB";
  /** 加密模式 CBC. */
  public static final String ALGORITHM_MODE_CBC = "CBC";

  /** 明文填充模式:java只支持无填充 和 PKCS5Padding. */
  public static final String ALGORITHM_PADDING_PKCS5Padding = "PKCS5Padding";
  /** 明文填充模式 NOPadding. */
  public static final String ALGORITHM_PADDING_NOPadding = "NOPadding";

  private static final String defaultCharset = "UTF-8";

  /** 加密完整参数: 算法名/加密模式/填充模式, DES/ECB/PKCS5Padding. */
  private static String defaultAlgorithmArgs =
      String.format("%s/%s/%s",
          ALGORITHM_NAME_DES,
          ALGORITHM_MODE_ECB,
          ALGORITHM_PADDING_PKCS5Padding);

  /** 加密完整参数: 算法名/加密模式/填充模式, DES/ECB/PKCS5Padding. */
  public static void setAlgorithmArgs(String algorithmArgs) {
    defaultAlgorithmArgs = algorithmArgs;
  }

  /** 加密参数: 算法名/加密模式/填充模式, DES/ECB/PKCS5Padding. */
  public static void setAlgorithmArgs(String name, String mode, String padding) {
    defaultAlgorithmArgs = String.format("%s/%s/%s",
        name,
        mode,
        padding);
    ;
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
   * 对于3DES(DESede), 秘钥长度应为2长度（16byte）或3长度（24byte），传入的秘钥长度超过24byte,截断;
   * 若传入的秘钥长度等于16，按k1+k2+k3的次序拼接为3长度密钥；
   * 若传入的秘钥长度小于24byte,补0.如果密码位数少于等于64位，加密结果与DES相同.
   * 对于AES, Java支持128位秘钥. 传入的秘钥长度超过16byte,截断; 若传入的秘钥长度小于16byte, 补0.
   *
   * @param data 明文/密文数据
   * @param pwd 密码
   * @param isEncrypt true 执行加密，否则解密
   * @param algorithmArgs 算法参数，AES/ECB/PKCS5Padding，支持DES、3DES、AES.
   * @param initVector 向量数组，使用CBC加密模式时 需要传入非null值.
   */
  public static byte[] cipherDoFinal(byte[] data, byte[] pwd, boolean isEncrypt,
      String algorithmArgs, byte[] initVector) throws Exception {
    if (algorithmArgs == null) {
      return null;
    }
    String[] args = algorithmArgs.split("/");
    if (args.length != 3) {
      return null;
    }

    String algorithmName = args[0].toUpperCase();
    SecretKey key;
    if (ALGORITHM_NAME_DES.equalsIgnoreCase(algorithmName)) {
      key = new SecretKeySpec(pwd2Key(pwd, 8), algorithmName);
    } else if (ALGORITHM_NAME_AES.equalsIgnoreCase(algorithmName)) {
      key = new SecretKeySpec(pwd2Key(pwd, 16), algorithmName);
    } else if (ALGORITHM_NAME_3DES.equalsIgnoreCase(algorithmName)) {
      if (pwd.length == 16) {
        // 双长度密钥k1+k2，生成3长度密钥：k1 + k2 + k1
        byte[] key24 = new byte[24];
        System.arraycopy(pwd, 0, key24, 0, 16);
        System.arraycopy(pwd, 0, key24, 16, 8);
        key = new SecretKeySpec(key24, algorithmName);
      } else {
        key = new SecretKeySpec(pwd2Key(pwd, 24), algorithmName);
      }
    } else {
      return null;
    }

    Cipher cipher = Cipher.getInstance(algorithmArgs);
    int encryptMode = isEncrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
    if (args[1].equals(ALGORITHM_MODE_CBC) && initVector != null) {
      // CBC加密模式，需要向量数组
      cipher.init(encryptMode, key, new IvParameterSpec(initVector));
    } else {
      // ECB加密模式
      cipher.init(encryptMode, key);
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
  public static byte[] cipherDoFinal(byte[] data, byte[] pwd, boolean isEncrypt,
      String algorithmArgs) throws Exception {
    return cipherDoFinal(data, pwd, isEncrypt, algorithmArgs, null);
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

  /**
   * 算法参数.例：DES/ECB/PKCS5Padding
   * 支持3种加密算法：DES、3DES、AES，
   * 支持加密模式：ECB、CBC.
   * 支持：PKCS5Padding、NOPadding
   */
  private String algorithmArgs;

  private byte[] initVector;

  //private String name;
  //private String mode;
  //private String padding;

  /** 完整加密参数: 算法名/加密模式/填充模式, DES/ECB/PKCS5Padding. */
  public DesUtil(String algorithmArgs) {
    this.algorithmArgs = algorithmArgs;
  }

  /**
   * 完整加密参数: 算法名/加密模式/填充模式, DES/ECB/PKCS5Padding.
   *
   * @param initVector 向量数组，使用CBC加密模式时 需要传入非null值.
   */
  public DesUtil(String algorithmArgs, byte[] initVector) {
    this.algorithmArgs = algorithmArgs;
    this.initVector = initVector;
  }

  /**
   * 4项加密参数: 算法名/加密模式/填充模式, DES/ECB/PKCS5Padding.
   *
   * @param initVector 向量数组，使用CBC加密模式时 需要传入非null值.
   */
  public void setArgs(String name, String mode, String padding, byte[] initVector) {
    this.algorithmArgs = String.format("%s/%s/%s",
        name,
        mode,
        padding);
    this.initVector = initVector;
  }

  /** 完整加密参数: 算法名/加密模式/填充模式, DES/ECB/PKCS5Padding. */
  public void setArgs(String algorithmArgs) {
    this.algorithmArgs = algorithmArgs;
  }

  public byte[] encrypt(byte[] data, byte[] pwd) {
    try {
      return cipherDoFinal(data, pwd, true, algorithmArgs, initVector);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public byte[] encrypt(String data, byte[] pwd) {
    try {
      return encrypt(data.getBytes(defaultCharset), pwd);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public byte[] encrypt(byte[] data, String pwd) {
    try {
      return encrypt(data, pwd.getBytes(defaultCharset));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public byte[] encrypt(String data, String pwd) {
    try {
      return encrypt(data.getBytes(defaultCharset), pwd.getBytes(defaultCharset));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public byte[] decrypt(byte[] data, byte[] pwd) {
    try {
      return cipherDoFinal(data, pwd, false, algorithmArgs, initVector);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public byte[] decrypt(String data, byte[] pwd) {
    try {
      return decrypt(data.getBytes(defaultCharset), pwd);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public byte[] decrypt(byte[] data, String pwd) {
    try {
      return decrypt(data, pwd.getBytes(defaultCharset));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public byte[] decrypt(String data, String pwd) {
    try {
      return decrypt(data.getBytes(defaultCharset), pwd.getBytes(defaultCharset));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  static void testKey(byte[] keyBytes, String algorithm) {
    try {
      //第一种，Factory
      DESKeySpec keySpec = new DESKeySpec(keyBytes);
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(algorithm);
      SecretKey key1 = keyFactory.generateSecret(keySpec);

      //第二种, Generator
      KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);
      keyGenerator.init(56, new SecureRandom(keyBytes));//key为8个字节，实际用了56位； 后面随机数用key作为种子seed生成
      SecretKey key2 = keyGenerator.generateKey();

      //第三种， SecretKeySpec
      SecretKey key3 = new SecretKeySpec(keyBytes, algorithm);//SecretKeySpec类同时实现了Key和KeySpec接口

      //打印
      System.out.println("key1：" + BytesUtil.bytes2HexString(key1.getEncoded()));
      System.out.println("key2：" + BytesUtil.bytes2HexString(key2.getEncoded()));
      System.out.println("key3：" + BytesUtil.bytes2HexString(key3.getEncoded()));
    } catch (Exception e) {
      System.out.println(e.toString());
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws Exception {
    testKey(new byte[8], ALGORITHM_NAME_3DES);
    //testDes();
  }

  static void test3Des() throws Exception {
    String text = "12345678";
    String pwd = "12345678";
    byte[] dataEncrypt;
    byte[] dataDecrypt;

    pwd = "01234567";
    dataEncrypt = cipherDoFinal(text, pwd, true, "DESede/ECB/NOPadding");
    Logs.out(BytesUtil.bytes2HexString(dataEncrypt));

    pwd = "0123456789abcdef";
    dataEncrypt = cipherDoFinal(text, pwd, true, "DESede/ECB/NOPadding");
    Logs.out(BytesUtil.bytes2HexString(dataEncrypt));

    pwd = "0123456789abcdef";
    dataEncrypt = cipherDoFinal(text, pwd, true, "DESede/ECB/NOPadding");
    Logs.out(BytesUtil.bytes2HexString(dataEncrypt));
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
    System.out.println(String.format("%s, PKCS5Padding", BytesUtil.bytes2HexString(dataEncrypt)));

    dataEncrypt = cipherDoFinal(text, pwd, true, "DES/ECB/NoPadding");
    //dataDecrypt = cipherDoFinal(dataEncrypt, pwd, false, "DES/ECB/NOPadding");
    System.out.println(String.format("%s, NOPadding", BytesUtil.bytes2HexString(dataEncrypt)));

    dataEncrypt = cipherDoFinal(text, pwd, true, "DES/ECB/ZeroPadding");
    dataDecrypt = cipherDoFinal(dataEncrypt, pwd, false, "DES/ECB/ZeroPadding");
    System.out.println(String.format("%s, ZeroPadding", BytesUtil.bytes2HexString(dataEncrypt)));
  }
}
