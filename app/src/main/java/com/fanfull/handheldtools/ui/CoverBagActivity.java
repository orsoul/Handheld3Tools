package com.fanfull.handheldtools.ui;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.apkfuns.logutils.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.fanfull.handheldtools.R;
import com.fanfull.handheldtools.ui.base.InitModuleActivity;
import com.fanfull.libhard.lock3.task.CoverBagTask;
import com.fanfull.libjava.util.BytesUtil;
import com.fanfull.libjava.util.ClockUtil;

import org.orsoul.baselib.lock3.LockCoder;
import org.orsoul.baselib.lock3.bean.HandoverBean;
import org.orsoul.baselib.lock3.bean.Lock3Bean;
import org.orsoul.baselib.lock3.bean.Lock3InfoUnit;
import org.orsoul.baselib.util.ViewUtil;

import java.util.Arrays;

public class CoverBagActivity extends InitModuleActivity {

  private TextView tvShow;
  private Button btnOk;

  private MyCoverBagTask coverBagTask;
  private HandoverBean handoverBean;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initModule(true, true);

    coverBagTask = new MyCoverBagTask();
    Lock3Bean lock3Bean = new Lock3Bean();
    lock3Bean.addSa(
        Lock3Bean.SA_BAG_ID,
        Lock3Bean.SA_PIECE_TID,
        Lock3Bean.SA_LOCK_TID,
        Lock3Bean.SA_KEY_NUM,
        Lock3Bean.SA_STATUS,
        Lock3Bean.SA_STATUS_CHECK,
        Lock3Bean.SA_ENABLE,
        //Lock3Bean.SA_WORK_MODE,
        Lock3Bean.SA_VOLTAGE,
        Lock3Bean.SA_COVER_EVENT
    );
    coverBagTask.setLock3Bean(lock3Bean);
    coverBagTask.setTaskType(CoverBagTask.TASK_TYPE_COVER);

