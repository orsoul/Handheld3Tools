package com.fanfull.libhard.finger.impl;

import android.content.Context;
import com.fanfull.libhard.finger.IFingerListener;
import com.fanfull.libhard.finger.IFingerOperation;
import com.fanfull.libhard.finger.bean.FingerBean;
import com.fanfull.libhard.finger.db.FingerPrintSQLiteHelper;

public class FingerprintController implements IFingerOperation {
  //private IFingerOperation operation;
  public FingerOperationRd operation;
  private static FingerPrintSQLiteHelper fingerPrintSQLiteHelper;

  //private FingerprintController(IFingerOperation operation) {
  //  this.operation = operation;
  //}
  private FingerprintController(FingerOperationRd operation) {
    this.operation = operation;
  }

  /** 初始化数据库、并打开指纹模块 */
  public void init(Context context) {
    fingerPrintSQLiteHelper = FingerPrintSQLiteHelper.init(context);
    open();
  }

  @Override
  public boolean open() {
    return operation.open();
  }

  @Override
  public boolean isOpen() {
    return operation.isOpen();
  }

  @Override
  public boolean isScanning() {
    return operation.isScanning();
  }

  @Override
  public void release() {
    operation.release();
  }

  @Override
  public boolean send(byte[] data) {
    return operation.send(data);
  }

  @Override public int addFinger(int[] fingerIndexBuff) {
    return operation.addFinger(fingerIndexBuff);
  }

  @Override public int addFinger(int[] fingerIndexBuff, byte[] fingerFeatureBuff) {
    return operation.addFinger(fingerIndexBuff, fingerFeatureBuff);
  }

  public FingerBean addFinger() {
    int[] fingerIdBuff = new int[1];
    byte[] fingerFeatureBuff = new byte[FingerPrintCmd.FINGER_FEATURE_LEN];
    int res = operation.addFinger(fingerIdBuff, fingerFeatureBuff);
    FingerBean fingerBean = null;
    if (res == FingerPrintCmd.RES_CODE_SUCCESS) {
      fingerBean = new FingerBean(fingerIdBuff[0], fingerFeatureBuff);
    }
    return fingerBean;
  }

  @Override public int searchFinger(int[] fingerIndexBuff) {
    return operation.searchFinger(fingerIndexBuff);
  }

  @Override public int searchFinger(int[] fingerIndexBuff, byte[] fingerFeatureBuff) {
    return operation.searchFinger(fingerIndexBuff, fingerFeatureBuff);
  }

  @Override public int getFingerNum(int[] fingerNumBuff) {
    return operation.getFingerNum(fingerNumBuff);
  }

  @Override public boolean deleteFinger(int fingerIndex) {
    return operation.deleteFinger(fingerIndex);
  }

  @Override public boolean clearFinger() {
    return operation.clearFinger();
  }

  /** 清空指纹库 和 数据库中指纹. */
  public boolean clearFingerAll() {
    boolean clear = operation.clearFinger();
    if (clear) {
      fingerPrintSQLiteHelper.clearAll();
    }
    return clear;
  }

  @Override
  public void setListener(IFingerListener listener) {
    operation.setListener(listener);
  }

  private static class SingletonHolder {
    private static final FingerprintController instance =
        new FingerprintController(new FingerOperationRd());
  }

  public static FingerprintController getInstance() {
    return FingerprintController.SingletonHolder.instance;
  }
}
