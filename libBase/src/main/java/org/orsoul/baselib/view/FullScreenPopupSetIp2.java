package org.orsoul.baselib.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Switch;

import androidx.annotation.NonNull;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.libjava.io.socketClient.Options;
import com.fanfull.libjava.util.RegularUtil;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.impl.FullScreenPopupView;

import org.orsoul.baselib.R;

import java.util.List;

public class FullScreenPopupSetIp2 extends FullScreenPopupView implements View.OnClickListener {
  private Options options;

  private String ip;
  private int port;
  boolean reconnectEnable;
  boolean heartBeatEnable;
  private int reconnectInterval;
  private int heartBeatInterval;

  private List<String> hisIp;
  private List<Integer> hisPort;

  //public FullScreenPopupSetIp(@NonNull Context context) {
  //  super(context);
  //}

  @Override protected void onCreate() {
    super.onCreate();
    init(ip, port, hisIp, hisPort);
  }

  public FullScreenPopupSetIp2(@NonNull Context context, Options options, List<String> hisIp,
      List<Integer> hisPort,
      OnSettingIpListener onSetIpListener) {
    super(context);
    this.options = options;
    this.hisIp = hisIp;
    this.hisPort = hisPort;
    this.onSetIpListener = onSetIpListener;

    this.ip = options.serverIp;
    this.port = options.serverPort;
    this.reconnectEnable = options.reconnectEnable;
    this.reconnectInterval = options.reconnectInterval;
    this.heartBeatEnable = options.heartBeatEnable;
    this.heartBeatInterval = options.heartBeatInterval;
  }

  public FullScreenPopupSetIp2(@NonNull Context context, Options options,
      OnSettingIpListener onSetIpListener) {
    this(context, options, null, null, onSetIpListener);
  }

  @Override protected int getImplLayoutId() {
    return R.layout.fullscreen_popup_set_ip2;
  }

  AutoCompleteTextView autoIp;
  AutoCompleteTextView autoPort;
  Switch switchReconnect;
  Switch switchHeartBeat;
  EditText etReconnect;
  EditText etHeartBeat;
  View btnSave;

  OnSettingIpListener onSetIpListener;

  public void setOnSetIpListener(
      OnSettingIpListener onSetIpListener) {
    this.onSetIpListener = onSetIpListener;
  }

  private void init(String ip, int port, List<String> hisIp, List<Integer> hisPort) {
    autoIp = findViewById(R.id.auto_ip);
    setAdapt(autoIp, hisIp);
    autoIp.setText(ip);

    autoPort = findViewById(R.id.auto_port);
    setAdapt(autoPort, hisPort);
    autoPort.setText(String.valueOf(port));

    switchReconnect = findViewById(R.id.switch_reconnect);
    switchReconnect.setChecked(options.reconnectEnable);
    etReconnect = findViewById(R.id.et_reconnect);
    etReconnect.setText(String.valueOf(options.reconnectInterval));
    etReconnect.setEnabled(options.reconnectEnable);

    switchHeartBeat = findViewById(R.id.switch_beat);
    switchHeartBeat.setChecked(options.heartBeatEnable);
    etHeartBeat = findViewById(R.id.et_beat);
    etHeartBeat.setText(String.valueOf(options.heartBeatInterval));
    etHeartBeat.setEnabled(options.heartBeatEnable);

    btnSave = findViewById(R.id.btn_ip_save);
    btnSave.setOnClickListener(this);
    btnSave.setEnabled(false);

    findViewById(R.id.btn_ip_cancel).setOnClickListener(this);

    autoIp.setOnClickListener(this);
    autoPort.setOnClickListener(this);

    initEvent();

    //if (!TextUtils.isEmpty(ip)) {
    //  autoIp.setText(ip);
    //  autoIp.setSelection(ip.length());
    //}
    //if (0 < port) {
    //  autoPort.setText(String.valueOf(port));
    //}
  }

