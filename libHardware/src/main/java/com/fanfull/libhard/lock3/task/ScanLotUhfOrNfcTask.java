package com.fanfull.libhard.lock3.task;

import com.fanfull.libhard.EnumErrCode;
import com.fanfull.libhard.rfid.RfidController;
import com.fanfull.libhard.uhf.UhfCmd;
import com.fanfull.libhard.uhf.UhfController;

import org.orsoul.baselib.lock3.bean.Lock3Bean;
import org.orsoul.baselib.util.BytesUtil;

public abstract class ScanLotUhfOrNfcTask extends ScanLotTask<ScanLotUhfOrNfcTask.ScanLotBean> {
  protected boolean isNfcMode;
  protected boolean isReadTid = true;
  protected int readTidTimes = 3;
  private byte[] epcBuff = new byte[12];
  private byte[] tidBuff = new byte[12];
  private byte[] uidBuff = new byte[7];
  private UhfController uhfController;
  private RfidController rfidController;

  public ScanLotUhfOrNfcTask(UhfController uhfController,
      RfidController rfidController) {
    this.uhfController = uhfController;
    this.rfidController = rfidController;
  }

  public boolean isNfcMode() {
    return isNfcMode;
  }

  public void setNfcMode(boolean nfcMode) {
    isNfcMode = nfcMode;
  }

  @Override protected ScanLotBean scanOnce() {
    return isNfcMode ? scanOnceNfc() : scanOnceUhf();
  }

  protected ScanLotBean scanOnceUhf() {
    boolean readSuccess = uhfController.readEpc(epcBuff);
    if (!readSuccess) {
      return null;
    }

    //String bagId = BytesUtil.bytes2HexString(epcBuff);
    ScanLotBean scanLotBean = null;
    if (!isReadTid) {
      String bagId = BytesUtil.bytes2HexString(epcBuff);
      scanLotBean = new ScanLotBean(bagId, false);
      return scanLotBean;
    }
    for (int i = 0; i < readTidTimes; i++) {
      readSuccess =
          uhfController.read(UhfCmd.MB_TID, 0x00, tidBuff, UhfCmd.MB_EPC, 0x02, epcBuff);
      if (readSuccess) {
        String bagId = BytesUtil.bytes2HexString(epcBuff);
        scanLotBean = new ScanLotBean(bagId, false);
        scanLotBean.setTid(BytesUtil.bytes2HexString(tidBuff));
        break;
      }
    }

    return scanLotBean;
  }

  protected ScanLotBean scanOnceNfc() {
    EnumErrCode enumErrCode = rfidController.readNfc(Lock3Bean.SA_BAG_ID, epcBuff, uidBuff);
    boolean readSuccess = enumErrCode == EnumErrCode.SUCCESS;
    if (!readSuccess) {
      return null;
    }

    readSuccess = rfidController.readNfc(Lock3Bean.SA_PIECE_TID, tidBuff, false);
    if (!readSuccess) {
      return null;
    }

    String bagId = BytesUtil.bytes2HexString(epcBuff);
    ScanLotBean scanLotBean = new ScanLotBean(bagId, true);
    scanLotBean.setTid(BytesUtil.bytes2HexString(tidBuff));
    scanLotBean.setUid(BytesUtil.bytes2HexString(uidBuff));

    return scanLotBean;
  }

  public static class ScanLotBean {
    boolean isNfcMode;
    String bagId;
    String tid;
    String uid;

    public ScanLotBean(String bagId, boolean isNfcMode) {
      this.bagId = bagId;
      this.isNfcMode = isNfcMode;
    }

    public String getBagId() {
      return bagId;
    }

    public void setBagId(String bagId) {
      this.bagId = bagId;
    }

    public boolean isNfcMode() {
      return isNfcMode;
    }

    public void setNfcMode(boolean nfcMode) {
      isNfcMode = nfcMode;
    }

    public String getTid() {
      return tid;
    }

    public void setTid(String tid) {
      this.tid = tid;
    }

    public String getUid() {
      return uid;
    }

    public void setUid(String uid) {
      this.uid = uid;
    }
  }
}
