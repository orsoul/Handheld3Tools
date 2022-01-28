package com.fanfull.libjava.io.socketClient;

import com.fanfull.libjava.io.socketClient.interf.IReceiveListener;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 将字节数组 转成字符串,默认采用 UTF_8
 */
public interface ReceiveStringListener extends IReceiveListener<String> {

  @Override default String convert(byte[] data, int len) {
    return new String(data, 0, len, getCharset());
  }

  default Charset getCharset() {
    return StandardCharsets.UTF_8;
  }
}