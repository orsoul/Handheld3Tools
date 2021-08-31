package com.fanfull.libjava.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
  public static void main(String[] args) throws IOException {
    testZip();
  }

  public static void hexStr2BinFile(InputStream in, String filePath) {
    byte[] buff = new byte[1024];
    int len;
    try (OutputStream out = new BufferedOutputStream(new FileOutputStream(filePath))) {
      while (0 < (len = in.read(buff))) {
        String s = new String(buff, 0, len);
        byte[] toWrite = BytesUtil.hexString2Bytes(s);
        out.write(toWrite);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void encryptFile(InputStream in, String filePath, byte[] key) {
    byte[] buff = new byte[1024];
    DesUtil desUtil = new DesUtil("DES/ECB/NOPadding");
    int len;
    try (OutputStream out = new BufferedOutputStream(new FileOutputStream(filePath))) {
      while (0 < (len = in.read(buff))) {
        byte[] toWrite = desUtil.encrypt(buff, key);
        out.write(toWrite, 0, len);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static boolean zipFile(InputStream in, ZipOutputStream out, String name) {
    try {
      out.putNextEntry(new ZipEntry(name));
      Logs.out("zip name:%s", name);
      byte[] buff = new byte[1024 * 8];
      int len;
      while (0 < (len = in.read(buff))) {
        out.write(buff, 0, len);
      }
      return true;
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  private static boolean zipFile(File src, ZipOutputStream out, String pathInZip) {
    if (src == null || !src.exists() || out == null) {
      return false;
    }

    try {
      if (src.isFile()) {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(src));
        if (pathInZip == null) {
          pathInZip = src.getName();
        } else {
          pathInZip += "/" + src.getName();
        }
        boolean res = zipFile(in, out, pathInZip);
        in.close();
        return res;
      } else {
        Logs.out("== %s/%s ==", pathInZip, src.getName());
        File[] files = src.listFiles();
        for (File file : files) {
          if (file.isFile()) {
            zipFile(file, out, pathInZip);
          } else {
            String base;
            if (pathInZip == null) {
              base = file.getName();
            } else {
              base = pathInZip + "/" + file.getName();
            }
            //Logs.out(name);
            zipFile(file, out, base);
          }
        }
        return true;
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return false;
  }

  public static boolean zipFile(String src, String des) {
    return zipFile(new File(src), new File(des));
  }

  public static boolean zipFile(File src, File des) {
    if (src == null || !src.exists() || des == null) {
      return false;
    }
    try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(des))) {
      return zipFile(src, out, src.isDirectory() ? src.getName() : null);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return false;
  }

  static void testZip() throws IOException {
    Logs.out("==== 开始 ====");

    String src = "C:\\Users\\Administrator\\Desktop\\data.txt";
    String des = "C:\\Users\\Administrator\\Desktop\\dataBin";
    String desEncrypt = "C:\\Users\\Administrator\\Desktop\\dataEncrypt";
    String desEncrypt2 = "C:\\Users\\Administrator\\Desktop\\dataEncrypt2";
    String des7z = "C:\\Users\\Administrator\\Desktop\\dataBin.7z";

    BufferedInputStream in = new BufferedInputStream(new FileInputStream(src));

    //hexStr2BinFile(in, des);

    //byte[] key = new byte[8];
    //new Random().nextBytes(key);
    //Logs.out("key:%s", BytesUtil.bytes2HexString(key));
    //encryptFile(in, desEncrypt2, key);

    //zipFile(in, "C:\\Users\\Administrator\\Desktop\\dataZip.zip", "data.txt");

    zipFile("C:\\Users\\Administrator\\Desktop\\dataBin",
        "C:\\Users\\Administrator\\Desktop\\dataBin.zip");

    in.close();
    Logs.out("==== 完成 ====");
  }
}
