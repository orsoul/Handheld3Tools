package com.fanfull.libhard.finger;

import android.text.TextUtils;
import android.util.Log;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class ArrayUtils {

    /**
     * 十套代表F1-->F4的标志 和单片机A版本对接的，请勿修改
     */
    public static byte flagData[] = new byte[]{(byte) 0x23, (byte) 0x5f,
            (byte) 0x8e, (byte) 0x41, (byte) 0x4d, (byte) 0x8c, (byte) 0x3d,
            (byte) 0x6a, (byte) 0x23, (byte) 0x9c, (byte) 0x95, (byte) 0x3c,
            (byte) 0x4b, (byte) 0x11, (byte) 0x73, (byte) 0x1c, (byte) 0x3e,
            (byte) 0x22, (byte) 0x49, (byte) 0x83, (byte) 0x36, (byte) 0x47,
            (byte) 0x88, (byte) 0x26, (byte) 0x32, (byte) 0x28, (byte) 0x3d,
            (byte) 0x6f, (byte) 0x78, (byte) 0x7a, (byte) 0x99, (byte) 0x2d,
            (byte) 0x6c, (byte) 0x24, (byte) 0xa3, (byte) 0x8c, (byte) 0x7f,
            (byte) 0x9d, (byte) 0x36, (byte) 0xb2, (byte) 0x25, (byte) 0x39,
            (byte) 0x48, (byte) 0x76, (byte) 0xea, (byte) 0x2c, (byte) 0x36,
            (byte) 0x47, (byte) 0x79, (byte) 0x29,};

    private static final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',};

    public static String bytes2HexString(byte[] bArray, int start, int end) {
        if ((bArray == null) || (end <= start) || (bArray.length <= start)) {
            return null;
        }

        if (bArray.length < end) {
            end = bArray.length;
        }

        StringBuilder sb = new StringBuilder(end - start);
        for (int i = start; i < end; i++) {
            int h = (bArray[i] & 0xF0) >> 4;
            int l = (bArray[i] & 0x0F);
            sb.append(HEX_CHAR[h]).append(HEX_CHAR[l]);
        }
        return sb.toString().toUpperCase(Locale.getDefault());
    }

    public static String bytes2HexString(byte[] bArray) {
        if (null == bArray) {
            return null;
        }
        return bytes2HexString(bArray, 0, bArray.length);
    }

    public static byte[] hexString2Bytes(String hexString) {
        if (hexString == null || 0 == hexString.length()) {
            return null;
        }
        char[] hexChars = hexString.toUpperCase().toCharArray();

        byte[] reVal = new byte[(hexChars.length + 1) / 2];

        for (int i = reVal.length - 1, j = hexChars.length - 1; 0 < j; i--, j -= 2) {
            reVal[i] = (byte) (Character.digit(hexChars[j - 1], 16) << 4 | Character
                    .digit(hexChars[j], 16));
        }
        if (0 != hexChars.length % 2) {
            reVal[0] = (byte) Character.digit(hexChars[0], 16);
        }
        return reVal;
    }

    /**
     * @param arr1
     * @param arr2
     * @return 返回 拼接好后的新 字节数组
     * @description 拼接两个字节数组
     */
    public static byte[] concatArray(byte[] arr1, byte[] arr2) {
        // 处理 特殊情况
        if (null == arr1 && null == arr2) {
            return null;
        } else if (null == arr1) {
            return arr2;
        } else if (null == arr2) {
            return arr1;
        }

        byte[] reVal = new byte[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, reVal, 0, arr1.length);
        System.arraycopy(arr2, 0, reVal, arr1.length, arr2.length);
        return reVal;
    }

    /**
     * @param obj1
     * @param obj2
     * @return
     * @description 判断两个基本类型数组是否相等
     */
    public static boolean arrayIsEqulse(Object obj1, Object obj2) {
        if (null == obj1 || null == obj2) {
            return false;
        } else if (obj1 == obj2) {
            return true;
        }

        if (obj1 instanceof int[] && obj2 instanceof int[]) {
            int[] arr1 = (int[]) obj1;
            int[] arr2 = (int[]) obj2;

            if (arr1.length != arr2.length) {
                return false;
            }

            for (int i = 0; i < arr2.length; i++) {
                if (arr1[i] != arr2[i]) {
                    return false;
                }
            }
            return true;
        } else if (obj1 instanceof byte[] && obj2 instanceof byte[]) {
            byte[] arr1 = (byte[]) obj1;
            byte[] arr2 = (byte[]) obj2;

            if (arr1.length != arr2.length) {
                return false;
            }

            for (int i = 0; i < arr2.length; i++) {
                if (arr1[i] != arr2[i]) {
                    return false;
                }
            }
            return true;
        } else if (obj1 instanceof long[] && obj2 instanceof long[]) {
            long[] arr1 = (long[]) obj1;
            long[] arr2 = (long[]) obj2;

            if (arr1.length != arr2.length) {
                return false;
            }

            for (int i = 0; i < arr2.length; i++) {
                if (arr1[i] != arr2[i]) {
                    return false;
                }
            }
            return true;
        } else if (obj1 instanceof char[] && obj2 instanceof char[]) {
            char[] arr1 = (char[]) obj1;
            char[] arr2 = (char[]) obj2;

            if (arr1.length != arr2.length) {
                return false;
            }

            for (int i = 0; i < arr2.length; i++) {
                if (arr1[i] != arr2[i]) {
                    return false;
                }
            }
            return true;
        } else if (obj1 instanceof short[] && obj2 instanceof short[]) {
            short[] arr1 = (short[]) obj1;
            short[] arr2 = (short[]) obj2;

            if (arr1.length != arr2.length) {
                return false;
            }

            for (int i = 0; i < arr2.length; i++) {
                if (arr1[i] != arr2[i]) {
                    return false;
                }
            }
            return true;
        } else if (obj1 instanceof double[] && obj2 instanceof double[]) {
            double[] arr1 = (double[]) obj1;
            double[] arr2 = (double[]) obj2;

            if (arr1.length != arr2.length) {
                return false;
            }

            for (int i = 0; i < arr2.length; i++) {
                if (arr1[i] != arr2[i]) {
                    return false;
                }
            }
            return true;
        } else if (obj1 instanceof float[] && obj2 instanceof float[]) {
            float[] arr1 = (float[]) obj1;
            float[] arr2 = (float[]) obj2;

            if (arr1.length != arr2.length) {
                return false;
            }

            for (int i = 0; i < arr2.length; i++) {
                if (arr1[i] != arr2[i]) {
                    return false;
                }
            }
            return true;
        } else if (obj1 instanceof boolean[] && obj2 instanceof boolean[]) {
            boolean[] arr1 = (boolean[]) obj1;
            boolean[] arr2 = (boolean[]) obj2;

            if (arr1.length != arr2.length) {
                return false;
            }

            for (int i = 0; i < arr2.length; i++) {
                if (arr1[i] != arr2[i]) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 把字节数据，转化成整型值
     *
     * @param 字节数据
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
     * @param uid
     * @param n   0~9 对应10套加密方式
     * @return
     */
    public static byte getFlagData(byte[] uid, int n) {
        byte tmp = (byte) 0x0;
        if (null == uid || uid.length != 7) {
            Log.d("UID", "uid 错误");
            return tmp;
        }
        LogsUtil.d("getFlagData()", n + " -- uid" + bytes2HexString(uid));
        switch (n) {
            case 0:
                tmp = (byte) (uid[3] + uid[4] + uid[1]);
                LogsUtil.d("Flag0", ArrayUtils.bytes2HexString(new byte[]{tmp}));
                return tmp;
            case 1:
                tmp = (byte) (tmp ^ uid[1]);
                tmp = (byte) (tmp ^ uid[3]);
                tmp = (byte) (tmp ^ uid[5]);
                LogsUtil.d("Flag1", ArrayUtils.bytes2HexString(new byte[]{tmp}));
                return tmp;
            case 2:
                tmp = (byte) (tmp ^ uid[4]);
                tmp = (byte) (tmp ^ uid[5]);
                tmp = (byte) (tmp ^ uid[6]);
                LogsUtil.d("Flag2", ArrayUtils.bytes2HexString(new byte[]{tmp}));
                return tmp;
            case 3:
                tmp = (byte) (uid[3] + uid[2] + uid[1]);
                LogsUtil.d("Flag3", ArrayUtils.bytes2HexString(new byte[]{tmp}));
                return tmp;
            case 4:
                tmp = (byte) (uid[6] + 1);
                LogsUtil.d("Flag4", ArrayUtils.bytes2HexString(new byte[]{tmp}));
                return tmp;
            case 5:
                tmp = (byte) (uid[1] + uid[4] + uid[6]);
                LogsUtil.d("Flag5", ArrayUtils.bytes2HexString(new byte[]{tmp}));
                return tmp;
            case 6:
                tmp = (byte) (uid[1] ^ uid[3]);
                tmp = (byte) (tmp + uid[5]);
                LogsUtil.d("Flag6", ArrayUtils.bytes2HexString(new byte[]{tmp}));
                return tmp;
            case 7:
                tmp = (byte) (uid[4] + uid[6]);
                tmp = (byte) (tmp ^ uid[2]);
                LogsUtil.d("Flag7", ArrayUtils.bytes2HexString(new byte[]{tmp}));
                return tmp;
            case 8:
                tmp = (byte) (uid[2] + uid[4]);
                LogsUtil.d("Flag8", ArrayUtils.bytes2HexString(new byte[]{tmp}));
                return tmp;
            case 9:
                tmp = (byte) (uid[3] + uid[5]);
                tmp = (byte) (tmp ^ uid[4]);
                LogsUtil.d("Flag9", ArrayUtils.bytes2HexString(new byte[]{tmp}));
                return tmp;
            default:
                return tmp;
        }
    }

    /**
     * @param barcode
     * @return
     * @description 将(38个字符长度的barcode + 24个字符长度的bagId)分成两个 16字节长度的byte[],为
     * 后续写入RFID的两个区 做准备
     */
    public static byte[][] get2Data(String barcode) {
        if ((null == barcode) || (barcode.length() < 62)) {
            return null;
        }
        byte[] totalData = hexString2Bytes(barcode);
        byte[][] reVal = new byte[2][];
        reVal[0] = Arrays.copyOfRange(totalData, 0, 16);
        reVal[1] = new byte[16];

        byte[] tmp = Arrays.copyOfRange(totalData, 16, 31);
        for (int i = 0; i < tmp.length; i++) {
            reVal[1][i] = tmp[i];
        }
        return reVal;
    }

    /**
     * 将barcode截成 3个 16byte数组,准备 写入 M1卡的 4,5,6三个块区
     *
     * @param mBarcodeBuf
     * @return
     */
    public static byte[][] get3Data(byte[] mBarcodeBuf) {
        if ((null == mBarcodeBuf) || (mBarcodeBuf.length < 38)) {
            return null;
        }
        byte[][] reVal = new byte[3][];
        reVal[0] = Arrays.copyOfRange(mBarcodeBuf, 0, 16);// 0~15
        reVal[1] = Arrays.copyOfRange(mBarcodeBuf, 16, 32);// 16~31
        reVal[2] = new byte[16];
        System.arraycopy(mBarcodeBuf, 32, reVal[2], 0, mBarcodeBuf.length - 32); // 32~mBarcodeBuf.length
        return reVal;
    }

    /**
     * @param unlockEPC
     * @return 已上锁的 EPC号
     * @description 根据 未上锁EPC号 生成 已上锁的 EPC号
     */
    public static byte[] genLockEPC(byte[] unlockEPC) {

        if (null == unlockEPC || 12 != unlockEPC.length) {
            return null;
        }

        byte[] reVal = null;
        reVal = Arrays.copyOf(unlockEPC, unlockEPC.length);

        // 最后一个byte按位取反
        int end = reVal.length - 1;
        reVal[end] = (byte) (((int) reVal[end]) & 0xFF ^ 0xFF);
        return reVal;
    }

    /**
     * @param preEPC  未插锁片读到的 EPC号
     * @param postEPC 已插插锁片读到的 EPC号
     * @return 返回 true 表示 锁片 已经插上
     * @description 通过比较 两次读到的epc号，判断 条码卡片 是否 已经插上
     */
    public static boolean compareEPC(byte[] preEPC, byte[] postEPC) {
        if (null == preEPC || null == postEPC) {
            return false;
        }

        LogsUtil.s("preEPC:" + bytes2HexString(preEPC));
        LogsUtil.s("postEPC:" + bytes2HexString(postEPC));

        if (preEPC.length != postEPC.length) {
            return false;
        }

        // 判断 其余 位
        // int end = postEPC.length - 1;
        // for (int i = 0; i < end; i++) {
        // if (preEPC[i] != postEPC[i]) {
        // return false;
        // }
        // }
        // 判断最后一个字节 是否为 异或 关系
        // int t1 = preEPC[preEPC.length - 1] & 0xFF;
        // int t2 = postEPC[postEPC.length - 1] & 0xFF;
        // if ((t1 ^ t2) != 0xFF) {
        // return false;
        // }

        return (postEPC[postEPC.length - 1] & 0xFF) == 0xFF;
    }

    /**
     * @param datasource
     * @param password
     * @return
     * @description DEC 加密
     */
    public static byte[] encrypt(byte[] datasource, byte[] password) {
        try {
            password = buildKey(password);
            SecureRandom random = new SecureRandom();
            DESKeySpec desKey = new DESKeySpec(password);// password.getBytes()
            // 创建一个密匙工厂，然后用它把DESKeySpec转换成
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey securekey = keyFactory.generateSecret(desKey);
            // Cipher对象实际完成加密操作
            Cipher cipher = Cipher.getInstance("DES/ECB/NOPadding");// DES/ECB/
            // 用密匙初始化Cipher对象
            cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
            // 现在，获取数据并加密
            // 正式执行加密操作
            return cipher.doFinal(datasource);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param datasource
     * @param password
     * @return
     * @description DEC 解密
     */
    public static byte[] decrypt(byte[] datasource, byte[] password) {
        try {
            password = buildKey(password);
            SecureRandom random = new SecureRandom();
            DESKeySpec desKey = new DESKeySpec(password);// password.getBytes()
            // 创建一个密匙工厂，然后用它把DESKeySpec转换成
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey securekey = keyFactory.generateSecret(desKey);
            // Cipher对象实际完成加密操作
            Cipher cipher = Cipher.getInstance("DES/ECB/NOPadding");// DES/ECB/
            // 用密匙初始化Cipher对象
            cipher.init(Cipher.DECRYPT_MODE, securekey, random);
            // 正式执行解密操作
            return cipher.doFinal(datasource);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int yh(byte[] arr) {


        if (null == arr || arr.length == 0) {
            return -1;
        }
        int reVal = 0;
        for (int i = 0; i < arr.length; i++) {
            reVal ^= arr[i];
        }
        return reVal;
    }

    /**
     * 加密
     *
     * @param src
     * @param netkey
     * @return
     */
    public static byte[] encryption(byte[] src, byte[] netkey) {
        if (src == null || netkey == null) {
            return src;
        }
        int len = src.length;
        byte[] des = new byte[len];
        byte[] tmp = new byte[len];
        if (src.length >= netkey.length) {
            for (int i = 0; i < netkey.length; i++) {
                tmp[i] = netkey[i];
            }
            for (int i = 0; i < (src.length - netkey.length); i++) {
                tmp[netkey.length + i] = netkey[i % netkey.length]; // 补全密钥长度
            }
        } else {
            for (int i = 0; i < src.length; i++) {
                tmp[i] = netkey[i]; // 缩短密钥长度
            }
        }
        /** 开始异或加密 */
        for (int i = 0; i < tmp.length; i++) {
            des[i] = (byte) ((src[i]) ^ (tmp[i]));
        }
        /** 前后三位错位处理 */
        if (null != swapEnAndDec(des)) {
            return des;
        } else {
            return null;
        }

    }

    private static byte[] swapEnAndDec(byte[] src) {
        byte k = (byte) 0x0;
        if (null == src || src.length == 0) {
            return null;
        } else if (src.length == 1) {
            return src;
        } else if (src.length == 2 || src.length == 3) {
            k = src[0];
            src[0] = src[src.length - 1];
            src[src.length - 1] = k;
        } else if (src.length == 4 || src.length == 5) {
            k = src[1];
            src[1] = src[src.length - 2];
            src[src.length - 2] = k;
        } else {
            k = src[2];
            src[2] = src[src.length - 3];
            src[src.length - 3] = k;
        }
        return src;
    }

    /**
     * 解密
     *
     * @param src    需要解密的数据
     * @param netkey 密钥
     * @return 返回解完的数据
     */
    public static byte[] deciphering(byte[] src, byte[] netkey) {
        if (src == null || netkey == null) {
            return src;
        }
        int len = src.length;
        byte[] des = new byte[len];
        byte[] tmp = new byte[len];
        if (src.length >= netkey.length) {
            for (int i = 0; i < netkey.length; i++) {
                tmp[i] = netkey[i];
            }
            for (int i = 0; i < (src.length - netkey.length); i++) {
                tmp[netkey.length + i] = netkey[i % netkey.length]; // 补全密钥长度
            }
        } else {
            for (int i = 0; i < src.length; i++) {
                tmp[i] = netkey[i]; // 缩短密钥长度
            }
        }
        /** 恢复错位 */
        if (null == swapEnAndDec(src)) {
            return null;
        }
        /** 开始异或加密 */
        for (int i = 0; i < tmp.length; i++) {
            des[i] = (byte) ((src[i]) ^ (tmp[i]));
        }

        return des;
    }

    public static byte[] buildKey(byte[] src) {
        byte[] tmp = new byte[8];
        if (null == src) {
            return null;
        } else if (src.length == 4) {
            for (int i = 0; i < src.length; i++) {
                tmp[i] = src[i];
                tmp[7 - i] = src[i];
            }
        } else if (src.length == 6) {
            for (int i = 0; i < src.length; i++) {
                tmp[i] = src[i];
            }
            tmp[6] = src[3];
            tmp[7] = src[4];
        } else if (src.length == 7) {
            for (int i = 0; i < src.length; i++) {
                tmp[i] = src[i];
            }
            tmp[7] = src[4];
        }
        return tmp;
    }

    /**
     * 将基金袋上的袋id通过每两位异或得到检验位，合成新的芯片的袋id
     *
     * @param originalId
     * @return
     */
    public static String bagIdConvert(String originalId) throws NumberFormatException {
        if (TextUtils.isEmpty(originalId)) {
            return "";
        }
        if (originalId.length() % 2 == 1) {
            originalId = "0" + originalId;
        }
        int checkBit = 0;
        for (int i = 0; i < originalId.length() / 2; i++) {
            checkBit ^= Integer.parseInt(originalId.substring(i * 2, i * 2 + 2), 16);
        }
        return (originalId + String.format("%2s", Integer.toHexString(checkBit)).replace(' ', '0')).toUpperCase();
    }
}
