package com.fanfull.libhard.rd.barcodeScanTest;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * Created by Tom on 2017-06-14.
 */

public class NewApiService extends ScanApi {
    public static final String ACTION_OPEN = "barcodeservice.decoder.OPEN";
    public static final String ACTION_CLOSE = "barcodeservice.decoder.CLOSE";
    public static final String ACTION_SCAN = "barcodeservice.decoder.SCAN";
    public static final String ACTION_SCAN_CHARSET = "CHARSET";

    public static final String ACTION_STOP = "barcodeservice.decoder.STOP";

    public static final String ACTION_SAVE_IMG = "barcodeservice.decoder.SAVE_IMG";
    public static final String ACTION_SAVE_IMG_ENABLE = "ENABLE";
    public static final String ACTION_SAVE_IMG_PATH = "PATH";

    public static final String ACTION_RESULT = "barcodeservice.decoder.RESULT";
    public static final String ACTION_RESULT_EXTRA = "DATA";

    private static final String TAG = "NewApiService";

    private BarcodeReceiver resultReceiver = null;

    public class BarcodeReceiver extends BroadcastReceiver {
        public void onReceive(Context ctx, Intent intent) {
            if (intent.getAction().equals(ACTION_RESULT)) {
                String code = intent.getStringExtra(ACTION_RESULT_EXTRA);

                Log.i(TAG, "code: " + code);
                if (mDecodeCallback != null) {
                    mDecodeCallback.onDecodeComplete(0, code.length(), code.getBytes(), NewApiService.this);

                }
            }
        }
    }

    public void init(Context cnt) {
        super.init(cnt);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_RESULT);

        if (resultReceiver == null)
            resultReceiver = new BarcodeReceiver();

        mContext.registerReceiver(resultReceiver, filter);
    }

    @Override
    public void deInit() {
        powerOff();
        mContext.unregisterReceiver(resultReceiver);
    }

    @Override
    public void doScan() {
        Intent intent = new Intent(ACTION_SCAN);
        intent.putExtra(ACTION_SCAN_CHARSET, "UTF-8");
        broadcastSend(intent);
    }

    @Override
    public void cancelScan() {
        broadcastSend(new Intent(ACTION_STOP));
    }

    @Override
    public void powerOn() {
        broadcastSend(new Intent(ACTION_OPEN));
    }

    @Override
    public void powerOff() {
        broadcastSend(new Intent(ACTION_CLOSE));
    }

    @Override
    public void imageSave(boolean enable) {
        Intent intent = new Intent(ACTION_SAVE_IMG);
        intent.putExtra(ACTION_SAVE_IMG_ENABLE, enable);
        //intent.putExtra(ACTION_SAVE_IMG_PATH, "/sdcard/decodeimage.png");
        broadcastSend(intent);
    }

    private void broadcastSend(Intent intent) {
        if (mContext != null) {
            intent.setPackage("com.huayusoft.barcodeadmin");
            mContext.startService(intent);
        }
    }
}
