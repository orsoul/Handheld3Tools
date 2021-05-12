package com.fanfull.handheldtools.ui.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.libjava.util.RegularUtil;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.FullScreenPopupView;

import org.orsoul.baselib.R;

import java.util.List;

public class FullScreenPopupSetIp extends FullScreenPopupView implements View.OnClickListener {
  private String ip;
  private int port;
  private List<String> hisIp;
  private List<Integer> hisPort;

  //public FullScreenPopupSetIp(@NonNull Context context) {
  //  super(context);
  //}

  @Override protected void onCreate() {
    super.onCreate();
    init(ip, port, hisIp, hisPort);
  }

  public FullScreenPopupSetIp(@NonNull Context context, String ip, int port, List<String> hisIp,
      List<Integer> hisPort,
      OnSetIpListener onSetIpListener) {
    super(context);
    this.ip = ip;
    this.port = port;
    this.hisIp = hisIp;
    this.hisPort = hisPort;
    this.onSetIpListener = onSetIpListener;
  }

  public FullScreenPopupSetIp(@NonNull Context context, String ip, int port,
      OnSetIpListener onSetIpListener) {
    this(context, ip, port, null, null, onSetIpListener);
  }

  @Override protected int getImplLayoutId() {
    return R.layout.fullscreen_popup_set_ip;
  }

  AutoCompleteTextView autoIp;
  AutoCompleteTextView autoPort;
  View btnSave;
  boolean ipOk;
  boolean portOk;

  OnSetIpListener onSetIpListener;

  public void setOnSetIpListener(
      OnSetIpListener onSetIpListener) {
    this.onSetIpListener = onSetIpListener;
  }

  private void init(String ip, int port, List<String> hisIp, List<Integer> hisPort) {
    autoIp = findViewById(R.id.auto_ip);
    setAdapt(autoIp, hisIp);

    autoPort = findViewById(R.id.auto_port);
    setAdapt(autoPort, hisPort);

    btnSave = findViewById(R.id.btn_ip_save);
    btnSave.setOnClickListener(this);
    btnSave.setEnabled(ipOk && portOk);

    findViewById(R.id.btn_ip_cancel).setOnClickListener(this);

    autoIp.setOnClickListener(this);
    autoPort.setOnClickListener(this);

    autoIp.addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override public void onTextChanged(CharSequence s, int start, int before, int count) {

      }

      @Override public void afterTextChanged(Editable s) {
        if (RegularUtil.matchIP(s.toString())) {
          ipOk = true;
        } else {
          ipOk = false;
        }
        LogUtils.d("ipOk-portOk: %s, %s %s", s, ipOk, portOk);
        btnSave.setEnabled(ipOk && portOk);
      }
    });
    autoPort.addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override public void onTextChanged(CharSequence s, int start, int before, int count) {

      }

      @Override public void afterTextChanged(Editable s) {
        if (RegularUtil.matchDecimalString(s.toString())) {
          portOk = true;
        } else {
          portOk = false;
        }
        LogUtils.d("ip-port: %s, %s %s", s, ipOk, portOk);
        btnSave.setEnabled(ipOk && portOk);
      }
    });
    if (!TextUtils.isEmpty(ip)) {
      autoIp.setText(ip);
      autoIp.setSelection(ip.length());
    }
    if (0 < port) {
      autoPort.setText(String.valueOf(port));
    }
  }

  private <T> void setAdapt(AutoCompleteTextView autoView, List<T> list) {
    if (autoView != null && list != null) {
      autoView.setAdapter(
          new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, list));
    }
  }

  public void setIpData(List<String> ipArr) {
    setAdapt(autoIp, ipArr);
  }

  public void setPortData(List<Integer> portArr) {
    setAdapt(autoIp, portArr);
  }

  @Override public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.auto_ip) {
      autoIp.showDropDown();
    } else if (id == R.id.auto_port) {
      autoPort.showDropDown();
    } else if (id == R.id.btn_ip_cancel) {
      smartDismiss();
    } else if (id == R.id.btn_ip_save) {
      if (onSetIpListener != null) {
        String ip = autoIp.getText().toString();
        int port;
        String s = autoPort.getText().toString();
        if (TextUtils.isEmpty(s)) {
          port = -1;
        } else {
          port = Integer.parseInt(s);
        }
        boolean b = onSetIpListener.onSetIp(ip, port,
            ip.equals(this.ip) && port == this.port);
        if (b) {
          smartDismiss();
        }
      } else {
        smartDismiss();
      }
    }
  }

  public static void showIpPortSetting(Context context, String ip, int port, List<String> hisIp,
      List<Integer> hisPort, FullScreenPopupSetIp.OnSetIpListener onSetIpListener) {
    FullScreenPopupSetIp fullScreenPopupSetIp =
        new FullScreenPopupSetIp(context, ip, port, hisIp,
            hisPort, onSetIpListener);

    new XPopup.Builder(context)
        .hasStatusBarShadow(true)
        .autoOpenSoftInput(false)
        .asCustom(fullScreenPopupSetIp)
        .show();
  }

  public static void showIpPortSetting(Context context, String ip, int port,
      FullScreenPopupSetIp.OnSetIpListener onSetIpListener) {
    showIpPortSetting(context, ip, port, null, null, onSetIpListener);
  }

  public interface OnSetIpListener {
    boolean onSetIp(String ip, int port, boolean settingNoChange);
  }
}