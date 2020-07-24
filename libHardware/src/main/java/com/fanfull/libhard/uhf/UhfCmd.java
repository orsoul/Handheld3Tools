package com.fanfull.libhard.uhf;

import com.apkfuns.logutils.LogUtils;
import java.util.Arrays;
import org.orsoul.baselib.util.ArrayUtils;

/**
 *
 */
public abstract class UhfCmd {
  private static final String TAG = UhfCmd.class.getSimpleName();
  /** epc区. */
  public static final int MB_EPC = 1;
  /** tid区. */
  public static final int MB_TID = 2;
  /** use区. */
  public static final int MB_USE = 3;

  public static final int RECEIVE_TYPE_GET_DEVICE_VERSION = 0x01;
  public static final int RECEIVE_TYPE_GET_DEVICE_ID = 0x05;

  public static final int RECEIVE_TYPE_FAST_EPC = 0x81;
  public static final int RECEIVE_TYPE_FAST_TID = 0x8F;
  public static final int RECEIVE_TYPE_READ = 0x85;
  public static final int RECEIVE_TYPE_WRITE = 0x87;

  public static final int RECEIVE_TYPE_SET_FAST_ID = 0x5D;
  public static final int RECEIVE_TYPE_GET_FAST_ID = 0x5F;

  public static final int RECEIVE_TYPE_SET_POWER = 0x11;
  public static final int RECEIVE_TYPE_GET_POWER = 0x13;

  public static final int RECEIVE_TYPE_READ_LOT = 0x83;

  /** 获取当前设备版本. */
  public static final byte[] CMD_GET_DEVICE_VERSION = new byte[] {
      (byte) 0xA5, (byte) 0x5A, (byte) 0x00, (byte) 0x08, (byte) 0x00, (byte) 0x08, (byte) 0x0D,
      (byte) 0x0A,
  };
  /** 获取当前设备ID. */
  public static final byte[] CMD_GET_DEVICE_ID = new byte[] {
      (byte) 0xA5, (byte) 0x5A, (byte) 0x00, (byte) 0x08, (byte) 0x04, (byte) 0x0C, (byte) 0x0D,
      (byte) 0x0A,
  };
  /** 获取当前设备发射功率. */
  public static final byte[] CMD_GET_POWER = new byte[] {
      (byte) 0xA5,
      (byte) 0x5A, 0x00, 0x08, (byte) 0x12, 0x1A, 0x0D, 0x0A
  };
  /** 设置发射功率 14byte. */
  public static final byte[] CMD_SET_POWER = new byte[] {
      (byte) 0xA5, 0x5A,
      0x00, 0x0E, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0D,
      0x0A,
  };
  /** 单次寻标签 命令; [5] = 帧类型 = 0x80 [6,7] timeOut, 时间到或寻到标签 回传应答帧. */
  public static final byte[] CMD_FAST_READ_EPC = new byte[] {
      (byte) 0xA5,
      (byte) 0x5A, 0x00, 0x0A, (byte) 0x80, 0x00, 0x64, (byte) 0xEE,
      0x0D, 0x0A
  };
  /** 快速读取 TID. */
  public static final byte[] CMD_FAST_READ_TID = new byte[] {
      (byte) 0xA5,
      (byte) 0x5A, 0x00, 0x0C, (byte) 0x8E, 0x00, 0x00, 0x00, 0x00, 0x00,
      0x0D, 0x0A
  };
  /** 快速读取 TID. */
  public static final byte[] CMD_SET_FAST_ID = new byte[] {
      (byte) 0xA5, (byte) 0x5A, (byte) 0x00, (byte) 0x0A, (byte) 0x5C, (byte) 0x01, (byte) 0x00,
      (byte) 0x57, (byte) 0x0D, (byte) 0x0A,
  };
  /** 获取FastId 是否开启. */
  public static final byte[] CMD_GET_FAST_ID = new byte[] {
      (byte) 0xA5, (byte) 0x5A, (byte) 0x00, (byte) 0x0A, (byte) 0x5E, (byte) 0x00, (byte) 0x00,
      (byte) 0x54, (byte) 0x0D, (byte) 0x0A,
  };
  private static final byte[] CMD_READ_LOT = new byte[] {
      (byte) 0xA5, (byte) 0x5A, 0x00,
      0x0A, (byte) 0x82, 0x00, 0x00, (byte) 0x00, 0x0D, 0x0A
  };

  public static final byte[] CMD_STOP_READ_LOT = new byte[] {
      (byte) 0xA5, (byte) 0x5A,
      0x00, 0x08, (byte) 0x8C, (byte) 0x84, 0x0D, 0x0A
  };

  /** EPC区大小 12byte. */
  private final int EPC_LEN = 12;
  /** 超高频 最大 读写功率 30. */
  public static final int MAX_POWER = 25;
  /** 超高频 最小 读写功率 5. */
  public static final int MIN_POWER = 5;

