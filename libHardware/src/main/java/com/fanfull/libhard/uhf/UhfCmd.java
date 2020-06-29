package com.fanfull.libhard.uhf;

import android.util.Log;

import com.apkfuns.logutils.LogUtils;
import com.finger.LogsUtil;

import org.orsoul.baselib.util.ArrayUtils;

/**
 *
 */
public class UhfCmd {
    private static final String TAG = "UHFOperation";
    /**
     * epc区
     */
    public static final int MB_EPC = 1;
    /**
     * tid区
     */
    public static final int MB_TID = 2;
    /**
     * use区
     */
    public static final int MB_USE = 3;
    /**
     * 用于存储epc区数据
     */
    public static final byte[] sEPC = new byte[12];

    private final static String CHOOSED_SERIAL = "/dev/s3c2410_serial2";
    private final static int CHOOSED_BUAD = 115200;

    /**
     * 获取当前设备发射功率
     */
    private final static byte[] CMD_GET_POWER = new byte[]{(byte) 0xA5,
            (byte) 0x5A, 0x00, 0x08, (byte) 0x12, 0x1A, 0x0D, 0x0A};
    /**
     * 设置发射功率 14byte
     */
    private final static byte[] CMD_SET_POWER = new byte[]{(byte) 0xA5, 0x5A,
            0x00, 0x0E, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0D,
            0x0A,};
    /**
     * 单次寻标签 命令; [5] = 帧类型 = 0x80 [6,7] timeOut, 时间到或寻到标签 回传应答帧
     */
    public static final byte[] CMD_FAST_READ_EPC = new byte[]{(byte) 0xA5,
            (byte) 0x5A, 0x00, 0x0A, (byte) 0x80, 0x00, 0x64, (byte) 0xEE,
            0x0D, 0x0A};
    /**
     * 快速读取 TID
     */
    private final static byte[] CMD_FAST_READ_TID = new byte[]{(byte) 0xA5,
            (byte) 0x5A, 0x00, 0x0C, (byte) 0x8E, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x0D, 0x0A};
    /**
     * EPC区大小 12byte
     */
    private final int EPC_LEN = 12;
    /**
     * 超高频 最大 读写功率 30
     */
    public static final int MAX_POWER = 25;
    /**
     * 超高频 最小 读写功率 5
     */
    public static final int MIN_POWER = 5;
    private byte[] read_more = new byte[]{(byte) 0xA5, (byte) 0x5A, 0x00,
            0x0A, (byte) 0x82, 0x00, 0x00, (byte) 0x00, 0x0D, 0x0A};
    private byte[] stop_read_more = new byte[]{(byte) 0xA5, (byte) 0x5A,
            0x00, 0x08, (byte) 0x8C, (byte) 0x84, 0x0D, 0x0A};
    private byte[] read_more_time = new byte[]{(byte) 0xA5, (byte) 0x5A,
            0x00, 0x0D, (byte) 0x3C, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0D,
            0x0A};
    private byte[] buf = new byte[100];
    private int len;
    private int count = 0;// 记录读卡数

    // TODO

    private UhfCmd() {
    }

    /**
     * 读超高频，无过滤
     * @param mb      读取数据 的 区, 1 表示 EPC， 2 表示 TID， 3 表示user
     * @param sa      读取 数据的 起始地址, 单位 字（字长: 2byte）
     * @param dataBuf 存放读取数据, 长度应为 偶数
     * @param runTime 执行时间, 设为0只读1次
     * @return 读到的数据，读取失败返回null
     */
    public boolean readUHFInTime(int mb, int sa, byte[] dataBuf, long runTime) {
        return readUHFInTime(mb, sa, dataBuf, runTime, null, 0, 0);
    }

