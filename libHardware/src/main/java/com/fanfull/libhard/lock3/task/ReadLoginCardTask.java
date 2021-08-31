package com.fanfull.libhard.lock3.task;

import com.fanfull.libhard.lock_zc.PsamHelper;
import com.fanfull.libhard.rfid.PSamCmd;
import com.fanfull.libhard.rfid.RfidController;
import com.fanfull.libjava.util.BytesUtil;
import com.fanfull.libjava.util.ThreadUtil;

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
      return onReadSuccess(cardBuff);
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

  /** 读卡成功.执行完成 返回true，继续执行返回false. */
  protected boolean onReadSuccess(byte[] cardId) {
    switch (cardType) {
      case CARD_M1:
        return onReadSuccess(BytesUtil.bytes2HexString(cardId));
      case CARD_CUP:
        String s = new String(cardId, 0, cardId.length - 2);
        return onReadSuccess(s);
    }
    return true;
  }

  /** 读卡成功. */
  protected abstract boolean onReadSuccess(String cardId);
}