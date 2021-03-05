package com.fanfull.libhard.lock3.task;

import com.fanfull.libhard.lock3.PsamHelper;
import com.fanfull.libhard.rfid.PSamCmd;
import com.fanfull.libhard.rfid.RfidController;

import org.orsoul.baselib.util.BytesUtil;
import org.orsoul.baselib.util.ThreadUtil;

/**
 * 获取登录卡ID任务.通过覆盖5个回调方法控制读卡过程，默认读m1卡uid
 */
public abstract class ReadLoginCardTask extends ThreadUtil.TimeThreadRunnable {

  public static final int CARD_M1 = 0;
  public static final int CARD_CUP = 1;
  private int cardType = CARD_M1;

  public int getCardType() {
    return cardType;
  }

  public void setCardType(int cardType) {
    this.cardType = cardType;
  }

  @Override protected boolean handleOnce() {
    byte[] cardBuff = readLoginCard();
    if (cardBuff != null) {
      onReadSuccess(cardBuff);
      return true;
    }
    return false;
  }

  /** 读卡中. */
  @Override protected void onHandleOnce(long runTime, int total) {
  }

  /** 读卡失败. */
  @Override protected void onTimeout(long runTime, int runTimes) {
  }

  /** 手动停止读卡. */
  @Override protected void onStop() {
  }

  /** 单次读卡逻辑，默认读取M1卡的uid，若要读他类型需要自行覆盖. */
  protected byte[] readLoginCard() {
    byte[] cardBuff = null;
    boolean readSuccess = false;
    switch (cardType) {
      case CARD_M1:
        cardBuff = RfidController.getInstance().getM1CardId();
        //readSuccess = RfidController.getInstance().findCard(cardBuff);
        readSuccess = cardBuff != null;
        break;
      case CARD_CUP:
        cardBuff = new byte[PSamCmd.COS_RES_CARD_LEN];
        readSuccess = PSamCmd.COS_RES_CARD_LEN == PsamHelper.readCPUCard(cardBuff);
        break;
    }
    if (readSuccess) {
      return cardBuff;
    }
    return null;
  }

  /** 读卡成功. */
  protected void onReadSuccess(byte[] cardId) {
    switch (cardType) {
      case CARD_M1:
        onReadSuccess(BytesUtil.bytes2HexString(cardId));
        return;
      case CARD_CUP:
        String s = new String(cardId, 0, cardId.length - 2);
        onReadSuccess(s);
        break;
    }
  }

  /** 读卡成功. */
  protected abstract void onReadSuccess(String cardId);
}