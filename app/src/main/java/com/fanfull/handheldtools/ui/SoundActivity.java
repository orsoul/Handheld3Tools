package com.fanfull.handheldtools.ui;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

import com.apkfuns.logutils.LogUtils;
import com.fanfull.handheldtools.R;
import com.fanfull.handheldtools.ui.base.BaseActivity;
import com.fanfull.libjava.util.ThreadUtil;

import org.orsoul.baselib.util.SoundHelper;
import org.orsoul.baselib.util.SoundPoolUtil;
import org.orsoul.baselib.util.ViewUtil;

public class SoundActivity extends BaseActivity {

  private SeekBar sbSound;

  private EditText etTimes;
  private EditText etPeriod;
  private Button btnPlay;

  private EditText etNum;
  private EditText etRate;
  private Button btnPlayMoney;

  private Button btnPlayNums;
  private Button btnPlaySuccess;
  private Button btnPlayFailed;
  private Button btnPlayDrop;

  private int soundIdPlaying;
  private long period;

  private MySoundPlayTask soundPlayTask;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initVolumeBar();

    soundPlayTask = new MySoundPlayTask();
    soundPlayTask.set(
        SoundHelper.TONE_BIU, 0.6F, 1, period, 0, 20 * 1000);
  }

  @Override protected void initView() {
    setContentView(R.layout.activity_sound);

    sbSound = findViewById(R.id.sb_sound);

    etNum = findViewById(R.id.et_sound_num);
    etRate = findViewById(R.id.et_sound_rate);
    btnPlayMoney = findViewById(R.id.btn_sound_play_money);
    btnPlayMoney.setOnClickListener(this);

    etTimes = findViewById(R.id.et_sound_times);
    etPeriod = findViewById(R.id.et_sound_period);
    btnPlay = findViewById(R.id.btn_sound_play);
    btnPlay.setOnClickListener(this);

    btnPlayNums = findViewById(R.id.btn_sound_play_num);
    btnPlaySuccess = findViewById(R.id.btn_sound_play_success);
    btnPlayFailed = findViewById(R.id.btn_sound_play_failed);
    btnPlayDrop = findViewById(R.id.btn_sound_play_drop);
    btnPlayNums.setOnClickListener(this);
    btnPlaySuccess.setOnClickListener(this);
    btnPlayFailed.setOnClickListener(this);
    btnPlayDrop.setOnClickListener(this);

    ViewUtil.requestFocus(btnPlay);
  }

  private void initVolumeBar() {
    int max = SoundHelper.getAudioVolume(true);
    int current = SoundHelper.getAudioVolume(false);
    sbSound.setMax(max);
    sbSound.setProgress(current);
    sbSound.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        SoundHelper.setAudioVolume(progress, true, true);
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {

      }
    });
  }

  @Override public void onClick(View v) {
    v.setEnabled(false);
    switch (v.getId()) {
      case R.id.btn_sound_play_money:
        int num = Integer.parseInt(etNum.getText().toString());
        int rate = Integer.parseInt(etRate.getText().toString());
        //SoundHelper.playNum(num, rate);
        SoundHelper.getInstance().playNum(num, 1, rate);
        break;
      case R.id.btn_sound_play:
        clickPlay();
        break;
      case R.id.btn_sound_play_num:
        ThreadUtil.executeInSingleThread(() -> {
          for (int i = 0; i < 10; i++) {
            SoundHelper.playNum(i);
            if (i < 9) {
              SystemClock.sleep(400);
            }
          }
        });
        break;
      case R.id.btn_sound_play_success:
        SoundHelper.playToneSuccess();
        break;
      case R.id.btn_sound_play_failed:
        SoundHelper.playToneFailed();
        break;
      case R.id.btn_sound_play_drop:
        SoundHelper.playToneDrop();
        break;
    }
    v.setEnabled(true);
  }

  private void clickPlay() {
    if (soundPlayTask.isRunning()) {
      soundPlayTask.stopThread();
      return;
    }

    int times = 2;
    int period = 500;
    try {
      times = Integer.parseInt(etTimes.getText().toString());
      period = Integer.parseInt(etPeriod.getText().toString());
    } catch (Exception e) {
    }
    soundPlayTask.setTotal(times);
    soundPlayTask.setPeriod(period);
    soundPlayTask.startThread();
  }

  @Override protected void onEnterPress() {
    btnPlay.performClick();
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    SoundPoolUtil soundHelper = SoundHelper.getInstance();
    switch (keyCode) {
      case KeyEvent.KEYCODE_1:
        if (soundIdPlaying < 0) {
          soundIdPlaying = soundHelper.play(
              SoundHelper.TONE_DIDA, soundHelper.getVolume(0.6F), 0, 1f);
        } else {
          soundHelper.stop(soundIdPlaying);
          soundIdPlaying = -1;
        }
        LogUtils.d("%s:%s", SoundHelper.TONE_DIDA, soundIdPlaying);
        break;
      case KeyEvent.KEYCODE_3:
        break;
    }

    return super.onKeyDown(keyCode, event);
  }

  @Override protected void onDestroy() {
    if (0 <= soundIdPlaying) {
      SoundHelper.getInstance().stop(soundIdPlaying);
      soundIdPlaying = -1;
    }
    if (soundPlayTask != null) {
      soundPlayTask.stopThread();
    }
    super.onDestroy();
  }

  class MySoundPlayTask extends SoundHelper.PlaySoundTask {
    @Override protected void onTaskBefore() {
      runOnUiThread(() -> btnPlay.setText("停止"));
    }

    @Override protected void onTaskFinish() {
      runOnUiThread(() -> btnPlay.setText("播放"));
    }
  }
}