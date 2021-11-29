package org.javanote;

import com.fanfull.libjava.util.Logs;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FileRenameUtil {

  public static void main(String[] args) {
    //testRename();
    testRename2();
  }

  private static void testRename2() {
    File rootDir = new File("D:\\迅雷下载\\威尔和格蕾丝S1 (Will&Grace S)");
    List<File> listV = new ArrayList<>();
    List<File> listAss = new ArrayList<>();

    rootDir.listFiles(file -> {
      String name = file.getName();
      if (name.endsWith(".avi")) {
        listV.add(file);
      } else {
        listAss.add(file);
      }
      //Logs.out(file.getName());
      return false;
    });

    for (int i = 0; i < listAss.size(); i++) {
      File fileAss = listAss.get(i);
      File fileV = listV.get(i);

      String newName = fileAss.getName().substring(13);
      boolean rename = rename(fileAss, newName);
      Logs.out("%s <-%s- %s", newName, rename, fileAss.getName());

      String newName2 = newName.substring(0, newName.lastIndexOf(".")) + ".avi";
      rename = rename(fileV, newName2);
      Logs.out("%s <-%s- %s", newName2, rename, fileV.getName());
    }
  }

  private static void testRename3() {
    renameFiles("D:\\!Videos\\Captures\\魁拔之殊途", new FileNameHandler() {
      @Override public String getNewName(String oldName, String extension) {
        //return super.getNewName(oldName, extension);
        if (9 < oldName.length()) {
          oldName = oldName.substring(6, 6 + 2);
        }
        return oldName + extension;
      }
    });
  }

  private static void testRename() {
    File file = new File("D:\\!Desktop\\莫言全集");
    renameFiles(file, new FileNameHandler() {
      @Override public String getNewName(String oldName, String extension) {
        String name = super.getNewName(oldName, extension);
        if (!name.contains("莫言")) {
          name += "-莫言";
        }
        String format = String.format("%s%s", name, extension);
        Logs.out("%s -> %s", oldName, format);
        return format;
      }

      @Override public LinkedHashMap<String, String> getReplaceMap() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("[《》\\[\\]]", "");
        map.put("作者[ ：]", "-");
        map.put("莫言$", "-莫言");
        map.put("--", "-");
        return map;
      }
    });
  }

  private static boolean isSpace(final String s) {
    if (s == null) {
      return true;
    }
    for (int i = 0, len = s.length(); i < len; ++i) {
      if (!Character.isWhitespace(s.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  public static boolean renameFiles(File file, FileNameHandler handler) {
    if (file == null || handler == null) {
      return false;
    }
    if (file.isFile()) {
      return rename(file, handler.getNewName(file.getName()));
    }

    File[] files = file.listFiles();
    for (File f : files) {
      boolean rename = rename(f, handler.getNewName(f.getName()));
      if (!rename) {
        return false;
      }
    }
    return true;
  }

  public static boolean renameFiles(String file, FileNameHandler handler) {
    return renameFiles(new File(file), handler);
  }

  public static boolean rename(final File file, final String newName) {
    // file is null then return false
    if (file == null) {
      return false;
    }
    // file doesn't exist then return false
    if (!file.exists()) {
      return false;
    }
    // the new name is space then return false
    if (isSpace(newName)) {
      return false;
    }
    // the new name equals old name then return true
    if (newName.equals(file.getName())) {
      return true;
    }
    File newFile = new File(file.getParent() + File.separator + newName);
    // the new name of file exists then return false
    return !newFile.exists() && file.renameTo(newFile);
    //return true;
  }

  public static abstract class FileNameHandler {
    public String getNewName(File file) {
      if (file == null) {
        return null;
      }
      return getNewName(file.getName());
    }

    /** @param oldName 文件名，包含后缀名 */
    public String getNewName(String oldName) {
      int i = oldName.lastIndexOf('.');
      if (0 < i) {
        String name = oldName.substring(0, i);
        String extension = oldName.substring(i);
        return getNewName(name, extension);
      } else if (0 == i) {
        return getNewName("", oldName);
      } else {
        return getNewName(oldName, "");
      }
    }

    /**
     * @param oldName 原文件，不含后缀名
     * @param extension 后缀名，包含.，如：.txt
     */
    public String getNewName(String oldName, String extension) {
      if (oldName == null || extension == null) {
        return null;
      }
      Map<String, String> map = getReplaceMap();
      if (map == null || map.isEmpty()) {
        return null;
      }

      for (Map.Entry<String, String> entry : map.entrySet()) {
        oldName = oldName.replaceAll(entry.getKey(), entry.getValue());
        System.out.print(String.format(" -(%s)-> %s", entry.getKey(), oldName));
      }
      System.out.println();

      return oldName;
    }

    /**
     * 获取替换map，key为正则表达式，value为目标替换字符串，
     * 原文文件名将按map中键值对次序，依次执行replaceAll(key, value)
     */
    public LinkedHashMap<String, String> getReplaceMap() {
      return null;
    }
  }
}
