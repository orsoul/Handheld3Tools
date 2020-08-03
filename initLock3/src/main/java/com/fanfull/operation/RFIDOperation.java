package com.fanfull.operation;

import android.os.SystemClock;
import android.util.Log;
import com.apkfuns.logutils.LogUtils;
import com.fanfull.utils.ArrayUtils;
import com.hardware.Hardware;
import java.util.List;

/**
 * @author Zyp
 * @ClassName: MainOperation
 * @Description: 执行的主要操作
 * @date 2016-3-4 下午03:25:09
 */
public class RFIDOperation {
  private final static String TAG = RFIDOperation.class.getSimpleName();
  /** 是否显示所有调试信息，此举是以后区分软件调试版和产品版 */
  private boolean isDebug = true;
  /** 接收回复信息 */
  private byte[] buf = new byte[48];
  /** 读写命令的长度 */
  private int len;
  /** 保存m1卡的卡号，在认证的时候仍需要传入卡号 */
  byte[] mUidBuf = new byte[4];
  /** 激活命令 */
  private byte[] activatecard = new byte[] {
      (byte) 0x55, (byte) 0x55, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
      (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
      (byte) 0x00, (byte) 0x00,
      (byte) 0x00, (byte) 0xFF, (byte) 0x03, (byte) 0xFD, (byte) 0xD4, (byte) 0x14, (byte) 0x01,
      (byte) 0x17, (byte) 0x00
  };//RF
  /** 寻卡命令 */
  private byte[] findCard = new byte[] {
      (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0x04, (byte) 0xfc,
      (byte) 0xd4, (byte) 0x4a, (byte) 0x01, (byte) 0x00, (byte) 0xe1, (byte) 0x00
  };//寻卡
  /** 读数据命令 */
  private byte[] readcmd = new byte[] {
      (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x05, (byte) 0xFB, (byte) 0xD4, (byte) 0x40,
      (byte) 0x01, (byte) 0x30, (byte) 0x06, (byte) 0xB5, (byte) 0x00
  };
  /** 写数据命令 */
  private byte[] writecmd = new byte[] {
      (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0x15, (byte) 0xeb, (byte) 0xd4, (byte) 0x40,
      (byte) 0x01,
      (byte) 0xA0, (byte) 0x06, (byte) 0x0f, (byte) 0x0e, (byte) 0x0d, (byte) 0x0c, (byte) 0x0b,
      (byte) 0x0a, (byte) 0x09, (byte) 0x08, (byte) 0x07, (byte) 0x06, (byte) 0x05, (byte) 0x04,
      (byte) 0x03, (byte) 0x02, (byte) 0x01, (byte) 0x00, (byte) 0xcd, (byte) 0x00
  };//将数据写在06块，数据为00-0f
  /** 选择串口 */
  private final String[] mChooseSerial = { "/dev/ttyUSB0", "/dev/ttyUSB1" };
  /** 选择波特率 */
  private final int mChooseBuad = 115200;
  /** 文件描述符fd */
  public int fd = -1;
  /** 串口操作对象 */
  private final Hardware hardware;
  /** M1卡操作对象，定义静态的，单例模式 */
  private static RFIDOperation mRFIDMainOperation = null;

  /**
   * 构造方法，实例化串口操作对象
   */
  public RFIDOperation() {
    hardware = Hardware.getInstance();
  }

  /**
   * 获取单例的操作对象，同时上电，打开GPIO相关引脚
   *
   * @return 实例化的M1卡操作对象
   */

  //可以考虑用内部类的方式增加效率
      /*  private static class SingletonLoader {
            private static final RFIDOperation INSTANCE = new RFIDOperation();
      }

      public static RFIDOperation getInstance() {
           return SingletonLoader.INSTANCE;
      }*/
  public static RFIDOperation getInitInstance() {
    if (null == mRFIDMainOperation) {
      mRFIDMainOperation = new RFIDOperation();
    }
    mRFIDMainOperation.open();
    return mRFIDMainOperation;
  }

  public static RFIDOperation getInstance() {
    if (null == mRFIDMainOperation) {
      mRFIDMainOperation = new RFIDOperation();
    }
    return mRFIDMainOperation;
  }

  /**
   * 打开GPIO，以及拉低5号引脚，给高频模块上电
   */
  public void open() {
    hardware.openGPIO();
    hardware.setGPIO(0, 5);
    LogUtils.tag(TAG).d("rfid 已经上电");
  }

  /**
   * 激活，唤醒读卡器，即可开始寻卡，读写操作，上电后只需激活唤醒一次
   *
   * @return true 激活成功，false激活失败
   */
  public boolean workupcard() {// 激活,唤醒操作

    byte buf[] = new byte[48];
    while ((len = hardware.read(fd, buf, 48)) > 0) ;//clear buf
    len = hardware.write(fd, activatecard);
    if (len < 1) {
      LogUtils.d("write error");
    }
    SystemClock.sleep(100);
    if (hardware.select(fd, 0, 200) == 1) {
      len = hardware.read(fd, buf, 36);
    } else {
      LogUtils.tag(TAG).d("activatecard len ==0 fd=" + fd);
      return false;
    }
    if (checkFirst6Data(buf) && buf[6] == (byte) 0x00
        && buf[7] == (byte) 0x00 && buf[8] == (byte) 0xff
        && buf[9] == (byte) 0x02 && buf[10] == (byte) 0xfe
        && buf[11] == (byte) 0xd5 && buf[12] == (byte) 0x15
        && buf[13] == (byte) 0x16 && buf[14] == (byte) 0x00) {
      LogUtils.tag(TAG).d("已激活");
      return true;
    } else {
      LogUtils.tag(TAG).d("114 激活失败");
      return false;
    }
  }

  /**
   * @param @return 设定文件
   * @return String 返回类型
   * @throws
   * @Title: connection
   * @Description: 打开串口连接, 成功打开后，调用激活操作
   */
  public boolean connection() {
    if (fd != -1) hardware.close(fd);
    fd = hardware.openSerialPort(mChooseSerial[0], mChooseBuad, 8, 1);//打开串口
    if (fd == -1) {
      LogUtils.tag(TAG).d("open->ttyUSB>>>>>>>>>>faild");
      fd = hardware.openSerialPort(mChooseSerial[0], mChooseBuad, 8, 1);//打开串口
      if (fd == -1) {
        LogUtils.i("高频模块打开失败76！！！");
        return false;
      }
    }
    LogUtils.i("高频模块已打开！！   fd=" + fd);

    int n = 0;
    while (n++ < 10) {
      if (workupcard()) {//正常打开读卡器
        break;
      } else {
        LogUtils.tag(TAG).d("144 激活失败");
      }
    }
    if (n == 11) return false;
    return true;
  }

  /**
   * @return byte[] 返回类型 字节数组数据，即卡号，m1卡长度4个字节，NFC卡长度7个字节
   * @throws
   * @Title: activatecard
   * @Description: 寻卡操作
   */
  public byte[] activatecard() { //寻卡
    byte buf[] = new byte[50];
    while ((len = hardware.read(fd, buf, 48)) > 0) ;//clear buf
    len = hardware.write(fd, findCard);
    if (len < 1) {
      LogUtils.d("write cmd error fd=" + fd);
    }
    SystemClock.sleep(50);
    if (hardware.select(fd, 0, 200) == 1) {
      len = hardware.read(fd, buf, 36);
    } else {
      LogUtils.tag(TAG).d("activatecard len ==0 fd=" + fd);
      if (-1 == fd) connection();//再次初始化一次，纠错处理，不影响正常功能使用
      LogUtils.tag(TAG).v("174  null");
      return null;
    }
    if (checkFirst6Data(buf) && buf.length > 18 && buf[18] > 0) {
      if (buf[18] == 0x04 && buf.length > 22) {
        mUidBuf = new byte[] { buf[19], buf[20], buf[21], buf[22] };
        return mUidBuf;
      } else if (buf[18] == 0x07 && buf.length > 25) {
        byte[] id = new byte[] { buf[19], buf[20], buf[21], buf[22], buf[23], buf[24], buf[25] };
        return id;
      }
    } else {
      LogUtils.tag(TAG).v("187  null: " + ArrayUtils.bytesToHexString(buf));
      return null;
    }
    LogUtils.tag(TAG).v("190  null");
    return null;
  }

  /**
   * @param blokID 块区编号
   * @return true 认证成功，false认证失败
   * @Description: 对卡进行认证
   */
  private boolean authKey(int blokID) { //int blokID  mUidBuf
    if ((mUidBuf = activatecard()) == null || mUidBuf.length == 7) {//先寻卡,且寻到的是M1卡，才需要认证操作
      return false;
    }
    byte[] authKey = new byte[] {
        (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0x0f, (byte) 0xf1, (byte) 0xd4, (byte) 0x40,
        (byte) 0x01,
        (byte) 0x60, (byte) 0x07, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
        (byte) 0xff, mUidBuf[3], mUidBuf[2], mUidBuf[1], mUidBuf[0], (byte) 0xc2, (byte) 0x00
    };
    authKey[9] = (byte) blokID; // 块区
    authKey[20] = getCheckNumber(15, authKey);// 校验位
    len = hardware.write(fd, authKey);
    LogUtils.tag(TAG).d(ArrayUtils.bytesToHexString(authKey));
    if (len < 1) {
      LogUtils.d("write error");
    }
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {

      e.printStackTrace();
    }
    byte buf[] = new byte[16];
    if (hardware.select(fd, 1, 0) == 1) {
      len = hardware.read(fd, buf, 16);
      if (len < 1) {
        LogUtils.d("read error");
      }
      LogUtils.d(ArrayUtils.bytesToHexString(buf) + "\n");
      if (checkFirst6Data(buf)
          && buf[6] == (byte) 0x00
          && buf[7] == (byte) 0x00
          && buf[8] == (byte) 0xff
          && buf[9] == (byte) 0x03
          && buf[10] == (byte) 0xfd
          && buf[11] == (byte) 0xd5
          && buf[12] == (byte) 0x41
          && buf[13] == (byte) 0x00
          && buf[14] == (byte) 0xea
          && buf[15] == (byte) 0x00) {
        LogUtils.tag(TAG).d("209-认证通过");
        return true;
      } else {
        return false;
      }
    }
    return false;
  }

  /**
   * 通过传入命令，以及数据位的长度，得到最后的检验和，校验和 和数据位总和异或为0
   *
   * @param n 命令数据位的长度
   * @param cmd 命令
   * @return 字节数据，得到校验和
   */
  private byte getCheckNumber(int n, byte cmd[]) {
    byte a = 0x00;
    for (int i = 0; i < n; i++) {
      a += cmd[i + 5];
    }
    String string = Integer.toHexString(~(0xFF & a) + 1);
    if (string != null && string.equals("0")) {
      return (byte) 0x00;
    }
    if (string.length() > 2) {
      string = (String) string.subSequence(string.length() - 2, string.length());
    }
    byte[] ab = hexStringToBytes(string);
    return (byte) ab[0];
  }

  /**
   * 将一个字符串转化成一个字节数组
   *
   * @param hexString the hex string
   * @return byte[]
   */
  public byte[] hexStringToBytes(String hexString) {
    if (hexString == null || hexString.equals("")) {
      return null;
    }
    hexString = hexString.toUpperCase();
    int length = hexString.length() / 2;
    char[] hexChars = hexString.toCharArray();
    byte[] d = new byte[length];
    for (int i = 0; i < length; i++) {
      int pos = i * 2;
      d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
    }
    return d;
  }

  /**
   * 将一个字符转化成字节
   *
   * @param c char
   * @return byte
   */
  private byte charToByte(char c) {
    return (byte) "0123456789ABCDEF".indexOf(c);
  }

  /**
   * 验证回复的信息的前六位是否准确
   *
   * @param buf 命令返回的信息
   */
  private boolean checkFirst6Data(byte[] buf) {
    if (null == buf || buf.length < 6) {
      return false;
    }
    if (buf[0] == (byte) 0x00 && buf[1] == (byte) 0x00 && buf[2] == (byte) 0xff
        && buf[3] == (byte) 0x00 && buf[4] == (byte) 0xff && buf[5] == (byte) 0x00) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * @return String 返回类型
   * @throws
   * @Title: 读块区
   * @Description:读取块区内容
   */
  public String readBlock(int blockID) {
    if (activatecard() == null) {
      Log.d(TAG, "寻卡失败");
      return null;
    }
    byte buf[] = new byte[40];
    while ((len = hardware.read(fd, buf, 48)) > 0) ;// clear buf
    // 00 00 ff 05 fb D4 40 01 30 06 B5 00 //读第7块
    if (!authKey(blockID)) {
      LogUtils.tag(TAG).d("第" + blockID + "块认证失败");
      return null;
    }// 认证确认读写密码通过
    readcmd[9] = (byte) blockID;
    readcmd[10] = getCheckNumber(5, readcmd);
    len = hardware.write(fd, readcmd);
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    if (hardware.select(fd, 1, 0) == 1) {
      len = hardware.read(fd, buf, 40);
      LogUtils.d(len);
      if (len < 1) {
        LogUtils.d("read error");
      }
      LogUtils.d("read data:" + ArrayUtils.bytesToHexString(buf) + "\n");
      //333----0000FF00FF000000FF03FDD54113D7000408048AD8B06854000000
      if (checkFirst6Data(buf) && buf[12] == 0x41 && buf[13] == 0x00) {
        byte[] readbuf;
        readbuf = new byte[16];
        for (int i = 0; i < 16; i++) {
          readbuf[i] = buf[i + 14];
        }
        return ArrayUtils.bytesToHexString(readbuf);
      } else {
        LogUtils.tag(TAG).d("read data error....");//读取数据错误，可能是认证不成功
        return null;
      }
    } else {
      LogUtils.tag(TAG).d("no data read");
      return null;
    }
  }

  /**
   * @return byte[] 返回类型
   * @throws
   * @Title: 读块区
   * @Description:读取块区内容
   */
  public byte[] readBlockToByte(int blockID) {
    byte buf[] = new byte[40];
    while ((len = hardware.read(fd, buf, 48)) > 0) ;// clear buf
    // 00 00 ff 05 fb D4 40 01 30 06 B5 00 //读第7块
    if (!authKey(blockID)) {
      LogUtils.tag(TAG).d("第" + blockID + "块认证失败");
      return null;
    }// 认证确认读写密码通过
    readcmd[9] = (byte) blockID;
    readcmd[10] = getCheckNumber(5, readcmd);
    len = hardware.write(fd, readcmd);
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    if (hardware.select(fd, 1, 0) == 1) {
      len = hardware.read(fd, buf, 40);
      LogUtils.d(len);
      if (len < 1) {
        LogUtils.d("read error");
      }
      LogUtils.d("read data:" + ArrayUtils.bytesToHexString(buf) + "\n");
      //333----0000FF00FF000000FF03FDD54113D7000408048AD8B06854000000
      if (checkFirst6Data(buf) && buf[12] == 0x41 && buf[13] == 0x00) {
        byte[] readbuf;
        readbuf = new byte[16];
        for (int i = 0; i < 16; i++) {
          readbuf[i] = buf[i + 14];
        }
        return readbuf;
      } else {
        LogUtils.tag(TAG).d("read data error....");//读取数据错误，可能是认证不成功
        return null;
      }
    } else {
      LogUtils.tag(TAG).d("no data read");
      return null;
    }
  }

  /**
   * @param @return 设定文件
   * @return Boolean 返回类型
   * @throws
   * @Title: writeBlock
   * @Description: 写入袋码
   */
  public Boolean writeBlock(int blockID, String index) {
    if (authKey(blockID)) {
      LogUtils.d("renzhengchengg");
      while ((len = hardware.read(fd, buf, 48)) > 0) ;// clear buf
      // 拼接命令
      writecmd[9] = (byte) blockID;
      for (int i = 0; i < index.length(); i++) {
        byte b = (byte) index.charAt(i);
        writecmd[10 + i] = (byte) b;
        // LogUtils.d(b);
      }

      writecmd[writecmd.length - 2] = getCheckNumber(writecmd.length - 7, writecmd);
      len = hardware.write(fd, writecmd);
      SystemClock.sleep(100);
      if (hardware.select(fd, 1, 0) == 1) {
        len = hardware.read(fd, buf, 36);
        if (len < 1) {
          LogUtils.d("read error");
        }
        LogUtils.d("write data:" + ArrayUtils.bytesToHexString(buf) + "\n");
        if (checkFirst6Data(buf)
            && buf[6] == (byte) 0x00
            && buf[7] == (byte) 0x00
            && buf[8] == (byte) 0xff
            && buf[9] == (byte) 0x03
            && buf[10] == (byte) 0xfd
            && buf[11] == (byte) 0xd5
            && buf[12] == (byte) 0x41
            && buf[13] == (byte) 0x00
            && buf[14] == (byte) 0xea
            && buf[15] == (byte) 0x00) {
          return true;
        } else {
          LogUtils.tag(TAG).d("write data error....");//写入数据错误，可能是认证不成功
          return false;
        }
      } else {
        LogUtils.tag(TAG).d("no data read");
        return false;
      }
    } else {
      return false;
    }
  }

  /**
   * @return Boolean 返回类型
   * @throws
   * @Title: writeBlock
   * @Description: 写入袋码
   */
  public Boolean writeBlock(int blockID, byte[] data) {
    if (activatecard() == null) {
      Log.d(TAG, "寻卡失败");
      return false;
    }
    Log.d(TAG, "开始认证");
    if (authKey(blockID)) {
      while ((len = hardware.read(fd, buf, 48)) > 0) ;// clear buf
      // 拼接命令
      writecmd[9] = (byte) blockID;
      for (int i = 0; i < data.length; i++) {
        byte b = (byte) data[i];
        writecmd[10 + i] = (byte) b;
      }
      writecmd[writecmd.length - 2] = getCheckNumber(writecmd.length - 7, writecmd);
      len = hardware.write(fd, writecmd);
      SystemClock.sleep(100);
      if (hardware.select(fd, 1, 0) == 1) {
        len = hardware.read(fd, buf, 36);
        LogUtils.d(len);
        if (len < 1) {
          LogUtils.d("read error");
        }
        LogUtils.d("write data:" + ArrayUtils.bytesToHexString(buf) + "\n");
        if (checkFirst6Data(buf)
            && buf[6] == (byte) 0x00
            && buf[7] == (byte) 0x00
            && buf[8] == (byte) 0xff
            && buf[9] == (byte) 0x03
            && buf[10] == (byte) 0xfd
            && buf[11] == (byte) 0xd5
            && buf[12] == (byte) 0x41
            && buf[13] == (byte) 0x00
            && buf[14] == (byte) 0xea
            && buf[15] == (byte) 0x00) {
          LogUtils.tag(TAG).d("写入成功");
          return true;
        } else {
          LogUtils.tag(TAG).d("write data error....");//写入数据错误，可能是认证不成功
          return false;
        }
      } else {
        LogUtils.tag(TAG).d("no data read");
        return false;
      }
    } else {
      return false;
    }
  }

  /**
   * @param blockID 区块
   * @param data 要写入的数据
   * @Description: 复写索引
   */
  public Boolean writeindex(int blockID, byte[] data) {
    if (authKey(blockID)) {
      while ((len = hardware.read(fd, buf, 48)) > 0) ;// clear buf
      // 拼接命令
      writecmd[9] = (byte) blockID;
      for (int i = 0; i < data.length; i++) {
        byte b = (byte) data[i];
        writecmd[10 + i] = (byte) b;
        // LogUtils.d(b);
      }
      writecmd[writecmd.length - 3] = (byte) 0x88;
      writecmd[writecmd.length - 2] = getCheckNumber(writecmd.length - 7, writecmd);
      LogUtils.d("write data:" + ArrayUtils.bytesToHexString(writecmd) + "\n");
      len = hardware.write(fd, writecmd);
      SystemClock.sleep(100);
      if (hardware.select(fd, 1, 0) == 1) {
        len = hardware.read(fd, buf, 36);
        if (len < 1) {
          LogUtils.d("read error");
        }
        LogUtils.d("write data:" + ArrayUtils.bytesToHexString(buf) + "\n");
        if (checkFirst6Data(buf)
            && buf[6] == (byte) 0x00
            && buf[7] == (byte) 0x00
            && buf[8] == (byte) 0xff
            && buf[9] == (byte) 0x03
            && buf[10] == (byte) 0xfd
            && buf[11] == (byte) 0xd5
            && buf[12] == (byte) 0x41
            && buf[13] == (byte) 0x00
            && buf[14] == (byte) 0xea
            && buf[15] == (byte) 0x00) {
          return true;
        } else {
          LogUtils.tag(TAG).d("write data error....");//写入数据错误，可能是认证不成功
          return false;
        }
      } else {
        LogUtils.tag(TAG).d("no data read");
        return false;
      }
    } else {
      return false;
    }
  }

  //    public int readBagLock (){
  //        if(activatecard() == null){
  //            return -1;
  //        }
  //        int n = -1;
  //        try {
  //            n = Integer.parseInt(readBlock(9).charAt(0)+"");
  //            Log.d(TAG, "n="+n);
  //            return n;
  //        } catch (Exception e) {
  //        }
  //        return -1;
  //    }

  /**
   * @return String 返回类型
   * @throws
   * @Title: 读块区 ,读第5块区的数据（在封签操作的时候需要把扫描到的条码内容写入到第5块去，这里在入库的时候需要读取第5块区的数据）
   * @Description:读取块区内容
   */
  public String readBarCode() {
    if (activatecard() == null) {
      return null;
    }
    while ((len = hardware.read(fd, buf, 48)) > 0) ;// clear buf
    String strData = null;
    String strData2 = null;
    strData = readBlock(5);
    if (null != strData) {
      strData2 = readBlock(6);
      if (strData2 != null) {
        strData += strData2.substring(0, 6);
      }
    }
    return strData;
  }

  /**
   * @return String 返回类型
   * @throws
   * @Title: 读块区 ,读第5块区的数据（在封签操作的时候需要把扫描到的条码内容写入到第5块去，这里在入库的时候需要读取第5块区的数据）
   * @Description:读取块区内容
   */
  public String readBagBarCode() {
    if (activatecard() == null) {
      return null;
    }
    while ((len = hardware.read(fd, buf, 64)) > 0) ;// clear buf
    String strData = null;
    String strData2 = null;
    strData = readBlock(5);
    if (null != strData) {
      strData2 = readBlock(6);
      if (strData2 != null) {
        strData += strData2.substring(0, 18);//封签事件码共50位 id(26)+封签码（12）+时间（6）
      }
    }
    return strData;
  }

  /**
   * @param @return 设定文件
   * @return Boolean 返回类型
   * @throws
   * @Title: writeBarCode
   * @Description: 写入条码
   */
  public boolean writeBarCode(List<byte[]> data) {
    if (activatecard() == null) {
      return false;
    }
    if (!authKey(5)) {
      LogUtils.tag(TAG).d("第" + 5 + "块认证失败");
      return false;
    }// 认证确认读写密码通过
    if (writeBarcardToM1(5, data.get(0))) {
      if (!authKey(5)) {
        LogUtils.tag(TAG).d("第" + 6 + "块认证失败");
        return false;
      }// 认证确认读写密码通过
      if (writeBarcardToM1(6, data.get(1))) {
        return true;//这里才是写入数据到M1卡数据成功
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  /**
   * 往指定扇区中写入数据
   *
   * @param n 指定扇区
   * @param data 数据内容
   * @return true 写入成功，false 写入失败
   */
  private boolean writeBarcardToM1(int n, byte data[]) {
    writecmd[9] = (byte) n;
    byte[] fifth = data;
    for (int i = 0; i < fifth.length; i++) {
      writecmd[10 + i] = fifth[i];
    }
    writecmd[writecmd.length - 2] = getCheckNumber(writecmd.length - 7, writecmd);
    while ((len = hardware.read(fd, buf, 48)) > 0) ;
    len = hardware.write(fd, writecmd);
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    if (hardware.select(fd, 1, 0) == 1) {
      len = hardware.read(fd, buf, 36);
      if (len < 1) {
        LogUtils.d("read error");
      } else {
        LogUtils.d(ArrayUtils.bytesToHexString(buf) + "\n");
        if (checkFirst6Data(buf)
            && buf[6] == (byte) 0x00
            && buf[7] == (byte) 0x00
            && buf[8] == (byte) 0xff
            && buf[9] == (byte) 0x03
            && buf[10] == (byte) 0xfd
            && buf[11] == (byte) 0xd5
            && buf[12] == (byte) 0x41
            && buf[13] == (byte) 0x00
            && buf[14] == (byte) 0xea
            && buf[15] == (byte) 0x00) {
          LogUtils.d("blockwancheng--" + n);
          return true;
        } else {
          return false;
        }
      }
    } else {
      LogUtils.d("meishuju");
      return false;
    }
    return false;
  }

  /**
   * @return void 返回类型
   * @throws
   * @Title: close
   * @Description: 关闭串口, 关闭高频模块
   */
  public void close() {
    int n = 0;
    while (n++ < 10) {
      if (closeRf()) {//正常关闭读卡器
        break;
      }
    }
    hardware.setGPIO(1, 5);
    hardware.closeGPIO();
    hardware.close(fd);
    mRFIDMainOperation = null;
    LogUtils.tag(TAG).d("---closeRFID 高频已经关闭----");
  }

  /**
   * @return void 返回类型
   * @throws
   * @Title: uclose
   * @Description: 异常关闭串口
   */
  public void uclose() {
    hardware.setGPIO(1, 5);
  }

  /**
   * @return void 返回类型
   * @Title: closeRf
   * @Description:正常关闭高频模块
   */
  public boolean closeRf() {//关闭RF
    byte[] close = new byte[] {
        (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x04, (byte) 0xFC,
        (byte) 0xD4, (byte) 0x32, (byte) 0x01, (byte) 0x00, (byte) 0xF9, (byte) 0x00
    };
    len = hardware.write(fd, close);
    if (len < 1) {
      LogUtils.d("write error");
    }
    SystemClock.sleep(20);
    if (hardware.select(fd, 0, 50) == 1) {
      len = hardware.read(fd, buf, 36);
      LogUtils.d(len + "");
      if (len < 1) {
        LogUtils.d("read error");
      }//66 84 01
      if (buf[0] == (byte) 0x00 && buf[11] == (byte) 0xD5 && buf[12] == (byte) 0x33) {
        LogUtils.tag(TAG).d("关闭成功");
        return true;
      } else {
        LogUtils.tag(TAG).d("关闭不成功");
      }
    } else {
      LogUtils.tag(TAG).d("关闭不成功");
    }
    return false;
  }
}
