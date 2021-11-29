package org.javanote;

import com.fanfull.libjava.util.CmdUtil;
import com.fanfull.libjava.util.IOUtils;
import com.fanfull.libjava.util.Logs;
import com.google.gson.Gson;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class BiliConvert {
  public static void main(String[] args) {
    //String v = "D:\\!Desktop\\bili\\video\\763536993\\c_423233487\\64\\video.m4s";
    //String a = "D:\\!Desktop\\bili\\video\\763536993\\c_423233487\\64\\video.m4s";
    //String d = "D:\\!Desktop\\bili\\video\\763536993.mp4";
    //boolean b = ffMpeg(v, a, d);
    //Logs.out(b);

    //File rootDir = new File("D:\\!Desktop\\bili\\video");
    //search(rootDir, rootDir);
    convertVideo("D:\\!Desktop\\bili", false);
  }

  static void convertVideo(String desDir, boolean isPull) {
    if (isPull) {
      String pullCmd = "adb pull sdcard/Android/data/tv.danmaku.bili/download " + desDir;
      String info = CmdUtil.cmd(pullCmd, true, "gbk", true);
      boolean res = info != null;
      Logs.out("cmd %s:%s", res, pullCmd);
      Logs.out("info:%s", info);
      if (!res) {
        return;
      }
    }
    File rootDir = new File(desDir);
    search(rootDir, rootDir);
  }

  static void search(File parent, File desFile) {
    File[] files;
    if (parent == null || null == (files = parent.listFiles())) {
      Logs.out("空目录:%s", parent.getPath());
      return;
    }
    for (File file : files) {
      if (file.isDirectory()) {
        search(file, desFile);
        continue;
      }

      String entry = file.getName();
      if (!entry.endsWith("entry.json")) {
        continue;
      }

      String json = IOUtils.readFile2String(file);
      Map<String, String> map = new Gson().fromJson(json, HashMap.class);
      String tag = map.get("type_tag");
      String title = map.get("title");

      String audio = String.format("%s/%s/audio.m4s", file.getParent(), tag);
      String video = String.format("%s/%s/video.m4s", file.getParent(), tag);

      title = title.replaceAll("[\\\\/]", "-");
      String des = String.format("%s/%s.mp4", desFile.getPath(), title);

      Logs.out("video:%s", video);
      Logs.out("audio:%s", audio);
      boolean b = ffMpeg(video, audio, des);

      Logs.out("====== %s des:%s ======", b, des);

      //Logs.out(map.get("title"));
      //Logs.out(map.get("type_tag"));
    }
  }

  static boolean ffMpeg(String video, String audio, String des) {
    String cmd = String.format(
        "ffmpeg -i \"%s\" -i \"%s\" -c:v copy -c:a aac -strict experimental \"%s\"",
        audio, video, des);
    boolean gbk = null !=
        CmdUtil.cmd(cmd, true, "gbk", true);
    return gbk;
  }
}
