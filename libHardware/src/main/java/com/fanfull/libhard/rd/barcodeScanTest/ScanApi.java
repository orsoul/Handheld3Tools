package com.fanfull.libhard.rd.barcodeScanTest;

import android.content.Context;

/**
 * Created by Tom on 2017-06-14.
 */

public abstract class ScanApi {
    protected Context mContext = null;
    protected DecodeCallback mDecodeCallback;
    protected ErrorCallback mErrorCallback;

    public void init(Context cnt) {mContext = cnt;}

    public abstract void deInit();

    public abstract void doScan();

    public abstract void cancelScan();

    public abstract void powerOn();

    public abstract void powerOff();

    public abstract void imageSave(boolean enable);

    public final void setDecodeCallback(DecodeCallback cb) { mDecodeCallback = cb; }

    public interface DecodeCallback {
        void onDecodeComplete(int symbology, int length, byte[] data, ScanApi api);

        void onEvent(int event, int info, byte[] data, ScanApi api);
    }

    public interface ErrorCallback {
        void onError(int error, ScanApi api);
    }
}
