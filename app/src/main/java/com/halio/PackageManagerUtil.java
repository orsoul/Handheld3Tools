package com.halio;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class PackageManagerUtil {
    private static Context ctx;
    private static PackageManagerUtil mInstance;
    private static PackageInfo info = null;

    public static PackageManagerUtil getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new PackageManagerUtil();
        }
        ctx = context;
        PackageManager manager = context.getPackageManager();
        try {
            info = manager.getPackageInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return mInstance;
    }

    public String getVersion() {
        if (info == null) {
            return null;
        }
        String appVersion = info.versionName;
        return appVersion;
    }

}
