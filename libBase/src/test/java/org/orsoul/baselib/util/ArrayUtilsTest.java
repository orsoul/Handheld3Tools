package org.orsoul.baselib.util;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class ArrayUtilsTest {

  @Test
  public void long2Bytes() {
    List<Long> list = new ArrayList<>();

    list.add(Long.MAX_VALUE);
    list.add(Long.MIN_VALUE);
    list.add(0L);
    list.add(-1L);
    list.add(1L);

    for (Long aLong : list) {
      byte[] data = ArrayUtils.long2Bytes(aLong, 8);
      Assert.assertEquals(String.format("%016X", aLong), ArrayUtils.bytes2HexString(data));
    }

    list.clear();
    list.add((long) Integer.MAX_VALUE);
    list.add((long) Integer.MIN_VALUE);
    list.add(0L);
    list.add(-1L);
    list.add(1L);
    for (Long aLong : list) {
      byte[] data = ArrayUtils.long2Bytes(aLong, 4);
      Assert.assertEquals(String.format("%08X", aLong), ArrayUtils.bytes2HexString(data));
    }
  }

  @Test
  public void bytes2Long() {
    List<byte[]> list = new ArrayList<>();

    list.add(new byte[] { (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, });

    for (byte[] data : list) {
      long res = ArrayUtils.bytes2Long(data, 0, data.length);
      Assert.assertEquals(String.format("%08X", res), ArrayUtils.bytes2HexString(data));
    }
  }
}