package org.orsoul.baselib.util;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * DES, AES加密解密 组件
 *
 * @author orsoul
 */
public abstract class AESCoder {
  public static final String ALGORITHM_AES = "AES";
  public static final String ALGORITHM_DES = "DES";
  private static final String CIPHER_ARGS = "/ECB/PKCS5Padding";
  // private static final String CIPHER_ARGS = "/ECB/NOPadding";
  private static final String CHARSET = "UTF-8";
  /**
   * 算法名
   */
  private static String algorithmName = ALGORITHM_AES;
  /**
   * 加密 参数: DES/ECB/PKCS5Padding, AES/ECB/PKCS5Padding
   */
  private static String cipherArgs = algorithmName + CIPHER_ARGS;

  /**
   * 转化 秘钥
   *
   * @param key 二进制形式的 秘钥
   * @return 根据 参数key 生成的秘钥
   */
  private static Key toKey(byte[] key) {
    SecretKey secretKey = new SecretKeySpec(key, algorithmName);
    return secretKey;
  }

  /**
   * @param data 待处理数据
   * @param key 秘钥
   * @param mode 工作模式: Cipher.ENCRYPT_MODE 加密, Cipher.DECRYPT_MODE 解密
   */
  private static byte[] cipher(byte[] data, byte[] key, int mode)
      throws Exception {

    Key genKey = toKey(key);

    Cipher cipher = Cipher.getInstance(cipherArgs);

    cipher.init(mode, genKey);

    return cipher.doFinal(data);
  }

  private static int getKeyLen() {
    if (algorithmName.equalsIgnoreCase(ALGORITHM_DES)) {
      return 56;// DES Java仅支持56位秘钥
    } else if (algorithmName.equalsIgnoreCase(ALGORITHM_AES)) {
      return 128; // or 192, or 256
    }

    return 0;
  }

  /**
   * 设置组件 使用的 算法: ALGORITHM_DES ALGORITHM_AES
   */
  public static void setAlgorithm(String algorithm) {
    algorithmName = algorithm;
    cipherArgs = algorithmName + CIPHER_ARGS;
  }

  /**
   * 处理秘钥.
   *
   * @param key 字符串秘钥， 根据 String.getBytes("UTF-8")转成byte
   * @return 如果选用DES算法，返回8个字节长度的 二进制秘钥；如果选用AES算法，返回16个字节长度的 二进制秘钥
   * @throws Exception
   * @des 对于DES, Java仅支持56位秘钥. 如果传入的秘钥长度超过8byte,截断; 若传入的秘钥长度 小于8byte, 补0.
   * @des 对于AES, Java支持128,192,256位秘钥. 这里取128， 传入的秘钥长度超过16byte,截断; 若传入的秘钥长度
   * 小于16byte, 补0.
   */
  public static byte[] makeKey(String key) throws Exception {
    return makeKey(key.getBytes(CHARSET));
  }

  /**
   * 处理秘钥.
   *
   * @param key 二进制秘钥
   * @return 如果选用DES算法，返回8个字节长度的 二进制秘钥；如果选用AES算法，返回16个字节长度的 二进制秘钥
   * @throws Exception
   * @des 对于DES, Java仅支持56位秘钥. 如果传入的秘钥长度超过8byte,截断; 若传入的秘钥长度 小于8byte, 补0.
   * @des 对于AES, Java支持128,192,256位秘钥. 这里取128， 传入的秘钥长度超过16byte,截断; 若传入的秘钥长度
   * 小于16byte, 补0.
   */
  public static byte[] makeKey(byte[] key) {
    int genLen = getKeyLen();
    if (genLen == 56) {
      genLen = 8;
    } else {
      genLen = 16;
    }
    byte[] genKey = new byte[genLen];

    int len = Math.min(genLen, key.length);
    System.arraycopy(key, 0, genKey, 0, len);

    return genKey;
  }

  /**
   * 随机生成秘钥
   *
   * @return 字节秘钥
   * @throws Exception
   * @des 对于DES, 随机生成8byte秘钥
   * @des 对于AES, 随机生成16byte秘钥
   */
  public static byte[] genRandomKey() throws Exception {

    KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithmName);

    int keyLen = getKeyLen();
    keyGenerator.init(keyLen);

