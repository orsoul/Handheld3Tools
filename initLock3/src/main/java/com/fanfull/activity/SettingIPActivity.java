package com.fanfull.activity;

import android.widget.Button;
import android.widget.TextView;
import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.fanfull.base.BaseActivity;
import com.fanfull.contexts.MyContexts;
import com.fanfull.contexts.StaticString;
import com.fanfull.initbag3.R;
import com.fanfull.utils.SPUtils;
import com.orsoul.view.IPEditText;
import org.orsoul.baselib.util.ViewUtil;

public class SettingIPActivity extends BaseActivity {

  private IPEditText mVSetIP;
  private Button mBtnChangeIP;
  private Button mBtnQuit;
  private TextView mTvShow;

  @Override
  protected void initView() {
    setContentView(R.layout.activity_setting_ip);
    mVSetIP = (IPEditText) findViewById(R.id.et_setting_ip_set_ip);
    mBtnChangeIP = (Button) findViewById(R.id.btn_setting_ip_change);
    mBtnQuit = (Button) findViewById(R.id.btn_setting_ip_quit);
    mTvShow = (TextView) findViewById(R.id.tv_setting_ip_show_ip);
    ViewUtil.requestFocus(mBtnChangeIP);
  }

  @Override
  protected void initData() {
    String ip = SPUtils.getString(MyContexts.Key_IP1, StaticString.IP);
    mVSetIP.setIp(ip);
    mTvShow.setText(ip);
  }

  @Override
  protected void initEvent() {
    mBtnChangeIP.setOnClickListener(v -> {
      String ip1 = mVSetIP.getText();
      if (!RegexUtils.isIP(ip1)) {
        ToastUtils.showShort("ip地址不合法");
        return;
      }
      SPUtils.putString(MyContexts.Key_IP1, ip1);
      ToastUtils.showShort("修改成功");
      finish();
    });

    mBtnQuit.setOnClickListener(v -> finish());
  }

  @Override public void onBackPressed() {
    finish();
  }
}
