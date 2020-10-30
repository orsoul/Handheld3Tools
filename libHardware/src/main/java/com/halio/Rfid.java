package com.halio;

public class Rfid {

  public static byte ISOTYPE_14443A = 'A';
  public static byte ISOTYPE_14443B = 'B';
  public static byte ISOTYPE_ICODE = '1';
  public static byte ANTENNA_ON = 1;
  public static byte ANTENNA_OFF = 0;
  public static byte CARD_ALL = 0x52;
  public static byte CARD_NOSLEEP = 0x26;

  public static byte AUTH_KEY_A = 0x60;
  public static byte AUTH_KEY_B = 0x61;

  public static byte[] DEFAULT_KEY =
      { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };

  public static byte PSAM_NUM_1 = 0x01;
  public static byte PSAM_NUM_2 = 0x02;
  public static byte PSAM_NUM_3 = 0x03;
  public static byte PSAM_NUM_4 = 0x04;

  public static byte[] PSAM_NUM = { PSAM_NUM_1, PSAM_NUM_2, PSAM_NUM_3, PSAM_NUM_4 };
  public static String[] PSAM_NUM_STR = { "PSAM_1", "PSAM_2", "PSAM_3", "PSAM_4" };

  public static byte PSAM_MODE_38400 = (byte) 0x38;
  public static byte PSAM_MODE_9600 = (byte) 0x96;
  //public static byte PSAM_MODE_55800 = (byte)0x55;

  public static byte[] PSAM_MODE = {
      PSAM_MODE_38400, PSAM_MODE_9600
      //,PSAM_MODE_55800
  };
  public static String[] PSAM_MODE_STR = {
      "38400", "9600"
      //,"55800"
  };

  /////////////////////////////
  public static native boolean notifyBootStart();

  public static native boolean openPort(byte port, int baud);

  public static native boolean cmdTrancevice(int cmd, byte[] param, int paramLength,
      byte[] bResponseData, int[] bResponseDataLength);
  //////////////////////////////

  //HP打印机
  public static native boolean powerOnHP();

  public static native boolean powerOffHP();

  public static native boolean openCommPortHP();

  public static native boolean closeCommPortHP();

  //ISP
  public static native boolean enterBootMode();

  public static native boolean exitBootMode();

  public static native boolean sendCommData(byte[] bCommData, int commDataLength);

  public static native boolean readCommData(byte[] bCommData, byte[] bCommDataLength);

  public static native boolean powerOn();

  public static native boolean powerOff();

  public static native boolean openCommPort();

  public static native boolean closeCommPort();

  public static native int getHwVersion(byte[] hwVersion);

  public static native boolean PcdConfigISOType(byte isoType);

  public static native boolean PcdRequest(byte req_code, byte[] pTagType);

  public static native boolean PcdHalt();

  public static native boolean PcdAnticoll(byte[] pSnr);

  public static native boolean PcdSelect(byte[] pSnr, byte[] pSize);

  public static native boolean ChangeCodeKey(byte[] pUncoded, byte[] pCoded);

  public static native boolean PcdAuthKey(byte[] pCoded);

  public static native boolean PcdAuthState(byte auth_mode, byte block, byte[] pSnr);

  public static native boolean PcdRead(byte addr, byte[] pReaddata);

  public static native boolean PcdWrite(byte addr, byte[] pWritedata);

  public static native boolean PcdDoAuthen(byte auth_mode, byte block, byte[] pKey);

  public static native boolean PcdValue(byte dd_mode, byte addr, byte[] pValue);

  public static native boolean PcdRestore(byte addr);

  public static native boolean PcdTransfer(byte addr);

  public static native boolean SingleInitvalue(byte block, byte[] value);

  public static native boolean SingleReadvalue(byte block, byte[] value);

  //PSAM卡测试
  public static native boolean samReset(byte samNum, byte samBaud, byte[] cosResponse,
      int[] cosResponseLength);

  public static native boolean samCos(byte samNum, byte[] cosCmd, int cosCmdLength,
      byte[] cosResponse, int[] cosResponseLength);

  //CPU卡测试
  public static native boolean iso14443bReset(byte bMode, byte[] pUID, byte[] pUIDLength);

