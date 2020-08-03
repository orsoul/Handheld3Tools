package com.fanfull.operation;

import android.os.SystemClock;
import android.util.Log;
import com.apkfuns.logutils.LogUtils;
import com.fanfull.contexts.StaticString;
import com.fanfull.utils.ArrayUtils;
import java.util.Arrays;

/**
 * 读取高频RFID卡里面的内容
 *
 * @author Keung
 * @ClassName: BagOperation
 * @Description: 基金袋操作
 * @date 2016-03-04
 */
public class NFCBagOperation {
  private final static String TAG = NFCBagOperation.class.getSimpleName();
  String bagID = null;
  private String BarValue = null;
  // 交接内容
  private String organization = "0000000000";
  private String people = "001002003";
  private String time = "000000000000";
  private String operation = "0";
  private String thing = "0";
  private String result = "0";
  private String number = "16";
  private String check = "00";
  private String replayID = null;// 比较回复的袋ID

  private NfcOperation mNfcOperation;

  public NFCBagOperation() {
    mNfcOperation = NfcOperation.getInstance();
  }

  public static final int BAG_NO_LOCK = 1;// 袋子未锁
  public static final int BAG_LOCKED = 2;// 袋子已锁

  public static final int BAG_MATCH = 3;// 正确的袋子,已初始化
  public static final int BAG_NO_INIT = 4;// 袋子未初始化
  public static final int READ_BAG_FAILED = -1;// 条码 未 扫描

  public static final int BAG_TYPE_NO_MATCH = 6;// 袋型号不匹配
  public static final int BARCODE_NO_SCANNED = 7;// 条码 未 扫描

  /**
   * @return 0 成功；1 扫描失败；2 补写条码；3，袋型号不匹配；4 条码扫描不对
   * @Description: 读袋ID == 读 RFID 第一块区
   */
  public int readBagID() {
    //        mRFIDOp.reset();
    int count = 0;
    while (bagID == null) {
      if (20 < ++count) {
        return READ_BAG_FAILED;// 扫描失败
      }
      bagID = mNfcOperation.reaAddr((byte) 0x04);//2016/3/4
      SystemClock.sleep(20);
    }
    int bagVersion = Integer.valueOf(bagID.substring(0, 2));// 袋ID版本
    StaticString.bagid = bagID.substring(0, 24);
    Log.d(TAG, "readBagID:" + StaticString.bagid);
    if (bagVersion == 5) {
      bagID = null;
      return bagVersion;
    } else {
      bagID = null;
      return BAG_NO_INIT;// 版本不对需要补写
    }
  }

  public String getBagID() {
    bagID = null;
    int count = 0;
    while (bagID == null) {
      if (30 < ++count) {
        return null;// 扫描失败
      }
      bagID = mNfcOperation.reaAddr((byte) 0x04);//2016/3/4
      SystemClock.sleep(20);
    }
    return bagID.substring(0, 24);
  }

  /**
   * 读取地址为0x10位置的标志位信息
   *
   * @return -1 读取失败，0初始化时没有更改标志位，1锁片没锁好，2已插上锁片 3可以正常开袋 4错误，单片机没有将其循环利用
   */
  public int readInitFlag() {//判断锁片有没有锁好
    int count = 0;
    String flag = null;
    while (flag == null) {
      if (20 < ++count) {
        return -1;// 扫描失败
      }
      flag = mNfcOperation.reaAddr((byte) 0x10);//2016/3/4
      SystemClock.sleep(20);
    }
    return Integer.parseInt(flag.substring(0, 2));
  }

  /**
   * 地址0x11 - 0x13  10个字节启用码
   *
   * @return 20个字节的字节数组
   */
  public byte[] readEnableBagCode() {
    byte[] data = null;
    int count = 0;
    while (data == null) {
      data = mNfcOperation.reaAddrToByte((byte) 0x11);
      count++;
      if (count > 40) {
        return null;// 扫描不到
      }
      SystemClock.sleep(50);
    }
    byte[] tmp = new byte[4];
    for (int i = 0; i < tmp.length; i++) {
      tmp[i] = data[i];
    }
    return tmp;
  }

