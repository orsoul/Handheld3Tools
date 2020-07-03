package com.rd;

import android.os.SystemClock;
import android.util.Log;

import com.blankj.utilcode.util.Utils;
import com.finger.FingerPrint;
import com.finger.LogsUtil;
import com.halio.IRfidParam;
import com.halio.Rfid;
import com.io.CByteRecord;
import com.io.CTranslate;
import com.rd.barcodeScanTest.ScanApi;
import com.rd.barcodeScanTest.ScanApiFactory;
import com.rd.io.EMgpio;
import com.rd.io.Platform;
import com.rd.io.SerialPort;

import org.orsoul.baselib.util.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.TreeSet;

public class Hardware2 {

    private static String TAG = "FF_Hardware";
    private static Hardware2 mHardware = new Hardware2();

    static {
        EMgpio.GPIOInit();
    }

    public Hardware2() {

    }

    public static Hardware2 getInstance() {
        return mHardware;
    }

    public static void setHardware() {
        if (null != mHardware) {
            mHardware = null;
        }
    }

    /* oled */
    public int openGPIO() {
        return -1;
    }

    private CTranslate s_trans;

    private int keyOled = (int) 0x0;

    public int openSPI() {
        s_trans = new CTranslate(Utils.getApp());
        return 101;
    }

    public void oledShowString(int row, int back_color, int draw_color, String str) {

        CByteRecord record = s_trans.getString(str, 0, 0);
        byte[] bDataRotate = new byte[322];
        int Height = 0;
        Height = record.m_iHeight;
        if (record.m_iHeight > 16) {
            Height = 16;
        }


        for (int i = 0; i < Height; i++) {
            for (int j = 0; j < (record.m_iWidth); j++) {
                bDataRotate[20 * i + j] = record.m_bData[record.m_iWidth * i + j];
            }
        }

        byte[] bData = new byte[324];
        byte[] bRecData = new byte[324];
        int[] iRecLenth = new int[1];
        bData[0] = (byte) row;
        bData[1] = (byte) back_color;
        bData[2] = (byte) draw_color;
        for (int i = 0; i < 320; i++) {
            bData[i + 3] = (byte) (bDataRotate[i] & 0xff);
        }
        Rfid.cmdTrancevice(0x2000, bData, 324, bRecData, iRecLenth);
    }

    public void OledShowAll(int back_color, int draw_color, String str) {

        byte[] bData = new byte[2564];
        byte[] bRecData = new byte[2564];
        int[] iRecLenth = new int[1];

        if (keyOled == 0) {
            keyOled = 1;
            bData[0] = (byte) 0;
            bData[1] = (byte) back_color;
            bData[2] = (byte) draw_color;
            for (int i = 0; i < 2560; i++) {
                bData[i + 3] = (byte) back_color;
            }
        } else {
            keyOled = 0;
            bData[0] = (byte) 0;
            int BLACK = (int) 0x00;
            bData[1] = (byte) BLACK;
            int WHITE = (int) 0xff;
            bData[2] = (byte) WHITE;
            for (int i = 0; i < 2560; i++) {
                bData[i + 3] = (byte) back_color;
            }
        }
        Rfid.cmdTrancevice(0x2002, bData, 2564, bRecData, iRecLenth);
    }

    public int transferString(int fd_spi, char len, char XSpos, char YSpos, char CharWidth, char CharHeight,
                              long BackColor, long TextColor, byte[] buffer) {
        oledShowString(XSpos, (int) 0x00, (int) 0xFF, new String(buffer));
        return 103;
    }

    public int clearScreen(int fd_spi, char XSpos, char XCpos, char YSpos, char YCpos) {
        return -1;
    }

    public int fillScreen(int fd_spi, char XSpos, char XCpos, char YSpos, char YCpos, long FillColor) {
        SystemClock.sleep(50);
        OledShowAll((int) 0x00, (int) 0x00, "");
        return 102;
    }

    public int copyRect(int fd_spi, char XSpos, char XCpos, char YSpos, char YCpos, char NewXSpos, char NewYSpos) {
        return -1;
    }

    public int drawRect(int fd_spi, char XSpos, char XCpos, char YSpos, char YCpos, long LineColor, long FillColor) {
        return -1;
    }

    public int drawLine(int fd_spi, char XSpos, char XCpos, char YSpos, char YCpos, long LineColor) {
        return -1;
    }

