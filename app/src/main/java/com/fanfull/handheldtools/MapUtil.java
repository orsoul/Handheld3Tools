package com.fanfull.handheldtools;

import com.fanfull.libjava.util.Logs;

import java.util.HashMap;
import java.util.Map;

public class MapUtil<K> {
  private Map<K, Integer> map;

  public MapUtil(Map<K, Integer> map) {
    this.map = map;
  }

  public Map<K, Integer> getMap() {
    return map;
  }

  public Integer add(K key) {
    if (map == null) {
      return -1;
    }

    int num;
    if (map.containsKey(key)) {
      num = map.get(key) + 1;
    } else {
      num = 1;
    }
    Integer put = map.put(key, num);
    //Logs.out("add:%s=%s",key, put);
    return num;
  }

  public int getNum(K key) {
    if (map == null) {
      return -1;
    }
    if (!map.containsKey(key)) {
      return 0;
    }
    return map.get(key);
  }

  public void clear() {
    if (map != null) {
      map.clear();
    }
  }

  public String getFormatString() {
    if (map == null) {
      return null;
    }
    if (map.isEmpty()) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<K, Integer> entry : map.entrySet()) {
      sb.append(entry.getKey()).append(':').append(entry.getValue()).append('\n');
    }
    return sb.toString();
  }

  public static void main(String[] args) {

    HashMap<String, Integer> map = new HashMap<>();
    String str = new String("123");
    //String s = str;

    MapUtil<String> mapUtil = new MapUtil<>(map);

    Logs.out("map:%s", map);
    map.put("aa", 1);
    //map.put("bb", 1);

    mapUtil.add("aa");
    mapUtil.add("bb");

    Logs.out("\n%s", mapUtil.getFormatString());
  }
}
