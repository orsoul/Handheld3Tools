package com.fanfull.libjava.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class CmdUtil {
  public static void main(String[] args) throws IOException {
    //String cmd = "ping www.baidu.com";
    //String cmd = "ping localhost&&calc";
    String cmd = "ping localhost";
    //String cmd = "ffmpeg -i \"D:\\!Desktop\\bili\\video\\763536993\\c_423233487/64/audio.m4s\" -i \"D:\\!Desktop\\bili\\video\\763536993\\c_423233487/64/video.m4s\" -c:v copy -c:a aac -strict experimental \"【孙燕姿】 2021-10-10新加坡总统星光慈善晚宴全程.mp4\"";
    //String cmd = "adb pull sdcard/Android/data/tv.danmaku.bili/downloads ./video";
    Logs.out("cmd:%s", cmd);

    //String res = cmd(cmd, true, "gbk", true);
    //Logs.out("res:%s", res);
    Logs.out("resShow:%s", cmd(cmd, true));
    Logs.out("res:%s", cmd(cmd));
  }

  public static boolean cmd(String cmd) {
    return cmd(cmd, false);
  }

  public static boolean cmd(String cmd, boolean showWindow) {
    return null != cmd(cmd, false, null, showWindow);
  }

  public static String cmd(String cmd, boolean waitForMsg, String charset) {
    return cmd(cmd, waitForMsg, charset, false);
  }

  public static String cmd(String cmd, boolean waitForMsg, String charset, boolean showWindow) {
    Runtime runtime = Runtime.getRuntime();
    Process proc = null;
    try {
      if (showWindow) {
        proc = runtime.exec("cmd /C start " + cmd);
        //proc = runtime.exec("cmd /K " + cmd);
      } else {
        proc = runtime.exec(cmd);
      }

      String reVal;
      if (waitForMsg) {
        if (charset == null) {
          charset = "utf-8";
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(
            proc.getInputStream(), charset));
        StringBuffer stringBuffer = new StringBuffer();
        String line = null;
        while ((line = in.readLine()) != null) {
          stringBuffer.append(line + "\n");
          Logs.out(line);
        }
        // 执行结果
        reVal = stringBuffer.toString();
        proc.waitFor();
        proc.exitValue();
      } else if (proc.waitFor() == 0) {
        // 执行成功
        reVal = "";
      } else {
        // 失败
        reVal = null;
      }
      return reVal;
    } catch (Exception e) {
      if (waitForMsg) {
        return e.getMessage();
      } else {
        return null;
      }
    } finally {
      try {
        proc.destroy();
      } catch (Exception e2) {
      }
    }
  }

  public static void execSh(String cmd) throws IOException, InterruptedException {
    Process process = null;
    DataOutputStream os = null;
    BufferedReader in = null;

    try {
      System.out.println("process pre:" + process);
      process = Runtime.getRuntime().exec("/system/xbin/su");
      System.out.println("process pro:" + process);
      os = new DataOutputStream(process.getOutputStream());

      os.writeBytes(cmd + ""); // 这里可以执行具有root 权限的程序了
      os.flush();
      int waitFor = process.waitFor();
      System.out.println("waitFor:" + waitFor);
      if (waitFor != 0) {
        System.out.println("exit value = " + process.exitValue());
      }

      in = new BufferedReader(new InputStreamReader(
          process.getInputStream()));
      StringBuffer stringBuffer = new StringBuffer();
      String line = null;
      while ((line = in.readLine()) != null) {
        stringBuffer.append(line + "\n");
      }
      System.out.println("run res:\n" + stringBuffer.toString());
    } finally {
      if (os != null) {
        os.close();
      }
      if (in != null) {
        in.close();
      }
      process.destroy();
    }
  }
}
