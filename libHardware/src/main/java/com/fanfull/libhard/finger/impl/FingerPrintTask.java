package com.fanfull.libhard.finger.impl;

import com.fanfull.libhard.finger.bean.FingerBean;
import com.fanfull.libhard.finger.db.FingerPrintSQLiteHelper;
import org.orsoul.baselib.util.ClockUtil;
import org.orsoul.baselib.util.ThreadUtil;

/**
 * 添加、搜索指纹 任务，使用前应先初始化 FingerprintController 和 FingerPrintSQLiteHelper.
 */
public class FingerPrintTask extends ThreadUtil.ThreadRunnable {
  private FingerprintController fingerprintController;
  private FingerPrintSQLiteHelper fingerPrintSQLiteHelper;
  private FingerSearchListener fingerSearchListener;

  /** 任务持续时间. */
  private long runTime = 5000L;
  /** 添加或搜索成功时 是否终止线程，默认true. */
  private boolean successStop = true;
  /**
   * 工作模式：false为匹配指纹，true为添加指纹.<br/>
   * 工作模式设为添加指纹时 默认会获取特征码<br/>
   * 工作模式设为搜索指纹时 默认不获取特征码<br/>
   */
  private boolean isAddMode;
  /**
   * 是否获取指纹特征码.<br/>
   * 工作模式设为添加指纹时 默认会获取特征码<br/>
   * 工作模式设为搜索指纹时 默认不获取特征码<br/>
   */
  private boolean isGetFeature;

  private byte[] fingerFeature;

  public FingerPrintTask() {
    fingerPrintSQLiteHelper = FingerPrintSQLiteHelper.getInstance();
    fingerprintController = FingerprintController.getInstance();
  }

  public FingerPrintTask(FingerSearchListener listener) {
    super();
    fingerSearchListener = listener;
  }

  public FingerPrintTask(FingerprintController fingerprintController,
      FingerPrintSQLiteHelper fingerPrintSQLiteHelper) {
    this.fingerprintController = fingerprintController;
    this.fingerPrintSQLiteHelper = fingerPrintSQLiteHelper;
  }

  public void setAddMode(boolean addMode) {
    isAddMode = addMode;
    setGetFeature(addMode);
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

  public boolean isSuccessStop() {
    return successStop;
  }

  public void setSuccessStop(boolean successStop) {
    this.successStop = successStop;
  }

  public void setRunTime(long runTime) {
    this.runTime = runTime;
  }

  public FingerSearchListener getFingerSearchListener() {
    return fingerSearchListener;
  }

  public void setFingerSearchListener(
      FingerSearchListener fingerSearchListener) {
    this.fingerSearchListener = fingerSearchListener;
  }

  @Override
  public void run() {
    //LogUtils.i("run start");

    int[] fingerIdBuff = new int[2];
    ClockUtil.resetRunTime();
    while (!stopped) {
      if (runTime <= ClockUtil.runTime()) {
        if (fingerSearchListener != null) {
          fingerSearchListener.onFailed(isAddMode, FingerPrintCmd.RES_CODE_TIMEOUT);
        }
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

      if (res == FingerPrintCmd.RES_CODE_NO_FINGER) { // 感应器无指纹
        if (fingerSearchListener != null) {
          fingerSearchListener.onNoFinger();
        }
      } else if (res == FingerPrintCmd.RES_CODE_NO_MATCH) { // 无匹配的指纹
        if (fingerSearchListener != null) {
          fingerSearchListener.onSearchNoMatch();
        }
      } else if (res == FingerPrintCmd.RES_CODE_SUCCESS) { // 匹配到指纹、添加指纹成功
        if (isGetFeature) {
          onSuccess(isAddMode, fingerIdBuff[0], fingerIdBuff[1], fingerFeature);
        } else {
          onSuccess(isAddMode, fingerIdBuff[0], fingerIdBuff[1], null);
        }
        if (successStop) {
          break;
        }
      } else {
        if (fingerSearchListener != null) {
          fingerSearchListener.onFailed(isAddMode, res);
        }
        break;
      }
    } // end while
    //LogUtils.i("run end");
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
      fingerBean.setFingerName(String.format("未命名%s", fingerIndex));
      boolean isSaveInDB = false;
      if (fingerPrintSQLiteHelper != null) {
        isSaveInDB = 0 < fingerPrintSQLiteHelper.saveOrUpdate(fingerBean);
      }
      if (fingerSearchListener != null) {
        fingerSearchListener.onAddSuccess(fingerBean, isSaveInDB);
      }
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
      if (fingerSearchListener != null) {
        fingerSearchListener.onSearchSuccess(fingerBean, isSaveInDB);
      }
    }
  }

  public interface FingerSearchListener {
    /** 感应器无指纹时 回调. */
    default void onNoFinger() {
    }

    default void onAddSuccess(FingerBean fingerBean, boolean isSaveInDB) {
    }

    default void onSearchSuccess(FingerBean fingerBean, boolean isSaveInDB) {
    }

    /** 搜索指纹时，指纹库中无匹配指纹时进行回调. */
    default void onSearchNoMatch() {
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
    default void onFailed(boolean isAddMode, int errorCode) {
    }
  }
}