  private byte[] read_more = new byte[] {
      (byte) 0xA5, (byte) 0x5A, 0x00,
      0x0A, (byte) 0x82, 0x00, 0x00, (byte) 0x00, 0x0D, 0x0A
  };
  private byte[] stop_read_more = new byte[] {
      (byte) 0xA5, (byte) 0x5A,
      0x00, 0x08, (byte) 0x8C, (byte) 0x84, 0x0D, 0x0A
  };
  private byte[] read_more_time = new byte[] {
      (byte) 0xA5, (byte) 0x5A,
      0x00, 0x0D, (byte) 0x3C, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0D,
      0x0A
  };

  public static boolean isUhfCmd(byte[] cmd) {
    // 帧头 帧长度 帧类型 数据 校验码 帧尾
    // 2 字节 2 字节 1 字节 N 字节 1 字节 2 字节
    //帧头 0xA5、0x5A
    //帧尾 0x0D、0x0A
    if (cmd == null || cmd.length < 8) {
      return false;
    }

    return cmd[0] == (byte) 0xA5
        && cmd[1] == (byte) 0x5A
        && cmd[cmd.length - 2] == (byte) 0x0D
        && cmd[cmd.length - 1] == (byte) 0x0A;
  }

  /**
   * 向超高频写入数据，无过滤
   *
   * @param data 需要写入的 数据
   * @param mb memory bank，用户需要写入的数据的 bank号 0x01 表示 EPC， 0x02 表示 TID， 0x03
   * 表示USR，其他值为非法值
   * @param sa 写入起始位置, 单位为 字（字长: 2byte）
   * @param runTime 执行时间, 设为0只写1次
   * @return 写入成功返回true
   */
  public boolean writeUHFInTime(byte[] data, int mb, int sa, long runTime) {
    return writeUHFInTime(data, mb, sa, runTime, null, 0, 0);
  }

  public boolean writeUHFInTime(byte[] data, int mb, int sa, long runTime,
      byte[] filter, int mmb, int msa) {
    //mCoverEventData, UHFOperation.MB_USE, 0x00, 500, null, 1, 0x20)
    boolean reVal = false;
    try {
      if (null == data) {
        return false;
      }

      if (null == filter) {
        filter = new byte[0];
      }
      // 总长度 = 22其他信息 + 过滤数据 + 50写入数据
      int totalLen = 22 + filter.length + data.length;

      byte[] cmd_write = new byte[totalLen];

      // hardware.setGPIO(0, 2);
      // 帧头
      cmd_write[0] = (byte) 0xA5;
      cmd_write[1] = (byte) 0x5A;

      // 帧长度
      if (0xff < totalLen) {
        cmd_write[2] = (byte) (totalLen >> 8);
      } else {
        cmd_write[2] = (byte) 0x00;
      }
      cmd_write[3] = (byte) totalLen;

      // 帧类型
      cmd_write[4] = (byte) 0x86;
      // 密码ap
      cmd_write[5] = (byte) 0x00;
      cmd_write[6] = (byte) 0x00;
      cmd_write[7] = (byte) 0x00;
      cmd_write[8] = (byte) 0x00;
      // MMB 为启动过滤操作的 bank 号， 0x01 表示 EPC， 0x02 表示 TID， 0x03 表示USR，其他值为非法值
      cmd_write[9] = (byte) mmb;

      // MSA启动过滤操作的起始地址, 单位为 bit // 20
      int bitMsa = msa << 4; // 字 转 bit
      if (0xff < msa) {
        cmd_write[10] = (byte) (bitMsa >> 8);
      } else {
        cmd_write[10] = (byte) 0x00;
      }
      cmd_write[11] = (byte) bitMsa;
      // MDL过滤的数据长度, 单位为 bit, 若为0表示不进行过滤
      int bitLen = filter.length << 3;
      if (0xff < bitLen) {
        cmd_write[12] = (byte) (bitLen >> 8);
      } else {
        cmd_write[12] = (byte) 0x00;
      }
      cmd_write[13] = (byte) bitLen;

      // 要写的EPC号
      int filterLen = filter.length;
      for (int i = 0; i < filterLen; i++) {
        cmd_write[14 + i] = filter[i];
      }
      //System.arraycopy(filter, 0, cmd_write, 14, filter.length);

      // MB:memory bank，用户需要写入的数据的 bank号
      cmd_write[14 + filterLen] = (byte) mb;

      // 写入起始位置, 单位为 字 // 0x02
      if (0xff < sa) {
        cmd_write[15 + filterLen] = (byte) (sa >> 8);
      } else {
        cmd_write[15 + filterLen] = (byte) 0x00;
      }
      cmd_write[16 + filterLen] = (byte) sa;

      // 数据大小, 单位为 字
      int wordLen = data.length / 2;
      if (0xff < wordLen) {
        cmd_write[17 + filterLen] = (byte) (wordLen >> 8);
      } else {
        cmd_write[17 + filterLen] = (byte) 0x00;
      }
      cmd_write[18 + filterLen] = (byte) wordLen;

      // 写入的数据 12位
      int dataLen = data.length;
      for (int i = 0; i < dataLen; i++) {// data.length
        cmd_write[19 + filterLen + i] = data[i];
      }

      // 校验,将除了 帧头 和 帧尾 之外的所有数据进行异或
      int checkIndex = 19 + filterLen + dataLen; // 校验位
      cmd_write[checkIndex] = 0x00;
      for (int i = 2; i < checkIndex; i++) {
        cmd_write[checkIndex] ^= cmd_write[i];
      }

      // 帧尾
      cmd_write[checkIndex + 1] = 0x0D;
      cmd_write[checkIndex + 2] = 0x0A;
      byte[] buf = new byte[10];

      long time = System.currentTimeMillis();
      do {
        LogUtils.i("wuh", "cmd_write:" + ArrayUtils.bytes2HexString(cmd_write));
        int len = runCmd(cmd_write, buf);
        LogUtils.tag(TAG).d("writeUHFInTime(" + mb + ") len=10:" + (len));
        if (buf[4] == (byte) 0x87 && buf[5] == 0x01) {
          reVal = true;
          LogUtils.d("wuh", "writeUHFInTime()成功 : buf[4] == ");
          break;
        } else {
          LogUtils.d("wuh", "writeUHFInTime()失败 : buf[4] == " + buf[4]
              + " buf[5] == " + buf[5]);
        }
      } while ((System.currentTimeMillis() - time) < runTime);
    } catch (Exception e) {
      LogUtils.tag(TAG).e("writeUHFInTime e:" + e);
    }
    return reVal;
  }

