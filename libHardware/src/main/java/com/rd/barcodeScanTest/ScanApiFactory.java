package com.rd.barcodeScanTest;

/**
 * Created by Tom on 2017-06-15.
 */

public class ScanApiFactory {
    private int idx = 0;
    private NewApiBroadcast newApiBroadcast = new NewApiBroadcast();
    private NewApiService newApiService = new NewApiService();


    public ScanApiFactory(int idx) {
        this.idx = idx;
    }

    public ScanApi createApi() {
        return createApi(this.idx);
    }

    public ScanApi createApi(int idx) {
        if (idx == 1)
            return newApiBroadcast;
        else if (idx == 0)
            return newApiService;
        else
            return null;
    }
}
