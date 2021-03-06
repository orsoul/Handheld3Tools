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
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
  public static void main(String[] args) throws IOException {
    //testZip();
    //test3Des();
    testHexStr();
  }

  static void test3Des() {
    byte[] sha = MessageDigestUtil.sha("82984888");
    byte[] key = new byte[24];
    Arrays.fill(key, (byte) 0x85);
    System.arraycopy(sha, 0, key, 0, sha.length);
    Logs.out("key:%s", BytesUtil.bytes2HexString(key));

    //String pathIn = "C:\\Users\\Administrator\\Desktop\\BagCirc.dat";
    //String filePath = "C:\\Users\\Administrator\\Desktop\\BagCirc.txt";

    String pathIn = "C:\\Users\\Administrator\\Desktop\\3ds.txt";
    String filePath = "C:\\Users\\Administrator\\Desktop\\3dsBin";

    byte[] vector = new byte[8];

    byte[] buff = new byte[1024];
    DesUtil desUtil = new DesUtil("DESede/CBC/PKCS5Padding");
    int len;
    try (OutputStream out = new BufferedOutputStream(new FileOutputStream(filePath));
         InputStream in = new BufferedInputStream(new FileInputStream(pathIn))) {
      while (0 < (len = in.read(buff))) {
        //byte[] toWrite = desUtil.decrypt(buff, key);
        byte[] toWrite = DesUtil.cipherDoFinal(buff, 0, len, key, true,
            "DESede/CBC/PKCS5Padding", vector);
        out.write(toWrite);
        //out.write(toWrite, 0, len);
        if (len != buff.length) {
          Logs.out("len=%s", len);
        }
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
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
    DesUtil desUtil = new DesUtil("DES/CBC/NOPadding");
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
        }
        boolean res = zipFile(in, out, pathInZip);
        in.close();
        return res;
      } else {
        Logs.out("== %s, %s ==", pathInZip, src.getName());
        File[] files = src.listFiles();
        for (File file : files) {
          String base;
          if (pathInZip == null) {
            base = file.getName();
          } else {
            base = pathInZip + "/" + file.getName();
          }
          boolean success = zipFile(file, out, base);
          if (!success) {
            return false;
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
      return zipFile(src, out, src.getName());
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return false;
  }

  static void testZip() throws IOException {
    Logs.out("==== ?????? ====");

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

    zipFile("C:\\Users\\Administrator\\Desktop\\apks",
        "C:\\Users\\Administrator\\Desktop\\apks.zip");

    in.close();
    Logs.out("==== ?????? ====");
  }

  static void testHexStr() throws IOException {
    Logs.out("==== ?????? ====");

    //String src = "C:\\Users\\Administrator\\Desktop\\hex.txt";
    //String des = "C:\\Users\\Administrator\\Desktop\\hex.txt.bin";
    String src = "C:\\Users\\Administrator\\Desktop\\hex.dockerfile";
    String des = "C:\\Users\\Administrator\\Desktop\\hex.dockerfile.bin";

    BufferedInputStream in = new BufferedInputStream(new FileInputStream(src));

    //hexStr2BinFile(in, des);

    //byte[] key = new byte[8];
    //new Random().nextBytes(key);
    //Logs.out("key:%s", BytesUtil.bytes2HexString(key));
    //encryptFile(in, desEncrypt2, key);

    //zipFile(in, "C:\\Users\\Administrator\\Desktop\\dataZip.zip", "data.txt");

    hexStr2BinFile(in, des);

    String s = IOUtils.readFile2String(des);
    Logs.out(s);

    in.close();
    Logs.out("==== ?????? ====");
  }
}
