package com.fanfull.libhard.finger.impl;

import android.content.Context;
import com.apkfuns.logutils.LogUtils;
import com.fanfull.libhard.finger.IFingerListener;
import com.fanfull.libhard.finger.IFingerOperation;
import com.fanfull.libhard.finger.bean.FingerBean;
import com.fanfull.libhard.finger.db.FingerPrintSQLiteHelper;
import org.orsoul.baselib.util.ClockUtil;
import org.orsoul.baselib.util.ThreadUtil;

public class FingerprintController implements IFingerOperation {
  //private IFingerOperation operation;
  public FingerOperationRd operation;
  private FingerPrintTask searchThread;
  private boolean searchThreadRunning;
  private static FingerPrintSQLiteHelper fingerPrintSQLiteHelper;

  //private FingerprintController(IFingerOperation operation) {
  //  this.operation = operation;
  //}
  private FingerprintController(FingerOperationRd operation) {
    this.operation = operation;
  }

  /** 初始化数据库 */
  public void init(Context context) {
    fingerPrintSQLiteHelper = FingerPrintSQLiteHelper.init(context);
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
    stopSearchFingerPrint();
    operation.release();
  }

  @Override
  public boolean send(byte[] data) {
    return operation.send(data);
  }

  @Override public int addFinger(int[] fingerIdBuff) {
    return operation.addFinger(fingerIdBuff);
  }