  /**
   * 写模式 选择，将模式选择写在地址为0x14
   */
  public boolean writeModeChoice(byte[] data) {
    return mNfcOperation.writeMode(data);
  }

  public void close() {
    mNfcOperation.close();
  }

  /**
   * 地址0x20 - 0x22  12个字节的目录信息
   */
  public byte[] readIndexAddr() {
    byte[] data = null;
    int count = 0;
    while (data == null) {
      data = mNfcOperation.reaAddrToByte((byte) 0x20);
      count++;
      if (count > 40) {
        return null;// 扫描不到
      }
      SystemClock.sleep(50);
    }
    byte indexData[] = new byte[12];
    for (int i = 0; i < indexData.length; i++) {
      indexData[i] = data[i];
    }
    return indexData;
  }

  /**
   * @return 0, 成功；1,扫描失败；2，补写条码；3，袋型号不匹配；4,条码扫描不对
   * @throws
   * @Title: checkBag
   * @Description: 校验基金袋中类型
   */
  public int checkBag() {
    Log.d(TAG, "checkBag+BarValue" + BarValue);
    int count = 0;
    int barvalue = 0;
    if (BarValue != null) {
      barvalue = Integer.valueOf(BarValue.substring(17, 19));// 条码类型
    } else {
      return BARCODE_NO_SCANNED;
    }
    while (bagID == null) {
      bagID = mNfcOperation.reaAddr((byte) 0x05);
      count++;
      if (count > 20) {
        return READ_BAG_FAILED;// 扫描不到
      }
    }
    bagID = bagID.substring(0, 18);
    int bagtype = Integer.valueOf(bagID.substring(6, 8));// 袋类型
    System.out.println("bagtype" + bagtype + "tiama" + barvalue);
    System.out.println("bagId" + bagID);
    System.out.println("replay" + replayID);

    if (bagID.endsWith(replayID)) {
      if (bagtype == barvalue) {
        bagID = null;
        return BAG_MATCH;
      } else {
        bagID = null;
        return BAG_TYPE_NO_MATCH;
      }
    } else {
      bagID = null;
      return BAG_NO_INIT;// 版本不对需要补写
    }
  }

  /**
   * @Description: 写入19字节的条码信息，为了和M1卡保存统一，这也使用了二维数组分割数据。
   */
  public Boolean writeBarcode() {
    Log.v(TAG, "writeBarcode+BarValue:" + BarValue + " barcode:\n" + StaticString.barcode);
    int count = 0;// 写入次数级数
    //Log.d(TAG, "BarValue.trim():" + BarValue.trim());
    //        if (BarValue != null) {
    //    byte[][] data = ArrayUtils.get2Data(BarValue.trim());
    byte[][] data = ArrayUtils.get2Data(StaticString.barcode);
    if (data != null) {
      while (!(mNfcOperation.writeBarCode(data))) {
        // 可做提示操作
        count++;// 超过十次失败退出
        if (count > 10) {
          Log.v("writeBarcode", "写入count:" + count + "次");
          return false;
        }
      }
      return true;
    } else {
      Log.v("write", "failed");
      return false;
    }
    //    }
    //        return false;
  }

  /**
   * @Description: 写入12字节的袋ID和19字节的条码信息，为了和M1卡保存统一，这也使用了二维数组分割数据。
   */
  public Boolean writeBagBarcode(byte[] data) {
    int count = 0;// 写入次数级数
    while (!(mNfcOperation.writeBagBarCode(data))) {
      // 可做提示操作
      count++;// 超过十次失败退出
      if (count > 10) {
        Log.v("writeBarcode", "写入count:" + count + "次");
        return false;
      }
    }
    return true;
  }

  //    /**
  //     *
  //     * @Title: readBarCOde
  //     * @Description:读出基金袋锁芯片中的条码比对
  //     * @param @return 设定文件
  //     * @return ture,比对条码成功，false比对条码失败
  //     * @throws
  //     */
  //    public Boolean readBarCode() {
  //        int count = 0;
  //        String data = null;
  //        while (data == null) {
  //            data = mNfcOperation.readBarCode();
  //            count++;
  //            if (count > 50) {
  //                return false;
  //            }
  //        }
  //        StaticString.bagbarcode = data;
  //        System.err.println("bagbarcode:" + data);
  //        return true;
  //    }