  /**
   * 获取读 超高频 指令
   *
   * @param mb 读取数据 的 区, 1 表示 EPC， 2 表示 TID， 3 表示user
   * @param sa 读取 数据的 起始地址, 单位 字（字长: 2byte）
   * @param readLen 读取数据, 长度应为 偶数
   * @param filter 过滤数据
   * @param mmb 过滤的区
   * @param msa 过滤的起始地址, 单位 字
   */
  public static byte[] getReadCmd(int mb, int sa, int readLen, byte[] filter, int mmb, int msa) {

    if (null == filter) {
      filter = new byte[0];
    }

    int totalLen = 22 + filter.length;

    byte[] cmdRead = new byte[totalLen];

    // 帧头
    cmdRead[0] = (byte) 0xA5;
    cmdRead[1] = (byte) 0x5A;
    // 帧长度
    if (0xff < totalLen) {
      cmdRead[2] = (byte) (totalLen >> 8);
    } else {
      cmdRead[2] = (byte) 0x00;
    }
    cmdRead[3] = (byte) totalLen;
    // 帧类型
    cmdRead[4] = (byte) 0x84;
    // 密码
    cmdRead[5] = (byte) 0x00;
    cmdRead[6] = (byte) 0x00;
    cmdRead[7] = (byte) 0x00;
    cmdRead[8] = (byte) 0x00;

    // MMB 为启动过滤操作的 bank 号， 0x01 表示 EPC， 0x02 表示 TID， 0x03
    // 表示USR，其他值为非法值；//01
    cmdRead[9] = (byte) mmb;

    // MSA启动过滤操作的起始地址, 单位为 bit // 20
    int bitMsa = msa << 4;
    if (0xff < bitMsa) {
      cmdRead[10] = (byte) (bitMsa >> 8);
    } else {
      cmdRead[10] = (byte) 0x00;
    }
    cmdRead[11] = (byte) bitMsa;

    // MDL过滤的数据长度, 单位为 bit // 60
    int bitLen = filter.length << 3;
    if (0xff < bitLen) {
      cmdRead[12] = (byte) (bitLen >> 8);
    } else {
      cmdRead[12] = (byte) 0x00;
    }
    cmdRead[13] = (byte) bitLen;

    // 过滤数据
    int filterLen = filter.length;
    for (int i = 0; i < filterLen; i++) {
      cmdRead[14 + i] = filter[i];
    }
    //System.arraycopy(filter, 0, cmd_read, 14, epcLen);

    // MB // 0x02
    cmdRead[filterLen + 14] = (byte) mb;

    // 写入起始位置
    cmdRead[15 + filterLen] = (byte) (sa >> 8);
    cmdRead[16 + filterLen] = (byte) sa;

    // 读取数据的长度,单位 字
    int dataLenWord = (readLen + 1) >> 1; // 把 字节长度 转为 字长度
    if (0xff < dataLenWord) {
      cmdRead[17 + filterLen] = (byte) (dataLenWord >> 8);
    } else {
      cmdRead[17 + filterLen] = (byte) 0x00;
    }
    cmdRead[18 + filterLen] = (byte) dataLenWord;

    cmdRead[filterLen + 19] = (byte) 0x00; // 校验位
    for (int i = 2; i < totalLen - 3; i++) {
      cmdRead[filterLen + 19] ^= cmdRead[i];
    }
    // 帧尾
    cmdRead[filterLen + 20] = 0x0D;
    cmdRead[filterLen + 21] = 0x0A;
    return cmdRead;
  }

