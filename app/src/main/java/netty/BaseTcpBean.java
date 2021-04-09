package netty;

import com.fanfull.libjava.util.BytesUtil;

import java.nio.charset.StandardCharsets;

public class BaseTcpBean {
  public static final byte[] HEAD1 = new byte[] { (byte) 0xBF, (byte) 0xBF };
  public static final int HEAD_LEN = 9;

  int dataLen;
  int type;
  int msgNum;
  String json;

  public BaseTcpBean() {
  }

  public BaseTcpBean(String json, int type, int msgNum) {
    this.type = type;
    this.msgNum = msgNum;
    this.json = json;
  }

  public byte[] encode() {
    return string2Msg(json, type, msgNum);
  }

  public static byte[] string2Msg(String json, int type, int cmdNum) {
    byte[] data = json.getBytes(StandardCharsets.UTF_8);
    byte[] lenBuff = BytesUtil.long2Bytes(data.length, 2);
    byte[] typeBuff = new byte[] { (byte) type };
    byte[] numBuff = BytesUtil.long2Bytes(cmdNum, 4);
    byte[] msg = BytesUtil.concatArray(HEAD1, lenBuff, typeBuff, numBuff, data);
    return msg;
  }

  public static BaseTcpBean parse(byte[] msg) {
    if (msg == null || msg.length < HEAD_LEN) {
      return null;
    }

    //int head1 = msg[0] & 0xFF;
    //int head2 = msg[1] & 0xFF;
    if (HEAD1[0] != msg[0] || HEAD1[1] != msg[1]) {
      //if (head1 != 0xBF || head2 != 0xBF) {
      //LogUtils.w("协议头错误：%02X%02X", head1, head2);
      System.out.printf("协议头错误：%02X%02X\n", msg[0], msg[1]);
      return null;
    }
    int len = BytesUtil.bytes2Long(msg, 2, 2).intValue();
    int type = msg[4] & 0xFF;
    if (len != msg.length - HEAD_LEN) {
      //LogUtils.w("数据长度错误dataLen/allLen：%s/%s", len, msg.length);
      System.out.printf("数据长度错误dataLen/allLen：%s/%s\n", len, msg.length);
      return null;
    }

    int msgNum = BytesUtil.bytes2Long(msg, 5, 4).intValue();

    BaseTcpBean tcpBean = new BaseTcpBean();
    tcpBean.dataLen = len;
    tcpBean.type = type;
    tcpBean.msgNum = msgNum;
    tcpBean.json = new String(msg, HEAD_LEN, len, StandardCharsets.UTF_8);
    return tcpBean;
  }
}
