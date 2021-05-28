package com.fanfull.libjava;

import com.fanfull.libjava.util.DateFormatUtil;
import com.fanfull.libjava.util.Logs;
import com.fanfull.libjava.util.ThreadUtil;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MyClass {
  static void writeLogFile(Object line, FileWriter writer) {
    final String format = String.format("%s (%s):%s\n", DateFormatUtil.getStringTime(),
        Thread.currentThread().getName(), line);
    try {
      writer.write(format);
      writer.flush();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    int dId = new Random().nextInt(0x01000000) | 0x8D000000;
    Logs.out(dId);
    final String format = String.format("%X", dId);
    Logs.out("%X = %s", dId, format);
    Logs.out(Integer.parseInt("7D000000", 16));
    dId = (int) Long.parseLong(format, 16);
    Logs.out(dId);
  }

  static int lineNum = 0;

  static void testThreadFile() {
    String path = "C:\\Users\\orsoul\\Desktop\\1.txt";
    FileWriter fileWriter = null;
    try {
      fileWriter = new FileWriter(path);
      writeLogFile("开始记录 " + ++lineNum, fileWriter);

      for (int i = 0; i < 4; i++) {
        FileWriter finalFileWriter = fileWriter;
        ThreadUtil.execute(() -> {
          while (lineNum < 300) {
            final long l = (long) (Math.random() * 100L);
            //ThreadUtil.sleep(l);
            writeLogFile(++lineNum + " sleep:" + l, finalFileWriter);
          }
        });
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      //try {
      //  fileWriter.close();
      //} catch (IOException e) {
      //}
    }
  }

  public static void lambdaTest() {
    new Thread(() -> System.out.println("")).start();

    List<String> strLst = Arrays.asList("1", "2");
    Collections.sort(strLst, (o1, o2) -> o1.compareToIgnoreCase(o2));
    Collections.sort(strLst, String::compareToIgnoreCase);
    System.out.println(strLst);
  }

  public static void test() {
    //ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
    BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    new Thread("read") {
      @Override public void run() {
        //synchronized (nodeList) {
        //}

        int count = 0;
        while (true) {
          ThreadUtil.sleep((long) (Math.random() * 1000L));

          //String stringTime = DateFormatUtil.getStringTime();
          String str = System.currentTimeMillis() + "-" + ++count;
          //Node node = new Node();
          //node.data = str;
          //nodeList.add(node);
          //Logs.out("放入：%s size:%s", node.data, nodeList.count);
          queue.add(str);
          Logs.out("放入：%s size:%s", str, queue.size());
        }
      }
    }.start();

    new Thread("handle") {
      @Override public void run() {
        while (true) {

          //String stringTime = DateFormatUtil.getStringTime();
          //Node node = new Node();
          //node.data = stringTime;
          String poll = null;
          try {
            poll = queue.take();
            ThreadUtil.sleep((long) (Math.random() * 2000L));
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          Logs.out("取出：%s size:%s", poll, queue.size());
          //Logs.out("取出：%s", po.data);
          //Node po = nodeList.po();
          //Logs.out("取出：%s size:%s", po, nodeList.count);
        }
      }
    }.start();
  }
}