    /* I2C */
    public int openI2CDevice() {
        return -1;
    }

    public int writeByteDataToI2C(int fd, int pos, byte byteData) {
        return -1;
    }

    public int readByteDataFromI2C(int fd, int pos, byte[] data) {
        return -1;
    }

    public int sleep(int fd, int cmd) {
        return -1;
    }

    public int setGPIO(int state, int port) {
        if (scanApi != null) {
            if (state == 0 && port == 1) {
                Log.i(TAG, "do scanAsync");
                isScanStop = false;
                scanApi.doScan();
                return 1021;
            } else if (state == 1 && port == 1) {
                Log.i(TAG, "cancel scanAsync");
                isScanStop = true;
                scanApi.cancelScan();
                return 1023;
            }
        }
        return -1;
    }

    public int reset() {
        return -1;
    }

    public int closeGPIO() {
        return -1;
    }

    public int closeSPI() {
        Log.i(TAG, "exit the oled port");
        return -1;
    }

    public int openSerialPort(String devName, long baud, int dataBits, int stopBits) {
        return -1;
    }

    public static SerialPort serialPort;
    public static InputStream mInputStream;
    public static OutputStream mOutputStream;
    public static TreeSet<Integer> ports = new TreeSet<Integer>();
    public static boolean isExitDaemon = false;
    public static byte[] bufferCache = new byte[4096];
    public static int readLength = 0;
    public static int currentFd = -1;

    // "/dev/s3c2410_serial2" 超高频
    // "/dev/ttyUSB0" 高频
    // "/dev/ttyUSB1" 指纹
    private static ScanApi scanApi = null;
    private static ScanApiFactory scanApiFactory = null;
    private static byte[] barCodeData;
    private static boolean isScanStop = false;
    // "/dev/s3c2410_serial3" 二维码

    public int openSerialPortTest(String devName, int baud, int dataBits, int stopBits) {
        LogsUtil.d(TAG, "openPort ... ");
        int fd = -1;
        if ("/dev/ttyUSB0".equals(devName)) {
            if (initHF()) {
                fd = 53;
            }
            return fd;
        } else if ("/dev/ttyUSB1".equals(devName)) {
            fd = 54;
        } else if ("/dev/s3c2410_serial2".equals(devName)) {
            fd = 55;
        } else if ("/dev/s3c2410_serial3".equals(devName)) {
            fd = 56;
            scanApiFactory = new ScanApiFactory(1);
            if (scanApi != null) {
                scanApi.deInit();
            }
            scanApi = scanApiFactory.createApi(1);
            if (scanApi != null) {
                scanApi.setDecodeCallback(new CallBack());
                scanApi.init(Utils.getApp());

                scanApi.powerOn();
            }
            return fd;
        }
        if (openPort(fd, "/dev/ttyMT0", 115200, 0)) {
            ports.add(fd);
            LogsUtil.d(TAG, "open port ... ok");
            return fd;
        }
        return -1;
    }

    public boolean initHF() {
        Rfid.closeCommPort();
        Rfid.openPort((byte) 2, (int) 115200);
        Platform.initIO();
        Platform.SetGpioMode(6, 0);
        Platform.SetGpioOutput(6);
        Platform.SetGpioDataLow(6);

        try {
            Thread.sleep(50);
        } catch (InterruptedException ie) {
        }
        Platform.SetGpioDataHigh(6);
        try {
            Thread.sleep(120);
        } catch (InterruptedException ie) {
        }
        Log.e("test", "zhi: " + Rfid.notifyBootStart());
        try {
            Thread.sleep(200);
        } catch (InterruptedException ie) {
        }

        byte[] bVersion = new byte[16];
        int iVersionLength = Rfid.getHwVersion(bVersion);
        if (iVersionLength > 0) {
            return true;
        } else {
            return false;
        }

    }

