package com.fanfull.handheldtools.ui.view;

import android.content.Context;

import com.blankj.utilcode.util.NetworkUtils;
import com.fanfull.handheldtools.preference.MyPreference;
import com.lxj.xpopup.XPopup;

import org.orsoul.baselib.view.FullScreenPopupSetIp;

import java.util.ArrayList;
import java.util.Set;

public class SetIpPortHelper {

  public static void showIpPortSetting(Context context,
      FullScreenPopupSetIp.OnSetIpListener onSetIpListener) {

    String ip = NetworkUtils.getIPAddress(true);
    int port = MyPreference.SERVER_PORT1.getValue();
    Set<String> hisIp = MyPreference.HIS_IP.getValue();
    Set<Integer> hisPort = MyPreference.HIS_PORT.getValue();
    FullScreenPopupSetIp fullScreenPopupSetIp =
        new FullScreenPopupSetIp(context, ip, port, new ArrayList<>(hisIp),
            new ArrayList<>(hisPort), onSetIpListener);

    new XPopup.Builder(context)
        .hasStatusBarShadow(true)
        .autoOpenSoftInput(true)
        .asCustom(fullScreenPopupSetIp)
        .show();
  }
}