  public static byte[] getReadCmd(int mb, int sa, int readLen, int psw, byte[] filter, int mmb,
      int msa) {

    if (null == filter) {
      filter = new byte[0];
    }

    int totalLen = 22 + filter.length;

    byte[] cmdRead = new byte[totalLen];

    // 帧头
    cmdRead[0] = (byte) 0xA5;
    cmdRead[1] = (byte) 0x5A;
    // 帧长度
    if (0xff < totalLen) {
      cmdRead[2] = (byte) (totalLen >> 8);
    } else {
      cmdRead[2] = (byte) 0x00;
    }
    cmdRead[3] = (byte) totalLen;
    // 帧类型
    cmdRead[4] = (byte) 0x84;

    // 密码
    byte[] pwd = ArrayUtils.long2Bytes(psw, 4);
    if (pwd != null && pwd.length == 4) {
      cmdRead[5] = pwd[0];
      cmdRead[6] = pwd[1];
      cmdRead[7] = pwd[2];
      cmdRead[8] = pwd[3];
    } else {
      cmdRead[5] = (byte) 0x00;
      cmdRead[6] = (byte) 0x00;
      cmdRead[7] = (byte) 0x00;
      cmdRead[8] = (byte) 0x00;
    }

    // MMB 为启动过滤操作的 bank 号， 0x01 表示 EPC， 0x02 表示 TID， 0x03
    // 表示USR，其他值为非法值；//01
    cmdRead[9] = (byte) mmb;

    // MSA启动过滤操作的起始地址, 单位为 bit // 20
    int bitMsa = msa << 4;
    if (0xff < bitMsa) {
      cmdRead[10] = (byte) (bitMsa >> 8);
    } else {
      cmdRead[10] = (byte) 0x00;
    }
    cmdRead[11] = (byte) bitMsa;

    // MDL过滤的数据长度, 单位为 bit // 60
    int bitLen = filter.length << 3;
    if (0xff < bitLen) {
      cmdRead[12] = (byte) (bitLen >> 8);
    } else {
      cmdRead[12] = (byte) 0x00;
    }
    cmdRead[13] = (byte) bitLen;

    // 要读的EPC号
    int epcLen = filter.length;
    for (int i = 0; i < epcLen; i++) {
      cmdRead[14 + i] = filter[i];
    }
    //System.arraycopy(filter, 0, cmd_read, 14, epcLen);

    // MB // 0x02
    cmdRead[epcLen + 14] = (byte) mb;

    // 写入起始位置
    cmdRead[15 + epcLen] = (byte) (sa >> 8);
    cmdRead[16 + epcLen] = (byte) sa;

    // 读取数据的长度,单位 字
    int dataLenWord = (readLen + 1) >> 1; // 把 字节长度 转为 字长度
    if (0xff < dataLenWord) {
      cmdRead[17 + epcLen] = (byte) (dataLenWord >> 8);
    } else {
      cmdRead[17 + epcLen] = (byte) 0x00;
    }
    cmdRead[18 + epcLen] = (byte) dataLenWord;

    cmdRead[epcLen + 19] = (byte) 0x00; // 校验位
    for (int i = 2; i < totalLen - 3; i++) {
      cmdRead[epcLen + 19] ^= cmdRead[i];
    }
    // 帧尾
    cmdRead[epcLen + 20] = 0x0D;
    cmdRead[epcLen + 21] = 0x0A;
    return cmdRead;
  }

  public static byte[] getReadCmd(int mb, int sa, int readLen) {
    return getReadCmd(mb, sa, readLen, null, 0, 0);
  }

