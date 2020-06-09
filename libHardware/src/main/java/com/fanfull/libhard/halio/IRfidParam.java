package com.fanfull.libhard.halio;

public interface IRfidParam {
    public static byte ISOTYPE_14443A = 'A';
    public static byte ISOTYPE_14443B = 'B';
    public static byte ISOTYPE_ICODE = '1';
    public static byte ANTENNA_ON = 1;
    public static byte ANTENNA_OFF = 0;
    public static byte CARD_ALL = 0x52;
    public static byte CARD_NOSLEEP = 0x26;

    public static byte AUTH_KEY_A = 0x60;
    public static byte AUTH_KEY_B = 0x61;

    public static byte[] DEFAULT_KEY = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};

    public static byte PSAM_NUM_1 = 0x01;
    public static byte PSAM_NUM_2 = 0x02;
    public static byte PSAM_NUM_3 = 0x03;
    public static byte PSAM_NUM_4 = 0x04;

    public static byte[] PSAM_NUM = {PSAM_NUM_1, PSAM_NUM_2, PSAM_NUM_3, PSAM_NUM_4};
    //public static byte[] PSAM_NUM = {PSAM_NUM_2};
    public static String[] PSAM_NUM_STR = {"PSAM_1", "PSAM_2", "PSAM_3", "PSAM_4"};

    public static byte PSAM_MODE_38400 = (byte) 0x38;
    public static byte PSAM_MODE_9600 = (byte) 0x96;
    //public static byte PSAM_MODE_55800 = (byte)0x55;

    public static byte[] PSAM_MODE = {PSAM_MODE_38400, PSAM_MODE_9600
            //,PSAM_MODE_55800
    };
    public static String[] PSAM_MODE_STR = {"38400", "9600"
            //,"55800"
    };

    public static byte PSAM_TRANS_MODE_1 = (byte) 0x01;
    public static byte PSAM_TRANS_MODE_2 = (byte) 0x02;
    public static byte PSAM_TRANS_MODE_3 = (byte) 0x03;
    public static byte PSAM_TRANS_MODE_4 = (byte) 0x04;
}
