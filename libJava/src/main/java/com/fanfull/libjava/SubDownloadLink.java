package com.fanfull.libjava;

import com.fanfull.libjava.util.CmdUtil;
import com.fanfull.libjava.util.IOUtils;
import com.fanfull.libjava.util.Logs;
import com.google.gson.Gson;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SubDownloadLink {
  public static void main(String[] args) {
    //subEd2k();
    convertVideo();
  }

  static void search(File parent) {
    //parent.listFiles(new FileFilter() {
    //  @Override public boolean accept(File pathname) {
    //    return false;
    //  }
    //});
    for (File file : parent.listFiles()) {
      if (file.isDirectory()) {
        search(file);
        continue;
      }

      //if (file.getName().endsWith(".m4s"))
      String entry = file.getName();
      if (!entry.endsWith("entry.json")) {
        continue;
      }

      //new JsonObject().get
      //new GsonBuilder().create().
      String json = IOUtils.readFile2String(file);
      Map<String, String> map = new Gson().fromJson(json, HashMap.class);
      String tag = map.get("type_tag");
      String title = map.get("title");
      title = title.replaceAll("[\\\\/]", "-");

      String audio = String.format("%s/%s/audio.m4s", file.getParent(), tag);
      String video = String.format("%s/%s/video.m4s", file.getParent(), tag);
      String cmd = String.format(
          "ffmpeg -i \"%s\" -i \"%s\" -c:v copy -c:a aac -strict experimental \"%s.mp4\"",
          audio, video, title);
      //Logs.out(video);
      //Logs.out(audio);
      Logs.out(cmd);

      String gbk = CmdUtil.cmd(cmd, true, "gbk", true);
      Logs.out(gbk);
      //return;

      //Logs.out(map.get("title"));
      //Logs.out(map.get("type_tag"));
    }
  }

  static void convertVideo() {
    String root = "D:\\!Desktop\\bili\\video";
    File rootDir = new File(root);
    search(rootDir);
  }

  static void subEd2k() {
    String src = "D:\\!Desktop\\download.txt";

    String str = IOUtils.readFile2String(src);
    int start = str.indexOf("ed2k://");
    int end = str.indexOf("\";", start);
    String sub = str.substring(start, end);
    Logs.out(sub);

    String[] split = sub.split("###第\\d{1,2}集\\$");
    Logs.out(split.length);

    sub = sub.replaceAll("###第\\d{1,2}集\\$", "\n");
    IOUtils.writeFileFromString(src + ".txt", sub);
  }
}
