package com.finger;

import java.util.Arrays;

/**
 * Created by pm on 19-6-26.
 */

public class StringUtil {
    public static String fixFingerInfo(String fingerInfo) {
        if (fingerInfo.length() > 1024) {
            fingerInfo = fingerInfo.substring(0, 1024);
        } else if (fingerInfo.length() < 1024) {
            int len = 1024 - fingerInfo.length();
            byte[] b = new byte[len * 2];
            Arrays.fill(b, (byte) 0x00);
            fingerInfo = fingerInfo + ArrayUtils.bytes2HexString(b);
        }
        return fingerInfo;
    }


}
