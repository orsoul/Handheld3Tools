package com.fanfull.libhard.finger.impl;

import com.apkfuns.logutils.LogUtils;
import org.orsoul.baselib.util.BytesUtil;

public abstract class FingerPrintCmd {
  /** 指纹指令头 长度. */
  public static final int CMD_HEAD_LEN = 9;
  /** 指纹容量为128，指纹id范围 0~127. */
  public static final int FINGER_MAX_NUM = 128;

  /** 指纹模块缓冲区号1. */
  public static final byte BUFFER_ID_1 = 0x01;
  /** 指纹模块缓冲区号2. */
  public static final byte BUFFER_ID_2 = 0x02;
  /** 指纹模块 默认缓冲区号. */
  public static final byte BUFFER_ID = BUFFER_ID_1;

  /** 确认码 成功. */
  public static final int RES_CODE_SUCCESS = 0;
  /** 确认码 感应器无指纹. */
  public static final int RES_CODE_NO_FINGER = 0x02;
  /** 确认码 未匹配到指纹. */
  public static final int RES_CODE_NO_MATCH = 0x09;

  /** 读有效模板个数 PS_ValidTempleteNum. */
  public static final byte[] CMD_FINGER_NUM = {
      (byte) 0xEF, 0x01, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x01, 0x00, 0x03,
      0x1d, 0x00, 0x21
  };
  /** 获取指纹图像 PS_GetImage. */
  public static final byte[] CMD_GET_IMAGE = {
      (byte) 0xEF, 0x01, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x01, 0x00, 0x03,
      0x01, 0x00, 0x05
  };

  /** 生成特征 PS_GenChar. */
  private static final byte[] CMD_GEN_CHAR = {
      (byte) 0xEF, 0x01, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x01, 0x00, 0x04,
      0x02, BUFFER_ID, 0x00, 0x08
  };

  /** 合并特征（生成模板） PS_RegModel. */
  public static final byte[] CMD_REG_MODEL = {
      (byte) 0xEF, 0x01, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x01, 0x00, 0x03,
      0x05, 0x00, 0x09
  };

  /** 储存模板 PS_StoreChar. */
  private static final byte[] CMD_STORE_CHAR = {
      (byte) 0xEF, 0x01, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x01, 0x00, 0x06,
      0x06, 0x01, 0x00, 0x01, 0x00, 0x0F
  };
  /** 搜索指纹 PS_Search. */
  public static final byte[] CMD_SEARCH = {
      (byte) 0xEF, 0x01, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x01, 0x00, 0x08,
      0x04, 0x01, 0x00, 0x00, 0x00, 0x10, 0x00, 0x1E
  };
  /** 自动验证指纹 PS_Identify. */
  public static final byte[] CMD_SEARCH_IDENTIFY = {
      (byte) 0xEF, 0x01, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x01, 0x00, 0x03,
      0x11, 0x00, 0x15
  };

  /** 读系统基本参数 PS_ReadSysPara. */
  public static final byte[] CMD_READ_SYS_PARA = {
      (byte) 0xEF, 0x01, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x01, 0x00, 0x03,
      0x0F, 0x00, 0x13
  };