    private boolean openPort(int fd, String devName, int baud, int flags) {
        Log.i(TAG, "open port devName=" + devName + " fd=" + fd);
        if (ports.isEmpty() || serialPort == null) {
            try {
                serialPort = new SerialPort(new File(devName), baud, flags);
                mInputStream = serialPort.getInputStream();
                mOutputStream = serialPort.getOutputStream();
                isExitDaemon = false;
                new Thread(new ReadDataDaemon()).start();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (SecurityException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public int write(int fd, byte[] data) {
        LogsUtil.d("hd", "w fd:" + fd);
        if (currentFd == fd) {

        } else if (fd == 53) {
            //			HF();
            currentFd = 53;
        } else if (fd == 54) {
            FP();
        } else if (fd == 55) {
            UHF();
        } else if (fd == 56) {
            //二维码
        }
        if (currentFd == 53) {
            return writeRFID(data);
        }
        if (mOutputStream != null) {
            try {
                mOutputStream.write(data);
                mOutputStream.flush();
                readLength = 0;
                Arrays.fill(bufferCache, (byte) 0);
                Log.i("hd", "write the fd = " + fd + " data = " + ArrayUtils.bytes2HexString(data));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            exitPort();
        }
        return 3721;
    }

    boolean hasSelect = false;

    public int read(int fd, byte[] buf, int len) {
        LogsUtil.d("hd", "r fd:" + fd);
        if (fd == 53) {
            if (hasSelect) {
                int length = -1;
                Log.i("hd", "read the rfidRet = " + (rfidRet == null));
                if (rfidRet != null) {
                    length = rfidRet.length;
                    System.arraycopy(rfidRet, 0, buf, 0, Math.min(len, length));
                }
                rfidRet = null;
                hasSelect = false;
                return length;
            }
        }
        if (fd == 56) {
            int length = -1;
            if (barCodeData != null) {
                length = barCodeData.length;
                System.arraycopy(barCodeData, 0, buf, 0, Math.min(len, length));
            }
            barCodeData = null;
            return length;
        }
        if (hasSelect) {
            System.arraycopy(bufferCache, 0, buf, 0, len);
            Log.i("hd", "read the data from cache= " + ArrayUtils.bytes2HexString(buf));
            hasSelect = false;
            return readLength;
        }
        return -1;
    }

    public int select(int fd, int sec, int usec) {
        LogsUtil.d("hd", "s fd:" + fd);
        if (fd == 56) {
            int i = 0;
            while (i < 100 && !isScanStop) {
                i++;
                SystemClock.sleep(100);
                if (barCodeData != null) {
                    return 1;
                }
            }
            return -1;
        } else if (fd == 54) {
            SystemClock.sleep(FingerPrint.fingerWaitTime);
        } else if (fd == 53) {

        } else {
            SystemClock.sleep(60);
        }

        hasSelect = true;
        return 1;
    }

    public void close(int fd) {
        ports.remove(fd);
        if (fd == 56) {
            if (scanApi != null) {
                scanApi.cancelScan();
                scanApi.powerOff();
                scanApi.deInit();
            }
            scanApi = null;
            scanApiFactory = null;
        } else if (ports.isEmpty()) {
            exitPort();
        }
    }

    public void DecodeBarcode(byte[] data) {
    }

    public String unimplementedStringFromJNI() {
        return "";
    }

    public static class ReadDataDaemon implements Runnable {

        @Override
        public void run() {

            try {
                while (!isExitDaemon) {
                    if (mInputStream != null) {
                        int available = mInputStream.available();
                        if (available > 0) {
                            Log.i(TAG, "read the data readLength= " + readLength + " available=" + available);
                            readLength += mInputStream.read(bufferCache, readLength, available);
                            byte[] a = new byte[readLength];
                            System.arraycopy(bufferCache, 0, a, 0, readLength);
                            Log.i(TAG, "read the data = " + ArrayUtils.bytes2HexString(a));
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                exitPort();
            }

            Log.i(TAG, "end read daemon");
        }
    }

    public static class CallBack implements ScanApi.DecodeCallback {

        @Override
        public void onDecodeComplete(int symbology, int length, byte[] data, ScanApi api) {
            if (length > 0) {
                barCodeData = data;
                Log.i(TAG, "scanAsync result=" + new String(data));
            }
        }

        @Override
        public void onEvent(int event, int info, byte[] data, ScanApi api) {

        }
    }

    public static void exitPort() {

        isExitDaemon = true;

        EMgpio.GPIOUnInit();

        if (mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch (IOException e) {
            }
            mOutputStream = null;
        }

        if (mInputStream != null) {
            try {
                mInputStream.close();
            } catch (IOException e) {
            }
            mInputStream = null;
        }

        if (serialPort != null) {
            serialPort.close();
            serialPort = null;
        }

        //		Rfid.closeCommPort();
        //
        //		Platform.SetGpioDataLow(6);
        Log.i(TAG, "exit the port");

    }

    private void UHF() {
        //Platform.SetGpioMode(59, 0);            //URXD3  MODE1
        //Platform.SetGpioMode(60, 0);            //UTXD3  MODE1
        //		Platform.SetGpioMode(57, 0);            //URXD2  MODE1
        //
        //		Platform.SetGpioOutput(59);                //URXD3   SWITCH B
        //Platform.SetGpioOutput(60);                //UTXD3   UHF_VCC enable
        //		Platform.SetGpioOutput(57);                //URXD2   SWITCH A
        //
        //		Platform.SetGpioDataHigh(60);
        //		Platform.SetGpioDataHigh(59);
        //		Platform.SetGpioDataHigh(57);

        Platform.SetGpioMode(64, 0);
        Platform.SetGpioMode(62, 0);

        Platform.SetGpioOutput(64);
        Platform.SetGpioOutput(62);

        Platform.SetGpioDataHigh(64);
        Platform.SetGpioDataLow(62);
        SystemClock.sleep(10);
        currentFd = 55;
        Log.i(TAG, "UHF MODE");
    }

    private void HF() {
        //		//模拟开关的使能脚SWITCH A  SWITCH B的GPIO设置成IO模式
        //		Platform.SetGpioMode(59, 0);            //URXD3  SWITCH B MODE1
        //		Platform.SetGpioMode(57, 0);            //URXD2  SWITCH A MODE1
        //		//UHF  HF 的电源使能脚设置成IO模式
        //		//Platform.SetGpioMode(60, 0);			//UTXD3 UHF_VCC   MODE1
        //		Platform.SetGpioMode(0, 0);                //HF  POWER
        //
        //		//模拟开关的使能脚SWITCH A  SWITCH B的GPIO设置成输出
        //		Platform.SetGpioOutput(59);                //URXD3   SWITCH B
        //		Platform.SetGpioOutput(57);                //URXD2   SWITCH A
        //
        //		//UHF  HF 的电源使能脚设置成输出
        //		//Platform.SetGpioOutput(60);						//UTXD3   UHF_VCC enable
        //		Platform.SetGpioOutput(0);              //GPIO0_ID HF VCC
        //
        //		//模拟开关的使能脚SWITCH B拉低
        //		Platform.SetGpioDataLow(59);            //URXD3   SWITCH B
        //		//模拟开关的使能脚SWITCH A拉高
        //		Platform.SetGpioDataHigh(57);
        //
        //		//HF 的电源使能脚设置成高电平输出，打开电源
        //		//Platform.SetGpioDataHigh(60);					    //UTXD3   UHF_VCC enable
        //		Platform.SetGpioDataHigh(0);            //GPIO0_ID HF VCC

        Platform.SetGpioMode(64, 0);
        Platform.SetGpioMode(62, 0);

        Platform.SetGpioOutput(64);
        Platform.SetGpioOutput(62);

        Platform.SetGpioDataLow(64);
        Platform.SetGpioDataHigh(62);

        SystemClock.sleep(100);
        currentFd = 53;

        Log.i(TAG, "HF MODE");
    }

    private void FP() {
        //		//模拟开关的使能脚SWITCH A  SWITCH B的GPIO设置成IO模式
        //		Platform.SetGpioMode(59, 0);            //URXD3  SWITCH B MODE1
        //		Platform.SetGpioMode(57, 0);            //URXD2  SWITCH A MODE1
        //		//UHF  HF 的电源使能脚设置成IO模式
        //		//Platform.SetGpioMode(60, 0);			//UTXD3 UHF_VCC   MODE1
        //		Platform.SetGpioMode(0, 0);                //HF  FP POWER
        //
        //		//模拟开关的使能脚SWITCH A  SWITCH B的GPIO设置成输出
        //		Platform.SetGpioOutput(59);                //URXD3   SWITCH B
        //		Platform.SetGpioOutput(57);                //URXD2   SWITCH A
        //
        //		//UHF  HF 的电源使能脚设置成输出
        //		//Platform.SetGpioOutput(60);						//UTXD3   UHF_VCC enable
        //		Platform.SetGpioOutput(0);              //GPIO0_ID HF FP  VCC
        //
        //		//模拟开关的使能脚SWITCH B拉低
        //		Platform.SetGpioDataLow(59);            //URXD3   SWITCH B
        //		//模拟开关的使能脚SWITCH A拉高
        //		Platform.SetGpioDataLow(57);
        //
        //		//HF 的电源使能脚设置成高电平输出，打开电源
        //		//Platform.SetGpioDataHigh(60);					    //UTXD3   UHF_VCC enable
        //		Platform.SetGpioDataHigh(0);            //GPIO0_ID HF FP VCC

        Platform.SetGpioMode(64, 0);
        Platform.SetGpioMode(62, 0);
        Platform.SetGpioOutput(64);
        Platform.SetGpioOutput(62);
        Platform.SetGpioDataHigh(64);
        Platform.SetGpioDataHigh(62);

        currentFd = 54;
        Log.i(TAG, "FP MODE");
    }

    //=================================新RFID 读写=================
    byte[] rfidRet;
    byte[] CMD_WAKEUP = new byte[]{(byte) 0x55, (byte) 0x55,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF,
            (byte) 0x03, (byte) 0xFD, (byte) 0xD4, (byte) 0x14, (byte) 0x01,
            (byte) 0x17, (byte) 0x00};// RF
    byte[] CMD_FINDCARD = new byte[]{(byte) 0x00, (byte) 0x00,
            (byte) 0xff, (byte) 0x04, (byte) 0xfc, (byte) 0xd4, (byte) 0x4a,
            (byte) 0x01, (byte) 0x00, (byte) 0xe1, (byte) 0x00};// 寻卡
    byte[] CMD_NFC_READ = new byte[]{(byte) 0x00, (byte) 0x00,
            (byte) 0xFF, (byte) 0x05, (byte) 0xFB, (byte) 0xD4, (byte) 0x40,
            (byte) 0x01, (byte) 0x30, (byte) 0x06, (byte) 0xB5, (byte) 0x00};//读NFC命令
    byte[] CMD_NFC_WRITE = new byte[]{(byte) 0x00, (byte) 0x00,
            (byte) 0xFF, (byte) 0x09, (byte) 0xF7, (byte) 0xD4, (byte) 0x40,
            (byte) 0x01, (byte) 0xA2, (byte) 0x07, (byte) 0x44, (byte) 0x33,
            (byte) 0x22, (byte) 0x11, (byte) 0x98, (byte) 0x00};//写NFC命令

    public void initFirst6Data(byte[] buf) {
        buf[0] = (byte) 0x00;
        buf[1] = (byte) 0x00;
        buf[2] = (byte) 0xff;
        buf[3] = (byte) 0x00;
        buf[4] = (byte) 0xff;
        buf[5] = (byte) 0x00;
    }

    public int writeRFID(byte[] buf) {
        Log.i(TAG, "writeRFID=" + ArrayUtils.bytes2HexString(buf));
        if (Arrays.equals(buf, CMD_WAKEUP)) {
            Log.i(TAG, "valueRFID=" + ArrayUtils.bytes2HexString(CMD_WAKEUP));
            rfidRet = new byte[15];
            initFirst6Data(rfidRet);
            rfidRet[6] = (byte) 0x00;
            rfidRet[7] = (byte) 0x00;
            rfidRet[8] = (byte) 0xff;
            rfidRet[9] = (byte) 0x02;
            rfidRet[10] = (byte) 0xfe;
            rfidRet[11] = (byte) 0xd5;
            rfidRet[12] = (byte) 0x15;
            rfidRet[13] = (byte) 0x16;
            rfidRet[14] = (byte) 0x00;
            return 3721;
        } else if (Arrays.equals(buf, CMD_FINDCARD)) {
            Log.i(TAG, "valueRFID=" + ArrayUtils.bytes2HexString(CMD_WAKEUP));
            byte[] ret = findNFC();
            if (ret != null) {
                rfidRet = new byte[28];
                initFirst6Data(rfidRet);
                rfidRet[18] = 0x07;
                rfidRet[19] = ret[0];
                rfidRet[20] = ret[1];
                rfidRet[21] = ret[2];
                rfidRet[22] = ret[3];
                rfidRet[23] = ret[4];
                rfidRet[24] = ret[5];
                rfidRet[25] = ret[6];
                return 3721;
            }
            ret = findM1();
            if (ret != null) {
                rfidRet = new byte[25];
                initFirst6Data(rfidRet);
                rfidRet[18] = 0x04;
                rfidRet[19] = ret[3];
                rfidRet[20] = ret[2];
                rfidRet[21] = ret[1];
                rfidRet[22] = ret[0];
                return 3721;
            }
            return -1;

        } else if (checkCmd(CMD_NFC_READ, buf)) {
            byte pos = buf[9];
            rfidRet = new byte[30];
            initFirst6Data(rfidRet);
            rfidRet[12] = 0x41;
            rfidRet[13] = 0x00;
            byte[] ret = readNFCData(pos);
            if (ret == null) {
                return -1;
            }
            for (int i = 0; i < ret.length; i++) {
                rfidRet[14 + i] = ret[i];
            }
            return 3721;
        } else if (checkCmd(CMD_NFC_WRITE, buf)) {
            byte pos = buf[9];
            rfidRet = new byte[32];
            initFirst6Data(rfidRet);
            if (writeNFCData(pos, new byte[]{buf[10], buf[11], buf[12], buf[13]})) {
                rfidRet[6] = (byte) 0x00;
                rfidRet[7] = (byte) 0x00;
                rfidRet[8] = (byte) 0xff;
                rfidRet[9] = (byte) 0x03;
                rfidRet[10] = (byte) 0xfd;
                rfidRet[11] = (byte) 0xd5;
                rfidRet[12] = (byte) 0x41;
                rfidRet[13] = (byte) 0x00;
                rfidRet[14] = (byte) 0xea;
                rfidRet[15] = (byte) 0x00;
                return 3721;
            }
            return -1;
        } else {
            return -1;
        }
    }

    public byte[] findM1() {
        if (!Rfid.PcdConfigISOType((byte) 'A')) {
            return null;
        }

        byte[] tagType = new byte[2];
        if (!Rfid.PcdRequest((byte) 0x52, tagType)) {
            return null;
        }
        byte[] cardNumber = new byte[4];
        if (!Rfid.PcdAnticoll(cardNumber)) {
            return null;
        }
        return cardNumber;
    }

    public byte[] findNFC() {
        if (!Rfid.PcdConfigISOType((byte) 'A')) {
            return null;
        }

        byte[] tagType = new byte[2];
        if (!Rfid.PcdRequest(IRfidParam.CARD_ALL, tagType)) {
            return null;
        }

        byte[] cardNumber = new byte[7];
        if (!Rfid.ULPcdAnticoll(cardNumber)) {
            return null;
        }
        return cardNumber;
    }

    public byte[] readNFCData(byte sa) {
        if (!Rfid.PcdConfigISOType((byte) 'A')) {
            Log.i(TAG, "readNFCData PcdConfigISOType failed");
            return null;
        }

        byte[] tagType = new byte[2];
        if (!Rfid.PcdRequest(IRfidParam.CARD_ALL, tagType)) {
            Log.i(TAG, "readNFCData PcdRequest failed");
            return null;
        }
        byte[] cardNumber = new byte[7];
        if (!Rfid.ULPcdAnticoll(cardNumber)) {
            Log.i(TAG, "readNFCData ULPcdAnticoll failed");
            return null;
        }
        byte[] blockData = new byte[16];
        for (int i = 0; i < 4; i++) {
            byte[] b = new byte[4];
            if (!Rfid.ULPcdRead((byte) (i + sa), b)) {
                Log.i(TAG, "readNFCData ULPcdRead failed");
                return null;
            }
            for (int j = 0; j < 4; j++) {
                blockData[i * 4 + j] = b[j];
            }
        }
        return blockData;
    }

    public boolean writeNFCData(byte sa, byte[] data) {
        if (!Rfid.PcdConfigISOType((byte) 'A')) {
            Log.i(TAG, "writeNFCData PcdConfigISOType failed");
            return false;
        }

        byte[] tagType = new byte[2];
        if (!Rfid.PcdRequest(IRfidParam.CARD_ALL, tagType)) {
            Log.i(TAG, "writeNFCData PcdRequest failed");
            return false;
        }
        byte[] cardNumber = new byte[7];
        if (!Rfid.ULPcdAnticoll(cardNumber)) {
            Log.i(TAG, "writeNFCData ULPcdAnticoll failed");
            return false;
        }
        if (!Rfid.ULPcdWrite(sa, data)) {
            Log.i(TAG, "writeNFCData ULPcdWrite failed");
            return false;
        }
        return true;
    }

    public boolean checkCmd(byte[] buf, byte[] data) {
        for (int i = 0; i < 9; i++) {
            if (buf[i] != data[i]) {
                return false;
            }
        }
        return true;
    }

}