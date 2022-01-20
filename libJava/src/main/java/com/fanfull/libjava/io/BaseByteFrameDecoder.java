package com.fanfull.libjava.io;

import com.fanfull.libjava.util.BytesUtil;
import com.fanfull.libjava.util.Logs;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class BaseByteFrameDecoder {
  /** 消息的 最大长度 */
  protected final int maxFrameLength;
  /** 长度字段所在位置 的偏移量，即指令头1长度 */
  protected final int lengthFieldOffset;
  /** 长度字段 宽度 */
  protected final int lengthFieldLength;
  /** 长度修正 */
  protected final int lengthAdjustment;

  /** 长度字段 末尾所在位置，lengthFieldOffset + lengthFieldLength */
  protected final int lengthFieldEndOffset;

  public BaseByteFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
    this(maxFrameLength, lengthFieldOffset, lengthFieldLength, 0);
  }

  public BaseByteFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
      int lengthAdjustment) {
    this.maxFrameLength = maxFrameLength;
    this.lengthFieldOffset = lengthFieldOffset;
    this.lengthFieldLength = lengthFieldLength;
    this.lengthAdjustment = lengthAdjustment;

    this.lengthFieldEndOffset = lengthFieldOffset + lengthFieldLength;
  }

  public int decode(byte[] data, int start, int end) {
    if (data == null || start < 0 || end <= start || data.length < end) {
      // 参数错误
      return -1;
    }

    int dataLen = end - start;
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
      // 1.1 个完整包
      return end;
    } else if (frameLen < dataLen) {
      // 1.2 多于1个完整包
      return start + frameLen;
    } else {
      // 1.3 半包
      return 0;
    }
  }

  static void test() {
    List<byte[]> dataList = new ArrayList<>();

    ///* 1包 */
    //dataList.add(new byte[]{0x55, 0x55, 0x00, 0x07, 0x77, 0x01, 0x02,});
    ///* 2包 */
    //dataList.add(new byte[]{0x11, 0x11, 0x00, 0x07, 0x77, 0x01, 0x02,
    //    0x22, 0x22, 0x00, 0x07, 0x7E, 0x01, 0x02,
    //});
    ///* 3包 */
    //dataList.add(
    //    new byte[]{0x11, 0x11, 0x00, 0x07, 0x77, 0x01, 0x02,
    //        0x22, 0x22, 0x00, 0x07, 0x7E, 0x01, 0x02,
    //        0x33, 0x33, 0x00, 0x07, 0x7E, 0x01, 0x02,
    //    });

    /* 2合1 包 */
    //dataList.add(new byte[]{0x11, 0x11, 0x00, 0x07,});
    //dataList.add(new byte[]{0x77, 0x01, 0x02,});

    /* 3合1 包 */
    //dataList.add(new byte[]{0x11, 0x11, 0x00, 0x08,});
    //dataList.add(new byte[]{0x77, 0x01,});
    //dataList.add(new byte[]{0x02, 0x03,});

    /* 1包-半包，半包 */
    //dataList.add(new byte[]{0x11, 0x11, 0x00, 0x06, 0x01, 0x01, 0x11, 0x11, 0x00, 0x06, });
    //dataList.add(new byte[]{0x02, 0x02,});

    /* 1包-半包，半包，半包 */
    dataList.add(new byte[]{0x11, 0x11, 0x00, 0x06, 0x01, 0x01, 0x11, 0x11, 0x00, 0x07,});
    dataList.add(new byte[]{0x03, 0x03,});
    dataList.add(new byte[]{0x03,});

    //test(dataList);
    testDecode(dataList);
  }

  private static void testDecode(List<byte[]> dataList) {
    BaseByteFrameDecoder decoder = new BaseByteFrameDecoder(1024, 2, 2, -4);
    for (byte[] data : dataList) {
      int decode = decoder.decode(data, 0, data.length);
      if (data.length == decode) {
        Logs.out("1个整包:%s", decode);
      } else if (0 < decode) {
        Logs.out("多于整包:%s", decode);
      } else if (0 == decode) {
        Logs.out("头半包:%s", decode);
      } else if (-1 == decode) {
        Logs.out("参数错误:%s", decode);
      } else if (-2 == decode) {
        Logs.out("半包:%s", decode);
      } else {
        Logs.out("长度错误:%s", decode);
      }
    }
  }

  public static void main(String[] args) {
    test();
    //testBuffer();
  }

  private static void testBuffer() {
    byte[] data = BytesUtil.hexString2Bytes(
        "A55A0019833000EFAC800000048400145A0100FD5901030D0A");
    //ByteBuffer buffer = ByteBuffer.wrap(data);
    ByteBuffer buffer = ByteBuffer.allocate(data.length);
    short len = buffer.getShort(2);

    Logs.out("len:%s,limit:%s", len, buffer.limit());
    Logs.out("position:%s", buffer.position());
  }
}
