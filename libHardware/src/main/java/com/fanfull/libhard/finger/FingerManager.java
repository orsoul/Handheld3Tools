package com.fanfull.libhard.finger;

import java.util.ArrayList;
import java.util.List;


public class FingerManager {
    private static FingerManager mFingerManager = null;
    private static List<FingerListener> mFingerListenerSet = new ArrayList<FingerListener>();


    public FingerManager() {
    }

    public static FingerManager getInstance() {
        if (null == mFingerManager) {
            mFingerManager = new FingerManager();
        }

        return mFingerManager;
    }

    /**
     * @des 添加指纹回调
     */
    public void registListener(FingerListener fingerCallback) {
        if (mFingerListenerSet != null) {
            mFingerListenerSet.add(fingerCallback);
        }
    }

    public void getLocalFingerSucess(int n) {
        for (FingerListener listener : mFingerListenerSet) {
            listener.getLocalFingerSucess(n);
        }
    }

    public void getLocalFingerError() {
        for (FingerListener listener : mFingerListenerSet) {
            listener.getLocalFingerError();
        }
    }

    public void getLocalFingerNoData() {
        for (FingerListener listener : mFingerListenerSet) {
            listener.getLocalFingerNoData();
        }
    }

    public void openFingerSerialPortSuccess(boolean flag) {
        for (FingerListener listener : mFingerListenerSet) {
            listener.openFingerSerialPortSuccess(flag);
        }
    }

    public void addFingerData(int flag, String info) {
        for (FingerListener listener : mFingerListenerSet) {
            listener.addFingerData(flag, info);
        }
    }

    public void deleteFingerNmber(boolean flag) {
        for (FingerListener listener : mFingerListenerSet) {
            listener.deleteFingerNmber(flag);
        }
    }

    public void emptyFinger(boolean flag) {
        for (FingerListener listener : mFingerListenerSet) {
            listener.emptyFinger(flag);
        }
    }

    public void stopSearchFinger(boolean flag) {
        for (FingerListener listener : mFingerListenerSet) {
            listener.stopSearchFinger(flag);
        }
    }

    public void unregistListener(FingerListener fingerCallback) {
        if (mFingerListenerSet != null && mFingerListenerSet.size() > 0) {
            mFingerListenerSet.remove(fingerCallback);
        }
    }

    public interface FingerListener {
        void getLocalFingerSucess(int n);//

        void getLocalFingerError();//在读取指纹过程中发生错误

        void getLocalFingerNoData();//本地指纹库没有该指纹

        void openFingerSerialPortSuccess(boolean flag);

        void addFingerData(int flag, String info);

        void deleteFingerNmber(boolean flag);

        void emptyFinger(boolean flag);

        void stopSearchFinger(boolean flag);
    }

}
                                                  