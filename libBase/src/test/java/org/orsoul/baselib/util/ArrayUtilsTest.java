package org.orsoul.baselib.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  @Test
  public void hexString2Bytes() {

    List<String> list = new ArrayList<>();

    /* 测试非法输入 */
    list.add(null);
    list.add("");
    list.add("123");
    list.add("az");
    for (String s : list) {
      byte[] bytes = ArrayUtils.hexString2Bytes(s);
      Assert.assertNull(bytes);
    }
    Assert.assertNull(ArrayUtils.bytes2HexString(null));
    Assert.assertNull(ArrayUtils.bytes2HexString(new byte[1], 1, 2));
    Assert.assertNull(ArrayUtils.bytes2HexString(new byte[2], 0, 3));
    Assert.assertEquals(ArrayUtils.bytes2HexString(new byte[0]), "");

    //String strTest;
    //byte[] dataTest;
    //String str;
    //byte[] data;
    //strTest = "00Ff12";
    //dataTest = new byte[] { 0, -1, 18 };
    //data = ArrayUtils.hexString2Bytes(strTest);
    //Assert.assertArrayEquals(data, dataTest);

    Map<String, byte[]> map = new HashMap<>();
    map.put("00Ff12", new byte[] { 0, (byte) 0xFF, 0x12 });
    map.put("fFaABbCcDdEe1234567890", new byte[] {
        (byte) 0xFF, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0x12,
        (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x90
    });
    map.put("1234567890abcdef", new byte[] { 18, 52, 86, 120, -112, -85, -51, -17 });
    //map.put("00Ff12", new byte[] { 0, -1, 18 });

    for (Map.Entry<String, byte[]> entry : map.entrySet()) {
      String str = ArrayUtils.bytes2HexString(entry.getValue());
      Assert.assertEquals(str, entry.getKey().toUpperCase());

      byte[] bytes = ArrayUtils.hexString2Bytes(entry.getKey());
      Assert.assertArrayEquals(bytes, entry.getValue());
    }

    list.clear();
    list.add("00Ff12");
    list.add("fFaABbCcDd1234567890");
    list.add("1234567890abcdef");
    list.add("1234567890ABCDEF");
    for (String s : list) {
      byte[] bytes = ArrayUtils.hexString2Bytes(s);
      System.out.println(Arrays.toString(bytes));
      String s1 = ArrayUtils.bytes2HexString(bytes);
      Assert.assertEquals(s1, s.toUpperCase());
    }
  }
}