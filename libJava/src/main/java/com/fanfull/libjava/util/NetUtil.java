package com.fanfull.libjava.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sun.net.util.IPAddressUtil;

public class NetUtil {
  public static byte[] ip4ToArr(int ip4) {
    byte[] addr = new byte[4];
    addr[0] = (byte) ((ip4 >>> 24) & 0xFF);
    addr[1] = (byte) ((ip4 >>> 16) & 0xFF);
    addr[2] = (byte) ((ip4 >>> 8) & 0xFF);
    addr[3] = (byte) (ip4 & 0xFF);
    return addr;
  }

  public static byte[] ip4ToArr(String ip4) {
    return IPAddressUtil.textToNumericFormatV4(ip4);
  }

  public static int ip4ToInt(byte[] ip4) {
    int address = ip4[3] & 0xFF;
    address |= ((ip4[2] << 8) & 0xFF00);
    address |= ((ip4[1] << 16) & 0xFF0000);
    address |= ((ip4[0] << 24) & 0xFF000000);
    return address;
  }

  public static int ip4ToInt(String ip4) {
    return ip4ToInt(ip4ToArr(ip4));
  }

  public static String ip4ToString(byte[] ip4) {
    return (ip4[0] & 0xff) + "." + (ip4[1] & 0xff) + "." + (ip4[2] & 0xff) + "." + (ip4[3] & 0xff);
  }

  public static String ip4ToString(int ip4) {
    return ip4ToString(ip4ToArr(ip4));
  }

  /**
   * @param address {192， 168，1， 1}
   * @param timeout 超时时间，毫秒
   * @return ping通返回 true
   */
  public static boolean isReachable(byte[] address, int timeout) throws IOException {
    return InetAddress.getByAddress(address).isReachable(timeout);
  }

  public static boolean isReachable(int address, int timeout) throws IOException {
    return isReachable(ip4ToArr(address), timeout);
  }

  /**
   * @param host 主机名 或 ip（如：192.168.1.1）
   * @param timeout 超时时间，毫秒
   * @return ping通返回 true
   */
  public static boolean isReachable(String host, int timeout) throws IOException {
    return InetAddress.getByName(host).isReachable(timeout);
  }

  public static void main(String[] args) throws Exception {
    //testIp();
    //pingLocalAll();
    pingLocalAllThreads();
  }

  static void testIp() throws UnknownHostException {
    String ipStr = "192.168.11.246";

    InetAddress localHost = InetAddress.getLocalHost();
    Logs.out("localHost:%s |||| HostAddress:%s", localHost, localHost.getHostAddress());
    byte[] address = localHost.getAddress();
    Logs.out(BytesUtil.bytes2HexString(address));
    Logs.out(ip4ToString(address));
    Logs.out(BytesUtil.bytes2HexString(ip4ToArr(localHost.getHostAddress())));

    Logs.out("getByName:%s", InetAddress.getByName(ipStr).getHostAddress());
    //Logs.out("getByName:%s", new Inet4Address(ipStr, null).ge);
    ;
  }

  /** 单线程，ping 局域网内所有ip */
  static void pingLocalAll() throws IOException {
    int timeout = 10;
    InetAddress localHost = InetAddress.getLocalHost();
    Logs.out("==== ping all:%s ====", localHost);
    byte[] address = localHost.getAddress();
    for (int i = 0; i < 256; i++) {
      address[3] = (byte) i;
      boolean ping = isReachable(address, timeout);
      if (ping) {
        Logs.out("isReachable:%s", ip4ToString(address));
      } else {
        //Logs.out("failed:%s", address[3] & 0xFF);
      }
    }
    Logs.out("==== ping all finish ====");
  }

  static void pingLocalAllThreads() throws IOException, InterruptedException {
    class PingTask implements Runnable {
      //int startIp;
      //int endIp;
      //int step;
      String ip;
      byte[] address;
      CountDownLatch countDownLatch;

      public PingTask(byte[] address, CountDownLatch countDownLatch) {
        this.address = Arrays.copyOf(address, address.length);
        this.ip = ip4ToString(address);
        this.countDownLatch = countDownLatch;
      }

      @Override public void run() {
        try {
          boolean isReachable = isReachable(address, 500);
          onPingFinish(isReachable, address, ip, null);
        } catch (IOException e) {
          onPingFinish(false, address, ip, e);
          e.printStackTrace();
        }
        countDownLatch.countDown();
      }

      protected void onPingFinish(boolean isReachable, byte[] address, String ip, Exception e) {
        if (isReachable) {
          Logs.out("\t%s", ip);
        } else if (e != null) {
          Logs.out("IOException %s:%s", ip, e.getMessage());
        }
      }
    } // end PingTask

    //AtomicInteger count = new AtomicInteger(0);
    int[] count = new int[1];
    Collection<String> ipList = new HashSet<>();
    //ipList = Collections.synchronizedCollection(ipList);

    ExecutorService pool = Executors.newCachedThreadPool();
    //ExecutorService pool = Executors.newFixedThreadPool(256);
    byte[] ip = InetAddress.getLocalHost().getAddress();
    int ipNum = 256;
    CountDownLatch countDownLatch = new CountDownLatch(ipNum);

    Logs.out("==== thread ping all:%s ====", InetAddress.getLocalHost());
    ClockUtil.runTime(true);
    for (int i = 0; i < ipNum; i++) {
      ip[3] = (byte) i;
      pool.execute(new PingTask(ip, countDownLatch) {
        @Override
        protected void onPingFinish(boolean isReachable, byte[] address, String ip, Exception e) {
          super.onPingFinish(isReachable, address, ip, e);
          if (isReachable) {
            count[0]++;
            ipList.add(ip);
          }
        }
      });
    }
    countDownLatch.await();

    pool.shutdown();
    Logs.out("==== finish runTime:%s  count:%s  size:%s ====", ClockUtil.runTime(), count[0],
        ipList.size());
    //Logs.out(ipList);
  }
}
