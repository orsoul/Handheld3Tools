package com.fanfull.libhard.finger.impl;

import android.content.Context;
import com.apkfuns.logutils.LogUtils;
import com.fanfull.libhard.finger.IFingerListener;
import com.fanfull.libhard.finger.IFingerOperation;
import com.fanfull.libhard.finger.bean.FingerBean;
import com.fanfull.libhard.finger.db.FingerPrintSQLiteHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
  public boolean init(Context context) {
    fingerPrintSQLiteHelper = FingerPrintSQLiteHelper.init(context);
    return open();
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
    fingerPrintSQLiteHelper = null;
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
    int res = addFinger(fingerIdBuff, fingerFeatureBuff);
    FingerBean fingerBean = null;
    if (res == FingerPrintCmd.RES_CODE_SUCCESS) {
      fingerBean = new FingerBean(fingerIdBuff[0], fingerFeatureBuff);
    }
    return fingerBean;
  }

  @Override public int searchFinger(int[] fingerIndexBuff) {
    return operation.searchFinger(fingerIndexBuff);
  }

  @Override public int loadFinger(int fingerIndex, byte[] fingerFeatureBuff) {
    return operation.loadFinger(fingerIndex, fingerFeatureBuff);
  }

  //public int loadFinger(byte[] fingerFeatureBuff) {
  //  fingerPrintSQLiteHelper.queryAllFingerIndex();
  //  return operation.loadFinger(fingerIndex, fingerFeatureBuff);
  //}

  /** 添加指纹特征码到指纹库. 返回成功添加的数量 */
  public int loadFinger(List<FingerBean> fingerBeans) {
    if (fingerBeans == null || fingerBeans.isEmpty()) {
      return FingerPrintCmd.RES_CODE_ARGS_WRONG;
    }

    int count = 0;
    for (FingerBean fingerBean : fingerBeans) {
      int res = loadFinger(fingerBean.getFingerIndex(), fingerBean.getFingerFeature());
      if (res == FingerPrintCmd.RES_CODE_SUCCESS) {
        count++;
      }
    }

    LogUtils.d("loadFinger count / total:  %s / %s", count, fingerBeans.size());
    return count;
  }

  /** 加载指纹特征码到指纹库. 返回成功添加的数量 */
  public int loadFinger(FingerBean... fingerBeans) {
    if (fingerBeans == null) {
      return FingerPrintCmd.RES_CODE_ARGS_WRONG;
    }

    List<FingerBean> list = new ArrayList<>();
    Collections.addAll(list, fingerBeans);
    return loadFinger(list);
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

  /**
   * 删除指纹.
   *
   * @param deleteDb true 删除数据库中指纹
   */
  public boolean delete(FingerBean fingerBean, boolean deleteDb) {
    if (fingerBean == null) {
      return false;
    }
    boolean deleteInLib = deleteFinger(fingerBean.getFingerIndex());
    if (deleteDb && deleteInLib) {
      return 0 < fingerBean.delete();
    }

    return deleteInLib;
  }

  /** 删除指纹. 不删除数据库中指纹 */
  public boolean delete(FingerBean fingerBean) {
    return delete(fingerBean, false);
  }

  /**
   * 删除指纹.
   *
   * @param deleteDb true 删除数据库中指纹
   */
  public int delete(List<FingerBean> fingerBeans, boolean deleteDb) {
    if (fingerBeans == null) {
      return 0;
    }
    int count = 0;
    for (FingerBean fingerBean : fingerBeans) {
      if (delete(fingerBean, deleteDb)) {
        count++;
      }
    }
    return count;
  }

  /** 删除指纹. 不删除数据库中指纹 */
  public int delete(List<FingerBean> fingerBeans) {
    return delete(fingerBeans, false);
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
