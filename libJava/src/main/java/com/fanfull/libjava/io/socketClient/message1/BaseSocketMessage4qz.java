package com.fanfull.libjava.io.socketClient.message1;

import com.fanfull.libjava.io.netty.future.MsgFuture;
import com.fanfull.libjava.io.netty.future.MsgFuture4qz;
import com.fanfull.libjava.io.socketClient.ReceiveListenerAbs;
import com.fanfull.libjava.io.socketClient.interf.ISocketMessage;
import com.fanfull.libjava.util.ThreadUtil;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 与前置TCP通讯协议基类.
 * $func res json msgNum#.
 * $14 00 {...} 001#
 */
public abstract class BaseSocketMessage4qz<T> implements ISocketMessage {
  /** 通讯消息字段 分隔符：空格. */
  public static final String CH_SPLIT = " ";
  /** 发送消息 的 头标识：$. */
  public static final String CH_HEAD_SEND = "$";
  /** 接收消息 的 头标识：*. */
  public static final String CH_HEAD_REC = "*";
  /** 接收、发送消息的 尾标识：#. */
  public final static String CH_END = "#";

  /** -a = $. */
  public static final String SP_HEAD_SEND = "-a";
  /** -b = 空格. */
  public static final String SP_SPACE = "-b";
  /** -c = #. */
  public static final String SP_END = "-c";
  /** -d = *. */
  public static final String SP_HEAD_REC = "-d";

  public static final Charset CHARSET = StandardCharsets.UTF_8;

  private static AtomicInteger sendCount = new AtomicInteger(0);

  public static int getSendCount() {
    return sendCount.get();
  }

  public static int addAndGetSendCount() {
    return sendCount.addAndGet(1);
  }

  /** 复核登录. */
  public static final int FUNC_LOGIN_2 = 0;
  /** 登录. */
  public static final int FUNC_LOGIN = 1;
  /** 失败. */
  public static final int FUNC_FAILED = 2;
  /** 选择任务. */
  public static final int FUNC_SELECT_TASK = 7;
  /** 选择任务-回复. */
  public static final int FUNC_SELECT_TASK_REC = 18;
  /** 提交袋ID-逐个扫描. */
  public static final int FUNC_UPLOAD_BAG = 22;
  /** 提交袋ID-逐个扫描-空包登记. */
  public static final int FUNC_UPLOAD_BAG_REG = 21;
  /** 提交袋ID-批量扫描. */
  public static final int FUNC_UPLOAD_BAG_LOT_SCAN = 32;
  /** 查询任务袋数量. */
  public static final int FUNC_QUERY_PLAIN_NUM = 37;
  /** 提交离线数据. */
  public static final int FUNC_SUBMIT_OFFLINE_DATA = 42;
  /** 非业务进出库-二代任务. */
  public static final int FUNC_PERSON_IN_OUT = 25;
  /** 指纹相关. */
  public static final int FUNC_FINGER = 14;
  /** 透传. */
  public static final int FUNC_TC = 1000;

  protected String message;

  public abstract String getMessage();

  @Override public byte[] getData() {
    message = getMessage();
    if (message == null) {
      return null;
    }
    return message.getBytes(CHARSET);
  }

  protected int func;
  protected String[] split;
  protected int msgNum;

  protected T jsonBean;

  public int getFunc() {
    return func;
  }

  public void setFunc(int func) {
    this.func = func;
  }

  public int getMsgNum() {
    return msgNum;
  }

  public void setMsgNum(int msgNum) {
    this.msgNum = msgNum;
  }

  public String[] getSplit() {
    return split;
  }

  public T getJsonBean() {
    return jsonBean;
  }

  public BaseSocketMessage4qz() {
  }

  protected BaseSocketMessage4qz(int func) {
    this.func = func;
  }

  public BaseSocketMessage4qz(int func, String[] split, int msgNum) {
    this.func = func;
    this.split = split;
    this.msgNum = msgNum;
  }

  public boolean initInfo(String recString) {
    String[] split = splitRecInfoWithHead(recString);
    if (split == null) {
      return false;
    }
    int func = Integer.parseInt(split[0]);
    int num = Integer.parseInt(split[split.length - 1]);

    this.func = func;
    this.msgNum = num;
    this.split = split;
    return true;
  }

  public boolean checkSplit() {
    if (split == null || split.length < 3) {
      return false;
    } else {
      return true;
    }
  }

  @Override public String toString() {
    if (message == null) {
      return "BaseSocketMessage{" +
          "split='" + Arrays.toString(split) + '\'' +
          '}';
    }
    return "BaseSocketMessage{" +
        "message='" + message + '\'' +
        '}';
  }

  //protected abstract T parseInfo(String[] split);

  /**
   * 检查并分段接收到的字符串，必须包含头尾标识符.会截去头尾标识符。
   *
   * @return 第一段和最后一段必须是数字串，分段成功返回长度至少为3的字符串数组，否则返回null.
   */
  public static String[] splitRecInfoWithHead(String recString) {
    if (recString == null
        //|| recString.startsWith(BaseSocketMessage.CH_RECEIVE_HEAD)
        //|| recString.endsWith(BaseSocketMessage.CH_END)
        || !recString.matches("^\\*\\d+ .+ \\d+#$")) {
      //LogUtils.w("split failed:%s", recString);
      return null;
    }
    String[] split =
        recString.substring(1, recString.length() - 1).split(BaseSocketMessage4qz.CH_SPLIT);
    if (split.length < 3) {
      return null;
    }
    return split;
  }

