package com.fanfull.libjava.io;

import com.fanfull.libjava.util.BytesUtil;
import com.fanfull.libjava.util.Logs;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class ByteFrameDecoder extends BaseByteFrameDecoder {

  private final ByteBuffer buffer;
  private int needDataLen;

  public ByteFrameDecoder(
      int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment) {
    super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment);
    buffer = ByteBuffer.allocate(maxFrameLength);
  }

  public boolean haveData() {
    return 0 < buffer.position();
  }

  public void clearData() {
    buffer.clear();
  }

  public void put(byte[] data, int offset, int len) {
    buffer.clear();
    buffer.put(data, offset, len);
  }

  public void add(byte[] data, int offset, int len) {
    buffer.put(data, offset, len);
  }

  public void getAndClear(byte[] dst, int offset, int len) {
    buffer.position(0);
    buffer.get(dst, offset, len);
    buffer.clear();
  }

  public byte[] getAndClear() {
    byte[] reVal;
    if (result == null) {
      reVal = new byte[buffer.position()];
      buffer.position(0);
      buffer.get(reVal);
      buffer.clear();
    } else {
      reVal = result;
    }
    result = null;
    return reVal;
  }

  byte[] result;

  @Override public int decode(byte[] data, int start, int end) {
    if (data == null || start < 0 || end <= start || data.length < end) {
      // 参数错误
      return -1;
    }
    int dataLen = end - start;

    /* ====== 1 新包 ====== */
    if (needDataLen == 0) {
      if (dataLen < lengthFieldEndOffset) {
        return -2;
      }

      int frameLen =
          BytesUtil.bytes2Long(data, start + lengthFieldOffset, lengthFieldLength).intValue() +
              lengthFieldEndOffset +
              lengthAdjustment;
      if (frameLen < 0 || maxFrameLength < frameLen) {
        return -3;
      }

      if (frameLen == dataLen) {
        // 1.1 完整包
        result = Arrays.copyOfRange(data, start, end);
        return 0;
      } else if (frameLen < dataLen) {
        // 1.2 多于1个完整包
        int e = start + frameLen;
        result = Arrays.copyOfRange(data, start, e);
        return e;
      } else {
        // 1.3 半包
        put(data, start, dataLen);
        needDataLen = frameLen - dataLen;
        return -4;
      }
    }

    /* ====== 2 之前有半包 ====== */
    if (needDataLen == dataLen) {
      // 2.1 完整包
      add(data, start, needDataLen);
      result = getAndClear();
      return 0;
    } else if (needDataLen < dataLen) {
      // 2.2 多于1个完整包
      add(data, start, needDataLen);
      result = getAndClear();
      int e = start + needDataLen;
      needDataLen = 0;
      return e;
    } else {
      // 2.3 半包
      needDataLen -= dataLen;
      add(data, start, dataLen);
      //return start + dataLen;
      return -4;
    }
  }

  static void test() {
    List<byte[]> dataList = new ArrayList<>();

    /* 1包 */
    //dataList.add(new byte[]{0x55, 0x55, 0x00, 0x07, 0x77, 0x01, 0x02,});

    /* 2包 */
    //dataList.add(new byte[]{0x11, 0x11, 0x00, 0x07, 0x77, 0x01, 0x02,
    //    0x22, 0x22, 0x00, 0x07, 0x7E, 0x01, 0x02,
    //});

    ///* 3包 */
    //dataList.add(
    //    new byte[]{0x11, 0x11, 0x00, 0x04,
    //        0x22, 0x22, 0x00, 0x07, 0x7E, 0x01, 0x02,
    //        0x33, 0x33, 0x00, 0x04,
    //    });

    /* 2合1 包 */
    //dataList.add(new byte[]{0x11, 0x11, 0x00, 0x07,});
    //dataList.add(new byte[]{0x77, 0x01, 0x02,});

    /* 4合1 包 */
    //dataList.add(new byte[]{0x11, 0x11, 0x00, 0x0A,});
    //dataList.add(new byte[]{0x06, 0x06, 0x06,});
    //dataList.add(new byte[]{0x06, 0x06,});
    //dataList.add(new byte[]{0x06,});

    /* 1包-半包，半包 */
    //dataList.add(new byte[]{0x11, 0x11, 0x00, 0x06, 0x02, 0x02, 0x22, 0x22, 0x00, 0x07,});
    //dataList.add(new byte[]{0x03, 0x03, 0x03,});

    /* 1包-半包，半包，半包 */
    //dataList.add(new byte[]{0x11, 0x11, 0x00, 0x06, 0x02, 0x02, 0x22, 0x22, 0x00, 0x07,});
    //dataList.add(new byte[]{0x03, 0x03,});
    //dataList.add(new byte[]{0x03,});

    /* 半包，半包-1包-半包，半包，半包 */
    dataList.add(new byte[]{0x11, 0x11, 0x00, 0x05,});
    dataList.add(new byte[]{0x01, 0x22, 0x22, 0x00, 0x06, 0x02, 0x02, 0x33, 0x33, 0x00, 0x07,});
    dataList.add(new byte[]{0x03, 0x03,});
    dataList.add(new byte[]{0x03,});

    //test(dataList);
    testDecode(dataList);
  }

  private static void testDecode(List<byte[]> dataList) {
    ByteFrameDecoder decoder = new ByteFrameDecoder(1024, 2, 2, -4);
    for (byte[] data : dataList) {
      Logs.out("====== %s ======", data.length);
      int decode = 0;
      do {
        decode = decoder.decode(data, decode, data.length);
        Logs.out("res:%s", decode);
        if (0 <= decode) {
          byte[] res = decoder.getAndClear();
          Logs.out("msg:%s", BytesUtil.bytes2HexString(res));
        }
      } while (0 < decode);

      //if (data.length == decode) {
      //  Logs.out("1个整包:%s", decode);
      //} else if (0 < decode) {
      //  Logs.out("多于整包:%s", decode);
      //} else if (0 == decode) {
      //  Logs.out("头半包:%s", decode);
      //} else if (-1 == decode) {
      //  Logs.out("参数错误:%s", decode);
      //} else if (-2 == decode) {
      //  Logs.out("半包:%s", decode);
      //} else {
      //  Logs.out("长度错误:%s", decode);
      //}
    }
  }

  public static void main(String[] args) {
    test();
  }
}
