package org.orsoul.baselib.io.socketClient.impl;

import com.apkfuns.logutils.LogUtils;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import org.orsoul.baselib.io.socketClient.GeneralException;
import org.orsoul.baselib.io.socketClient.Options;
import org.orsoul.baselib.io.socketClient.interf.ISocketClient;
import org.orsoul.baselib.io.socketClient.interf.ISocketClientListener;
import org.orsoul.baselib.io.transfer.BaseIoTransfer;
import org.orsoul.baselib.io.transfer.IoTransferListener;
import org.orsoul.baselib.util.ThreadUtil;

/**
 * 基本的socket客户端，提供收发功能、断线重连功能、回复超时监听功能.
 */
public class BaseSocketClient extends BaseIoTransfer implements ISocketClient, IoTransferListener {

  private final Options ops;
  private Socket socket = null;

  /** 监听者 容器 */
  private Set<ISocketClientListener> listenerSet = new HashSet<>();
  /** socket 已连接 */
  private boolean connected = false;
  /** 允许进行 自动重连 */
  private boolean canAutoReconnect = false;

  public BaseSocketClient(Options ops) {
    this.ops = ops;
  }

  public void setAutoReconnect(boolean autoReconnect) {
    this.ops.autoReconnect = autoReconnect;
  }

  public boolean isAutoReconnect() {
    return ops.autoReconnect;
  }

  public boolean removeSocketClientListener(ISocketClientListener listener) {
    return listenerSet.remove(listener);
  }

  public boolean addSocketClientListener(ISocketClientListener listener) {
    return this.listenerSet.add(listener);
  }

  public void setIpPort(String ip, int port) {
    ops.serverIp = ip;
    ops.serverPort = port;
  }

  @Override public boolean isConnected() {
    return connected;
  }

  private boolean isConnecting;

  @Override public boolean connect() {
    if (isConnected()) {
      return true;
    }
    if (isConnecting) {
      return false;
    }
    isConnecting = true;
    try {
      // 1. 初始化Socket
      if (socket == null || !socket.isConnected()) {
        socket = new Socket();
      }

      // 2. 建立连接
      InetSocketAddress address = new InetSocketAddress(ops.serverIp, ops.serverPort);
      socket.connect(address, ops.connectTimeout);
      this.inputStream = socket.getInputStream();
      this.outputStream = socket.getOutputStream();
      canAutoReconnect = ops.autoReconnect;

      setIoTransferListener(this);
      startReceive();

      // 3. 通知应用上层，连接已经成功建立
      for (ISocketClientListener listener : listenerSet) {
        listener.onConnect(ops.serverIp, ops.serverPort);
      }
      connected = true;
    } catch (IOException e) {
      // 创建连接 异常
      GeneralException
          generalException = new GeneralException(ops.serverIp + ":" + ops.serverPort, e);
      for (ISocketClientListener listener : listenerSet) {
        listener.onConnectFailed(generalException);
      }
    } catch (Exception e) {
      // 其他异常
      e.printStackTrace();
      closeSocket();
      //      throw new GeneralException(e);
      GeneralException generalException =
          new GeneralException(ops.serverIp + ":" + ops.serverPort, e);
      for (ISocketClientListener listener : listenerSet) {
        listener.onConnectFailed(generalException);
      }
    }
    isConnecting = false;
    return connected;
  }

  public void connectOnNewThread() {
    ThreadUtil.execute(() -> connect());
  }

  /**
   * 主动断开连接.
   * 主动断开连接不会触发 onDisconnect()，也不会进行重连
   */
  @Override public void disconnect() {
    canAutoReconnect = false;
    listenerSet.clear();
    // 关闭连接
    closeSocket();
    // 停止接收线程
    stopReceive();
  }

  private void closeSocket() {
    connected = false;
    if (socket != null) {
      try {
        socket.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      socket = null;
    }
  }

  @Override public boolean onReceive(byte[] data) {
    LogUtils.d("rec:%s", new String(data));
    for (ISocketClientListener listener : listenerSet) {
      listener.onReceive(data);
    }
    return true;
  }

  @Override public void onReceiveTimeout() {

  }

  @Override public void onStopReceive() {
    LogUtils.d("onStopReceive");
    closeSocket();
    for (ISocketClientListener listener : listenerSet) {
      listener.onDisconnect(ops.serverIp, ops.serverPort);
    }
    while (canAutoReconnect && !isConnected()) {
      LogUtils.i("canAutoReconnect: %s:%s", ops.serverIp, ops.serverPort);
      ThreadUtil.sleep(ops.reconnectInterval);
      connect();
    }
  }
}