  /**
   * 获取写 超高频 指令
   *
   * @param mb 读取数据 的 区, 1 表示 EPC， 2 表示 TID， 3 表示user
   * @param sa 读取 数据的 起始地址, 单位 字（字长: 2byte）
   * @param data 待写入数据, 长度应为 偶数
   * @param filter 过滤数据
   * @param mmb 过滤的区
   * @param msa 过滤的起始地址, 单位 字
   */
  public static byte[] getWriteCmd(int mb, int sa, byte[] data, byte[] filter, int mmb, int msa) {
    if (null == data) {
      return null;
    }

    if (null == filter) {
      filter = new byte[0];
    }
    // 总长度 = 22其他信息 + 过滤数据 + 50写入数据
    int totalLen = 22 + filter.length + data.length;

    byte[] cmd_write = new byte[totalLen];

    // 帧头
    cmd_write[0] = (byte) 0xA5;
    cmd_write[1] = (byte) 0x5A;

    // 帧长度
    //        if (0xff < totalLen) {
    //            cmd_write[2] = (byte) (totalLen >> 8);
    //        } else {
    //            cmd_write[2] = (byte) 0x00;
    //        }
    cmd_write[2] = (byte) ((totalLen >> 8) & 0xFF);
    cmd_write[3] = (byte) totalLen;

    // 帧类型
    cmd_write[4] = (byte) 0x86;
    // 密码ap
    cmd_write[5] = (byte) 0x00;
    cmd_write[6] = (byte) 0x00;
    cmd_write[7] = (byte) 0x00;
    cmd_write[8] = (byte) 0x00;
    // MMB 为启动过滤操作的 bank 号， 0x01 表示 EPC， 0x02 表示 TID， 0x03 表示USR，其他值为非法值
    cmd_write[9] = (byte) mmb;

    // MSA启动过滤操作的起始地址, 单位为 bit // 20
    int bitMsa = msa << 4; // 字 转 bit
    if (0xff < msa) {
      cmd_write[10] = (byte) (bitMsa >> 8);
    } else {
      cmd_write[10] = (byte) 0x00;
    }
    cmd_write[10] = (byte) ((bitMsa >> 8) & 0xFF);
    cmd_write[11] = (byte) bitMsa;
    // MDL过滤的数据长度, 单位为 bit, 若为0表示不进行过滤
    int bitLen = filter.length << 3;
    if (0xff < bitLen) {
      cmd_write[12] = (byte) (bitLen >> 8);
    } else {
      cmd_write[12] = (byte) 0x00;
    }
    cmd_write[13] = (byte) bitLen;

    // 要写的EPC号
    int filterLen = filter.length;
    for (int i = 0; i < filterLen; i++) {
      cmd_write[14 + i] = filter[i];
    }
    //System.arraycopy(filter, 0, cmd_write, 14, filter.length);

    // MB:memory bank，用户需要写入的数据的 bank号
    cmd_write[14 + filterLen] = (byte) mb;

    // 写入起始位置, 单位为 字 // 0x02
    if (0xFF < sa) {
      cmd_write[15 + filterLen] = (byte) (sa >> 8);
    } else {
      cmd_write[15 + filterLen] = (byte) 0x00;
    }
    cmd_write[16 + filterLen] = (byte) sa;

    // 数据大小, 单位为 字
    int wordLen = data.length / 2;
    if (0xff < wordLen) {
      cmd_write[17 + filterLen] = (byte) (wordLen >> 8);
    } else {
      cmd_write[17 + filterLen] = (byte) 0x00;
    }
    cmd_write[18 + filterLen] = (byte) wordLen;

    // 写入的数据 12位
    int dataLen = data.length;
    for (int i = 0; i < dataLen; i++) { // data.length
      cmd_write[19 + filterLen + i] = data[i];
    }

    // 校验,将除了 帧头 和 帧尾 之外的所有数据进行异或
    int checkIndex = 19 + filterLen + dataLen; // 校验位
    cmd_write[checkIndex] = 0x00;
    for (int i = 2; i < checkIndex; i++) {
      cmd_write[checkIndex] ^= cmd_write[i];
    }

    // 帧尾
    cmd_write[checkIndex + 1] = 0x0D;
    cmd_write[checkIndex + 2] = 0x0A;
    return cmd_write;
  }

  public static byte[] getWriteCmd(int mb, int sa, byte[] data) {
    return getWriteCmd(mb, sa, data, null, 0, 0);
  }

  public static byte[] getPowerCmd() {
    return CMD_GET_POWER;
  }

  /**
   * @param readPower 读 功率.范围:5-30
   * @param writePower 写 功率.范围:5-30
   * @param id 天线号
   * @param isSave 本次设置是否保存 , 0 = 不保存
   * @param isClosed 是否闭环, 非0 = 闭环, 默认0
   */
  public static byte[] getSetPowerCmd(int readPower, int writePower, int id, boolean isSave,
      boolean isClosed) {

    if (readPower < 0 || writePower < 0 || id < 0) {
      return null;
    }

    int save;
    if (isSave) {
      save = 1;
    } else {
      save = 0;
    }

    int closed;
    if (isClosed) {
      closed = 1;
    } else {
      closed = 0;
    }

    CMD_SET_POWER[5] = (byte) ((save << 1) | closed);

    // 设置 天线号
    CMD_SET_POWER[6] = (byte) id;

    // 限制 读功率范围
    if (MAX_POWER < readPower) {
      readPower = MAX_POWER;
    } else if (readPower < MIN_POWER) {
      readPower = MIN_POWER;
    }
    // 读功率,2字节
    readPower *= 100; // 功率要先乘以100,协议如此规定
    CMD_SET_POWER[7] = (byte) (readPower >> 8);
    CMD_SET_POWER[8] = (byte) readPower;

    // 限制 写功率范围
    if (MAX_POWER < writePower) {
      writePower = MAX_POWER;
    } else if (writePower < MIN_POWER) {
      writePower = MIN_POWER;
    }
    // 写功率,2字节
    writePower *= 100; // 功率要先乘以100,协议如此规定
    CMD_SET_POWER[9] = (byte) (writePower >> 8);
    CMD_SET_POWER[10] = (byte) writePower;

    setCheckByte(CMD_SET_POWER);
    return CMD_SET_POWER;
  }

