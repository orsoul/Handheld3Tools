package com.fanfull.socket;

import android.util.Log;
import com.apkfuns.logutils.LogUtils;
import com.fanfull.contexts.StaticString;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author
 * @ClassName: SentThread
 * @Description: 向服务器发送信息, 并开启 一个线程 接收服务器的回复信息
 * @date 2014-9-12 下午04:26:17
 */
public class SendTask implements Runnable {
  private final static String TAG = SendTask.class.getSimpleName();
  public DataOutputStream dout;
  private String msg = null;

  public SendTask(DataOutputStream dout) {
    this.dout = dout;
  }

  /**
   * @return void 返回类型
   * @throws
   * @Title: setProperty
   * @Description: 设置传输内容
   */
  public void setProperty(int taskID, String taskcount) {
    Log.d(TAG, "setProperty() " + " taskID:" + taskID + " taskcount:"
        + taskcount);
    switch (taskID) {
      case 798:// 初始化
        msg = "$ib " + StaticString.bagid + " " + taskcount + "#";
        break;
      case 796:
        msg = ("$rp " + StaticString.bagid + " " + taskcount + "#");
        break;
      default:

        break;
    }
  }

  @Override
  public void run() {
    LogUtils.e("sentTask " + msg);
    if (null == msg) {
      return;
    }
    try {
      // dout.writeUTF(msg);
      dout.writeBytes(msg);
      dout.flush();
    } catch (IOException e) {
      e.printStackTrace();
      LogUtils.i("SentThread socket reconnect");// $ib
      // 05531106803A87822652049C94
      // 220#
      SocketConnet.getInstance().connect(2000);
    }
  }

  public DataOutputStream getDout() {
    return dout;
  }

  public void setDout(DataOutputStream dout) {
    this.dout = dout;
  }
}