  @Override public int addFinger(int[] fingerIdBuff, byte[] fingerFeatureBuff) {
    return operation.addFinger(fingerIdBuff, fingerFeatureBuff);
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

  @Override public int searchFinger(int[] fingerIdBuff) {
    return operation.searchFinger(fingerIdBuff);
  }

  @Override public int searchFinger(int[] fingerIdBuff, byte[] fingerFeatureBuff) {
    return operation.searchFinger(fingerIdBuff, fingerFeatureBuff);
  }

  @Override public int getFingerNum(int[] fingerNumBuff) {
    return operation.getFingerNum(fingerNumBuff);
  }

  @Override public boolean clearFinger() {
    return operation.clearFinger();
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

  public synchronized void startSearchFingerPrint() {
    if (searchThread == null) {
      searchThread = new FingerPrintTask(this);
      searchThread.startRun();
    }
  }

  public synchronized void stopSearchFingerPrint() {
    if (searchThread != null) {
      searchThread.stopSearch();
      searchThread = null;
    }
  }

  public boolean isSearch() {
    return searchThread != null && searchThread.isRunning();
  }

  public static class FingerPrintTask implements Runnable {
    private FingerprintController fingerprintController;
    /** 搜索线程持续时间. */
    private long runTime = 5000L;
    /** 是否持续搜索直至 搜索成功或时间结束. */
    private boolean isContinue = true;
    /** true为添加指纹. */
    private boolean isAddMode;
    /** 是否获取指纹特征码. */
    private boolean isGetFeature;

    private boolean stopped = true;
    private byte[] fingerFeature;

    public FingerPrintTask(FingerprintController fingerprintController) {
      this.fingerprintController = fingerprintController;
    }

    public void setAddMode(boolean addMode) {
      isAddMode = addMode;
    }

    public boolean isAddMode() {
      return isAddMode;
    }

    public boolean isGetFeature() {
      return isGetFeature;
    }

    public void setGetFeature(boolean getFeature) {
      isGetFeature = getFeature;
      if (fingerFeature == null && isGetFeature) {
        fingerFeature = new byte[FingerPrintCmd.FINGER_FEATURE_LEN];
      }
    }

    public boolean isContinue() {
      return isContinue;
    }

    public void setContinue(boolean aContinue) {
      isContinue = aContinue;
    }

    public void setRunTime(long runTime) {
      this.runTime = runTime;
    }

    public synchronized void stopSearch() {
      this.stopped = true;
    }

    public synchronized boolean isRunning() {
      return !stopped;
    }

    public synchronized boolean startRun() {
      if (isRunning()) {
        return false;
      }
      ThreadUtil.execute(this);
      return true;
    }

    @Override
    public void run() {
      LogUtils.i("run start");

      stopped = false;
      int[] fingerIdBuff = new int[2];
      ClockUtil.resetRunTime();
      while (!stopped) {
        if (runTime <= ClockUtil.runTime()) {
          onFailed(isAddMode, FingerPrintCmd.RES_CODE_TIMEOUT);
          break;
        }

        int res;
        if (isAddMode) {
          if (isGetFeature) {
            res = fingerprintController.addFinger(fingerIdBuff, fingerFeature);
          } else {
            res = fingerprintController.addFinger(fingerIdBuff);
          }
        } else {
          if (isGetFeature) {
            res = fingerprintController.searchFinger(fingerIdBuff, fingerFeature);
          } else {
            res = fingerprintController.searchFinger(fingerIdBuff);
          }
        }

        if (res == FingerPrintCmd.RES_CODE_NO_FINGER) {
          onNoFinger();
        } else if (res == FingerPrintCmd.RES_CODE_NO_MATCH) {
          onSearchNoMatch();
          break;
        } else if (res == FingerPrintCmd.RES_CODE_SUCCESS) {
          if (isGetFeature) {
            onSuccess(isAddMode, fingerIdBuff[0], fingerIdBuff[1], fingerFeature);
          } else {
            onSuccess(isAddMode, fingerIdBuff[0], fingerIdBuff[1], null);
          }
          break;
        } else {
          onFailed(isAddMode, res);
          if (!isContinue) {
            break;
          }
        }
      } // end while
      LogUtils.i("run end");
      stopped = true;
    }

    /** 感应器无指纹时 回调. */
    protected void onNoFinger() {
    }

    /**
     * 添加或匹配指纹成功时 回调.
     *
     * @param isAddMode 工作方式是否为添加指纹
     * @param fingerIndex 指纹在指纹库中的保存位置
     * @param score 在匹配工作方式下，指纹匹配得分；添加指纹方式 此参数无意义
     * @param fingerFeature 所添加/匹配指纹的特征码；如果设置了不获取特征码，此参数为null
     */
    protected void onSuccess(boolean isAddMode, int fingerIndex, int score, byte[] fingerFeature) {
      if (isAddMode) {
        FingerBean fingerBean = new FingerBean(fingerIndex, fingerFeature);
        boolean isSaveInDB = false;
        if (fingerPrintSQLiteHelper != null) {
          isSaveInDB = 0 < fingerPrintSQLiteHelper.saveOrUpdate(fingerBean);
        }
        onAddSuccess(fingerBean, isSaveInDB);
      } else {
        FingerBean fingerBean = null;
        if (fingerPrintSQLiteHelper != null) {
          fingerBean = fingerPrintSQLiteHelper.queryFingerByFingerIndex(fingerIndex);
        }
        boolean isSaveInDB = true;
        if (fingerBean == null) {
          fingerBean = new FingerBean(fingerIndex, fingerFeature);
          isSaveInDB = false;
        }
        onSearchSuccess(fingerBean, isSaveInDB);
      }
    }

    protected void onAddSuccess(FingerBean fingerBean, boolean isSaveInDB) {
    }

    protected void onSearchSuccess(FingerBean fingerBean, boolean isSaveInDB) {
    }

    /** 搜索指纹时，指纹库中无匹配指纹时进行回调. */
    protected void onSearchNoMatch() {
    }

    /**
     * 添加或匹配指纹失败时 回调.
     *
     * @param isAddMode 是否为 添加加指纹
     * @param errorCode 错误码<br/>
     * 确认码=-1 非指纹指令返回<br/>
     * 确认码=-2 表示参数错误<br/>
     * 确认码=-3：搜索超时<br/>
     * 确认码=01H 表示收包有错<br/>
     */
    protected void onFailed(boolean isAddMode, int errorCode) {
    }
  }
}