  /**
   * 获取EPC区后12字节数据,无法进行过滤.epc区共16byte,前4个byte不可改.
   */
  public static byte[] getFastReadEpcCmd(int timeout) {
    CMD_FAST_READ_EPC[6] = (byte) (timeout & 0xFF); // 低位
    timeout >>= 8;
    CMD_FAST_READ_EPC[5] = (byte) (timeout & 0xFF); // 高位
    setCheckByte(CMD_FAST_READ_EPC);
    return CMD_FAST_READ_EPC;
  }

  public static byte[] getSetFastIdCmd(boolean on) {
    if (on) {
      CMD_SET_FAST_ID[5] = 1;
    } else {
      CMD_SET_FAST_ID[5] = 0;
    }
    setCheckByte(CMD_SET_FAST_ID);
    return CMD_SET_FAST_ID;
  }

  /**
   * 快速获取TID区数据, 无法进行过滤, TID区总大小 : 12字节； 获取唯一TID参数选择(0x06, 6)
   *
   * @param sa 获取数据 起始地址
   * @param len 获取数据长度
   */
  public static byte[] getFastReadTidCmd(int sa, int len) {
    // 起始地址, 单位为 字
    int wordLen = sa >> 1; // 字节 转 字
    CMD_FAST_READ_TID[5] = (byte) (wordLen >> 8);
    CMD_FAST_READ_TID[6] = (byte) wordLen;

    // 获取数据长度, 单位为 字
    wordLen = (len + 1) >> 1; // 字节 转 字
    if (0xFF < wordLen) {
      CMD_FAST_READ_TID[7] = (byte) (wordLen >> 8);
    } else {
      CMD_FAST_READ_TID[7] = (byte) 0x00;
    }
    CMD_FAST_READ_TID[8] = (byte) wordLen;

    setCheckByte(CMD_FAST_READ_TID);

    return CMD_FAST_READ_TID;
  }

  public static byte[] getReadLotCmd(int times) {
    if (times < 0) {
      times = 0;
    } else if (0xFFFF < times) {
      times = 0xFFFF;
    }
    CMD_READ_LOT[5] = (byte) (times >> 8);
    CMD_READ_LOT[6] = (byte) times;
    setCheckByte(CMD_READ_LOT);
    return CMD_READ_LOT;
  }

  public static byte[] getSetPwdCmd(int pwd, byte[] filter, int mmb, int msa) {

    if (null == filter) {
      filter = new byte[0];
    }

    int totalLen = 8 + 12 + filter.length;

    byte[] cmdSetPwd = new byte[totalLen];

    // 帧头
    cmdSetPwd[0] = (byte) 0xA5;
    cmdSetPwd[1] = (byte) 0x5A;
    // 帧长度
    cmdSetPwd[2] = (byte) (totalLen >> 8);
    cmdSetPwd[3] = (byte) totalLen;
    // 帧类型
    cmdSetPwd[4] = (byte) 0x88;
    // 密码
    byte[] pwdBuff = ArrayUtils.long2Bytes(pwd, 4);
    if (pwdBuff != null && pwdBuff.length == 4) {
      cmdSetPwd[5] = pwdBuff[0];
      cmdSetPwd[6] = pwdBuff[1];
      cmdSetPwd[7] = pwdBuff[2];
      cmdSetPwd[8] = pwdBuff[3];
    } else {
      cmdSetPwd[5] = (byte) 0x00;
      cmdSetPwd[6] = (byte) 0x00;
      cmdSetPwd[7] = (byte) 0x00;
      cmdSetPwd[8] = (byte) 0x00;
    }

    // MMB 为启动过滤操作的 bank 号， 0x01 表示 EPC， 0x02 表示 TID， 0x03
    // 表示USR，其他值为非法值；//01
    cmdSetPwd[9] = (byte) mmb;

    // MSA启动过滤操作的起始地址, 单位为 bit // 20
    int bitMsa = msa << 4;
    if (0xff < bitMsa) {
      cmdSetPwd[10] = (byte) (bitMsa >> 8);
    } else {
      cmdSetPwd[10] = (byte) 0x00;
    }
    cmdSetPwd[11] = (byte) bitMsa;

    // MDL过滤的数据长度, 单位为 bit // 60
    int bitLen = filter.length << 3;
    if (0xff < bitLen) {
      cmdSetPwd[12] = (byte) (bitLen >> 8);
    } else {
      cmdSetPwd[12] = (byte) 0x00;
    }
    cmdSetPwd[13] = (byte) bitLen;

    // 过滤数据
    for (int i = 0; i < filter.length; i++) {
      cmdSetPwd[14 + i] = filter[i];
    }
    // ld 3byte
    cmdSetPwd[totalLen - 6] = 0;
    cmdSetPwd[totalLen - 5] = 0;
    cmdSetPwd[totalLen - 4] = 0;
    // 帧尾
    cmdSetPwd[totalLen - 2] = 0x0D;
    cmdSetPwd[totalLen - 1] = 0x0A;
    setCheckByte(cmdSetPwd);
    return cmdSetPwd;
  }