  /**
   * @param @return 设定文件
   * @return ture, 比对条码成功，false比对条码失败
   * @throws
   * @Title: readBarCOde
   * @Description:读出基金袋锁芯片中的条码比对
   */
  public String readBagBarCode() {
    int count = 0;
    String data = null;
    while (data == null) {
      data = mNfcOperation.readBagBarCode();
      count++;
      if (count > 50) {
        return null;
      }
    }
    StaticString.bagbarcode = data;
    System.err.println("bagbarcode:" + data);
    return data;
  }

  /**
   * 使用NFC卡读取第地址0x8a的数据
   *
   * @param @return 设定文件
   * @return 0没有扫到袋子；1锁片没锁好；2成功；
   * @throws
   * @Title: readBagLock
   * @Description:读出基金袋锁
   */

  public int readBagLock() {
    //    mRFIDOp.reset();
    String data = null;
    int count = 0;
    while (data == null) {
      data = mNfcOperation.reaAddr((byte) 0x8a);
      count++;
      if (count > 60) {
        return READ_BAG_FAILED;// 扫描不到
      }
      SystemClock.sleep(50);
    }
    Log.v(TAG, "readBagLock()读开始地址0x8a数据" + "RFID" + data + " data.length:"
        + data.length());
    if (data.endsWith("9")) {
      return BAG_LOCKED;
    } else {
      return BAG_NO_LOCK;
    }
  }

