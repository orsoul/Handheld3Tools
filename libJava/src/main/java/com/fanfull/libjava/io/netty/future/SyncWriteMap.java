package com.fanfull.libjava.io.netty.future;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SyncWriteMap {

  public static Map<Object, MsgFuture> syncKey = new ConcurrentHashMap<>();
}