  public static native boolean iso14443bCos(byte[] pCosCmd, int cosCmdLength, byte[] pCosResponse,
      int[] pBufLength);

  public static native boolean iso14443aReset(byte bMode, byte[] pCosResponse, byte[] pBufLength);

  public static native boolean iso14443aCos(byte[] pCosCmd, int cosCmdLength, byte[] pCosResponse,
      int[] pBufLength);

  //////////////////////////////////////////////////////////////
  public static native boolean iso14443aQueryUid(byte mode, byte[] UID);

  public static native boolean iso14443aReadCard(byte querymode, byte authmode, byte block,
      byte[] authkey, byte[] Data);

  public static native boolean iso14443aWriteCard(byte querymode, byte authmode, byte block,
      byte[] authkey, byte[] Data);

  public static native boolean iso14443aInitValue(byte querymode, byte authmode, byte block,
      byte[] authkey, byte[] value);

  public static native boolean iso14443aReadValue(byte querymode, byte authmode, byte block,
      byte[] authkey, byte[] value);

  public static native boolean iso14443aDecrement(byte querymode, byte authmode, byte block,
      byte[] authkey, byte[] value);

  public static native boolean iso14443aIncrement(byte querymode, byte authmode, byte block,
      byte[] authkey, byte[] value);

  public static native boolean iso14443aReadContinuous(byte querymode, byte authmode, byte block,
      byte[] authkey, byte blocknum, byte[] data);

  public static native boolean iso14443aConfigAntenna(byte state);

  public static native boolean iso14443bTypebReset(byte mode, byte[] UID);

  public static native boolean iso15693Inventorys(byte[] UID);

  public static native boolean iso15693Read(byte mode, byte[] UID, byte block, byte blocknum,
      byte[] data, byte[] datalen);

  public static native boolean iso15693write(byte mode, byte[] UID, byte block, byte[] data,
      byte datalen);

  public static native boolean ULPcdAnticoll(byte[] pSnr);

  public static native boolean ULPcdRead(byte addr, byte[] pReaddata);

  public static native boolean ULPcdWrite(byte addr, byte[] pWritedata);

  public static native boolean doTest();

  //defire
  public static native boolean ComDesfireRst(byte[] pResponse, int[] pBufLength);

  public static native boolean desfirecos(byte[] pCosCmd, byte cosCmdLength, byte[] pCosResponse,
      int[] pBufLength);

  public static native boolean DesFireCardGetVersion(VERSIONINFO pstVersionInfo);

  public static native boolean DesFireCardGetKeyVersion(byte bKeyNumber, int[] pbKeyVersion);

  public static native boolean DesFireCardAuthenticate(byte bKeyNumber, byte[] pbAccessKey);

  public static native boolean DesFireCardChangeKey(byte bKeyNumber, byte[] pbNewKey,
      byte[] pbPrevKey, byte flag);

  public static native boolean DesFireCardFormatPICC();

  public static native boolean DesFireCardSelectApplication(long eApplicationID);

  public static native boolean DesFireCardCreateApplication(long eNewApplicationID,
      int bKeySettings, byte bNApplicationKeys);

  public static native boolean DesFireCardDeleteApplication(long eApplicationID);

  public static native boolean DesFireCardGetApplicationIDs(byte bMaxApplicationIDs,
      byte[] peApplicationIDs, byte[] pbNApplicationIDs);

  public static native boolean DesFireCardCreateStdDataFile(byte bFileID, byte bCommMode,
      int wAccessRights, long eFileSize);

  public static native boolean DesFireCardCreateBackupDataFile(byte bFileID, byte bCommMode,
      int wAccessRights, long eFileSize);

  public static native boolean DesFireCardCreateValueFile(byte bFileID, byte bCommMode,
      int wAccessRights, long lLowerLimit, long lUpperLimit, long lInitialValue,
      byte bLimitedCreditEnabled);

  public static native boolean DesFireCardCreateCyclicRecordFile(byte bFileID, byte bCommMode,
      int wAccessRights, long eRecordSize, long eMaxNRecords);

  public static native boolean DesFireCardWriteData(byte bFileID, byte bCommMode, long eFromOffset,
      byte[] pbDataBuffer);

