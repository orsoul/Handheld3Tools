package com.fanfull.libhard;

import java.util.Arrays;

/**
 * RFID模块操作类，提供对M1卡、NFC卡的基础操作。
 */
public class RfidCmd {
    /**
     * 关闭RFID电源
     */
    private final static byte[] CMD_CLOSE_RFID = new byte[]{(byte) 0x00,
            (byte) 0x00, (byte) 0xFF, (byte) 0x04, (byte) 0xFC, (byte) 0xD4,
            (byte) 0x32, (byte) 0x01, (byte) 0x00, (byte) 0xF9, (byte) 0x00};
    /**
     * 最近一次寻卡 得到的 uid
     */
    public static byte[] sLastUid;
    /**
     * M1卡操作对象，定义静态的，单例模式
     */
    /**
     * 选择串口
     */
    private final String[] CHOOSED_SERIAL = {"/dev/ttyUSB0", "/dev/ttyUSB1"};
    /**
     * 选择波特率
     */
    private final int CHOOSED_BUAD = 115200;
    /**
     * 唤醒读卡器 24
     */
    private byte[] CMD_WAKEUP = new byte[]{(byte) 0x55, (byte) 0x55,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF,
            (byte) 0x03, (byte) 0xFD, (byte) 0xD4, (byte) 0x14, (byte) 0x01,
            (byte) 0x17, (byte) 0x00};// RF
    // 22 33 44
    /**
     * 寻卡命令 11
     */
    private byte[] CMD_FINDCARD = new byte[]{(byte) 0x00, (byte) 0x00,
            (byte) 0xff, (byte) 0x04, (byte) 0xfc, (byte) 0xd4, (byte) 0x4a,
            (byte) 0x01, (byte) 0x00, (byte) 0xe1, (byte) 0x00};// 寻卡
    /**
     * 认证命令 22
     */
    private byte[] CMD_M1_AUTHKEY = new byte[]{(byte) 0x00, (byte) 0x00,
            (byte) 0xff, (byte) 0x0f, (byte) 0xf1, (byte) 0xd4, (byte) 0x40,
            (byte) 0x01, (byte) 0x60, (byte) 0x07, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xc2, (byte) 0x00};
    /**
     * 读数据命令 12
     */
    private byte[] CMD_M1_READ = new byte[]{(byte) 0x00, (byte) 0x00,
            (byte) 0xFF, (byte) 0x05, (byte) 0xFB, (byte) 0xD4, (byte) 0x40,
            (byte) 0x01, (byte) 0x30, (byte) 0x06, (byte) 0xB5, (byte) 0x00};
    /**
     * 写数据命令 28
     */
    private byte[] CMD_M1_WRITE = new byte[]{(byte) 0x00, (byte) 0x00,
            (byte) 0xff, (byte) 0x15, (byte) 0xeb, (byte) 0xd4, (byte) 0x40,
            (byte) 0x01, (byte) 0xA0, (byte) 0x06, (byte) 0x0f, (byte) 0x0e,
            (byte) 0x0d, (byte) 0x0c, (byte) 0x0b, (byte) 0x0a, (byte) 0x09,
            (byte) 0x08, (byte) 0x07, (byte) 0x06, (byte) 0x05, (byte) 0x04,
            (byte) 0x03, (byte) 0x02, (byte) 0x01, (byte) 0x00, (byte) 0xcd,
            (byte) 0x00};// 将数据写在06块，数据为00-0f
    /**
     * 读NFC命令
     */
    private byte[] CMD_NFC_READ = new byte[]{(byte) 0x00, (byte) 0x00,
            (byte) 0xFF, (byte) 0x05, (byte) 0xFB, (byte) 0xD4, (byte) 0x40,
            (byte) 0x01, (byte) 0x30, (byte) 0x06, (byte) 0xB5, (byte) 0x00};
    /**
     * 写NFC命令
     */
    private byte[] CMD_NFC_WRITE = new byte[]{(byte) 0x00, (byte) 0x00,
            (byte) 0xFF, (byte) 0x09, (byte) 0xF7, (byte) 0xD4, (byte) 0x40,
            (byte) 0x01, (byte) 0xA2, (byte) 0x07, (byte) 0x44, (byte) 0x33,
            (byte) 0x22, (byte) 0x11, (byte) 0x98, (byte) 0x00};