    SecretKey secretKey = keyGenerator.generateKey();

    return secretKey.getEncoded();
  }

  /**
   * 加密
   *
   * @param plainData 明文
   * @param key 二进制形式秘钥
   * @return 密文
   */
  public static byte[] encrypt(byte[] plainData, byte[] key) throws Exception {
    return cipher(plainData, makeKey(key), Cipher.ENCRYPT_MODE);
  }

  /**
   * 加密
   *
   * @param plainData 明文
   * @param key 二进制形式秘钥
   * @return 密文
   */
  public static byte[] encrypt(String plainData, String key) throws Exception {
    return encrypt(plainData.getBytes(CHARSET), makeKey(key));
  }

  public static byte[] encrypt(byte[] plainData, String key) throws Exception {
    return encrypt(plainData, makeKey(key));
  }

  public static byte[] encrypt(String plainData, byte[] key) throws Exception {
    return encrypt(plainData.getBytes(CHARSET), makeKey(key));
  }

  /**
   * @param secretData 密文
   * @param key 二进制形式秘钥
   * @return 明文
   */
  public static byte[] decrypt(byte[] secretData, byte[] key)
      throws Exception {
    return cipher(secretData, makeKey(key), Cipher.DECRYPT_MODE);
  }

  public static byte[] decrypt(String secretData, String key)
      throws Exception {
    return decrypt(secretData.getBytes(CHARSET), makeKey(key));
  }

  public static byte[] decrypt(String secretData, byte[] key)
      throws Exception {
    return decrypt(secretData.getBytes(CHARSET), makeKey(key));
  }

  public static byte[] decrypt(byte[] secretData, String key)
      throws Exception {
    return decrypt(secretData, makeKey(key));
  }

  /**
   * 加密/解密， 加解密后数据长度不变。用来对封签事件码进行加密
   *
   * @param data 明文/密文
   * @param key 密钥， 暂定取锁片TID
   * @param isEncrypt true对data进行加密； false对data解密
   * @return 加密/解密 成功 返回 true
   */
  public static boolean myEncrypt(byte[] data, byte[] key, boolean isEncrypt) {
    if (null == data || null == key || 0 == data.length || 0 == key.length) {
      return false;
    }
    if (!isEncrypt && data[0] == 0x05) {
      return true;// 遇到未加密的数据,无需解密
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
      encryptKey = encrypt(newKey, "$%&tF6&7G6R*&=[l");
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
      encodeLink(data, t);
      for (int i = 0; i < data.length; i++) {
        data[i] ^= encryptKey[i];
      }
    } else {
      // 解密， 与加密操作持续相反
      for (int i = 0; i < data.length; i++) {
        data[i] ^= encryptKey[i];
      }
      decodeLink(data, t);
    }
    return true;
  }

  private static void encodeLink(byte[] plain, int key) {
    plain[0] ^= key;
    for (int i = 1; i < plain.length; i++) {
      plain[i] ^= plain[i - 1];
    }
    for (int i = plain.length - 2; 0 <= i; i--) {
      plain[i] ^= plain[i + 1];
    }
  }

  private static void decodeLink(byte[] plain, int key) {
    for (int i = 0; i < plain.length - 1; i++) {
      plain[i] ^= plain[i + 1];
    }
    for (int i = plain.length - 1; 0 < i; i--) {
      plain[i] ^= plain[i - 1];
    }
    plain[0] ^= key;
  }

  /**
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    String key = "$%&tF6&7G6R*&=[l";
    String plain = "6427D532D93B68F4B41EB80DE1276AA61E651938091F72C3D181EC5792596478-24";
    // plain = "";
    // plain = "2017041700270100100009";

    // byte[] secretKey = makeKey(key);
    // // secretKey = genRandomKey();
    // byte[] data = plain.getBytes();

    plain = "";
    for (int i = 1; i <= 32; i++) {
      plain += "1";
      byte[] encrypt = encrypt(plain, key);

      String secretHexStr = BytesUtil.bytes2HexString(encrypt);
      System.out.println("明文长度:" + plain.length() + " --- 密文长度:"
          + secretHexStr.length());
      System.out.println(secretHexStr);
    }

    // byte[] decrypt = decrypt(encrypt, key);
    // System.out.println(new String(decrypt, CHARSET));
  }
}