  public static native boolean DesFireCardChangeFileSettings(byte bFileID, byte bNewCommMode,
      int wNewAccessRights);

  public static native boolean DesFireCardReadData(byte bFileID, byte bCommMode, long eFromOffset,
      long eNBytesToRead, byte[] pbDataBuffer, long[] peNBytesRead);

  public static native boolean DesFireCardGetValue(byte bFileID, byte bCommMode, long[] plAmount);

  public static native boolean DesFireCardCredit(byte bFileID, byte bCommMode, long lAmount);

  public static native boolean DesFireCardDebit(byte bFileID, byte bCommMode, long lAmount);

  public static native boolean DesFireCardWriteRecord(byte bFileID, byte bCommMode,
      long eFromOffset, byte[] pbDataBuffer);

  public static native boolean DesFireCardReadRecords(byte bFileID, byte bCommMode,
      long eFromRecord, long eNRecordsToRead, long eRecordSize, byte[] pbDataBuffer,
      long[] peNRecordsRead);

  public static native boolean DesFireCardCommitTransaction();

  public static native boolean DesFireCardGetFileIDs(byte bMaxFileIDs, byte[] pbFileIDBuffer,
      byte[] pbNFileIDsFound);

  public static native boolean DesFireCardDeleteFile(byte bFileID);

  public static native boolean DesFireCardGetFileSettings(byte bFileID, byte[] pbFileType,
      byte[] pbCommMode, int[] pwAccessRights, AdditionalFileSettings punAdditionalFileSettings);

  public static native boolean DesFireCardCreateLinearRecordFile(byte bFileID, byte bCommMode,
      int wAccessRights, long eRecordSize, long eMaxNRecords);

  public static native boolean DesFireCardClearRecordFile(byte bFileID);

  public static native boolean DesFireCardAbortTransaction();

  public static native boolean DesFireCardChangeKeySettings(int bNewKeySettings);

  public static native boolean DesFireCardGetKeySettings(int[] pbKeySettings, byte[] pbNKeys);

  ////////////////////////测试身份证////////////////////////////////////////////
  public static native boolean openSFZPower();

  public static native boolean closeSFZPower();

  public static native boolean openCommPortSFZ();

  public static native boolean closeCommPortSFZ();

  public static native boolean RequestCardSFZ();

  public static native boolean SelectCardSFZ();

  public static native boolean ReadCardSFZ(byte[] pCosResponse, int[] pBufLength);

  public static native boolean openS50Power();

  public static native boolean closeS5Power();

  public static native boolean openCommPortS5();

  public static native boolean closeCommPortS5();

  public static native boolean ActivateCardS5();

  public static native boolean AuthenCardS5();

  public static native boolean ReadDataS5(byte[] pCosResponse);

  public static native boolean ActivateCardCPU();

  public static native boolean DeActivateCardCPU();

  public static native boolean iso14443aCosS5(byte[] pCosCmd, int cosCmdLength, byte[] pCosResponse,
      int[] pBufLength);

  public class AdditionalFileSettings {

    public long eFileSize;  //文件类型为0,1的文件

    public long lLowerLimit;     //值文件 文件类型为2
    public long lUpperLimit;
    public long eLimitedCredit;
    public byte bLimitedCreditEnabled;

    public long eRecordSize;  //记录文件  文件类型为3,4
    public long eMaxNRecords;
    public long eCurrNRecords;
  }

  public class VERSIONINFO   //defire卡的版本信息
  {
    public byte bHwVendorID;
    public byte bHwType;
    public byte bHwSubType;
    public byte bHwMajorVersion;
    public byte bHwMinorVersion;
    public byte bHwStorageSize;
    public byte bHwProtocol;

    public byte bSwVendorID;
    public byte bSwType;
    public byte bSwSubType;
    public byte bSwMajorVersion;
    public byte bSwMinorVersion;
    public byte bSwStorageSize;
    public byte bSwProtocol;

    public byte[] abUid = new byte[7];
    public byte[] abBatchNo = new byte[5];
    public byte bProductionCW;
    public byte bProductionYear;
  }

  static {
    System.loadLibrary("hyio_gpio_api");
    System.loadLibrary("hyio_uart_api");
    System.loadLibrary("halio_rfid");
  }
}