  public static final byte[] loadchar = {
      (byte) 0xef, 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x01, 0x00, 0x06,
      0x07, 0x01, 0x00, 0x00, 0x00, 0x0f
  };
  /** 8. 上传特征或模板 PS_UpChar. */
  private static final byte[] CMD_GET_FINGER_FEATURE = {
      (byte) 0xef, 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x01, 0x00, 0x04,
      0x08, 0x01, 0x00, 0x0E
  };
  /** 自动注册模板 PS_Enroll. */
  public static final byte[] CMD_ADD_FINGER = {
      (byte) 0xef, 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x01, 0x00, 0x03,
      0x10, 0x00, 0x14
  };
  /** 13. 清空指纹库 PS_Empty. */
  public static final byte[] CMD_CLEAR_FINGER = {
      (byte) 0xEF, 0x01, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x01, 0x00, 0x03,
      0x0D, 0x00, 0x11
  };
  public static final byte[] mReadindex = {
      (byte) 0xEF, 0x01, (byte) 0xFF, (byte) 0xFF,
      (byte) 0xFF, (byte) 0xFF, 0x01, 0x00, 0x04, 0x1F, 0x00, 0x00, 0x24
  };// PS_ReadIndexTable
  public static final byte[] writeText = {
      (byte) 0xEF, 0x01, (byte) 0xFF, (byte) 0xFF,
      (byte) 0xFF, (byte) 0xFF, 0x01, 0x00, 0x24, 0x18, 0x00, 0x00, 0x00,
      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x3E
  };// PS_UpChar
  public static final byte[] readText = {
      (byte) 0xEF, 0x01, (byte) 0xFF, (byte) 0xFF,
      (byte) 0xFF, (byte) 0xFF, 0x01, 0x00, 0x04, 0x19, 0x00, 0x00, 0x1E
  };

  public static void main(String[] args) {
    setCheckSum(CMD_GET_IMAGE);
    setCheckSum(CMD_REG_MODEL);
    setCheckSum(CMD_READ_SYS_PARA);
  }

  /** 计算指令的校验和，并写在指令末尾的2字节. */
  private static boolean setCheckSum(byte[] cmd) {
    if (!isFingerCmd(cmd)) {
      return false;
    }

    int sum = 0;
    for (int i = 6; i < cmd.length - 2; i++) {
      sum += cmd[i];
    }
    cmd[cmd.length - 2] = (byte) ((sum >> 8) & 0xFF);
    cmd[cmd.length - 1] = (byte) (sum & 0xFF);
    //System.out.println("sum:" + ArrayUtils.bytes2HexString(cmd));
    return true;
  }

  /** 生成特征码 指令. */
  public static byte[] getCmdGenChar(int buffId) {
    if (buffId == BUFFER_ID_1) {
      CMD_GEN_CHAR[10] = BUFFER_ID_1;
      CMD_GEN_CHAR[CMD_GEN_CHAR.length - 1] = 0x08;
    } else {
      CMD_GEN_CHAR[10] = BUFFER_ID_2;
      CMD_GEN_CHAR[CMD_GEN_CHAR.length - 1] = 0x09;
    }
    return CMD_GEN_CHAR;
  }

  /** 生成特征码 指令. */
  public static byte[] getCmdGenChar() {
    return getCmdGenChar(BUFFER_ID);
  }

  /** 保存特征码 指令. */
  public static byte[] getCmdStoreChar(int pageID) {
    CMD_STORE_CHAR[11] = (byte) ((pageID >> 8) & 0xFF);
    CMD_STORE_CHAR[12] = (byte) (pageID & 0xFF);
    setCheckSum(CMD_STORE_CHAR);
    return CMD_STORE_CHAR;
  }

  /** 获取特征码 指令. */
  public static byte[] getCmdGetFingerFeature(int buffId) {
    if (buffId == BUFFER_ID_1) {
      CMD_GET_FINGER_FEATURE[10] = BUFFER_ID_1;
      CMD_GET_FINGER_FEATURE[CMD_GEN_CHAR.length - 1] = 0x0E;
    } else {
      CMD_GET_FINGER_FEATURE[10] = BUFFER_ID_2;
      CMD_GET_FINGER_FEATURE[CMD_GEN_CHAR.length - 1] = 0x0F;
    }
    return CMD_GET_FINGER_FEATURE;
  }

  /** 获取特征码 指令. */
  public static byte[] getCmdGetFingerFeature() {
    return getCmdGetFingerFeature(BUFFER_ID);
  }

