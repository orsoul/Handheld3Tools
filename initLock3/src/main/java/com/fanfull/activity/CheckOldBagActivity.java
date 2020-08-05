package com.fanfull.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.apkfuns.logutils.LogUtils;
import com.fanfull.base.BaseActivity;
import com.fanfull.initbag3.R;
import com.fanfull.libhard.rfid.RfidController;
import java.util.Arrays;
import org.orsoul.baselib.util.ArrayUtils;
import org.orsoul.baselib.util.SoundUtils;
import org.orsoul.baselib.util.ViewUtil;

public class CheckOldBagActivity extends BaseActivity {

  private TextView tvShow;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override protected void initView() {
    setContentView(R.layout.activity_check_old_bag);
    findViewById(R.id.btn_old_bag_write_lock).setOnClickListener(this);
    findViewById(R.id.btn_old_bag_write_unlock).setOnClickListener(this);

    tvShow = findViewById(R.id.tv_old_bag_show);

    View viewById = findViewById(R.id.btn_old_bag_check);
    viewById.setOnClickListener(this);
    ViewUtil.requestFocus(viewById);
  }

  @Override public void onClick(View v) {
    boolean isLock = false;
    switch (v.getId()) {
      case R.id.btn_old_bag_check:
        String info;
        byte[] block8 = RfidController.getInstance().readM1(9);
        if (block8 != null) {
          LogUtils.d("block9:%s", ArrayUtils.bytes2HexString(block8));
          SoundUtils.playInitSuccessSound();
          if (block8[0] == 0x39) {
            info = "已上锁";
          } else {
            info = "已开锁";
          }
        } else {
          SoundUtils.playFailedSound();
          info = "获取失败";
        }
        tvShow.setText(info);
        break;
      case R.id.btn_old_bag_write_lock:
        isLock = true;
      case R.id.btn_old_bag_write_unlock:
        byte[] bytes = new byte[16];
        if (isLock) {
          Arrays.fill(bytes, (byte) 0x39);
        }
        boolean b = RfidController.getInstance().writeM1(9, bytes);
        if (b) {
          SoundUtils.playInitSuccessSound();
          if (isLock) {
            info = "已上锁";
          } else {
            info = "已开锁";
          }
        } else {
          SoundUtils.playFailedSound();
          info = "更新失败";
        }
        tvShow.setText(info);
        break;
      default:
    }
  }
}
