package org.orsoul.baselib.util.lock;

/**
 * 封袋、出入库、开袋 会用到的一些方法。
 */
public abstract class Lock3Util {
  /**
   * 十套代表F1-->F4的标志 和单片机A版本对接的，请勿修改
   */
  public static final byte[] FLAG_DATA = new byte[] {
      (byte) 0x23,
      (byte) 0x5f, (byte) 0x8e, (byte) 0x41, (byte) 0x4d, (byte) 0x8c,
      (byte) 0x3d, (byte) 0x6a, (byte) 0x23, (byte) 0x9c, (byte) 0x95,
      (byte) 0x3c, (byte) 0x4b, (byte) 0x11, (byte) 0x73, (byte) 0x1c,
      (byte) 0x3e, (byte) 0x22, (byte) 0x49, (byte) 0x83, (byte) 0x36,
      (byte) 0x47, (byte) 0x88, (byte) 0x26, (byte) 0x32, (byte) 0x28,
      (byte) 0x3d, (byte) 0x6f, (byte) 0x78, (byte) 0x7a, (byte) 0x99,
      (byte) 0x2d, (byte) 0x6c, (byte) 0x24, (byte) 0xa3, (byte) 0x8c,
      (byte) 0x7f, (byte) 0x9d, (byte) 0x36, (byte) 0xb2, (byte) 0x25,
      (byte) 0x39, (byte) 0x48, (byte) 0x76, (byte) 0xea, (byte) 0x2c,
      (byte) 0x36, (byte) 0x47, (byte) 0x79, (byte) 0x29,
  };
  /** 启用码 4byte：未启用 00000000. */
  public static final byte[] ENABLE_CODE_DISABLE = new byte[4];
  /** 启用码 4byte：已启用 FFDDFFEE. */
  public static final byte[] ENABLE_CODE_ENABLE = new byte[] {
      (byte) 0xFF, (byte) 0xDD, (byte) 0xFF, (byte) 0xEE,
  };
  /** 启用码 4byte：已注销 EEEEEEEE注销. */
  public static final byte[] ENABLE_CODE_UN_REG = new byte[] {
      (byte) 0xEE, (byte) 0xEE, (byte) 0xEE, (byte) 0xEE,
  };
  /**
   * 最低电压值 2.85v
   */
  public static final double LOWEST_V = 2.85d;
  /**
   * 双电池，使用第1个
   */
  public static final int CELL_1 = 0xC3;
  /**
   * 双电池，使用第2个
   */
  public static final int CELL_2 = 0x3C;
  /** 0xFF 测试模式 */
  public static final byte MODE_DEBUG = (byte) 0xFF;
  /** 0x00 非FF,正常模式 */
  public static final byte MODE_NORMAL = 0x00;

  /**
   * 加密、解密标志位。1~5：对应标志位F1~F5.
   *
   * @param status 标志位 密文或明文
   * @param keyNum 原始 密钥 编号 A0~A9
   * @param uid 锁内NFC的UID
   * @param isEncrypt false：解密加密标志位解出明文标志位；true: 加密 明文标志位 获得 密文标志位
   * @return -1：uid错误； -2：密钥编号错误； -3：解出的标志位错误； 明文1~5：对应标志位F1~F5；
   * ！isPlainFlag： 加密成功返回
   */
  public static int getStatus(int status, int keyNum, byte[] uid,
      boolean isEncrypt) {
    if (null == uid || uid.length != 7) {
      return -1;
    }

    byte key = (byte) 0x0;// 解密密钥

    // int n = miyueNum & 0x0F;// 密钥编号 0~9
    switch (keyNum & 0x0F) {// 密钥编号 : A0~A9 --> 0~9
      case 0:
        key = (byte) (uid[3] + uid[4] + uid[1]);
        break;
      case 1:
        key = (byte) (key ^ uid[1]);
        key = (byte) (key ^ uid[3]);
        key = (byte) (key ^ uid[5]);
        break;
      case 2:
        key = (byte) (key ^ uid[4]);
        key = (byte) (key ^ uid[5]);
        key = (byte) (key ^ uid[6]);
        break;
      case 3:
        key = (byte) (uid[3] + uid[2] + uid[1]);
        break;
      case 4:
        key = (byte) (uid[6] + 1);
        break;
      case 5:
        key = (byte) (uid[1] + uid[4] + uid[6]);
        break;
      case 6:
        key = (byte) (uid[1] ^ uid[3]);
        key = (byte) (key + uid[5]);
        break;
      case 7:
        key = (byte) (uid[4] + uid[6]);
        key = (byte) (key ^ uid[2]);
        break;
      case 8:
        key = (byte) (uid[2] + uid[4]);
        break;
      case 9:
        key = (byte) (uid[3] + uid[5]);
        key = (byte) (key ^ uid[4]);
        break;
      default:
        // 密钥编号 非法
        return -2;
    }

    int t = (uid[1] & 0xFF) % 10;
    if (!isEncrypt) {
      // 解出 明文 标志位
      int flagPlain = key ^ status;
      for (int i = 0; i < 5; i++) {
        if (flagPlain == FLAG_DATA[5 * t + i]) {
          return i + 1;
        }
      }
      return -3;
    } else {
      // 生成 密文 标志位
      byte encodeFlag = FLAG_DATA[5 * t + (status - 1)];
      return encodeFlag ^ key;
    }
  }

  /** 解析 电压值. */
  public static float parseV(byte v) {
    int t = v & 0xFF;
    return (2.5f * t) / 128;
  }

  /** 解析 电压值. */
  public static String parseV2String(byte v) {
    float vf = parseV(v);
    return String.format("%.3f", vf);
  }

  private static boolean checkKeyNum(int keyNum) {
    return 0 <= keyNum && keyNum <= 9;
  }

  public static int parseKeyNum(int keyNumOriginal) {
    int keyNum = keyNumOriginal & 0x0F;
    if (checkKeyNum(keyNum)) {
      return keyNum;
    } else {
      return -1;
    }
  }

  public static String getStatusDesc(int status) {
    //    F1：空袋状态（未插锁片，此时可插拔锁片）
    //    F2：上锁状态（已插锁片，此时可插拔锁片）
    //    F3：已封袋状态（该袋已成功封签，此时不可插拔锁片，否则触发报警）
    //    F4：已开袋状态（此时可拆锁片，拆锁片后会回到F1空袋状态）
    //    F5：袋锁电量不足（该袋无法使用，需返厂维修）
    switch (status) {
      case 1:
        return "F1(空袋)";
      case 2:
        return "F2(已插锁片)";
      case 3:
        return "F3(已封袋)";
      case 4:
        return "F4(已开袋)";
      case 5:
        return "F5(电量低)";
      default:
        return String.valueOf(status);
    }
  }
}
