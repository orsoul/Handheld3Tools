package com.fanfull.libjava.io;

import com.fanfull.libjava.util.BytesUtil;
import com.fanfull.libjava.util.Logs;
import com.fanfull.libjava.util.ThreadUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class SocketServiceDemo {
  static int port = 13579;

  public static void main(String[] args) {
    SocketServiceThread socketServiceThread = new SocketServiceThread();
    socketServiceThread.start();

    //new SocketClientThread().run();
  }

  static boolean send(OutputStream out, String data) {
    if (data == null) {
      return false;
    }
    return send(out, data.getBytes());
  }

  static boolean send(OutputStream out, byte data[]) {
    if (data == null) {
      return false;
    }
    return send(out, data, 0, data.length);
  }

  static boolean send(OutputStream out, byte data[], int off, int len) {
    try {
      out.write(data, off, len);
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  static class SocketServiceThread extends Thread {
    boolean isStop;

    public boolean isStop() {
      return isStop;
    }

    public void shutdown() {
      isStop = true;
    }

    private Map<String, Socket> clientMap = new HashMap<>();

    private void accept(Socket socket, String name) {
      String key = String.format("accept:%s-%s", socket.getInetAddress(), socket.getPort());
      Logs.out(key);

      InputStream in;
      OutputStream out;
      try {
        in = socket.getInputStream();
        out = socket.getOutputStream();
      } catch (Exception e) {
        Logs.out("==== client %s run failed ====", name);
        e.printStackTrace();
        return;
      }

      clientMap.put(key, socket);
      Logs.out("==== %s run, client num:%s ====", name, clientMap.size());
      byte[] buff = new byte[1024];
      int len;
      while (socket.isConnected()) {
        try {
          len = in.read(buff);
          if (len <= 0) {
            Logs.out("client %s rec 0", socket.getInetAddress());
            break;
          }

          String hex = BytesUtil.bytes2HexString(buff, 0, len);
          Logs.out("%s rec %s:%s", name, len, hex);

          boolean send = send(out, buff, 0, len);
          Logs.out("%s send %s %s:%s", name, send, len, hex);

        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      clientMap.remove(key);
      Logs.out("====  %s end  num:%s ====", name, clientMap.size());
    }

    @Override public void run() {

      ServerSocket serverSocket;
      try {
        serverSocket = new ServerSocket(port, 1);
      } catch (IOException e) {
        e.printStackTrace();
        Logs.out("server run failed");
        return;
      }
      Logs.out("====== server %s run ======", serverSocket.getLocalSocketAddress());
      AtomicInteger count = new AtomicInteger(1);
      while (!isStop) {
        try {
          Socket client = serverSocket.accept();
          ThreadUtil.execute(() -> accept(client, "s-" + count.getAndIncrement()));
        } catch (IOException e) {
          e.printStackTrace();
          return;
        }
      } // while end
      Logs.out("====== server %s end ======", serverSocket.getLocalSocketAddress());
    }
  }

  static class SocketClientThread extends Thread {
    String name = "localhost";

    @Override public void run() {
      Socket socket = null;
      OutputStream out = null;
      InputStream in = null;

      try {
        socket = new Socket();
        SocketAddress socketAddress = new InetSocketAddress("localhost", port);
        socket.connect(socketAddress, 10 * 1000);
        socket.setSoTimeout(10 * 1000);
        in = socket.getInputStream();
        out = socket.getOutputStream();
        in = new GZIPInputStream(in);
        out = new GZIPOutputStream(out);
      } catch (IOException ex) {
        Logs.out("%s run failed", name);
        ex.printStackTrace();
        return;
      }

      //send(out, "hello");

      Logs.out("==== %s run ====", name);
      byte[] buff = new byte[1024];
      int len;
      while (socket.isConnected()) {
        try {
          len = in.read(buff);
          if (len <= 0) {
            Logs.out("client %s rec 0", socket.getInetAddress());
            break;
          }

          String hex = BytesUtil.bytes2HexString(buff, 0, len);
          Logs.out("%s rec %s:%s", name, len, hex);

          //boolean send = send(out, buff, 0, len);
          //Logs.out("%s send %s %s:%s", name, send, len, hex);
        } catch (SocketTimeoutException e) {
          send(out, e.getMessage());
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      Logs.out("====  %s end ====", name);
    }
  }
}
