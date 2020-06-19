package com.finger;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.rd.Hardware2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class FingerPrint {
    public static boolean enable;
    private static FingerPrint mFinger = null;
    private final String TAG = FingerPrint.class.getSimpleName();
    /**
     * 选择串口
     */
    private final String[] mChooseSerial = {"/dev/ttyUSB0", "/dev/ttyUSB1"};
    /**
     * 选择波特率
     */
    private final int mChooseBuad = 115200;
    public int mFd;
    private byte[] mFingerNumber = {(byte) 0xEF, 0x01, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x01, 0x00, 0x03, 0x1d,
            0x00, 0x21};// 获取当前已经存在的指纹数
    private byte[] getimage = {(byte) 0xEF, 0x01, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, 0x01, 0x00, 0x03, 0x01, 0x00, 0x05};// PS_GetImage
    private byte[] genchar = {(byte) 0xEF, 0x01, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, 0x01, 0x00, 0x04, 0x02, 0x01, 0x00, 0x08};// PS_GenChar
    private byte[] mRegFinger = {(byte) 0xEF, 0x01, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, 0x01, 0x00, 0x03, 0x05, 0x00, 0x09};// PS_RegModel
    private byte[] storechar = {(byte) 0xEF, 0x01, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, 0x01, 0x00, 0x06, 0x06, 0x01, 0x00, 0x01,
            0x00, 0x0F};// PS_StoreChar
    private byte[] loadchar = {(byte) 0xef, 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x01, 0x00, 0x06, 0x07, 0x01, 0x00, 0x00, 0x00, 0x0f};//PS_LoadChar
    private byte[] upchar = {(byte) 0xef, 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x01, 0x00, 0x04, 0x08, 0x01, 0x00, 0x0e};//PS_UpChar
    private byte[] mAddFinger = {(byte) 0xef, 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x01, 0x00, 0x03, 0x05, 0x00, 0x09};//PS_RegModel
    private byte[] search = {(byte) 0xEF, 0x01, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, 0x01, 0x00, 0x08, 0x04, 0x02, 0x00, 0x00,
            0x00, 0x10, 0x00, 0x1F};// PS_Search 搜索指纹库中的指纹
    private byte[] mReadindex = {(byte) 0xEF, 0x01, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, 0x01, 0x00, 0x04, 0x1F, 0x00, 0x00, 0x24};// PS_ReadIndexTable
    private byte[] mEmptyFinger = {(byte) 0xEF, 0x01, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x01, 0x00, 0x03, 0x0d,
            0x00, 0x11};// PS_Empty 删除所有指纹
    private byte[] writeText = {(byte) 0xEF, 0x01, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, 0x01, 0x00, 0x24, 0x18, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x3E};// PS_UpChar
    private byte[] readText = {(byte) 0xEF, 0x01, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, 0x01, 0x00, 0x04, 0x19, 0x00, 0x00, 0x1E};
    private byte[] buf = new byte[24];// 接收命令缓存
    private CharSequence temp = "NULL";
    private int cmd_num;// 命令编号
    private int len;
    private Hardware2 hardware;
    private FingerManager fManager;
    private FingerPrintTask mFingerPrintTask;
    private boolean isInit = false;

    //注册指纹的时候，时间改为700ms成功率高，在用指纹登录的时候50ms就看可以了
    public static long fingerWaitTime = 50;

    // 启动模块
    public FingerPrint() {
        hardware = Hardware2.getInstance();
        fManager = FingerManager.getInstance();
    }

    /**
     * 提供外部使用的指纹操作实例
     *
     * @return
     */
    public static FingerPrint getInstance() {
        if (null == mFinger) {
            mFinger = new FingerPrint();
        }
        return mFinger;
    }

    /**
     * 把字节数据，转化成整型值
     *
     * @return 整型值
     */
    public static int byteToInt2(byte b) {
        int mask = 0xff;
        int temp = 0;
        int n = 0;
        n <<= 8;
        temp = b & mask;
        n |= temp;
        return n;
    }

    /**
     * 将一个整型数据转化成两个字节的字节数组
     *
     * @param value 整型值
     * @return 两个字节的字节数组
     */
    public static byte[] intToBytes(int value) {
        byte[] src = new byte[2];
        src[0] = (byte) ((value >> 8) & 0xFF);
        src[1] = (byte) (value & 0xFF);
        return src;
    }

    public boolean getIsinit() {
        return isInit;
    }

    public void setInit(boolean isinit) {
        this.isInit = isinit;
    }

    /**
     * 打开指纹的GPIO引脚，再打开ttyUSB1串口 当串号打开成功或者失败后，回调对应的方法
     */
    public void open() {
        hardware.openGPIO();// 20170614
        int gpio = hardware.setGPIO(0, 4);
        // 上电到打开串号，需延迟
        SystemClock.sleep(500);
        LogsUtil.d(TAG, "指纹模块已经上电=" + gpio);

        int n = 0;
        while (n++ < 10) {
            if (connection()) {
                // 回调打开指纹串口成功
                fManager.openFingerSerialPortSuccess(true);
                LogsUtil.d(TAG, "打开指纹串口成功！");
                isInit = true;
                return;
            }
            SystemClock.sleep(100);
        }
        if (n == 11) {
            // 回调打开指纹串口失败
            fManager.openFingerSerialPortSuccess(false);
            LogsUtil.d(TAG, "打开指纹串口失败！");
        }
    }

    /**
     * @param @return 设定文件
     * @return String 返回类型
     * @throws
     * @Title: connection
     * @Description: 打开串口连接
     */
    public boolean connection() {
        LogsUtil.d("Hardware2", "connection ...");
        mFd = hardware.openSerialPortTest(mChooseSerial[1], mChooseBuad, 8, 1);// 打开串口2(指纹模块只可使用第二个串口？）
        if (mFd == -1) {
            mFd = hardware.openSerialPortTest(mChooseSerial[1], mChooseBuad, 8, 1);// 打开串口
            // 重启adb.需要系统权限，需定义为系统应用
            //			 Settings.Secure.putInt(BaseApplication.getContext().getContentResolver(),
            //			 Settings.Secure.ADB_ENABLED, 0);
            //			 Settings.Secure.putInt(BaseApplication.getContext().getContentResolver(),
            //					 Settings.Secure.ADB_ENABLED,1);
            //			LogsUtil.d(TAG, "----Reset adb----");
            return mFd != -1;
        } else {
            LogsUtil.d(TAG, "指纹模块已打开！！！");
            while ((len = hardware.read(mFd, buf, 24)) > 0) ;// clear buf

        }
        return true;

    }

    /**
     * @return void 返回类型
     * @throws
     * @Title: close
     * @Description: 关闭串口
     */
    public void closeFinger() {
        hardware.setGPIO(1, 4);
        hardware.closeGPIO();// 20170614
        hardware.close(mFd);
        mFd = -1;
        isInit = false;
        LogsUtil.d(TAG, "---closeFinger 指纹已经关闭----");
    }

    /**
     * 复位GPIO引脚
     */
    public void reset() {
        hardware.reset();
    }

    /**
     * @return 指纹扫描线程已在运行返回 true
     */
    public boolean isSearching() {
        return null != mFingerPrintTask && !mFingerPrintTask.isStop();
    }

    /**
     * 启动线程，开始搜索指纹
     */
    public void startSearchFinger() {

        if (null == mFingerPrintTask) {
            mFingerPrintTask = new FingerPrintTask();
        }
        mFingerPrintTask.stop(false);
        new Thread(mFingerPrintTask).start();
    }

    /**
     * 停止搜索指纹
     */
    public void stopSearchFinger() {
        if (null != mFingerPrintTask) {
            mFingerPrintTask.stop(true);
            LogsUtil.d(TAG, "stop Search  finger!");
        }
    }

    /**
     * 停止搜索指纹 并 关闭指纹模块
     */
    public void stopSearchAndClose() {
        if (null != mFingerPrintTask) {
            if (mFingerPrintTask.isStop()) {
                closeFinger();
            } else {
                mFingerPrintTask.stop(true);
            }
        } else {
            closeFinger();
        }
    }

    /**
     * 清空本地库中所有的指纹
     *
     * @return 成功返回 0 清空失败返回 -1；
     */
    public void emptyFinger() {
        while ((len = hardware.read(mFd, buf, 24)) > 0)
            ;// clear buf
        byte[] buf = new byte[24];// 接收命令缓存
        len = hardware.write(mFd, mEmptyFinger);
        SystemClock.sleep(100);
        if (hardware.select(mFd, 0, 500) == 1) {
            //***
            len = hardware.read(mFd, buf, 12);
            LogsUtil.d("PS_Empty:" + ArrayUtils.bytes2HexString(buf));
        } else {
            fManager.emptyFinger(false);
            return;
        }
        if (buf[9] == 0x00 && buf[0] == (byte) 0xef && buf[1] == 0x01) {
            LogsUtil.d(TAG, "Delete all finger successfully!\n");
            fManager.emptyFinger(true);
            return;
        }
        fManager.emptyFinger(false);
    }

    /**
     * 获取当前指纹库中有效的存储模板
     *
     * @return 有效数目
     */
    public int getAllFingerNmber() {

        while ((len = hardware.read(mFd, buf, 12)) > 0)
            ;// clear buf
        len = hardware.write(mFd, mFingerNumber);
        int tmp = 0;
        tmp = hardware.select(mFd, 1, 0);
        if (1 == tmp) {
            //***
            len = hardware.read(mFd, buf, 12);
            // EF 01 FF FF FF FF 07 00 05 00 00 03 000000000000000000000000
            LogsUtil.d(TAG, ArrayUtils.bytes2HexString(buf));
            if (buf[10] == 0x00 && buf[0] == (byte) 0xef && buf[1] == 0x01) {

                LogsUtil.d(TAG,
                        "Successfully Get:" + (Integer.valueOf(buf[11] + ""))
                                + " fingers");
                return Integer.valueOf(buf[11] + "");
            }
        }
        return -1;
    }

    /**
     * 根据编号删除指定指纹
     *
     * @return
     */
    public void deleteFingerNmber(int delid, int v) {
        LogsUtil.d(TAG, "delId=" + delid);

        byte[] delchar = {(byte) 0xEF, 0x01, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xff, 0x01, 0x00, 0x07, 0x0C, 0x00, 0x01,
                0x00, 0x01, 0x00, 0x0E};
        byte[] fingerid = intToBytes(delid);
        delchar[10] = fingerid[0];
        delchar[11] = fingerid[1];

        fingerid = getDownCheckNumber(delchar);
        delchar[14] = fingerid[0];
        delchar[15] = fingerid[1];

        // EF01FFFFFFFF 01 00 07 0C 0000 0001 00 16
        LogsUtil.v(TAG, "del:" + ArrayUtils.bytes2HexString(delchar));
        while ((len = hardware.read(mFd, buf, 12)) > 0)
            ;// clear buf
        len = hardware.write(mFd, delchar);
        SystemClock.sleep(300);
        if (1 == hardware.select(mFd, 1, 0)) {
            //***
            len = hardware.read(mFd, buf, 12);
        } else {
            fManager.deleteFingerNmber(false);
            return;
        }
        LogsUtil.v(TAG, "del buf:" + ArrayUtils.bytes2HexString(buf));
        if (buf[9] == 0x00 && buf[0] == (byte) 0xef && buf[1] == 0x01) {
            LogsUtil.v(TAG, "Successfully delete:");
            fManager.deleteFingerNmber(true);
            return;
        }
        fManager.deleteFingerNmber(false);
    }


    /**
     * 单次搜索指纹
     *
     * @param n 指定读取数据的延迟时间，单位为毫秒
     * @return >=0 成功搜索到指纹编号 -3本地指纹库中，没有该指纹
     */
    public int searchFingerOnce(int n) {
        byte[] buf = new byte[24];// 接收命令缓存
        while ((len = hardware.read(mFd, buf, 24)) > 0) ;// clear buf

        while (len < 1) {
            len = hardware.write(mFd, getimage);
        }
        SystemClock.sleep(360);
        if (hardware.select(mFd, 1, 0) == 1) {
            buf[0] = 0x00;
            len = hardware.read(mFd, buf, 12);
        } else {
            LogsUtil.v(TAG, "1.0 select=0");
            return -4;
        }
        Log.d(TAG, "getImage buf :" + ArrayUtils.bytes2HexString(buf));
        if (buf[9] == 0x00 && buf[0] == (byte) 0xef && buf[1] == 0x01) {
            Log.d(TAG, "getImage succeed!");
            genchar[10] = 0x02;
            genchar[12] = 0x09;
            len = hardware.write(mFd, genchar);
            if (len < 1) {
                return -1;
            }
            SystemClock.sleep(50);
            SystemClock.sleep(500);//经测试加一个450ms的延迟可以有效提高指纹识别的效率
        } else {
            return -6;// 传感器没有获取到手指 02
        }
        if (hardware.select(mFd, 1, 0) == 1) {
            buf[0] = 0x00;
            len = hardware.read(mFd, buf, 12);
        } else {
            LogsUtil.d(TAG, "gen select=0");
            return -4;
        }
        Log.d(TAG, "GenChar buf :" + ArrayUtils.bytes2HexString(buf));
        if (buf[0] == (byte) 0xef && buf[1] == 0x01 && buf[9] == 0x00) {
            Log.d(TAG, "Search GenChar succeed!");
            len = hardware.write(mFd, search);
            if (len < 1) {
                LogsUtil.d("write error");
                return -1;
            }
            SystemClock.sleep(50);
        } else {
            LogsUtil.d("---builder char failed---");
            return -5;// 没有成功将手指合成特征码
        }
        if (hardware.select(mFd, 1, 0) == 1) {
            buf[0] = 0x00;
            len = hardware.read(mFd, buf, 24);
            LogsUtil.v("PS_Search:" + ArrayUtils.bytes2HexString(buf));
        } else {
            LogsUtil.d("---search 1.0---");
            return -6;
        }
        // [12][13]是匹配的得分，不可能为0
        Log.d(TAG, "Search fingerId buf :" + ArrayUtils.bytes2HexString(buf));
        if (buf[9] == 0x00 && buf[0] == (byte) 0xef && buf[1] == 0x01
                && (buf[12] != 0x00 || buf[13] != 0x00)) {
            //LogsUtil.d(TAG,"Search fingerId buf :"+ArrayUtils.bytes2HexString(buf));
            int id = byteToInt2(buf[10]) * 256 + byteToInt2(buf[11]);
            temp = "Search Success finger ID : " + id + "\n";
            Log.d(TAG, "Search result temp=" + temp);
            return id;
        } else {
            return -3;// 指纹库中没有该指纹
        }
    }


    /**
     * 获得指纹id号 如果有网络，则有网络提供id数据 如果没有网络，则由本地数据得到ID。
     *
     * @return 非负数表示获得的ID，-1表示获取失败
     */
    public int getFingerId() {
        int[] validnum = {-1, -1};
        byte[] tempbuf = new byte[139];
        for (int m = 0; m < 2; m++) {
            while ((len = hardware.read(mFd, buf, 12)) > 0)
                ;// clear buf
            len = hardware.write(mFd, mReadindex);
            if (hardware.select(mFd, 1, 0) == 1) {
                tempbuf[0] = 0x00;
                SystemClock.sleep(50);
                len = hardware.read(mFd, tempbuf, 44);
                LogsUtil.d("readindextable:"
                        + ArrayUtils.bytes2HexString(tempbuf));
                if (tempbuf[9] == 0x00 && tempbuf[0] == (byte) 0xef
                        && tempbuf[1] == 0x01) {
                    for (int i = 10; i < 42; i++) {
                        if (tempbuf[i] != (byte) 0xff) {
                            for (int j = 0; j < 8; j++) {
                                if (tempbuf[i] % 2 == 1) {
                                    tempbuf[i] /= 2;
                                    validnum[m]++;
                                } else {
                                    i = 42;
                                    break;
                                }
                            }
                        } else {
                            validnum[m] += 8;
                        }
                    }
                } else {
                    LogsUtil.d(TAG, "获取编号异常");
                    return -1;
                }
                LogsUtil.d("validnum>>>>>>>>:" + validnum[m]);
            }
        }
        if (-1 != validnum[0] && validnum[0] == validnum[1]) {
            LogsUtil.d(TAG, "获取编号:" + Integer.valueOf(validnum[0]));
            return Integer.valueOf(validnum[0]);
        } else {
            LogsUtil.d(TAG, "获取编号异常");
            return -1;
        }
    }

    /**
     * 添加指定编号的ID，该编号由本地数据库生成，确保是唯一的编号 添加指纹的步骤：
     * 1.获取图像，将图像数据缓存在图像缓冲区
     * 2.生成两次特征码
     * 3.将特征码合成特征模板
     *
     * @param fingerID
     */

    public void addFingerPrint(int fingerID) {
        String temp = "";
        for (int i = 0; i < 3; i++) {
            LogsUtil.d(ArrayUtils.bytes2HexString(getimage));
            if (i == 0) {
                LogsUtil.v(TAG, "please sweep first!" + "\n");
            } else {
                LogsUtil.v(TAG, "please sweep second!" + "\n");
            }
            len = getImage(5);
            LogsUtil.v(TAG, "len:" + len);
            if (len != 1) {
                fManager.addFingerData(-1, null);
                return;
            } else {
                genchar[10] = (byte) (i + 1);
                genchar[12] = (byte) (i + 8);
                len = hardware.write(mFd, genchar);
                buf[0] = 0x00;
                if (hardware.select(mFd, 1, 0) == 0) {
                    LogsUtil.v(TAG, temp);
                    fManager.addFingerData(-1, null);
                    return;
                } else {
                    //***
                    len = hardware.read(mFd, buf, 12);
                    if (i == 1 && buf[0] == (byte) 0xef && buf[1] == 0x01
                            && buf[9] == 0x00) {
                        len = hardware.write(mFd, mRegFinger);
                        if (len < 1) {
                            LogsUtil.v("write error");
                        }
                    } else {
                        continue;
                    }
                    if (hardware.select(mFd, 1, 0) == 0) {
                        LogsUtil.v(TAG, temp);
                        fManager.addFingerData(-1, null);
                        return;
                    } else {
                        SystemClock.sleep(100);
                        //***

                        len = hardware.read(mFd, buf, 12);
                    }
                    if (buf[9] == 0x00 && buf[0] == (byte) 0xef
                            && buf[1] == 0x01) {
                        LogsUtil.v(TAG, "PS_RegModel Success");
                        LogsUtil.v(TAG, "fingerID:" + fingerID);
                        byte fingerNumber[] = intToBytes(fingerID);
                        storechar[10] = (byte) 0x01;
                        storechar[11] = fingerNumber[0];
                        storechar[12] = fingerNumber[1];
                        fingerNumber = getDownCheckNumber(storechar);
                        storechar[13] = fingerNumber[0];
                        storechar[14] = fingerNumber[1];
                        len = hardware.write(mFd, storechar);
                        LogsUtil.v(
                                TAG,
                                "store:"
                                        + ArrayUtils
                                        .bytes2HexString(storechar));
                        if (len < 1) {
                            LogsUtil.v("write error");
                        }
                        SystemClock.sleep(30);
                        if (hardware.select(mFd, 1, 0) == 0) {
                            temp = "regmodel failed!" + "\n";
                            LogsUtil.v(TAG, temp);
                            fManager.addFingerData(-1, null);
                            return;
                        } else {
                            //buf[9] = (byte) 0xff;//？
                            SystemClock.sleep(100);
                            //***
                            len = hardware.read(mFd, buf, 12);
                            LogsUtil.v("PS_StoreChar:"
                                    + ArrayUtils.bytes2HexString(buf));
                        }
                        if (len != 12) {
                            fManager.addFingerData(-1, null);
                            return;
                        }
                        if (buf[9] == 0x00 && buf[0] == (byte) 0xef
                                && buf[1] == 0x01) {
                            LogsUtil.v("Enroll Success");
                            LogsUtil.v(
                                    TAG,
                                    "Enroll Success finger 十六进制 ID : "
                                            + ArrayUtils
                                            .bytes2HexString(intToBytes(fingerID))
                                            + "\n");
                            LogsUtil.v(TAG, "Enroll Success finger 十进制 ID : "
                                    + fingerID + "\n");
                            fManager.addFingerData(2, null);
                            //upChar(fingerID);//获取特征数据，将数据保存后发送到前置
                            upFingerDataToSever(fingerID);// 此時特征碼緩衝區不為空
                            return;
                        } else {
                            temp = "storechar failed!" + "\n";
                            LogsUtil.v(TAG, temp);
                            fManager.addFingerData(-1, null);
                            return;
                        }
                    } else {
                        temp = "PS_regmodel failed!" + "\n";
                        LogsUtil.v(TAG, temp);
                        fManager.addFingerData(-1, null);
                        return;
                    }
                }
            }
        }
        fManager.addFingerData(-1, null);
    }


    /**
     * 获取手指图像，将图像数据缓存在图像缓冲区中
     *
     * @return 1 表示获取成功 0 获取失败
     */
    public int getImage(int times) {
        while ((len = hardware.read(mFd, buf, 24)) > 0) {
        }
        for (int j = 0; j < 3 * times; j++) {
            len = hardware.write(mFd, getimage);
            if (len < 1) {
                LogsUtil.v(TAG, "write error");
                break;
            }
            if (hardware.select(mFd, 1, 0) == 1) {
                //***
                len = hardware.read(mFd, buf, 12);
                LogsUtil.v(TAG, "read len:" + len);
                LogsUtil.v(TAG, "PS_GetImage:" + ArrayUtils.bytes2HexString(buf));
                if (buf[9] == 0x00 && buf[0] == (byte) 0xef && buf[1] == 0x01) {
                    LogsUtil.v(TAG, "getimage success!");
                    return 1;
                }
            }
            LogsUtil.v(TAG, "getimage error!");
        }
        return 0;
    }

    /**
     * 从缓冲区中获取特征文件
     *
     * @param id 需要上传到服务器的指纹编号。
     */
    public void upChar(int id) {
        /*while ((len = hardware.read(mFd, buf, 24)) > 0)
            ;// clear buf
		LogsUtil.d(TAG, "**********cmd send PS_LoadChar:" + ArrayUtils.bytes2HexString(loadchar));
		//len = hardware.write(mFd, getLoadcharCmd(id));
		len = hardware.write(mFd, loadchar);
		if (hardware.select(mFd, 1, 0) == 1) {
			buf[0] = 0x00;
			SystemClock.sleep(30);
			Arrays.fill(buf, (byte) 0);
			len = hardware.read(mFd, buf, 12);
			LogsUtil.s("PS_LoadChar:" + ArrayUtils.bytes2HexString(buf));
			if (buf[9] == 0x00 && buf[0] == (byte) 0xef && buf[1] == 0x01) {*/
        LogsUtil.d(TAG, "****LoadChar successfully!\n");
        while ((len = hardware.read(mFd, buf, 24)) > 0)
            ;// clear buf
        LogsUtil.d(TAG, "**********cmd send PS_UpChar:" + ArrayUtils.bytes2HexString(upchar));
        len = hardware.write(mFd, upchar);

        if (hardware.select(mFd, 1, 0) == 1) {
            buf[0] = 0x00;
            //***
            buf[0] = 0x00;
            SystemClock.sleep(30);
            int readNum = 568;//568
            byte[] loadbuf = new byte[readNum];
            len = hardware.read(mFd, loadbuf, readNum);
            if (loadbuf[9] == 0x00 && loadbuf[0] == (byte) 0xef && loadbuf[1] == 0x01) {
                LogsUtil.d(TAG, "****UpChar successfully!\n");
                StringBuilder infobBuilder = new StringBuilder();
                infobBuilder.append(ArrayUtils.bytes2HexString(loadbuf));
                //LogsUtil.d(TAG, "==========>to sever info:" + infobBuilder.toString());
                //LogsUtil.d(TAG, "==========>to sever info2:" + infobBuilder.toString().substring(92));
                String sendInfo = (infobBuilder.toString()).substring(24);
                //LogsUtil.d(TAG, "==========>send info:" + sendInfo);
                fManager.addFingerData(1, sendInfo);
                // 开始将文件保存在手持中
                writeDataToFile(id, loadbuf);
            } else {
                LogsUtil.d(TAG, "UpChar failed!\n");
                return;
            }
        } else {
            LogsUtil.d(TAG, "UpChar failed!\n");
            return;
        }
            /*} else {
                LogsUtil.d(TAG, "LoadChar failed!\n");
				return;
			}
		} else {
			LogsUtil.d(TAG, "LoadChar failed!\n");
			return;
		}*/
    }


    private void writeDataToFile(int id, byte[] loadbuf) {
        String path = "/data/data/com.fanfull.fff/files/";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
            LogsUtil.v(TAG, "make dir");
        }
        try {
            // 打开一个随机访问文件流，按读写方式
            RandomAccessFile randomFile = new RandomAccessFile(path + id
                    + ".dat", "rw");
            // 文件长度，字节数
            long fileLength = randomFile.length();
            // 将写文件指针移到文件尾。
            randomFile.seek(fileLength);
            randomFile.write(loadbuf);
            randomFile.close();
        } catch (IOException e) {

            LogsUtil.e(TAG + "  data文件失败");
            e.printStackTrace();
        }
    }

    private void upFingerDataToSever(int id) {
        //byte[] upchar = {(byte) 0xEF, 0x01, (byte) 0xFF, (byte) 0xFF,
        //    (byte) 0xFF, (byte) 0xFF, 0x01, 0x00, 0x04, 0x08, 0x01, 0x00,
        //  0x0E};
        StringBuilder infobBuilder = new StringBuilder();
        while ((len = hardware.read(mFd, buf, 24)) > 0)
            ;// clear buf
        len = hardware.write(mFd, upchar);
        if (hardware.select(mFd, 1, 0) == 1) {
            buf[0] = 0x00;
            int rsize = 1204;//568
            byte[] rbuf = new byte[rsize];
            len = hardware.read(mFd, rbuf, rsize);
            if (rbuf[9] == 0x00 && rbuf[0] == (byte) 0xef && rbuf[1] == 0x01) {
                LogsUtil.d(TAG, "UpChar successfully!\n");
                int size = 139;
                byte[] check = new byte[2];
                int index1 = 0;
                for (int i = 0; i < 4; i++) {
                    byte[] loadbuf = new byte[size];
                    index1 = 12 + i * size;
                    System.arraycopy(rbuf, index1, loadbuf, 0, 139);
                    byte[] b = new byte[2];
                    System.arraycopy(loadbuf, 7, b, 0, 2);
                    String lenStr = ArrayUtils.bytes2HexString(b);
                    len = Integer.parseInt(lenStr, 16);
                    LogsUtil.i(TAG, "len:" + len);
                    String result = (ArrayUtils.bytes2HexString(loadbuf)).substring(18, len * 2 + 14);
                    infobBuilder.append(result);
                }
                LogsUtil.d(TAG, "==========>Send to sever info:" + infobBuilder.toString());
                fManager.addFingerData(1, infobBuilder.toString());
            } else {
                LogsUtil.d(TAG, "UpChar failed!\n");
            }

        } else {
            LogsUtil.d(TAG, "UpChar failed!\n");
            return;
        }

    }

    /**
     * 根据指定指纹id加载对应的ID
     *
     * @param id
     * @return byte[] 数据
     */
    private byte[] getLoadcharCmd(int id) {
        byte fingerNumber[] = intToBytes(id);
        byte[] loadchar = {(byte) 0xEF, 0x01, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, 0x01, 0x00, 0x06, 0x07, 0x01, 0x00,
                0x00, 0x00, 0x00};
        loadchar[11] = fingerNumber[0];
        loadchar[12] = fingerNumber[1];
        fingerNumber = getDownCheckNumber(loadchar);
        loadchar[13] = fingerNumber[0];
        loadchar[14] = fingerNumber[1];
        LogsUtil.s("PS_getLoadcharCmd:" + ArrayUtils.bytes2HexString(loadchar));
        return loadchar;
    }

    public byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 4];
        int n = 0;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }

    public void getFingerInfo() {
        RandomAccessFile raf;
        byte loadbuf[] = new byte[64];
        try {
            raf = new RandomAccessFile(
                    "/data/data/com.fanfull.fff/files/1.dat", "rw");
            for (int i = 0; i < 8; i++) {
                raf.read(loadbuf);
            }

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    /**
     * 将指纹特征模板数据，下载传入指纹库中。
     *
     * @return
     */
    public boolean downLoadChar(Context context, int fingerID, String k,
                                String fingerInfo) {
        boolean flag = false;
        fingerInfo = StringUtil.fixFingerInfo(fingerInfo);
        LogsUtil.i(TAG, "fingerID:" + fingerID + "::fingerInfo:" + fingerInfo);
        byte[] downchar = {(byte) 0xEF, 0x01, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, 0x01, 0x00, 0x04, 0x09, 0x01, 0x00,
                0x0F};
        byte[] storechar = {(byte) 0xEF, 0x01, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, 0x01, 0x00, 0x06, 0x06, 0x01, 0x00,
                0x01, 0x00, 0x0F};
        byte loadbuf[] = new byte[64];
        byte ldbuf[] = new byte[128];
        for (int x = 0; x < 3; x++) {
            if (flag)
                break;
            while ((len = hardware.read(mFd, buf, 24)) > 0) ;// clear buf
            LogsUtil.d(TAG, "==>PS_DownChar :" + ArrayUtils.bytes2HexString(downchar));
            len = hardware.write(mFd, downchar);
            if (hardware.select(mFd, 1, 0) == 1) {
                //***
                len = hardware.read(mFd, buf, 12);
                LogsUtil.d(TAG, "==>read buf:" + ArrayUtils.bytes2HexString(buf));
                if (buf[9] == 0x00 && buf[0] == (byte) 0xef && buf[1] == 0x01) {
                    LogsUtil.d(TAG, "==>PS_DownChar Success");
                    for (int i = 0; i < 4; i++) {
                        loadbuf = ArrayUtils.hexString2Bytes(fingerInfo.substring(
                                i * (fingerInfo.length() / 4), (i + 1)
                                        * (fingerInfo.length() / 4)));
                        if (i != 3) {
                            ldbuf = getdownData(loadbuf, 139, false);
                        } else {
                            ldbuf = getdownData(loadbuf, 139, true);
                        }
                        LogsUtil.d(TAG, "==>write buf:" + ArrayUtils.bytes2HexString(ldbuf));
                        len = hardware.write(mFd, ldbuf);
                        LogsUtil.i(TAG, "==>write packet len:" + len);
                    }
                    SystemClock.sleep(100);
                    for (int y = 0; y < 3; y++) {
                        // 开始向Flash中存储模板
                        byte fingerNumber[] = intToBytes(fingerID);
                        storechar[10] = (byte) 0x01;
                        storechar[11] = fingerNumber[0];
                        storechar[12] = fingerNumber[1];
                        fingerNumber = getDownCheckNumber(storechar);
                        storechar[13] = fingerNumber[0];
                        storechar[14] = fingerNumber[1];
                        //***
                        while ((len = hardware.read(mFd, buf, 24)) > 0) ;// clear buf
                        LogsUtil.d(TAG, "==>PS_StoreChar :" + ArrayUtils.bytes2HexString(storechar));
                        len = hardware.write(mFd, storechar);
                        if (len < 1) {
                            LogsUtil.d(TAG, "write error");
                        } else {
                            LogsUtil.d(TAG, "dowload len=" + len);
                        }
                        SystemClock.sleep(100);
                        if (hardware.select(mFd, 1, 0) == 1) {
                            //***
                            len = hardware.read(mFd, buf, 12);
                            LogsUtil.v(TAG, "=========>buf:" + ArrayUtils.bytes2HexString(buf));
                            if (buf[9] == 0x00 && buf[0] == (byte) 0xef
                                    && buf[1] == 0x01) {
                                LogsUtil.v(TAG, "PS_StoreChar Success");
                                LogsUtil.v(TAG, "PS_StoreChar Success finger 十进制 ID : " + fingerID + "\n");
                                // upChar(fingerID);
                                flag = true;
                                break;
                            } else {
                                LogsUtil.v(TAG, "PS_StoreChar Failed\n");
                                continue;
                            }
                        } else {
                            LogsUtil.v(TAG, "PS_StoreChar no data\n");
                        }
                    }
                } else {
                    LogsUtil.d(TAG, "==>PS_DownChar Failed\n");
                    continue;
                }
            }
        }
        return flag;
    }

    /**
     * 得到需要写入指纹库中的字节数据
     *
     * @param data  内容数据，不含包头和地址
     * @param n     长度
     * @param isEnd 是否为结束数据，如果为结束数据标识为08 非结束则为02
     * @return 完整的一条写入指纹库的数据命令，包含包头，数据，校验和等。
     */
    private byte[] getdownData(byte data[], int n, boolean isEnd) {

        byte ldbuf[] = new byte[n];
        ldbuf[0] = (byte) 0xEF;
        ldbuf[1] = (byte) 0x01;
        ldbuf[2] = (byte) 0xFF;
        ldbuf[3] = (byte) 0xFF;
        ldbuf[4] = (byte) 0xFF;
        ldbuf[5] = (byte) 0xFF;
        ldbuf[7] = (byte) 0x00;
        ldbuf[8] = (byte) 0x82;
        if (!isEnd) {
            ldbuf[6] = (byte) 0x02;
        } else {
            ldbuf[6] = (byte) 0x08;
        }
        // 拷贝内容数据到命令中
        for (int i = 0; i < 128; i++) {
            ldbuf[i + 9] = data[i];
        }
        // 计算校验和
        byte cc[] = new byte[2];
        cc = getDownCheckNumber2(ldbuf);
        ldbuf[ldbuf.length - 2] = cc[0];
        ldbuf[ldbuf.length - 1] = cc[1];

        return ldbuf;
    }


    /**
     * 得到数据的校验和 在计算校验和时，先把字节数据转换成整型值，相加，最后再转化成字节数组 这么做的目的时，
     * 对于字节数据的溢出不方便处理
     *
     * @param ldbuf
     * @return 返回两个字节的校验和字节数组
     */
    private byte[] getDownCheckNumber(byte[] ldbuf) {

        int a = 0;
        for (int i = 6; i < ldbuf.length - 2; i++) {//- 2
            a += byteToInt2(ldbuf[i]);
        }
        return intToBytes(a);
    }

    private byte[] getDownCheckNumber2(byte[] ldbuf) {

        int a = 0;
        for (int i = 6; i < ldbuf.length; i++) {//- 2
            a += byteToInt2(ldbuf[i]);
        }
        return intToBytes(a);
    }


    /**
     * 往指纹库中写记事本数据 大小为32个字节
     */
    public void writeTextFinger() {
        byte[] buf = new byte[12];// 接收命令缓存
        len = hardware.write(mFd, writeText);
        SystemClock.sleep(100);
        if (hardware.select(mFd, 0, 500) == 0) {
            return;
        }
        //***
        len = hardware.read(mFd, buf, 12);
        if (buf[9] == 0x00 && buf[0] == (byte) 0xef && buf[1] == 0x01) {
            LogsUtil.d(TAG, "writeText successfully!\n");
            return;
        }
    }

    /**
     * 阅读指纹数据中的记事本数据 长度为32个字节
     */
    public void readTextFinger() {
        byte[] buf = new byte[48];// 接收命令缓存
        len = hardware.write(mFd, readText);
        SystemClock.sleep(100);
        if (hardware.select(mFd, 0, 500) == 0) {
            return;
        }
        //***
        len = hardware.read(mFd, buf, 48);
        LogsUtil.d("PS_readText:" + ArrayUtils.bytes2HexString(buf));
        if (buf[9] == 0x00 && buf[0] == (byte) 0xef && buf[1] == 0x01) {
            LogsUtil.d(TAG, "readText successfully!\n");
            return;
        }
    }

    /**
     * 指纹搜索任务，当搜索到指纹，回调该指纹编号 当本地库中，没有指纹，则回调没有数据 当串口没有数据，
     * 或者传感器上没有手指，则回调出错
     *
     * @author zyp
     */
    class FingerPrintTask implements Runnable {
        private boolean stop = false;
        private boolean close = false;
        private int getFingerId = -1;

        public void close(boolean close) {
            this.close = close;
        }

        /**
         * 停止线程
         */
        public void stop(boolean stop) {
            this.stop = stop;
            if (this.stop) {
                mFingerPrintTask = null;
            }
        }

        public boolean isStop() {
            return stop;
        }

        @Override
        public void run() {
            try {

                LogsUtil.w(TAG, FingerPrintTask.class.getSimpleName() + " run");
                //stop = false;
                //close = false;
                while (!stop) {
                    getFingerId = searchFingerOnce(400);
                    Log.d("fc", "search...:" + getFingerId);
                    if (0 <= getFingerId) {
                        fManager.getLocalFingerSucess(getFingerId);
                        Log.d("fc", "===============>>search...sucess:" + getFingerId);
                        //break;
                    } else if (-3 == getFingerId) {
                        fManager.getLocalFingerNoData();
                    } else {
                        // 没有手指，或者串口没有数据
                        //fManager.getLocalFingerError();
                    }
                    if (close) {
                        FingerPrint.this.closeFinger();
                        break;
                    }
                }
                //fManager.stopSearchFinger(true);
                //stop = true;
                LogsUtil.w(TAG, FingerPrintTask.class.getSimpleName() + " running finish");
            } catch (Exception e) {
                LogsUtil.e(TAG, "e:" + e);
            }
        }
    }
}
