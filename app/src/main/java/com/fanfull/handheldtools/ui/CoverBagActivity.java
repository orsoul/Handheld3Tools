package com.fanfull.handheldtools.ui;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.fanfull.handheldtools.R;
import com.fanfull.handheldtools.ui.base.InitModuleActivity;
import com.fanfull.libhard.lock3.task.CoverOpenTask;

import org.orsoul.baselib.lock3.bean.Lock3Bean;
import org.orsoul.baselib.util.ClockUtil;

public class CoverBagActivity extends InitModuleActivity {

  private TextView tvShow;
  private Button btnOk;

  private MyCoverBagTask coverBagTask;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initModule(true, true);

    coverBagTask = new MyCoverBagTask();
    Lock3Bean lock3Bean = new Lock3Bean();
    lock3Bean.removeSa();
    lock3Bean.addSa(
        Lock3Bean.SA_BAG_ID,
        Lock3Bean.SA_PIECE_TID,
        Lock3Bean.SA_LOCK_TID,
        Lock3Bean.SA_STATUS,
        Lock3Bean.SA_ENABLE,
        Lock3Bean.SA_KEY_NUM,
        //Lock3Bean.SA_WORK_MODE,
        Lock3Bean.SA_VOLTAGE
    );
    coverBagTask.setLock3Bean(lock3Bean);
    coverBagTask.setTaskType(CoverOpenTask.TASK_TYPE_COVER);
  }

  @Override protected void initView() {
    setContentView(R.layout.activity_cover_bag);
    tvShow = findViewById(R.id.tv_cover_bag_show);
    tvShow.setMovementMethod(ScrollingMovementMethod.getInstance());
    tvShow.setOnClickListener(this);

    btnOk = findViewById(R.id.btn_cover_bag_cover);
    btnOk.setOnClickListener(this);
  }

  @Override public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_cover_bag_cover:
        if (coverBagTask.startThread()) {
          ClockUtil.runTime(true);
          showLoadingView("正在读取袋锁信息...");
        } else {
          ToastUtils.showShort("读袋锁信息线程已在运行");
        }
        break;
      case R.id.btn_check_bag_stopScan:
        showSetUhfPower(this);
        break;
      case R.id.tv_cover_bag_show:
        if (3 == ClockUtil.fastClickTimes()) {
          tvShow.setText("");
        }
        break;
      default:
    }
  }

  @Override protected void onDestroy() {
    if (coverBagTask != null) {
      coverBagTask.stopThread();
    }
    super.onDestroy();
  }

  class MyCoverBagTask extends CoverOpenTask {

    @Override public boolean onCheckSuccess(Lock3Bean lock3Bean) {
      return false;
    }

    @Override protected void onFailed(int errorCode) {
      super.onFailed(errorCode);
    }

    @Override protected void onTaskBefore() {
      runOnUiThread(() -> {
        //btnOk.setText(R.string.v_stop);
      });
    }

    @Override protected void onStop() {
      runOnUiThread(() -> {
      });
    }

    @Override protected void onTaskFinish() {
      runOnUiThread(() -> {
        dismissLoadingView();
      });
    }
  }
}