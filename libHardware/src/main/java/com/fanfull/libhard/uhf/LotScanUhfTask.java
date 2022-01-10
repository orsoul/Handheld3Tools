package com.fanfull.libhard.uhf;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.libjava.util.BytesUtil;

public abstract class LotScanUhfTask {// extends ThreadUtil.ThreadRunnable {
  //byte[] epcBuff = new byte[12];
  //byte[] tidBuff = new byte[12];

  private int total = 0;
  private int progress = 0;
  private boolean fastIdOn = false;
  private boolean isWork = false;
  private UhfController uhfController;
  private IUhfListener listener;

  public LotScanUhfTask(UhfController uhfController, IUhfListener listener) {
    this.uhfController = uhfController;
    this.listener = listener;
    this.uhfController.setListener(new MyUhfListener());
  }

  public boolean isWork() {
    return isWork;
  }

  public boolean startScan(int total) {
    if (isWork) {
      return false;
    }

    if (total < 0) {
      total = 0;
    }

    this.total = total;
    this.progress = 0;

    //UhfCmd.CMD_READ_LOT[5] = (byte) ((total >> 8) & 0xFF);
    //UhfCmd.CMD_READ_LOT[6] = (byte) (total & 0xFF);
    return uhfController.send(UhfCmd.getReadLotCmd(total));
  }

  public boolean stopScan() {
    if (!isWork) {
      return false;
    }
    return uhfController.send(UhfCmd.CMD_STOP_READ_LOT);
  }

  public void receiveData(byte[] parseData, int progress, int total) {
    if (parseData.length == 24) {
      String epc = BytesUtil.bytes2HexString(parseData, 0, 12);
      String tid = BytesUtil.bytes2HexString(parseData, 12, 24);
      onReceiveEpc(epc, tid, progress, total);
    } else if (parseData.length == 12) {
      String epc = BytesUtil.bytes2HexString(parseData);
      try {
        onReceiveEpc(epc, null, progress, total);
      } catch (Exception e) {
        LogUtils.w("Exception:%s", e.getMessage());
      }
    }
  }

  public abstract void onReceiveEpc(String epc, String tid, int progress, int total);

  public void onStop(int progress, int total) {}

  /**
   * 启动 连续扫描时 的回调.
   *
   * @param success 启动成功为true
   */
  public void onStart(boolean success, int progress, int total) {}

  class MyUhfListener implements IUhfListener {
    @Override public void onOpen(boolean openSuccess) {
      if (listener != null) {
        listener.onOpen(openSuccess);
      }
    }

    @Override public void onReceiveData(byte[] data) {
      if (listener != null) {
        listener.onReceiveData(data);
      }
      byte[] parseData = UhfCmd.parseData(data);
      if (parseData == null) {
        LogUtils.i("parseData failed:%s", BytesUtil.bytes2HexString(data));
        return;
      }
      int cmdType = data[4] & 0xFF;
      Object info = null;
      switch (cmdType) {
        case UhfCmd.RECEIVE_TYPE_GET_FAST_ID:
          if (parseData[0] == 1) {
            fastIdOn = true;
            info = "EPC、TID同时读取 开启";
          } else {
            fastIdOn = false;
            info = "EPC、TID同时读取 关闭";
          }
          break;
        case UhfCmd.RECEIVE_TYPE_SET_FAST_ID:
          // 1:成功， 0：失败
          if (parseData[0] == 1) {
            if (fastIdOn) {
              info = "EPC、TID同读设为 开启";
            } else {
              info = "EPC、TID同读设为 关闭";
            }
          } else {
            info = "设置 EPC、TID同读 失败";
          }
          break;
        case UhfCmd.RECEIVE_TYPE_READ_LOT:
          //byte[] epc = null;
          //byte[] tid = null;
          String epc = null;
          String tid = null;
          if (parseData.length == 24) {
            isWork = true;
            //epc = Arrays.copyOfRange(parseData, 0, 12);
            //tid = Arrays.copyOfRange(parseData, 12, 24);
            epc = BytesUtil.bytes2HexString(parseData, 0, 12);
            tid = BytesUtil.bytes2HexString(parseData, 12, 24);
          } else if (parseData.length == 12) {
            //epc = parseData;
            epc = BytesUtil.bytes2HexString(parseData);
            isWork = true;
          } else {
            // 续卡停止
            isWork = false;
            return;
          }
          progress++;
          if (isWork) {
            //onReceiveEpc(epc, tid, progress, total);
            receiveData(parseData, progress, total);
          }
          if (progress == 1) {
            if (isWork) {
              onStart(true, progress, total);
            } else {
              onStart(false, progress, total);
            }
          }
          break;
        case UhfCmd.RECEIVE_TYPE_READ_LOT_STOP:
          if (parseData[0] == 1) {
            // 成功
            isWork = false;
            onStop(progress, total);
          } else if (parseData[0] == 0) {
            // 失败
          }
          break;
        default:
          LogUtils.v("parseData:%s", BytesUtil.bytes2HexString(parseData));
      }
    }
  }
}