  private void initEvent() {
    autoIp.addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override public void afterTextChanged(Editable s) {
        ip = s.toString();
        if (RegularUtil.matchIP(ip)) {
          btnSave.setEnabled(isChange());
        } else {
          btnSave.setEnabled(false);
        }
        LogUtils.d("ip: %s", s);
      }
    });
    autoPort.addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

      @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

      @Override public void afterTextChanged(Editable s) {
        String s1 = s.toString();
        if (RegularUtil.matchDecimalString(s1)) {
          port = Integer.parseInt(s.toString());
          btnSave.setEnabled(isChange());
        } else {
          btnSave.setEnabled(false);
        }
        LogUtils.d("port: %s", s);
      }
    });
    etReconnect.addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

      @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

      @Override public void afterTextChanged(Editable s) {
        String s1 = s.toString();
        if (RegularUtil.matchDecimalString(s1)) {
          reconnectInterval = Integer.parseInt(s.toString());
          btnSave.setEnabled(isChange());
        } else {
          btnSave.setEnabled(false);
        }
        LogUtils.d("reconnect gap: %s", s);
      }
    });
    etHeartBeat.addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

      @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

      @Override public void afterTextChanged(Editable s) {
        String s1 = s.toString();
        if (RegularUtil.matchDecimalString(s1)) {
          heartBeatInterval = Integer.parseInt(s.toString());
          btnSave.setEnabled(isChange());
        } else {
          btnSave.setEnabled(false);
        }
        LogUtils.d("heartBeat gap: %s", s);
      }
    });
    switchReconnect.setOnCheckedChangeListener((buttonView, isChecked) -> {
      LogUtils.d("reconnectEnable: %s", isChecked);
      reconnectEnable = isChecked;
      etReconnect.setEnabled(isChecked);
      btnSave.setEnabled(isChange());
    });
    switchHeartBeat.setOnCheckedChangeListener((buttonView, isChecked) -> {
      LogUtils.d("heartBeatEnable: %s", isChecked);
      heartBeatEnable = isChecked;
      etHeartBeat.setEnabled(isChecked);
      btnSave.setEnabled(isChange());
    });
  }

  private boolean isChange() {
    boolean reVal = (!this.ip.equals(options.serverIp)) ||
        (this.port != options.serverPort) ||
        (this.reconnectEnable != options.reconnectEnable) ||
        (this.reconnectInterval != options.reconnectInterval) ||
        (this.heartBeatEnable != options.heartBeatEnable) ||
        (this.heartBeatInterval != options.heartBeatInterval);
    return reVal;
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
        //ip = autoIp.getText().toString();
        //String s = autoPort.getText().toString();
        //port = Integer.parseInt(s);
        boolean b = onSetIpListener.onSettingSave(ip, port, reconnectEnable,
            reconnectInterval, heartBeatEnable, heartBeatInterval);
        if (b) {
          smartDismiss();
        }
      } else {
        smartDismiss();
      }
    }
  }

  private BasePopupView popupView;

  public void dismissIpPortSetting() {
    if (popupView != null) {
      popupView.dismiss();
    }
  }

  @Override public void destroy() {
    if (popupView != null) {
      popupView.dismiss();
      popupView.destroy();
    }
    super.destroy();
  }

  public void showIpPortSetting() {
    if (popupView == null) {
      popupView = new XPopup.Builder(getContext())
          .hasStatusBarShadow(true)
          .autoOpenSoftInput(false)
          .asCustom(this);
    }
    popupView.show();
  }

  public static void showIpPortSetting(Context context, Options options, List<String> hisIp,
      List<Integer> hisPort, OnSettingIpListener onSetIpListener) {
    FullScreenPopupSetIp2 fullScreenPopupSetIp =
        new FullScreenPopupSetIp2(context, options, hisIp, hisPort, onSetIpListener);

    new XPopup.Builder(context)
        .hasStatusBarShadow(true)
        .autoOpenSoftInput(false)
        .asCustom(fullScreenPopupSetIp)
        .show();
  }

  public static void showIpPortSetting(Context context, Options options,
      OnSettingIpListener onSetIpListener) {
    showIpPortSetting(context, options, null, null, onSetIpListener);
  }

  /**
   * 返回true 对话框消失
   */
  public interface OnSettingIpListener {
    boolean onSettingSave(String ip, int port, boolean reconnect,
        int reconnectGap, boolean heartBeat, int beatGap);
  }
}