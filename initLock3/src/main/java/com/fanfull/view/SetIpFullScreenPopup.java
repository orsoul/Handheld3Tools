package com.fanfull.view;

import android.content.Context;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.fanfull.contexts.MyContexts;
import com.fanfull.contexts.StaticString;
import com.fanfull.initbag3.R;
import com.fanfull.utils.SPUtils;
import com.lxj.xpopup.impl.FullScreenPopupView;
import com.orsoul.view.IPEditText;
import org.orsoul.baselib.util.ViewUtil;

public class SetIpFullScreenPopup extends FullScreenPopupView {
  public SetIpFullScreenPopup(@NonNull Context context) {
    super(context);
  }

  public SetIpFullScreenPopup(@NonNull Context context, SetIpCallback callback) {
    super(context);
    this.callback = callback;
  }

  private SetIpCallback callback;

  @Override protected int getImplLayoutId() {
    return R.layout.activity_setting_ip;
  }

  private IPEditText mVSetIP;
  private Button mBtnChangeIP;
  private Button mBtnQuit;
  private TextView mTvShow;

  @Override protected void onCreate() {
    super.onCreate();
    mTvShow = (TextView) findViewById(R.id.tv_setting_ip_show_ip);
    mVSetIP = (IPEditText) findViewById(R.id.et_setting_ip_set_ip);
    mBtnChangeIP = (Button) findViewById(R.id.btn_setting_ip_change);
    mBtnQuit = (Button) findViewById(R.id.btn_setting_ip_quit);

    String ip = SPUtils.getString(MyContexts.Key_IP1, StaticString.IP);
    mVSetIP.setIp(ip);
    mTvShow.setText(ip);

    mBtnChangeIP.setOnClickListener(v -> {
      String ip1 = mVSetIP.getText();
      if (!RegexUtils.isIP(ip1)) {
        ToastUtils.showShort("ip地址不合法");
        return;
      }
      SPUtils.putString(MyContexts.Key_IP1, ip1);
      if (callback != null) {
        callback.onIpSet(ip1);
      }
      dismiss();
    });

    mBtnQuit.setOnClickListener(v -> dismiss());

    ViewUtil.requestFocus(mBtnQuit);
  }

  public interface SetIpCallback {
    void onIpSet(String ip);
  }
}