    /**
     * 读超高频，无过滤
     * @param mb      读取数据 的 区, 1 表示 EPC， 2 表示 TID， 3 表示user
     * @param sa      读取 数据的 起始地址, 单位 字（字长: 2byte）
     * @param dataBuf 存放读取数据, 长度应为 偶数
     * @param runTime 执行时间, 设为0只读1次
     * @param filter  过滤数据
     * @param mmb     过滤的区
     * @param msa     过滤的起始地址, 单位 字
     * @return 读到的数据，读取失败返回null
     */
    public boolean readUHFInTime(int mb, int sa, byte[] dataBuf, long runTime, byte[] filter, int mmb, int msa) {

        if (null == filter) {
            filter = new byte[0];
        }

        int totalLen = 22 + filter.length;

        byte[] cmd_read = new byte[totalLen];

        // 帧头
        cmd_read[0] = (byte) 0xA5;
        cmd_read[1] = (byte) 0x5A;
        // 帧长度
        if (0xff < totalLen) {
            cmd_read[2] = (byte) (totalLen >> 8);
        } else {
            cmd_read[2] = (byte) 0x00;
        }
        cmd_read[3] = (byte) totalLen;
        // 帧类型
        cmd_read[4] = (byte) 0x84;
        // 密码
        cmd_read[5] = (byte) 0x00;
        cmd_read[6] = (byte) 0x00;
        cmd_read[7] = (byte) 0x00;
        cmd_read[8] = (byte) 0x00;

        // MMB 为启动过滤操作的 bank 号， 0x01 表示 EPC， 0x02 表示 TID， 0x03
        // 表示USR，其他值为非法值；//01
        cmd_read[9] = (byte) mmb;

        // MSA启动过滤操作的起始地址, 单位为 bit // 20
        int bitMsa = msa << 4;
        if (0xff < bitMsa) {
            cmd_read[10] = (byte) (bitMsa >> 8);
        } else {
            cmd_read[10] = (byte) 0x00;
        }
        cmd_read[11] = (byte) bitMsa;

        // MDL过滤的数据长度, 单位为 bit // 60
        int bitLen = filter.length << 3;
        if (0xff < bitLen) {
            cmd_read[12] = (byte) (bitLen >> 8);
        } else {
            cmd_read[12] = (byte) 0x00;
        }
        cmd_read[13] = (byte) bitLen;

        // 要读的EPC号
        int epcLen = filter.length;
        for (int i = 0; i < epcLen; i++) {
            cmd_read[14 + i] = filter[i];
        }
        //System.arraycopy(filter, 0, cmd_read, 14, epcLen);

        // MB // 0x02
        cmd_read[epcLen + 14] = (byte) mb;

        // 写入起始位置
        if (0xff < sa) {
            cmd_read[15 + epcLen] = (byte) (sa >> 8);
        } else {
            cmd_read[15 + epcLen] = (byte) 0x00;
        }
        cmd_read[16 + epcLen] = (byte) sa;

        // 读取数据的长度,单位 字
        int dataLenWord = dataBuf.length >> 1;// 把 字节长度 转为 字长度
        if (0xff < dataLenWord) {
            cmd_read[17 + epcLen] = (byte) (dataLenWord >> 8);
        } else {
            cmd_read[17 + epcLen] = (byte) 0x00;
        }
        cmd_read[18 + epcLen] = (byte) dataLenWord;

        cmd_read[epcLen + 19] = (byte) 0x00; // 校验位
        for (int i = 2; i < totalLen - 3; i++) {
            cmd_read[epcLen + 19] ^= cmd_read[i];
        }
        // 帧尾
        cmd_read[epcLen + 20] = 0x0D;
        cmd_read[epcLen + 21] = 0x0A;

        boolean reVal = false;
        byte[] buf = new byte[dataBuf.length + 12];// 12byte 存储其他回复信息

        long time = System.currentTimeMillis();
        do {
            int len = runCmd(cmd_read, buf);
            LogsUtil.d("ruh", "readUHFInTime(" + mb + ") len=12:" + (len - dataBuf.length));
            if (buf[4] == (byte) 0x85 && buf[5] == 0x01) {
                // reVal = new byte[readDataLen];
                // for (int i = 0; i < reVal.length; i++) {
                // reVal[i] = buf[9 + i];
                // }
                System.arraycopy(buf, 9, dataBuf, 0, dataBuf.length);
                reVal = true;
                break;
            }
        } while ((System.currentTimeMillis() - time) < runTime);
        LogsUtil.d("ruh", "readUHFInTime dataBuf=" + ArrayUtils.bytes2HexString(dataBuf));

        return reVal;
    }


    /**
     * 向超高频写入数据，无过滤
     * @param data    需要写入的 数据
     * @param mb      memory bank，用户需要写入的数据的 bank号 0x01 表示 EPC， 0x02 表示 TID， 0x03
     *                表示USR，其他值为非法值
     * @param sa      写入起始位置, 单位为 字（字长: 2byte）
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
                LogsUtil.i("wuh", "cmd_write:" + ArrayUtils.bytes2HexString(cmd_write));
                int len = runCmd(cmd_write, buf);
                LogUtils.tag(TAG).d("writeUHFInTime(" + mb + ") len=10:" + (len));
                if (buf[4] == (byte) 0x87 && buf[5] == 0x01) {
                    reVal = true;
                    LogsUtil.d("wuh", "writeUHFInTime()成功 : buf[4] == ");
                    break;
                } else {
                    LogsUtil.d("wuh", "writeUHFInTime()失败 : buf[4] == " + buf[4]
                            + " buf[5] == " + buf[5]);
                }
            } while ((System.currentTimeMillis() - time) < runTime);

        } catch (Exception e) {
            LogUtils.tag(TAG).e("writeUHFInTime e:" + e);
        }
        return reVal;
    }

    public byte[] getWriteCmd(byte[] data, int mb, int sa, byte[] filter, int mmb, int msa) {
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
        return cmd_write;
    }

    /**
     * 获取EPC区后12字节数据,无法进行过滤.epc区共16byte,前4个byte不可改
     * @param epcBuf 存放epc区数据的数组,长度小于或等于12
     * @return 读取成功返回true
     */
    public boolean fastReadEpc(byte[] epcBuf) {
        boolean reVal = false;
        try {
            if (null == epcBuf || epcBuf.length == 0) {
                return reVal;
            }
            byte[] buf = new byte[25];
            int len = runCmd(CMD_FAST_READ_EPC, buf);

            LogUtils.tag(TAG).d("fastReadEpc() assum 25:" + len);
            if (25 == len && buf[4] == (byte) 0x81) {
                for (int i = 0; i < EPC_LEN && i < epcBuf.length; i++) {
                    epcBuf[i] = buf[i + 7];
                }
                reVal = true;
            } else {
            }
        } catch (Exception e) {
            LogUtils.tag(TAG).e("fastReadEpc e:" + e);
        }
        return reVal;
    }

