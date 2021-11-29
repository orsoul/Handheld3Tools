package org.javanote.crccalc;

import com.fanfull.libjava.util.BytesUtil;
import com.fanfull.libjava.util.Logs;

import static java.lang.System.out;

public class Main {

  public static void main(String[] args) {
    //Check(Crc8.Params);
    //Check(Crc16.Params);
    //Check(Crc32.Params);
    //Check(Crc64.Params);

    byte[] data = BytesUtil.hexString2Bytes("F10123B9A19B9D26397D64EE1D1500D7");

    //CrcCalculator calculator = new CrcCalculator(Crc8.Crc8);
    CrcCalculator calculator = new CrcCalculator(Crc32.Crc32);
    long crc = calculator.Calc(data, 0, data.length);
    Logs.out("%X", crc);
  }

  private static void Check(AlgoParams[] params) {
    for (int i = 0; i < params.length; i++) {
      CrcCalculator calculator = new CrcCalculator(params[i]);
      long result = calculator.Calc(CrcCalculator.TestBytes, 0, CrcCalculator.TestBytes.length);
      if (result != calculator.Parameters.Check) {
        out.println(calculator.Parameters.Name +
            " - BAD ALGO!!! " +
            Long.toHexString(result).toUpperCase());
      }
    }
  }
}