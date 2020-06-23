package com.fanfull.libhard.barcode;

import com.apkfuns.logutils.LogUtils;
import com.hardware.Hardware;

import org.orsoul.baselib.util.ArrayUtils;

/**
 * 老锁二维码 解密工具
 */
public abstract class BarcodeUtil {
    public static final int ORIGINAL_DATA_LEN = 23;
    public static final int DECODE_DATA_LEN = 38;

    public static byte[] decodeBarcode(byte[] data, int length) {
        //jQqgP/-Qt(<aq1UY'8d*
        //053101001000002010109031317002911101
        //mHKb`#l7r=<aq1UY'8d*
        //053101001000009010109031317002911101
        //+^JRwAk7#)<aq1UY'8d*
        //053101001000006010109031317002911101
        if (ORIGINAL_DATA_LEN == length) {// 读到正确的数据
            byte[] barcodeBuff = new byte[DECODE_DATA_LEN];
            System.arraycopy(data, 0, barcodeBuff, 0, DECODE_DATA_LEN);
            Hardware.decodeBarcode(barcodeBuff);// 解码
            LogUtils.d("barcode解码后hex:"
                               + ArrayUtils.bytes2HexString(barcodeBuff));
            LogUtils.d("barcode:%s", new String(barcodeBuff));
            return barcodeBuff;
        }
        return null;
    }

    public static byte[] decodeBarcode(byte[] data) {
        if (data == null) {
            return null;
        }
        return decodeBarcode(data, data.length);
    }
}