  /**
   * 检查并分段接收到的字符串，必须不包含头尾标识符.
   *
   * @return 第一段和最后一段必须是数字串，分段成功返回长度至少为3的字符串数组，否则返回null.
   */
  public static String[] splitRecInfoWithoutHead(String recString) {
    if (recString == null
        || !recString.matches("^\\d+ .+ \\d+$")) {
      //LogUtils.w("split failed:%s", recString);
      return null;
    }
    String[] split = recString.split(BaseSocketMessage4qz.CH_SPLIT);
    if (split.length < 3) {
      return null;
    }
    return split;
  }

  /**
   * 检查并分段接收的字符串，有无 头尾标识符 皆可.
   *
   * @return 第一段和最后一段必须是数字串，分段成功返回长度至少为3的字符串数组，否则返回null.
   */
  public static String[] splitRecInfo(String recString) {
    String[] split = splitRecInfoWithoutHead(recString);
    split = split != null ? split : splitRecInfoWithHead(recString);
    if (split != null) {
      split = ReceiveListenerAbs.replaceSpChar(split);
    }
    return split;
  }

  /**
   * 生成通讯指令.
   *
   * @param args 至少有3个参数，为null的参数会被忽略
   */
  public static String genProtocol(Object... args) {
    if (args == null || args.length < 3) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    sb.append(CH_HEAD_SEND); // #
    for (int i = 0; i < args.length; i++) {
      if (args[i] != null) {
        sb.append(args[i]).append(CH_SPLIT);
      }
    }
    sb.setLength(sb.length() - 1);
    sb.append(CH_END);
    return sb.toString();
  }

  /**
   * 根据接收到消息new一个基本消息对象.
   */
  public static BaseSocketMessage4qz<?> newInstance(String recString) {
    String[] split = BaseSocketMessage4qz.splitRecInfo(recString);
    if (split == null) {
      return null;
    }
    int func = Integer.parseInt(split[0]);
    int num = Integer.parseInt(split[split.length - 1]);
    return new BaseSocketMessage4qz(func, split, num) {
      @Override public String getMessage() {
        message = recString;
        return recString;
      }
    };
  }

  /** 向前置发送数据. */
  public boolean send() {
    return false;
  }

  /** 向前置发送数据,发送前命令序号+1. */
  public boolean sendAddCount() {
    msgNum = addAndGetSendCount();
    return send();
  }

  private ReplyListener replyListener;
  public static final long DEFAULT_TIMEOUT = 3000L;
  private static final Map<Object, MsgFuture<?>> syncKey = new ConcurrentHashMap<>();

  private void addMsgFuture(MsgFuture<?> value) {
    if (value == null) {
      return;
    }
    syncKey.put(value.requestId(), value);
  }

  private void removeMsgFuture(MsgFuture<?> value) {
    if (value == null) {
      return;
    }
    syncKey.remove(value.requestId());
  }

  public MsgFuture<?> sendSync(long timeout) {
    msgNum = addAndGetSendCount();
    MsgFuture4qz msgFuture4qz = new MsgFuture4qz(msgNum, timeout);
    addMsgFuture(msgFuture4qz);
    if (!send()) {
      removeMsgFuture(msgFuture4qz);
      msgFuture4qz.setSendSuccess(false);
      return msgFuture4qz;
    }

    msgFuture4qz.setSendSuccess(true);
    ThreadUtil.execute(() -> {
      BaseSocketMessage4qz<?> response = null;
      try {
        response = msgFuture4qz.getAndWaitTimeout();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      removeMsgFuture(msgFuture4qz);
      if (replyListener == null) {
        if (response != null) {
          replyListener.onReceive(response);
        } else {
          replyListener.onTimeout(BaseSocketMessage4qz.this);
        }
      }
    });

    return msgFuture4qz;
  }

  public MsgFuture<?> sendSync() {
    return sendSync(DEFAULT_TIMEOUT);
  }

  public ReplyListener getReplyListener() {
    return replyListener;
  }

  public void setReplyListener(
      ReplyListener replyListener) {
    this.replyListener = replyListener;
  }

  public interface ReplyListener {
    void onReceive(BaseSocketMessage4qz<?> recMsg);

    void onTimeout(BaseSocketMessage4qz<?> sendMsg);
  }

  public static void main(String[] args) {
    String res = "^\\*\\d+ .+ \\d+#$";
    String recString = "*22 userID 009#";
    boolean matches = recString.matches(res);
    System.out.printf("%s:%s\n", matches, recString);

    recString = "*22userID 009#";
    matches = recString.matches(res);
    System.out.printf("%s:%s\n", matches, recString);

    recString = "*22 userID009#";
    matches = recString.matches(res);
    System.out.printf("%s:%s\n", matches, recString);

    recString = "*22u serID 009#";
    matches = recString.matches(res);
    System.out.printf("%s:%s\n", matches, recString);

    recString = "*22 ^& 019#";
    matches = recString.matches(res);
    System.out.printf("%s:%s\n", matches, recString);

    recString = "*22 019#";
    matches = recString.matches(res);
    System.out.printf("%s:%s\n", matches, recString);

    recString =
        "*18 202100270100200055 808900830900800055 {\"batchId\":\"202100270100200055\",\"identStatus\":0} 3#";
    matches = recString.matches(res);
    System.out.printf("%s:%s\n", matches, recString);
  }
}