    private RfidCmd() {
    }


    /**
     * 验证回复的信息的前六位是否准确
     * @param buf 命令返回的信息
     * @return
     */
    private boolean checkFirst6Data(byte[] buf) {
        try {
            return (buf[0] == (byte) 0x00) && (buf[1] == (byte) 0x00)
                    && (buf[2] == (byte) 0xff) && (buf[3] == (byte) 0x00)
                    && (buf[4] == (byte) 0xff) && (buf[5] == (byte) 0x00);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 校验位 + 数据位总和 = 0
     * @param n   命令中的数据位长度，
     * @param cmd 命令，前5位为固定格式，数据位从 cmd[5]开始
     * @return 校验位
     */
    private byte getCheckBit(int n, byte cmd[]) {
        try {
            byte sum = 0;
            for (int i = 0; i < n; i++) {
                sum += cmd[i + 5];
            }
            return (byte) -sum;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 激活，唤醒读卡器，即可开始寻卡，读写操作，上电后只需激活唤醒一次
     * @return true 激活成功，false激活失败
     */
    public boolean wakeup() {// 激活,唤醒操作
        byte buf[] = new byte[15];
        // HARDWARE.setGPIO(0, 5);
        int runCmd = runCmd(CMD_WAKEUP, buf);
        // HARDWARE.setGPIO(1, 5);

        boolean reVal = (checkFirst6Data(buf) && buf[6] == (byte) 0x00
                && buf[7] == (byte) 0x00 && buf[8] == (byte) 0xff
                && buf[9] == (byte) 0x02 && buf[10] == (byte) 0xfe
                && buf[11] == (byte) 0xd5 && buf[12] == (byte) 0x15
                && buf[13] == (byte) 0x16 && buf[14] == (byte) 0x00);

        return reVal;
    }

    /**
     * @return byte[] 返回类型 字节数组数据，即卡号，M1卡长度4个字节，NFC卡长度7个字节
     * @throws
     * @Description: 寻卡操作. 可对 M1 和 NFC 进行操作。
     */
    public byte[] findCard() {
        byte[] reVal = null;

        byte[] buf = new byte[30];
        // HARDWARE.setGPIO(0, 5);
        int runCmd = runCmd(CMD_FINDCARD, buf);
        // HARDWARE.setGPIO(1, 5);

        if (checkFirst6Data(buf)) {
            if (buf[18] == 0x04 && 25 == runCmd) {
                // M1
                //reVal = new byte[]{buf[22], buf[21], buf[20], buf[19]};
                reVal = new byte[]{buf[19], buf[20], buf[21], buf[22]};
                sLastUid = reVal;
            } else if (buf[18] == 0x07 && 28 == runCmd) {
                // NFC
                reVal = new byte[]{buf[19], buf[20], buf[21], buf[22],
                        buf[23], buf[24], buf[25]};
                //reVal = new byte[] { buf[25], buf[24], buf[23], buf[22],
                //buf[21], buf[20], buf[19] };
                sLastUid = reVal;
            }
			/*for(int i=0; i<reVal.length;i++){

			}*/
        } else {
        }
      // LogUtils.d( "findCard() uid: " +
        // ArrayUtil.bytesToHexString(reVal));
        return reVal;
    }

    /**
     * 读取任意长度数据,读取长度由 缓存数组dataBuf 确定.
     * @param sa      起始地址,单位: 字 (字长32bit, 即4个byte)
     * @param dataBuf 保存 取出数据 的数组
     * @param runTime 读数据持续时间
     * @param uid     NFC卡的uid, 若该参数为null 则本次读取数据前会进行 寻卡操作
     * @return
     */
    public boolean readNFCInTime(int sa, byte[] dataBuf, long runTime,
                                 byte[] uid) {

        boolean reVal = false;

        if (sa < 0 || null == dataBuf || 0 == dataBuf.length) {
            return reVal;
        }

        long time = System.currentTimeMillis();
        if (null == uid) {
            // 寻卡
            do {
                uid = findCard();
                if (null != uid && 7 == uid.length) {
                    break;
                }
            } while ((System.currentTimeMillis() - time) < runTime);
        }

        if (null == uid) {
            // 寻卡失败
            return reVal;
        }

        //reVal = true;
        int dsa = 0;
        byte buf[] = new byte[32];

        int times = (dataBuf.length + 16 - 1) / 16;
        for (int j = 0; j < times; j++) {// 外层循环 控制 读取数据 的次数, 每次读取4个字(16byte)

            CMD_NFC_READ[9] = (byte) (sa + j * 4);// 起始地址后移4个字
            CMD_NFC_READ[10] = getCheckBit(5, CMD_NFC_READ);

            while (true) {
                if (runTime < System.currentTimeMillis() - time) {
                    return false;//
                }
                if (sa == 0x04) {
                }
                int runCmd = runCmd(CMD_NFC_READ, buf);
                if (checkFirst6Data(buf) && buf[12] == 0x41 && buf[13] == 0x00) {
                    int len = 16;
                    if (dataBuf.length - dsa < 16) {
                        len = dataBuf.length - dsa;
                    }
                    for (int i = 0; i < len; i++) { // 内层循环 将读到的数据 存入目标数组
                        dataBuf[dsa] = buf[i + 14];
                        dsa++;
                    }
                    reVal = true;
                    break;
                } else {
                }

            }// end while()
        }

        return reVal;
    }

    /**
     * 向NFC写入任意长度数据, 每次写入4byte, 直到数据写入完成.不满4byte的数据后面被0填充.
     * @param sa      起始地址 单位 字(字长4byte)
     * @param data    写入NFC的byte[], 长度应大于0
     * @param runTime 写入数据持续时间。
     * @param uid     NFC卡的uid, 若该参数为null 则本次写数据前会进行 寻卡操作
     * @return true 写入成功
     */
    public boolean writeNFCInTime(int sa, byte[] data, long runTime, byte[] uid) {
        boolean reVal = false;
        if (sa < 0) {
            return reVal;
        }

        if (null == data || 0 == data.length) {
            return reVal;
        }

        long time = System.currentTimeMillis();
        if (null == uid) {
            // 寻卡
            do {
                uid = findCard();
                if (null != uid && 7 == uid.length) {
                    break;
                }
            } while ((System.currentTimeMillis() - time) < runTime);
        }

        if (null == uid) {
            // 寻卡失败
            return reVal;
        }

        int writeTimes = (data.length + 4 - 1) / 4;

        byte[] buf = new byte[32];
        reVal = true;

        for (int i = 0; i < writeTimes; i++) { // 控制写入次数
            CMD_NFC_WRITE[9] = (byte) (sa + i);
            int dataPointer = 4 * i;

            for (int j = 0; j < 4; j++) {
                if (dataPointer < data.length) {
                    CMD_NFC_WRITE[10 + j] = data[dataPointer];
                    dataPointer++;
                } else {
                    CMD_NFC_WRITE[10 + j] = 0;// 写入数据的长度不被4整除,末尾填充0
                }
              // LogUtils.d( "for[" + (sa + i) + "," + j + "]: "
                // + bytesToHexString(CMD_NFC_WRITE, 10, 11 + j));
            }

            CMD_NFC_WRITE[CMD_NFC_WRITE.length - 2] = getCheckBit(CMD_NFC_WRITE.length - 7, CMD_NFC_WRITE);

            while (true) {
                if (runTime < System.currentTimeMillis() - time) {
                    return false;
                }
                Arrays.fill(buf, (byte) 0);
                int runCmd = runCmd(CMD_NFC_WRITE, buf);
                if (checkFirst6Data(buf) && buf[6] == (byte) 0x00
                        && buf[7] == (byte) 0x00 && buf[8] == (byte) 0xff
                        && buf[9] == (byte) 0x03 && buf[10] == (byte) 0xfd
                        && buf[11] == (byte) 0xd5 && buf[12] == (byte) 0x41
                        && buf[13] == (byte) 0x00 && buf[14] == (byte) 0xea
                        && buf[15] == (byte) 0x00) {
                    break;
                } else {
                }
            }// end while


        }
        //byte[] b = new byte[16];
        //readNFCInTime(sa, b,500,uid);
      //LogUtils.d("CoverNfcNewBagActivity", "send result b:"+bytes2HexString(b));
        return reVal;
    }

    /**
     * 读写M1卡前的 认证操作。若传入的uid为null，此方法会先执行 findCard() 获得uid。
     * @param blockNum 块区
     * @param uid      认证前 需要寻卡 得到 uid; 若uid为null，则会先执行 findCard() 获得uid
     * @return true 认证成功
     */
    public boolean authKey(int blockNum, byte[] uid) {
        if (null == uid) {
            uid = findCard();
        }
        if (null == uid || 4 != uid.length) {
            return false;
        }
        CMD_M1_AUTHKEY[9] = (byte) blockNum; // 块区
        // CMD_AUTHKEY[16] = uidBuf[0];
        // CMD_AUTHKEY[17] = uidBuf[1];
        // CMD_AUTHKEY[18] = uidBuf[2];
        // CMD_AUTHKEY[19] = uidBuf[3];
        CMD_M1_AUTHKEY[16] = uid[3];
        CMD_M1_AUTHKEY[17] = uid[2];
        CMD_M1_AUTHKEY[18] = uid[1];
        CMD_M1_AUTHKEY[19] = uid[0];
        // CMD_AUTHKEY[20] = getCheckNumber(15, CMD_AUTHKEY);// 校验位

        // CMD_AUTHKEY.length - 2：倒数第2位为 校验位
        // CMD_AUTHKEY.length - 7：CMD_AUTHKEY[5~倒数第3位] 为需要计算的数据
        CMD_M1_AUTHKEY[CMD_M1_AUTHKEY.length - 2] = getCheckBit(
                CMD_M1_AUTHKEY.length - 7, CMD_M1_AUTHKEY);// 校验位

        byte[] buf = new byte[32];
        int runCmd = runCmd(CMD_M1_AUTHKEY, buf);

        return (16 == runCmd && checkFirst6Data(buf) && buf[6] == (byte) 0x00
                && buf[7] == (byte) 0x00 && buf[8] == (byte) 0xff
                && buf[9] == (byte) 0x03 && buf[10] == (byte) 0xfd
                && buf[11] == (byte) 0xd5 && buf[12] == (byte) 0x41
                && buf[13] == (byte) 0x00 && buf[14] == (byte) 0xea && buf[15] == (byte) 0x00);
    }

    /**
     * 读M1卡 块区操作。此方法包含必要的 认证步骤
     * @param blockNum 块号
     * @param dataBuf  存储读到的数据的byte[]，长度=[1,16]
     * @param runTime  读数据持续时间. 设为0只读1次
     * @param uid      认证 需要的uid，若uid为null，认证步骤会尝试 寻卡 操作。
     * @return 读取成功返回 true
     */
    public boolean readM1(int blockNum, byte[] dataBuf, long runTime, byte[] uid) {

        long time = System.currentTimeMillis();

        boolean reVal = false;

        if (null == dataBuf || dataBuf.length == 0 || 16 < dataBuf.length) {
            return reVal;
        }

        boolean isAuth = false;
        do {
            // 认证步骤
            if (authKey(blockNum, uid)) {
                isAuth = true;
                break;
            }
        } while ((System.currentTimeMillis() - time) < runTime);
        if (!isAuth) {
            return reVal;
        }

        // 00 00 ff 05 fb D4 40 01 30 06 B5 00 //读第7块
        CMD_M1_READ[9] = (byte) blockNum;

        // CMD_NFC_READ[10] = getCheckNumber(5, CMD_NFC_READ); // 计算校验位
        // CMD_AUTHKEY.length - 2：倒数第2位为 校验位
        // CMD_AUTHKEY.length - 7：CMD_AUTHKEY[5~倒数第3位] 为需要计算的数据
        CMD_M1_READ[CMD_M1_READ.length - 2] = getCheckBit(
                CMD_M1_READ.length - 7, CMD_M1_READ); // 计算校验位

        byte[] buf = new byte[32];

        do {
            int runCmd = runCmd(CMD_M1_READ, buf);
            if (checkFirst6Data(buf) && buf[12] == 0x41 && buf[13] == 0x00) {
                // for (int i = 0; i < dataBuf.length; i++) {
                // dataBuf[i] = buf[i + 14];
                // }
                System.arraycopy(buf, 14, dataBuf, 0, dataBuf.length);
                reVal = true;
                break;
            }
        } while ((System.currentTimeMillis() - time) < runTime);
        return reVal;
    }

    /**
     * @param blockID 块区
     * @param data    需要写入的数据，长度必须为 32 hexString
     * @param runTime 写数据持续时间. 设为0只读1次
     * @param uid     认证 需要的uid，若uid为null，认证步骤会尝试 寻卡 操作。
     * @return true 写入
     */
    public boolean writeM1(int blockID, String data, long runTime, byte[] uid) {
        return false;
    }

    /**
     * @param blockNum 块区
     * @param data     需要写入的数据，长度必须为 16byte
     * @param runTime  写数据持续时间. 设为0只读1次
     * @param uid      认证 需要的uid，若uid为null，认证步骤会尝试 寻卡 操作。
     * @return true 写入成功
     */
    public boolean writeM1(int blockNum, byte[] data, long runTime, byte[] uid) {

        boolean reVal = false;

        if (null == data || data.length != 16) {
            // 数据长度 不符
            return reVal;
        }

        long time = System.currentTimeMillis();
        boolean isAuth = false;
        do {
            // 认证步骤
            if (authKey(blockNum, uid)) {
                isAuth = true;
                break;
            }
        } while ((System.currentTimeMillis() - time) < runTime);
        if (!isAuth) {
            return reVal;
        }

        // 拼接命令
        CMD_M1_WRITE[9] = (byte) blockNum;

        for (int i = 0; i < data.length; i++) {
            CMD_M1_WRITE[10 + i] = data[i];
        }
        // CMD_AUTHKEY.length - 2：倒数第2位为 校验位
        // CMD_AUTHKEY.length - 7：CMD_AUTHKEY[5~倒数第3位] 为需要计算的数据
        CMD_M1_WRITE[CMD_M1_WRITE.length - 2] = getCheckBit(
                CMD_M1_WRITE.length - 7, CMD_M1_WRITE);

        byte[] buf = new byte[16];
        do {
            int runCmd = runCmd(CMD_M1_WRITE, buf);
            reVal = (checkFirst6Data(buf) && buf[6] == (byte) 0x00
                    && buf[7] == (byte) 0x00 && buf[8] == (byte) 0xff
                    && buf[9] == (byte) 0x03 && buf[10] == (byte) 0xfd
                    && buf[11] == (byte) 0xd5 && buf[12] == (byte) 0x41
                    && buf[13] == (byte) 0x00 && buf[14] == (byte) 0xea && buf[15] == (byte) 0x00);
            if (reVal) {
                break;
            }
        } while ((System.currentTimeMillis() - time) < runTime);
        return reVal;
    }

    private int runCmd(byte[] cmd_m1_write, byte[] buf) {
        return 0;
    }
}