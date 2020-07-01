package com.fanfull.libhard.nfc;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.libhard.gpio.impl.GpioController;
import com.fanfull.libhard.serialport.impl.SerialPortController;
import com.halio.IRfidParam;
import com.halio.Rfid;
import com.rd.io.Platform;

import org.orsoul.baselib.util.ArrayUtils;

import java.util.concurrent.Executors;

public class RfidOperationRd extends AbsRfidOperation {
    private final String SERIAL_PORT_PATH = "/dev/ttyMT2";
    private final int BUADRATE = 115200;
    private SerialPortController serialPortController;
    //    private ExecutorService executor;

    public RfidOperationRd() {

    }

    @Override
    public boolean open() throws SecurityException {
        Rfid.closeCommPort();
        Rfid.openPort((byte) 2, 115200);
        Platform.initIO();
        Platform.SetGpioMode(6, 0);
        Platform.SetGpioOutput(6);
        Platform.SetGpioDataLow(6);

        try {
            Thread.sleep(50);
        } catch (java.lang.InterruptedException ie) {
        }
        Platform.SetGpioDataHigh(6);
        try {
            Thread.sleep(120);
        } catch (java.lang.InterruptedException ie) {
        }
        Rfid.notifyBootStart();
        try {
            Thread.sleep(200);
        } catch (java.lang.InterruptedException ie) {
        }

        byte[] buff = new byte[16];
        int len = Rfid.getHwVersion(buff);
        LogUtils.d("%s:%s", len, ArrayUtils.bytes2HexString(buff, 0, len));
        if (len > 0) {
            LogUtils.d("HwVersion:%s", new String(buff, 0, len).trim());
            isOpen = true;
            if (nfcListener != null) {
                nfcListener.onOpen();
            }
            executor = Executors.newSingleThreadExecutor();
            return true;
        } else {
            return false;
        }

    }

    @Override
    public void release() {
        Rfid.closeCommPort();
    }

    private boolean setGpioUhfMode() {

        boolean reVal = GpioController.getInstance().init()
                && GpioController.getInstance().setMode(64, 0)
                && GpioController.getInstance().setMode(62, 0)
                && GpioController.getInstance().setIO(64, false)
                && GpioController.getInstance().setIO(62, false)
                && GpioController.getInstance().set(64, true)
                && GpioController.getInstance().set(62, false);

        return reVal;
    }


    private boolean config() {
        if (!Rfid.PcdConfigISOType(IRfidParam.ISOTYPE_14443A)) {
            LogUtils.w("PcdConfigISOType() failed ");
            return false;
        }

        byte[] tagType = new byte[2];
        boolean reVal = Rfid.PcdRequest(IRfidParam.CARD_ALL, tagType);
        LogUtils.d("%s:%s", reVal, ArrayUtils.bytes2HexString(tagType));
        return reVal;
    }

    private byte[] findCard(boolean isFindNfc) {
        if (isScanning()) {
            return null;
        } else {
            setScanning(true);
        }

        if (!config()) {
            setScanning(false);
            return null;
        }

        byte[] uid;
        boolean findSuccess;
        if (isFindNfc) {
            uid = new byte[7];
            findSuccess = Rfid.ULPcdAnticoll(uid);
        } else {
            uid = new byte[4];
            findSuccess = Rfid.PcdAnticoll(uid);
        }
        if (findSuccess) {
            ArrayUtils.reverse(uid);
            setScanning(false);
            return uid;
        }
        setScanning(false);
        return null;
    }

    @Override
    public byte[] findNfc() {
        //        if (!config()) {
        //            return null;
        //        }
        //
        //        byte[] cardNumber = new byte[7];
        //        if (!Rfid.ULPcdAnticoll(cardNumber)) {
        //            return null;
        //        }
        return findCard(true);
    }

    @Override
    public byte[] findM1() {
        //        if (isScanning()) {
        //            return null;
        //        } else {
        //            setScanning(true);
        //        }
        //
        //        if (!config()) {
        //            setScanning(false);
        //            return null;
        //        }
        //
        //        byte[] uid = new byte[4];
        //        if (Rfid.PcdAnticoll(uid)) {
        //            ArrayUtils.reverse(uid);
        //            setScanning(false);
        //            return uid;
        //        }
        //        setScanning(false);
        return findCard(false);
    }

    @Override
    public boolean readNfc(int sa, byte[] buff, int len) {
        if (sa < 0 || buff == null || len < 1 || buff.length < len) {
            LogUtils.w("check failed sa:%s buff:%s len:%s", sa, buff, len);
            return false;
        }

        byte[] uid = findNfc();
        if (uid == null) {
            LogUtils.w("findNfc failed");
            return false;
        }


        byte[] oneWord = new byte[4];
        int wordDataLen = (len - 1) / 4 + 1;
        for (int i = 0; i < wordDataLen; i++) {
            byte newStart = (byte) (i + sa);
            if (!Rfid.ULPcdRead(newStart, oneWord)) {
                LogUtils.w("第%s次 读nfc addr[%s]失败", i, newStart);
                return false;
            }
            int destPos = i * 4;
            int copyLen = len - destPos;
            if (4 < copyLen) {
                copyLen = 4;
            }
            System.arraycopy(oneWord, 0, buff, destPos, copyLen);
        }
        return true;
    }

    @Override
    public boolean readNfc(int sa, byte[] buff) {
        if (buff == null) {
            return false;
        }
        return readNfc(sa, buff, buff.length);
    }


}