  /** 搜索指纹 指令.最大搜索范围 0~127 */
  public static byte[] getCmdSearch(int startPageId, int pageNum) {
    CMD_SEARCH[11] = (byte) ((startPageId >> 8) & 0xFF);
    CMD_SEARCH[12] = (byte) (startPageId & 0xFF);

    CMD_SEARCH[13] = (byte) ((pageNum >> 8) & 0xFF);
    CMD_SEARCH[14] = (byte) (pageNum & 0xFF);
    setCheckSum(CMD_SEARCH);
    return CMD_SEARCH;
  }

  /** 搜索指纹 指令，全范围搜索 0~127. */
  public static byte[] getCmdSearch() {
    return getCmdSearch(0, FINGER_MAX_NUM);
  }

  /**
   * 包头[2byte] 芯片地址[4byte] 包标识[1byte] 包长度[2byte] 确认码[1byte] 校验和[2byte]
   * 包头  芯片地址 包标识   包长度   确认码  校验和
   * [2byte] [4byte] [1byte] [2byte] [1byte] [2byte]
   *
   * @param data 例：EF 01 FF FF FF FF 07 00 03 00 00 0A
   */
  public static boolean isFingerCmd(byte[] data) {
    int len = 0;
    boolean isCmd = (data != null)
        && (CMD_HEAD_LEN <= data.length)
        && (data[0] == (byte) 0xEF)
        && (data[1] == 0x01)
        && (data[6] == 0x01 || data[6] == 0x07)
        && (len = (data[7] << 8) | (data[8] & 0xFF)) == data.length - CMD_HEAD_LEN;
    if (!isCmd) {
      LogUtils.i("not FingerCmd:%s, len:%s", BytesUtil.bytes2HexString(data), len);
    }
    return isCmd;
  }

  /**
   * 获取指纹模块返回的确认码.
   *
   * @param cmd 指纹模块返回的指令
   * @return 非指纹指令返回-1，否则返回正整数确认码：即 cmd[9]<br/>
   * 确认码=00H 表示搜索到<br/>
   * 确认码=01H 表示收包有错<br/>
   * 确认码=02H 表示传感器上无手指<br/>
   */
  public static int getFingerRes(byte[] cmd) {
    if (!isFingerCmd(cmd)) {
      return -1;
    }
    return cmd[9];
  }

  /**
   * 获取指纹搜索结果.
   *
   * @param cmd 指纹模块返回的指令
   * @param resBuff 保存结果，长度为2，resBuff[0]为指纹所在指纹库的pageId，resBuff[1]为指纹得分.
   * @return 非指纹指令返回-1；否则返回确认码，即 cmd[9]，返回0即为 搜索到结果
   */
  public static int getSearchRes(byte[] cmd, int[] resBuff) {
    int res = getFingerRes(cmd);
    if (res == RES_CODE_SUCCESS && resBuff != null && 2 <= resBuff.length) {
      resBuff[0] = (cmd[10] << 8) | (cmd[11] & 0xFF);
      resBuff[1] = (cmd[12] << 8) | (cmd[13] & 0xFF);
    }
    return res;
  }

  public static int[] getSearchRes(byte[] cmd) {
    int res = getFingerRes(cmd);
    int[] resBuff = null;
    if (res == RES_CODE_SUCCESS) {
      resBuff = new int[2];
      resBuff[0] = (cmd[10] << 8) | (cmd[11] & 0xFF);
      resBuff[1] = (cmd[12] << 8) | (cmd[13] & 0xFF);
    }
    return resBuff;
  }

  /**
   * 根据【自动注册模板 PS_Enroll】的应答消息，截取指纹id.
   *
   * @return 添加指纹成功返回大于或等于0的指纹ID，否则返回负数
   */
  public static int getFingerId(byte[] cmd) {
    int res = getFingerRes(cmd);
    if (res == RES_CODE_SUCCESS) {
      res = (cmd[10] << 8) | (cmd[11] & 0xFF);
    } else if (0 < res) {
      res = -res;
    }
    return res;
  }
}