  public static byte[] parseData(byte[] cmd) {
    if (!isUhfCmd(cmd)) {
      return null;
    }

    byte[] reVal = null;
    int cmdType = cmd[4] & 0xFF;
    switch (cmdType) {
      case RECEIVE_TYPE_GET_DEVICE_VERSION:
        reVal = new byte[] { cmd[5], cmd[6], cmd[7], };
        break;
      case RECEIVE_TYPE_GET_DEVICE_ID:
        reVal = new byte[] { cmd[5], cmd[6], cmd[7], cmd[8], };
        int id = 0;
        for (int i = 5; i < 9; i++) {
          id <<= 8;
          id |= (cmd[i] & 0xFF);
        }
        LogUtils.d("id:%s=%s", id, Integer.toHexString(id));
        break;
      case RECEIVE_TYPE_FAST_EPC:
        if (cmd[3] == 0x25) {
          reVal = Arrays.copyOfRange(cmd, 7, 31);
        } else {
          reVal = Arrays.copyOfRange(cmd, 7, 19);
        }
        break;
      case RECEIVE_TYPE_READ_LOT:
        if (cmd[3] == 0x25) {
          reVal = Arrays.copyOfRange(cmd, 7, 31);
        } else if (cmd[3] == 0x08) {
          // 连续寻卡 停止
          reVal = new byte[] { 1 };
        } else {
          reVal = Arrays.copyOfRange(cmd, 7, 19);
        }
        break;
      case RECEIVE_TYPE_FAST_TID:
      case RECEIVE_TYPE_READ:
        if (cmd[5] == 0x01 && cmd[6] == 0x00) {
          int len = cmd[7] & 0xFF;
          len <<= 8;
          len |= cmd[8] & 0xFF;
          len <<= 1;
          reVal = Arrays.copyOfRange(cmd, 9, 9 + len);
        }
        break;
      case RECEIVE_TYPE_GET_POWER:
        int h;
        int l;
        reVal = new byte[4];
        // 读 功率
        h = (cmd[7] & 0xFF) << 8; // 高8位
        l = cmd[8] & 0xFF; // 低8位
        reVal[0] = (byte) ((h | l) / 100); // 合并2字节 后 除以100即为 功率值
        // 写 功率
        h = (cmd[9] & 0xFF) << 8;
        l = cmd[10] & 0xFF;
        reVal[1] = (byte) ((h | l) / 100);

        // 天线号
        reVal[2] = cmd[6];
        // 闭环：1， 开环：0
        reVal[3] = cmd[5];
        break;
      case RECEIVE_TYPE_SET_POWER:
      case RECEIVE_TYPE_SET_FAST_ID:
        // 1:成功， 0：失败
        reVal = new byte[] { cmd[5] };
        break;
      case RECEIVE_TYPE_GET_FAST_ID:
        if (cmd[5] == 1) {
          // 1:开启， 0：关闭
          reVal = new byte[] { cmd[6] };
        }
        break;
      case RECEIVE_TYPE_WRITE:
        if (cmd[5] == 0x01 && cmd[6] == 0x00) {
          reVal = new byte[] {};
        } else {
          reVal = new byte[] { cmd[6] };
        }
        break;
    }

    return reVal;
  }

  private static boolean setCheckByte(byte[] cmd) {
    if (!isUhfCmd(cmd)) {
      return false;
    }

    int crc = 0;
    for (int i = 2; i < cmd.length - 3; i++) {
      crc ^= cmd[i];
    }
    cmd[cmd.length - 3] = (byte) crc;

    return true;
  }

  private int runCmd(byte[] cmdFastReadEpc, byte[] buf) {
    return 0;
  }

