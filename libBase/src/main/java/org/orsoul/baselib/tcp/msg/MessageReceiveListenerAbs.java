package org.orsoul.baselib.tcp.msg;

import com.apkfuns.logutils.LogUtils;

public abstract class MessageReceiveListenerAbs extends ReceiveListenerAbs {

  @Override public void onReceive(String recString) {
    BaseSocketMessage4qz msg = MessageHelper.parse(recString);
    LogUtils.i("SocketConnect:%s", msg);
    if (msg != null) {
      onRecMessage(msg);
    } else {
      super.onReceive(recString);
    }
  }

  protected abstract void onRecMessage(BaseSocketMessage4qz msg);
}