    handoverBean = new HandoverBean()
        .setOrgancode("002706001")
        //.setOrgName("研发部")
        .setTimeSecond(System.currentTimeMillis())
        .setFunction(HandoverBean.FUN_COVER_BAG)
        .setScaner1("研发-1")
        .setScaner2("研发-2");
  }

  @Override protected void initView() {
    setContentView(R.layout.activity_cover_bag);
    tvShow = findViewById(R.id.tv_cover_bag_show);
    tvShow.setMovementMethod(ScrollingMovementMethod.getInstance());
    tvShow.setOnClickListener(this);

    btnOk = findViewById(R.id.btn_cover_bag_cover);
    btnOk.setOnClickListener(this);

    findViewById(R.id.btn_cover_bag_open).setOnClickListener(this);
  }

  @Override public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_cover_bag_cover:
        coverBagTask.setTaskType(CoverBagTask.TASK_TYPE_COVER);
        handoverBean.setFunction(HandoverBean.FUN_COVER_BAG);
        if (coverBagTask.startThread()) {
          ClockUtil.runTime(true);
          showLoadingView("正在执行任务...");
        } else {
          ToastUtils.showShort("执行线程已在运行");
        }
        break;
      case R.id.btn_cover_bag_open:
        coverBagTask.setTaskType(CoverBagTask.TASK_TYPE_OPEN);
        handoverBean.setFunction(HandoverBean.FUN_OPEN_BAG);
        if (coverBagTask.startThread()) {
          ClockUtil.runTime(true);
          showLoadingView("正在执行任务...");
        } else {
          ToastUtils.showShort("执行线程已在运行");
        }
        //showSetUhfPower(this);
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

  class MyCoverBagTask extends CoverBagTask {

    @Override public boolean onCheckSuccess(Lock3Bean lock3Bean) {
      Lock3InfoUnit unitEvent = Lock3InfoUnit.newInstance(Lock3Bean.SA_COVER_EVENT);
      byte[] bagIdBuff = lock3Bean.getInfoUnit(Lock3Bean.SA_BAG_ID).buff;
      byte[] tid2 = Arrays.copyOfRange(lock3Bean.pieceTidBuff, 6, 12);
      unitEvent.buff = BytesUtil.concatArray(bagIdBuff, tid2, bagIdBuff);
      LockCoder.myEncrypt(unitEvent.buff, tid2, true);

      handoverBean.setTimeSecond(System.currentTimeMillis());
      Lock3Bean lock3BeanWrite = genLock3Bean2Write(lock3Bean, handoverBean, unitEvent);
      writeLock(lock3BeanWrite);
      return true;
    }

    @Override public boolean onWriteSuccess(Lock3Bean lock3Bean) {
      long l = ClockUtil.runTime();
      String format = String.format("执行成功，耗时：%.3f 秒", l / 1000.0);
      LogUtils.d(format);
      ToastUtils.showShort(format);
      runOnUiThread(new Runnable() {
        @Override public void run() {
          ViewUtil.appendShow(format, tvShow);
        }
      });
      return false;
    }

    @Override protected void onFailed(int errorCode) {
      super.onFailed(errorCode);
    }

    StringBuilder sb = new StringBuilder();

    @Override public boolean onCheckFailed(int res, Lock3Bean lock3Bean) {
      String msg;
      switch (res) {
        case CHECK_RES_FORMAT_WRONG:
          msg = "袋id格式错误";
          break;
        case CHECK_RES_BAG_ID_NOT_CONTAIN_UID:
          msg = "袋id与袋锁不匹配";
          break;
        case CHECK_RES_EPC_NOT_EQUALS:
          msg = "袋id与epc不一致，请检查锁片是否被更换";
          break;
        case CHECK_RES_EPC_EQUALS:
          msg = "袋id与epc一样，请检查锁片是否已插好";
          break;
        case CHECK_RES_STATUS_NOT_EQUALS_1:
          msg = "标志位不是F1(空袋)，flag=" + lock3Bean.getStatus();
          break;
        case CHECK_RES_STATUS_NOT_EQUALS_2:
          msg = "标志位不是F2(上锁)，flag=" + lock3Bean.getStatus();
          break;
        case CHECK_RES_STATUS_NOT_EQUALS_3:
          msg = "标志位不是F3(封袋)，flag=" + lock3Bean.getStatus();
          break;
        case CHECK_RES_STATUS_NOT_CHECK_EMPTY:
          msg = "未进行空袋登记，flag=" + lock3Bean.getStatusCheck();
          break;
        case CHECK_RES_V_LOW:
          msg = "电压不在允许范围，V=" + lock3Bean.getVoltage();
          break;
        case CHECK_RES_ENABLE_CODE_WRONG:
          msg = "启用码错误，enableCode=" + lock3Bean.getEnable();
          break;
        case CHECK_RES_TID_NOT_EQUALS:
          msg = "袋锁与锁片tid不匹配";
          break;
        case CHECK_RES_EVENT_CODE_WRONG:
          msg = "封签事件码解密失败";
          break;
        default:
          msg = "未定义错误";
      }
      LogUtils.d("%s", msg);
      sb.append(msg).append('\n');
      //runOnUiThread(new Runnable() {
      //  @Override public void run() {
      //    ViewUtil.appendShow(msg, tvShow);
      //  }
      //});
      return false;
    }

    @Override public boolean onWriteFailed(int res, Lock3Bean lock3Bean) {
      String msg;
      switch (res) {
        case WRITE_RES_ARGS_WRONG:
          msg = "袋id格式错误";
          break;
        case WRITE_RES_WRITE_EPC_FAILED:
          msg = "更新epc失败";
          break;
        case WRITE_RES_WRITE_NFC_FAILED:
          msg = "更新nfc失败";
          break;
        case WRITE_RES_WRITE_HANDOVER_FAILED:
          msg = "写入袋流转信息失败";
          break;
        default:
          msg = "未定义错误";
      }
      LogUtils.d("onUpdateFailed：%s，%s", res, msg);
      sb.append(msg).append('\n');
      return false;
    }

    @Override public boolean onWaitWriteTimeout(Lock3Bean lock3Bean) {
      return false;
    }

    @Override protected void onTaskBefore() {
      ClockUtil.runTime(true);
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
        ViewUtil.appendShow(sb.toString(), tvShow);
        sb.setLength(0);
      });
    }
  }
}