  /**
   * @return Boolean 返回类型
   * @throws
   * @Title: writeTrace
   * @Description: 往锁芯片中写入痕迹
   * 将35个数字转化为30个十六进制数（每七个转化为一个6位十六进制数，少了自动补0），将每两个转为byte
   * 【】写入到第16块区
   */
  public Boolean writeTrace(int index) {
    StringBuffer trace = new StringBuffer();
    StringBuffer tempString = new StringBuffer();

    tempString.append(organization).append(people).append(time)
        .append(operation).append(thing).append(result).append(number);
    System.out.println(tempString);
    for (int a = 0; a < 5; a++) {
      int tempindex = Integer.valueOf(tempString.substring(a * 7,// 转化为int时前面为的0自动消失
          ((a + 1) * 7)));
      System.out.println("tempindex=" + tempindex);
      String content = String.valueOf(Integer.toHexString(tempindex));
      if (content.length() < 7) {
        int j = content.length();
        for (int i = j; i < 6; i++) {
          content = "0" + content;
        }
      }
      trace.append(content);
      System.out.println(content);
    }

    int count = 0;// 写入次数级数
    System.out.println("finish tracedata init");
    byte[] data = ArrayUtils.traceToByte(new String(trace));
    if (data != null) {
      while (!(mNfcOperation.writeAddr((byte) index, data))) {
        // 可做提示操作

        count++;// 超过十次失败退出
        if (count > 20) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * 得到新锁的ID内容
   *
   * @param data 扫描Epc得到的Tid号
   */
  public byte[] getNewBagID(byte[] data) {
    LogUtils.tag(TAG).d("writeBagID");
    // 05310100100210307091411071425050000100共38位数据
    // 0531(地区ID4位) 01（网点ID2位） 001（库ID3位）002103（流水号6位） 07 (币种2位) 09(袋型号2位)
    // 141107（日期6位）142505（时间6位） 0(残损1位)0(清分1位)0（复点1位）01（人头2位）00(补2位00)
    // 袋ID数据组成 版本2位（04开头），地区码，
    StringBuffer newBagID = new StringBuffer();
    // 在stringBuffer中新建一个元素以04开头，然后附加读到第1块区的第1位到第3位。。。。。
    newBagID.append("04")
        .append(StaticString.barcode.substring(1, 4)); // 地区码

    char ch = StaticString.barcode.charAt(31);
    newBagID.append(ch);
    if ('1' == ch) {
      newBagID.append(StaticString.barcode.charAt(33));
    } else {
      newBagID.append(StaticString.barcode.charAt(32));
    }

    newBagID.append(StaticString.barcode.substring(
        StaticString.barcode.length() - 21,
        StaticString.barcode.length() - 19)).append('0');// 袋型号
    byte[] id = new byte[12];
    for (int i = 0; i < 4; i++) {// 前4位
      // 数是截取newid(id[0],id[1],id[2],id[3]),前4位组成版本号（04）,地区码，残损标志，袋型号
      id[i] = (byte) (Integer.valueOf(newBagID.charAt(i * 2) - 48) * 16 + Integer
          .valueOf(newBagID.charAt(i * 2 + 1) - 48));
    }
    for (int i = 0; i < data.length;
        i++) { // 第5位到第8位(id[4],id[5],id[6],id[7],id[8],id[9]是读取厂家码6位)  即共10位数据
      id[4 + i] = data[i];
    }
    id[10] = (byte) 0x00;
    for (int i = 0; i < 10; i++) { // 校验码id[10](与前面所有的数异或)
      id[10] ^= id[i];
    }
    for (int i = 11; i < id.length; i++) { // id[9],id[10]....全设为 0x00
      id[i] = 0x00;
    }
    //RFID bagID new: 04871100F8000827DEA03B00
    LogUtils.i("RFID bagID new: " + ArrayUtils.bytesToHexString(
        id));// id=0444400021A39DCED100000000000000(16个byte)以16进制输出
    return id;
  }

  /**
   * 初始化基金袋的时候用到的方法
   *
   * @Title: writeBagID
   * @Description: 补写基金袋ID
   * @param @return 设定文件
   * @return Boolean 返回类型
   * @throws
   */
  public byte[] uid = null;

  /**
   * @return 生成 袋ID 号, 并写入 第一块区
   */
  public byte[] writeBagID() {
    LogUtils.tag(TAG).d("writeBagID");
    uid = new byte[4];
    String bagID = null;
    // 05310100100210307091411071425050000100共38位数据
    // 0531(地区ID4位) 01（网点ID2位） 001（库ID3位）002103（流水号6位） 07 (币种2位) 09(袋型号2位)
    // 141107（日期6位）142505（时间6位） 0(残损1位)0(清分1位)0（复点1位）01（人头2位）00(补2位00)
    // 袋ID数据组成 版本2位（04开头），地区码，
    StringBuffer newBagID = new StringBuffer();
    int count = 0;
    while (bagID == null) {
      uid = mNfcOperation.activatecard();
      bagID = mNfcOperation.reaAddr((byte) 0x04);
      count++;// 超过 次失败退出
      if (count > 100) {
        LogUtils.i("读取RFID第一区失败");
        return null;
      }
      SystemClock.sleep(50);
    }
    // 在stringBuffer中新建一个元素以04开头，然后附加读到第1块区的第1位到第3位。。。。。
    newBagID.append("04")
        .append(StaticString.barcode.substring(1, 4))
        // 地区码
        .append(StaticString.barcode.substring(
            StaticString.barcode.length() - 32,
            StaticString.barcode.length() - 31))// 残损标志
        .append(StaticString.barcode.substring(
            StaticString.barcode.length() - 21,
            StaticString.barcode.length() - 19));// 袋型号
    byte[] id = new byte[16];
    for (int i = 0; i < 4; i++) {// 前4位
      // 数是截取newid(id[0],id[1],id[2],id[3]),前4位组成版本号（04）,地区码，残损标志，袋型号
      id[i] = (byte) (Integer.valueOf(newBagID.charAt(i * 2) - 48) * 16 + Integer
          .valueOf(newBagID.charAt(i * 2 + 1) - 48));
    }
    for (int i = 0; i < uid.length; i++) { // 第5位到第8位(id[4],id[5],id[6],id[7]是读取厂家码4位)
      id[4 + i] = uid[i];
    }
    id[11] = (byte) 0x00;
    for (int i = 0; i < 11; i++) { // 校验码id[8](与前面所有的数异或)
      id[11] ^= id[i];
    }
    for (int i = 12; i < id.length; i++) { // id[9],id[10]....全设为 0x00
      id[i] = 0x00;
    }
    LogUtils.tag(TAG)
        .d("new id=" + ArrayUtils.bytesToHexString(
            id));// id=0444400021A39DCED100000000000000(16个byte)以16进制输出
    while (!mNfcOperation.writeAddr((byte) 0x05, id)) { // 把上面的数据都写到第一块区
      count++;
      if (count > 100) {
        LogUtils.i("写入RFID第一区失败");
        return null;
      }
      SystemClock.sleep(50);
    }
    LogUtils.i("写入RFID第一区成功");
    return id;
  }

  /**
   * @param addr地址 data需要写的内容
   * @return Boolean 返回类型
   * @throws
   * @Title: writeBagId
   * @Description: 写入袋ID
   */
  public boolean writeBagId(byte[] data) {
    return mNfcOperation.writeBagID(data);
  }

  /**
   * 初始化写目录索引信息，开始地址为0x20 共12个字节
   */
  public boolean writeIndexInfo(byte[] data) {
    return mNfcOperation.writeIndexInfo(data);
  }

  /**
   * 初始化读目录索引信息，开始地址为0x20 共12个字节
   */
  public String readIndexInfo() {
    return mNfcOperation.readIndexInfo();
  }

  /**
   * 写启用码，将启用码写在地址为0x11的位置
   */
  public boolean writeEnableCode(byte[] data) {
    return mNfcOperation.writeEnableCode(data);
  }

  /**
   * 写标志位，将标志位写在地址为0x04
   */
  public boolean writeFlag(byte data) {
    byte[] tmp = new byte[4];
    Arrays.fill(tmp, (byte) 0x0);
    tmp[0] = data;
    //return writeAddr((byte)0x10, tmp);
    return mNfcOperation.writeFlag(tmp);
  }

  /**
   * 初始化0x12-0x14
   */
  public boolean init0x12_0x17(byte data[]) {
    return mNfcOperation.init0x12_0x17(data);
  }

  /**
   * 读标志位，将标志位从0x10读出。
   *
   * @return byte
   */
  public byte readFlag() {
    byte[] tmp = mNfcOperation.reaAddrToByte((byte) 0x10);
    if (null == tmp) {
      return (byte) 0xF0;//标志位读取异常
    } else {
      return tmp[0];
    }
  }

  /**
   * 读TID，将TID从0x14读出。
   *
   * @return byte
   */
  public byte[] read0x14() {
    byte[] tmp = new byte[8];
    byte[] tmp2 = mNfcOperation.reaAddrToByte((byte) 0x14);
    if (tmp2 != null && tmp2.length >= 7) {
      for (int i = 0; i < tmp.length; i++) {
        tmp[i] = tmp2[i];
      }
    }
    return tmp;
  }

  /**
   * 写交接信息
   *
   * @return byte
   */
  public boolean writeExangeInfo(byte addr, byte[] data) {
    byte tmp[] = new byte[12];
    Arrays.fill(tmp, (byte) 0x0);
    for (int i = 0; i < data.length; i++) {
      tmp[i] = data[i];
    }
    return mNfcOperation.writeExangeInfo(addr, data);
  }

  /**
   * @param addr地址 data需要写的内容
   * @return Boolean 返回类型
   * @throws
   * @Title: writeAddr   一次只能写4个字节 即一个地址
   * @Description: 写入信息
   */
  public boolean writeAddr(byte addr, byte[] data) {
    if (mNfcOperation == null) {
      mNfcOperation = NfcOperation.getInstance();
      LogUtils.d(TAG + "---------------");
    }
    return mNfcOperation.writeAddr(addr, data);
  }

  public String getOrganization() {
    return organization;
  }

  public void setOrganization(String organization) {
    this.organization = organization;
  }

  public String getPeople() {
    return people;
  }

  public void setPeople(String people) {
    this.people = people;
  }

  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time;
  }

  public String getOperation() {
    return operation;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }

  public String getThing() {
    return thing;
  }

  public void setThing(String thing) {
    this.thing = thing;
  }

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }

  public String getNumber() {
    return number;
  }

  public void setNumber(String number) {
    this.number = number;
  }

  public String getCheck() {
    return check;
  }

  public void setCheck(String check) {
    this.check = check;
  }

  public String getBarValue() {
    return BarValue;
  }

  public void setBarValue(String barValue) {
    BarValue = barValue;
  }

  public String getReplayID() {
    return replayID;
  }

  public void setReplayID(String replayID) {
    this.replayID = replayID;
  }
}
