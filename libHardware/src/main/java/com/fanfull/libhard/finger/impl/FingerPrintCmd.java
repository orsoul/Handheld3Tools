package com.fanfull.libhard.finger.impl;

import com.apkfuns.logutils.LogUtils;
import org.orsoul.baselib.util.BytesUtil;

public abstract class FingerPrintCmd {
  /** 指纹指令头 长度. */
  public static final int CMD_HEAD_LEN = 9;
  /** 获取指纹特征码 回复指令的长度. */
  public static final int CMD_FINGER_FEATURE_LEN = 568;

  /** 指纹特征码 长度. */
  public static final int FINGER_FEATURE_LEN = 512;
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

  /** 自定义确认码 非指纹指令返回或返回超时. */
  public static final int RES_CODE_NOT_FINGER_REPLY = -1;
  /** 自定义确认码 搜索超时. */
  public static final int RES_CODE_TIMEOUT = -3;
  /** 自定义确认码 参数错误. */
  public static final int RES_CODE_ARGS_WRONG = -2;

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

  /** 7. 读出模板 PS_LoadChar. */
  private static final byte[] CMD_LOAD_FINGER_FEATURE = {
      (byte) 0xEF, 0x01, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x01, 0x00, 0x06,
      0x07, 0x01, 0x00, 0x00, 0x00, 0x0F
  };

  /** 9. 下载特征或模板 PS_DownChar. */
  private static final byte[] CMD_SAVE_FINGER_FEATURE = {
      (byte) 0xEF, 0x01, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x01, 0x00, 0x04,
      0x09, 0x01, 0x00, 0x0F
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
    //setCheckSum(CMD_GET_IMAGE);
    //setCheckSum(CMD_REG_MODEL);
    //setCheckSum(CMD_READ_SYS_PARA);

    int start = 128;
    int num = 127;
    byte[] cmdSearch = getCmdSearch(start, num);
    System.out.printf("cmdSearch %s~%s:%s\n", start, num, BytesUtil.bytes2HexString(cmdSearch));

    num = 128;
    cmdSearch = getCmdSearch(start, num);
    System.out.printf("cmdSearch %s~%s:%s\n", start, num, BytesUtil.bytes2HexString(cmdSearch));
  }

  /** 计算指令的校验和，并写在指令末尾的2字节. */
  private static boolean setCheckSum(byte[] cmd) {
    if (!isFingerCmd(cmd)) {
      return false;
    }

    int sum = 0;
    for (int i = 6; i < cmd.length - 2; i++) {
      sum += cmd[i] & 0xFF;
    }
    cmd[cmd.length - 2] = (byte) ((sum >> 8) & 0xFF);
    cmd[cmd.length - 1] = (byte) (sum & 0xFF);
    //System.out.println("sum:" + sum);
    return true;
  }

  public static byte[] convertFingerFeature2Cmd(byte[] fingerFeatureBuff) {

    if (fingerFeatureBuff == null
        || fingerFeatureBuff.length < FingerPrintCmd.FINGER_FEATURE_LEN) {
      return null;
    }

    // 每条指令长139字节，其中[9-137]为128字节的部分指纹特征码
    byte[] cmdBuff = new byte[139];
    cmdBuff[0] = (byte) 0xEF;
    cmdBuff[1] = (byte) 0x01;
    cmdBuff[2] = (byte) 0xFF;
    cmdBuff[3] = (byte) 0xFF;
    cmdBuff[4] = (byte) 0xFF;
    cmdBuff[5] = (byte) 0xFF;
    cmdBuff[7] = (byte) 0x00;
    cmdBuff[8] = (byte) 0x82;
    cmdBuff[6] = (byte) 0x02;

    byte[] reVal = new byte[CMD_FINGER_FEATURE_LEN];
    for (int i = 0; i < 4; i++) {
      int srcPos = i * 128;
      System.arraycopy(fingerFeatureBuff, srcPos, cmdBuff, 9, 128);
      if (i == 3) {
        cmdBuff[6] = (byte) 0x08;
      }
      setCheckSum(cmdBuff);
      srcPos = i * cmdBuff.length;
      System.arraycopy(cmdBuff, 0, reVal, srcPos, cmdBuff.length);
    }

    LogUtils.d("convertFingerFeature2Cmd:%s", BytesUtil.bytes2HexString(reVal));
    return reVal;
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

  /** 8. 上传特征或模板 PS_UpChar. 从缓冲区获取特征码 指令. */
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

  /** 8. 上传特征或模板 PS_UpChar. 从缓冲区获取特征码 指令，使用 bufferID1. */
  public static byte[] getCmdGetFingerFeature() {
    return getCmdGetFingerFeature(BUFFER_ID);
  }

  /** 载入特征码到缓冲区. 9. 下载特征或模板 PS_DownChar. */
  public static byte[] getCmdSaveFingerFeature(int buffId) {
    if (buffId == BUFFER_ID_1) {
      CMD_SAVE_FINGER_FEATURE[10] = BUFFER_ID_1;
      CMD_SAVE_FINGER_FEATURE[CMD_GEN_CHAR.length - 1] = 0x0F;
    } else {
      CMD_SAVE_FINGER_FEATURE[10] = BUFFER_ID_2;
      CMD_SAVE_FINGER_FEATURE[CMD_GEN_CHAR.length - 1] = 0x10;
    }
    return CMD_SAVE_FINGER_FEATURE;
  }

  /** 载入特征码到缓冲区. 9. 下载特征或模板 PS_DownChar,使用 bufferID1. */
  public static byte[] getCmdSaveFingerFeature() {
    return getCmdSaveFingerFeature(BUFFER_ID);
  }

  /** 7. 读出模板 PS_LoadChar 获取指定位置指纹的特征码 指令. */
  public static byte[] getCmdLoadFingerFeature(int buffId, int fingerIndex) {
    if (buffId == BUFFER_ID_1) {
      CMD_LOAD_FINGER_FEATURE[10] = BUFFER_ID_1;
    } else {
      CMD_LOAD_FINGER_FEATURE[10] = BUFFER_ID_2;
    }

    CMD_LOAD_FINGER_FEATURE[11] = (byte) ((fingerIndex >> 8) & 0xFF);
    CMD_LOAD_FINGER_FEATURE[12] = (byte) (fingerIndex & 0xFF);
    setCheckSum(CMD_LOAD_FINGER_FEATURE);
    return CMD_LOAD_FINGER_FEATURE;
  }

  /** 7. 读出模板 PS_LoadChar 获取指定位置指纹的特征码 指令，使用 bufferID1. */
  public static byte[] getCmdLoadFingerFeature(int pageId) {
    return getCmdLoadFingerFeature(BUFFER_ID, pageId);
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
        && (data[6] == 0x01 || data[6] == 0x07 || data[6] == 0x02 || data[6] == 0x08)
        && (len = (data[7] << 8) | (data[8] & 0xFF)) <= data.length - CMD_HEAD_LEN;
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
      return FingerPrintCmd.RES_CODE_NOT_FINGER_REPLY;
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
