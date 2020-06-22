package com.fanfull.libhard.barcode;

import android.content.Context;

import com.apkfuns.logutils.LogUtils;
import com.blankj.utilcode.util.Utils;

import org.orsoul.baselib.util.ClickUtil;

public class BarcodeController {
    private IBarcodeOperation barcodeOperation;
    private IBarcodeListener barcodeListener;
    private BarcodeReadThread readThread;

    private BarcodeController(IBarcodeOperation barcodeOperation) {
        this.barcodeOperation = barcodeOperation;
    }

    public static BarcodeController newInstance(IBarcodeOperation barcodeOperation) {
        return new BarcodeController(barcodeOperation);
    }

    public void setBarcodeListener(IBarcodeListener barcodeListener) {
        this.barcodeListener = barcodeListener;
        barcodeOperation.setBarcodeListener(barcodeListener);
    }

    public void open(Context context) {
        barcodeOperation.open(context);
    }

    public void release() {
        barcodeOperation.release();
    }

    public synchronized void startReadThread() {
        if (readThread == null || readThread.getState() == Thread.State.TERMINATED) {
            readThread = new BarcodeController.BarcodeReadThread();
            readThread.start();
        }
    }

    public synchronized void stopReadThread() {
        if (readThread != null) {
            readThread.stopRead();
            readThread.interrupt();
            readThread = null;
        }
    }

    private class BarcodeReadThread extends Thread {
        private boolean stopped;

        public synchronized boolean isStop() {
            return stopped;
        }

        public synchronized void stopRead() {
            this.stopped = true;
        }

        @Override
        public void run() {
            LogUtils.i("run start");
            //            barcodeOperation.powerOn();
            //            SystemClock.sleep(500);
            //            ClickUtil.click();
            if (!barcodeOperation.isOpen()) {
                LogUtils.d("%s", ClickUtil.click());
                barcodeOperation.open(Utils.getApp());
                LogUtils.d("%s", ClickUtil.click());
            }
            while (!isStop()) {
                barcodeOperation.scan();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    //                    break;
                }
                barcodeOperation.cancelScan();
                LogUtils.v("cancelScan");
            }
            stopRead();
            int i = 5 / 0;
            LogUtils.i("run end");
        }
    }
}
