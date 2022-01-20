package com.fanfull.libhard.uhf;

import com.fanfull.libjava.io.ByteFrameDecoder;

/**
 * 超高频指令接收监听，增加分包-粘包处理
 */
public abstract class UhfSliceListener implements IUhfListener {
  ByteFrameDecoder frameDecoder;

  public UhfSliceListener() {
    this(1024, 2, 2, -4);
  }

  public UhfSliceListener(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
      int lengthAdjustment) {
    frameDecoder = new ByteFrameDecoder(
        maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment);
  }

  @Override public void onReceiveData(byte[] data, int len) {
    int decode = 0;
    do {
      decode = frameDecoder.decode(data, decode, len);
      if (0 <= decode) {
        byte[] res = frameDecoder.getAndClear();
        if (res != null) {
          onReceiveData(res);
        }
      }
    } while (0 < decode);
  }
}