  public void readEpcs(int readTimes) {

    // 寻标签次数
    read_more[5] = (byte) ((readTimes & 0xFF00) >> 8);//
    read_more[6] = (byte) (readTimes & 0xFF);

    // 检验位
    int check = 0;
    for (int i = 2; i < 7; i++) {
      check ^= read_more[i];
    }
    read_more[7] = (byte) check;

    // 发送命令
    int write = 1;
    if (write < 1) {
      LogUtils.tag(TAG).d("write() failed len: " + write);
    }

    byte buf[] = new byte[32];
    int len = 0;
    int count = 0;
    while (count++ < readTimes) {
      LogUtils.tag(TAG).d("readTiims:" + count);
      LogUtils.tag(TAG).d("readEpcs len: " + len);
    }
  }

  /**
   * @param rwPower 0:读功率， 1：写功率
   * @return 0<: 命令执行成功, 0 : 串口未检测到数据, - 1 : 执行命令失败, - 2 获取串口数据的长度小于1, - 3 : fd < 1
   */
  public int getPower(int[] rwPower) {

    int reVal = -1;

    if (null == rwPower || rwPower.length < 2) {
      return reVal;
    }

    byte[] buff = new byte[20];
    LogUtils.d("SettingPowerActivity",
        "CMD_GET_POWER:" + ArrayUtils.bytes2HexString(CMD_SET_POWER));
    reVal = runCmd(CMD_GET_POWER, buff);
    LogUtils.tag(TAG).d("getPower() len=12:" + (reVal));
    LogUtils.d("SettingPowerActivity", "buff:" + ArrayUtils.bytes2HexString(buff));
    if (14 <= reVal && buff[4] == (byte) 0x13) {
      int h = 0;
      int l = 0;
      // 读 功率
      h = (buff[7] & 0xFF) << 8; // 高8位
      l = buff[8] & 0xFF; // 低8位
      rwPower[0] = (h | l) / 100; // 合并2字节 后 除以100即为 功率值

      // 写 功率
      h = (buff[9] & 0xFF) << 8;
      l = buff[10] & 0xFF;
      rwPower[1] = (h | l) / 100;

      LogUtils.tag(TAG).d("readPower:" + rwPower[0] + " writePower:"
          + rwPower[1]);
    } else {
      reVal = -4;
    }

    return reVal;
  }

  /**
   * @param readPower 读 功率.范围:5-30
   * @param writePower 写 功率.范围:5-30
   * @return 设置成功 返回 大于0的int
   */
  public int setPower(int readPower, int writePower) {
    return setPower(readPower, writePower, 1, 1, 0);
  }

  /**
   * @param readPower 读 功率.范围:5-30
   * @param writePower 写 功率.范围:5-30
   * @param id 天线号
   * @param save 本次设置是否保存 , 0 = 不保存
   * @param open 是否闭环, 非0 = 闭环, 默认0
   * @return 设置成功 返回 大于0的int
   */
  public int setPower(int readPower, int writePower, int id, int save,
      int open) {

    if (readPower < 0 || writePower < 0 || id < 0) {
      return -4;
    }

    if (0 != save) {
      save = 1;
    }
    if (0 != open) {
      open = 1;
    }

    CMD_SET_POWER[5] = (byte) ((save << 1) + open);

    // 设置 天线号
    CMD_SET_POWER[6] = (byte) id;

    // 限制 读功率范围
    if (MAX_POWER < readPower) {
      readPower = MAX_POWER;
    } else if (readPower < MIN_POWER) {
      readPower = MIN_POWER;
    }
    // 读功率,2字节
    readPower *= 100; // 功率要先乘以100,协议如此规定
    CMD_SET_POWER[7] = (byte) (readPower >> 8);
    CMD_SET_POWER[8] = (byte) readPower;

    // 限制 写功率范围
    if (MAX_POWER < writePower) {
      writePower = MAX_POWER;
    } else if (writePower < MIN_POWER) {
      writePower = MIN_POWER;
    }
    // 写功率,2字节
    writePower *= 100; // 功率要先乘以100,协议如此规定
    CMD_SET_POWER[9] = (byte) (writePower >> 8);
    CMD_SET_POWER[10] = (byte) writePower;

    // 校验位
    CMD_SET_POWER[11] = (byte) 0x00;
    for (int i = 2; i < 11; i++) {
      CMD_SET_POWER[11] ^= CMD_SET_POWER[i];
    }

    byte[] buff = new byte[16];
    int reVal = runCmd(CMD_SET_POWER, buff);

    if ((0 < reVal) && (buff[4] == 0x11) && (buff[5] == (byte) 0x01)) {
    } else {
      reVal = -4;
    }
    LogUtils.tag(TAG).d("17：buf[4] == " + buff[4] + ",1：buf[5] == " + buff[5]);
    return reVal;
  }

  public static void main(String[] args) {
    byte[] setPwdCmd = getSetPwdCmd(0x760039AD, null, 0, 0);
    System.out.println(ArrayUtils.bytes2HexString(setPwdCmd));
  }
}