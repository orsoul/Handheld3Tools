package com.fanfull.libhard.lock3.task;

import com.fanfull.libjava.util.ThreadUtil;

public abstract class ScanLotTask<T> extends ThreadUtil.TimeThreadRunnable {

  /** 返回true结束扫描，否则继续扫描. */
  protected abstract boolean onScanSuccess(T data);

  /** 返回true结束扫描，否则继续扫描. */
  protected abstract boolean onScanFailed();

  protected abstract T scanOnce();

  @Override protected boolean handleOnce() {
    T data = scanOnce();
    boolean res;
    if (data != null) {
      res = onScanSuccess(data);
    } else {
      res = onScanFailed();
    }
    return res;
  }
}