    private int runCmd(byte[] cmdFastReadEpc, byte[] buf) {
        return 0;
    }

    /**
     * 快速获取TID区数据, 无法进行过滤, TID区总大小 : 24字节； 获取唯一TID参数选择(0x06, byte[6])
     * @param sa      获取数据 起始地址, 单位 字节
     * @param dataBuf 存取数据数组
     * @return 读取成功返回 true
     */
    public boolean fastReadTID(int sa, byte[] dataBuf) {

        boolean reVal = false;
        try {
            if (null == dataBuf || dataBuf.length == 0) {
                return reVal;
            }

            // 起始地址, 单位为 字
            int wordLen = sa >> 1; // 字节 转 字
            if (0xFF < wordLen) {
                CMD_FAST_READ_TID[5] = (byte) (wordLen >> 8);
            } else {
                CMD_FAST_READ_TID[5] = (byte) 0x00;
            }
            CMD_FAST_READ_TID[6] = (byte) wordLen;

            // 获取数据长度, 单位为 字
            wordLen = (dataBuf.length + 1) >> 1; // 字节 转 字
            if (0xFF < wordLen) {
                CMD_FAST_READ_TID[7] = (byte) (wordLen >> 8);
            } else {
                CMD_FAST_READ_TID[7] = (byte) 0x00;
            }
            CMD_FAST_READ_TID[8] = (byte) wordLen;

            // 校验位
            CMD_FAST_READ_TID[9] = 0;
            for (int i = 2; i < CMD_FAST_READ_TID.length - 3; i++) {
                CMD_FAST_READ_TID[9] ^= CMD_FAST_READ_TID[i];
            }

            byte[] buf = new byte[40];
            int len = runCmd(CMD_FAST_READ_TID, buf);
            LogUtils.tag(TAG).d("fastReadTID() len:" + len);

            if ((buf[4] == ((byte) 0x8F)) && (buf[5] == 0x01)) {
                int start = 9 + sa % 2;
                int dataLen = Math.min(dataBuf.length, buf[8] << 1); // buf[7-8]为回复的数据长度，单位字

                System.arraycopy(buf, start, dataBuf, 0, dataLen);

                reVal = true;
                // LogUtils.tag(TAG).d( "fastReadTID(): " + bytes2HexString(dataBuf, 0,
                // dataLen));
            } else {
                Log.d(TAG, "buf[4] = " + buf[4] + "  buf[5] = " + buf[5]);
            }

        } catch (Exception e) {
            LogUtils.tag(TAG).e("fastReadTID e:" + e);
        }
        return reVal;
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

    public void stopMore() {
    }

    public boolean findMore() {
        boolean flag = false;
        return flag;

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
        LogsUtil.d("SettingPowerActivity", "CMD_GET_POWER:" + ArrayUtils.bytes2HexString(CMD_SET_POWER));
        reVal = runCmd(CMD_GET_POWER, buff);
        LogUtils.tag(TAG).d("getPower() len=12:" + (reVal));
        LogsUtil.d("SettingPowerActivity", "buff:" + ArrayUtils.bytes2HexString(buff));
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
     * @param readPower  读 功率.范围:5-30
     * @param writePower 写 功率.范围:5-30
     * @return 设置成功 返回 大于0的int
     */
    public int setPower(int readPower, int writePower) {
        return setPower(readPower, writePower, 1, 1, 0);
    }

    /**
     * @param readPower  读 功率.范围:5-30
     * @param writePower 写 功率.范围:5-30
     * @param id         天线号
     * @param save       本次设置是否保存 , 0 = 不保存
     * @param open       是否闭环, 非0 = 闭环, 默认0
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

}