package org.orsoul.baselib.tcp.msg;

import com.apkfuns.logutils.LogUtils;

public abstract class MessageReceiveListenerAbs extends ReceiveStringArrayListenerAbs {

  @Override public void onReceive(String recString) {
    BaseSocketMessage4qz msg = MessageHelper.parse(recString);
    LogUtils.i("SocketConnect:%s", msg);
    if (msg != null) {
      //msg.recString = recString;
      onRecMessage(msg);
    } else {
      super.onReceive(recString);
    }
  }

  @Override protected void onReceive(String[] split) {

  }

  public abstract void onRecMessage(BaseSocketMessage4qz msg);
}
