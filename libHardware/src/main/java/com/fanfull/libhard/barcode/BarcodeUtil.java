package com.fanfull.libhard.barcode;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.libjava.util.BytesUtil;
import com.hardware.Hardware;

import java.util.Arrays;

/**
 * 老锁二维码 解密工具
 */
public final class BarcodeUtil {
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
            System.arraycopy(data, 0, barcodeBuff, 0, length);
            Hardware.decodeBarcode(barcodeBuff);// 解码
          String s = BytesUtil.bytes2HexString(barcodeBuff);
          LogUtils.wtf("barcode:%s", s);
          //LogUtils.d("barcode解码后hex:"
          //                   + ArrayUtils.bytes2HexString(barcodeBuff));
          LogUtils.wtf("barcode:%s", new String(barcodeBuff));
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

    /**
     * 将barcode截成 3个 16byte数组,准备 写入 M1卡的 4,5,6三个块区
     * @param barcodeBuff
     * @return
     */
    public static byte[][] get3Data(byte[] barcodeBuff) {
        if ((null == barcodeBuff) || (barcodeBuff.length < 38)) {
            return null;
        }
        byte[][] reVal = new byte[3][];
        reVal[0] = Arrays.copyOfRange(barcodeBuff, 0, 16);// 0~15
        reVal[1] = Arrays.copyOfRange(barcodeBuff, 16, 32);// 16~31
        reVal[2] = new byte[16];
        System.arraycopy(barcodeBuff, 32, reVal[2], 0, barcodeBuff.length - 32); // 32~mBarcodeBuf.length
        return reVal;
    }
}
