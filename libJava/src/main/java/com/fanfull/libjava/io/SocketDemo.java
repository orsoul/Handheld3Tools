package com.fanfull.libjava.io;

import com.fanfull.libjava.io.socketClient.Options;
import com.fanfull.libjava.io.socketClient.impl.BaseSocketClient;
import com.fanfull.libjava.io.socketClient.interf.ISocketClientListener;
import com.fanfull.libjava.util.Logs;
import com.fanfull.libjava.util.ThreadUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketDemo {

  public static String cmd1 =
      "000001A88000000005F5E1352002800001C800000172B1E278D8000001987B22636F6465223A36353533372C2262757369496E666F4C697374223A5B7B22766F7563686572547970654944223A22313031353131222C22766F7563686572547970654E616D65223A22E7BAB8313030E58583EFBC883035E78988EFBC89222C2276616C223A223130302E30222C227061706572547970654944223A2233222C227061706572547970654E616D65223A22E69CAAE6B885E58886E5AE8CE695B4E588B8222C227361636B4D6F6E6579223A22323030303030302E3030227D5D2C227061636B496E666F4C697374223A5B7B227361636B4E6F223A22303535333231303130343132393143413345363138304534222C22766F7563686572547970654944223A22313031303031222C22766F7563686572547970654E616D65223A22E7BAB8313030E58583222C2276616C223A223130302E30227D5D2C22737461636B496E666F4C697374223A5B7B2273737461636B436F6465223A223733323031303031303130313032222C2273737461636B4E616D65223A22E58D97E4BAACE5B882E4B8ADE694AFE5BA932DE4B88BE5BA93227D5D7D";
  public static String cmd2 = "0000000A0000000005F5E13B2004000001C7000000008000";

  public static void main(String[] args) {
    //String str =
    //    "{\"busiInfoList\":[{\"paperTypeID\":\"3\",\"paperTypeName\":\"未清分完整券\",\"sackMoney\":\"2000000.00\",\"val\":\"100.0\",\"voucherTypeID\":\"101511\",\"voucherTypeName\":\"纸100元（05版）\"}],\"code\":65537,\"packInfoList\":[{\"sackNo\":\"050271010480613ECA9112E5\",\"val\":\"100.0\",\"voucherTypeID\":\"101001\",\"voucherTypeName\":\"纸100元\"}],\"stackInfoList\":[{\"sstackCode\":\"73201001010102\",\"sstackName\":\"南京市中支库-下库\"}]}\n";
    //runSocketDemo();
    runSocketClients();
    //new Thread(new SocketServiceDemo()).start();
  }

  static void runSocketClients() {
    ExecutorService executorService = Executors.newCachedThreadPool();
    for (int i = 0; i < 1024; i++) {
      Options ops = new Options();
      ops.serverIp = "192.168.11.161";
      ops.serverPort = 10001;
      BaseSocketClient client = new BaseSocketClient(ops);
      int finalI = i;
      client.addSocketClientListener(
          new ISocketClientListener() {
            int total = 0;

            @Override
            public void onDisconnect(String serverIp, int serverPort, boolean isActive) {
              Logs.out("onDisconnect %s:%s", serverIp, serverPort);
            }

            @Override public void onReceive(byte[] data) {
              Logs.out("onReceive: %s", new String(data));
            }

            @Override public void onSend(boolean isSuccess, byte[] data, int offset, int len) {

            }

            @Override
            public void onConnect(String serverIp, int serverPort) {
              Logs.out("%s,onConnect %s:%s", finalI, serverIp, serverPort);
            }

            @Override
            public void onConnectFailed(Throwable e) {
              System.out.println(e.getClass() + "-onConnectFailed " + e.getMessage());
            }
          });
      boolean connect = client.connect();
      if (connect) {
        String s = "hello, I am " + i;
        client.send(s.getBytes());
        executorService.execute(() -> {
          for (int j = 1; j < Integer.MAX_VALUE; j++) {
            String format = String.format("$200 {} %s#", j);
            client.send(format.getBytes());
            ThreadUtil.sleepSeconds(15);
          }
        });
      }
      ThreadUtil.sleepSeconds(1);
    }
  }

  static void runSocketDemo() {
    new Thread(new SocketServiceDemo()).start();

    Options ops = new Options();
    //    ops.serverIp = "localhost";
    ops.serverIp = "192.168.11.246";
    ops.serverPort = 23456;
    BaseSocketClient client = new BaseSocketClient(ops);
    client.addSocketClientListener(
        new ISocketClientListener() {
          int total = 0;

          @Override
          public void onDisconnect(String serverIp, int serverPort, boolean isActive) {
            System.out.println("onDisconnect");
          }

          @Override public void onReceive(byte[] data) {
          }

          @Override public void onSend(boolean isSuccess, byte[] data, int offset, int len) {

          }

          @Override
          public void onConnect(String serverIp, int serverPort) {
            System.out.println("onConnect");
          }

          @Override
          public void onConnectFailed(Throwable e) {
            System.out.println(e.getClass() + "-onConnectFailed " + e.getMessage());
          }
        });
    //        client.connect();
  }

  static class SocketServiceDemo implements Runnable {
    public void send(Socket client, String data) throws IOException {
      send(client, data.getBytes());
    }

    public void send(Socket client, byte[] data) throws IOException {
      if (client == null || !client.isConnected() || data == null) {
        return;
      }
      client.getOutputStream().write(data);
      Logs.out("send:%s", new String(data));
    }

    ExecutorService executorService = Executors.newCachedThreadPool();

    void handler(Socket client) {
      executorService.execute(() -> {
        try {
          send(client, "hello,you are " + client.getInetAddress());
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
    }

    @Override
    public void run() {
      ServerSocket serverSocket;
      try {
        serverSocket = new ServerSocket(12345);
        System.out.println(String.format("server %s run", serverSocket.getLocalSocketAddress()));
        while (true) {
          Socket client = serverSocket.accept();
          Logs.out("%s,onConnect %s", client.getInetAddress());
          handler(client);
        }
      } catch (IOException e) {
        e.printStackTrace();
        return;
      }
    }
  }
}
