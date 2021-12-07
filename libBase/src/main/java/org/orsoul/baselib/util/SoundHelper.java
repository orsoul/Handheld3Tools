package org.orsoul.baselib.util;

import android.app.Activity;
import android.content.Context;

import com.fanfull.libjava.util.MoneyConvert;
import com.fanfull.libjava.util.ThreadUtil;

import org.orsoul.baselib.R;

import java.util.Timer;
import java.util.TimerTask;

/** 常用音效、数字音效工具类. */
public class SoundHelper extends SoundPoolUtil {

  private static class SingletonHolder {
    private static SoundHelper instance = new SoundHelper();
  }

  public static SoundHelper getInstance() {
    return SoundHelper.SingletonHolder.instance;
  }

  private static final int MONEY_PLAY_GAP = 350;
  private static final char[] MONEY_NUM =
      {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '9' + 1};
  private static final char[] MONEY_UNIT_BASE = {'9' + 1, '9' + 2, '9' + 3};

  public static int[] numSoundIdArr;
  public static int TONE_DROP;
  public static int TONE_FAILED;
  public static int TONE_SUCCESS;
  public static int TONE_BIU;
  public static int TONE_DIDA;

  public void loadTone(Context context) {
    TONE_DROP = mSoundPool.load(context, R.raw.tone_drop, DEFAULT_PRIORITY);
    TONE_FAILED = mSoundPool.load(context, R.raw.tone_failed, DEFAULT_PRIORITY);
    TONE_SUCCESS = mSoundPool.load(context, R.raw.tone_success, DEFAULT_PRIORITY);
    TONE_BIU = mSoundPool.load(context, R.raw.tone_biu, DEFAULT_PRIORITY);
    TONE_DIDA = mSoundPool.load(context, R.raw.dida1018, DEFAULT_PRIORITY);
  }

  public void loadNum(Context context) {
    int[] resArr = new int[]{R.raw.c0, R.raw.c1, R.raw.c2, R.raw.c3, R.raw.c4, R.raw.c5,
        R.raw.c6, R.raw.c7, R.raw.c8, R.raw.c9, R.raw.cshi, R.raw.cbai,};
    numSoundIdArr = new int[resArr.length];
    for (int i = 0; i < numSoundIdArr.length; i++) {
      numSoundIdArr[i] = mSoundPool.load(context, resArr[i], DEFAULT_PRIORITY);
    }
  }

  public static void loadSounds(Context context) {
    SingletonHolder.instance.loadTone(context);
    SingletonHolder.instance.loadNum(context);
  }

  private static int toneDadaId;

  public static int playToneDadaLoop(float volume, float rate) {
    if (toneDadaId != -1) {
      stopToneDataLoop();
    }
    return toneDadaId = SingletonHolder.instance.play(TONE_DIDA, volume, 0, rate);
  }

  public static void stopToneDataLoop() {
    SingletonHolder.instance.stop(toneDadaId);
    toneDadaId = -1;
  }

  public static int playRepeat(int id, float percentVolume) {
    return SingletonHolder.instance.play(id, getInstance().getVolume(percentVolume), 0, 1);
  }

  public static class PlaySoundTask extends ThreadUtil.TimeThreadRunnable {
    private int id;
    private int onceId;
    private int oncePeriod = 200;
    private float volume = 1F;
    private float rate = 1F;
    private long period = 500;

    public int getId() {
      return id;
    }

    public void setId(int id, int onceId) {
      this.onceId = onceId;
      this.id = id;
    }

    public void setId(int id) {
      this.id = id;
    }

    public float getVolume() {
      return volume;
    }

    public void setVolume(float volume) {
      this.volume = volume;
    }

    public float getRate() {
      return rate;
    }

    public void setRate(float rate) {
      this.rate = rate;
    }

    public long getPeriod() {
      return period;
    }

    public void setPeriod(long period) {
      this.period = period;
    }

    public void set(int id, float volume, float rate, long period, int times, long runTime) {
      setId(id);
      setVolume(volume);
      setRate(rate);
      setPeriod(period);

      setTotal(times);
      setRunTime(runTime);
    }

    public boolean play(int id, float volume, float rate, long period, int times, long runTime) {
      set(id, volume, rate, period, times, runTime);
      return startThread();
    }

    public boolean play(int id) {
      if (id <= 0) {
        return false;
      }
      setId(id);
      return startThread();
    }

    @Override protected boolean handleOnce() {
      int id = getId();
      if (id <= 0) {
        return true;
      }

      if (0 < pauseTime) {
        ThreadUtil.sleep(pauseTime);
        pauseTime = 0;
      }

      if (0 < onceId) {
        SoundHelper.getInstance().play(onceId, volume, 1, rate);
        onceId = -1;
        ThreadUtil.sleep(oncePeriod);
      } else {
        SoundHelper.getInstance().play(id, volume, 1, rate);
      }
      int total = getTotal();
      if (runCount < total || total <= 0) {
        ThreadUtil.sleep(period);
      }
      return false;
    }

    long pauseTime = 0;

    public synchronized void pause(long pauseTime) {
      this.pauseTime = pauseTime;
    }

    public synchronized void pauseEnd() {
      pauseTime = 0;
      interrupt();
    }
  }

  /** 以固定的速率 播放音效. */
  private class PlayLoopTimer extends Timer {
    private int id;
    private float volume;
    private float rate;
    private long period;
    private int times;

    public PlayLoopTimer(int id, float volume, float rate, long period, int times) {
      this.id = id;
      this.volume = volume;
      this.rate = rate;
      this.period = period;
      this.times = times;
    }

    private boolean isPlaying;
    private int playCount;

    public void play() {
      scheduleAtFixedRate(new TimerTask() {
        @Override public void run() {
          SoundHelper.getInstance().play(id, volume, 1, rate);
          playCount++;
          if (times != 0 && times <= playCount) {
            stop();
          }
        }
      }, 0, period);
      isPlaying = true;
    }

    public void stop() {
      cancel();
      isPlaying = false;
    }
  }

  /** 在主线程播放成功声. */
  public static void playToneSuccess(Activity activity) {
    if (activity != null) {
      activity.runOnUiThread(() -> playToneSuccess());
    } else {
      playToneSuccess();
    }
  }

  /** 播放成功声. */
  public static int playToneSuccess() {
    return SingletonHolder.instance.play(TONE_SUCCESS);
  }

  /** 播放 正常按键声音 */
  public static int playToneDrop() {
    return SingletonHolder.instance.play(TONE_DROP);
  }

  /** 在主线程播放 错误声音. */
  public static void playToneFailed(Activity activity) {
    if (activity != null) {
      activity.runOnUiThread(() -> playToneFailed());
    } else {
      playToneFailed();
    }
  }

  /** 播放 错误声音. */
  public static int playToneFailed() {
    return SingletonHolder.instance.play(TONE_FAILED);
  }

  /** 播放扫描音效. */
  public static int playToneBiu() {
    return SingletonHolder.instance.play(TONE_BIU);
  }

  /** 播放 滴答声. */
  public static int playToneDiDa() {
    return SingletonHolder.instance.play(TONE_DIDA);
  }

  public static void playNum(int num, float rate, int interval) {
    if (num < 0) {
      // TODO: 2021/7/26 暂无‘千'的音效
      return;
    }
    String s;
    if (num < 100) {
      // 小于100 完整 报金额
      s = MoneyConvert.convertThousand(num, MONEY_NUM, MONEY_UNIT_BASE);
    } else if (num < 1000 && num % 100 == 0) {
      // 整百 完整 报金额
      s = MoneyConvert.convertThousand(num, MONEY_NUM, MONEY_UNIT_BASE);
    } else {
      // 大于等于 1000 只报 数字
      s = MoneyConvert.convertMoney2Num(num);
    }
    char[] chars = s.toCharArray();
    int[] ids = new int[chars.length];
    for (int i = 0; i < ids.length; i++) {
      ids[i] = numSoundIdArr[chars[i] - '0'];
    }
    SingletonHolder.instance.playArr(rate, interval, ids);
  }

  public static void playNum(int num, float rate) {
    playNum(num, rate, MONEY_PLAY_GAP);
  }

  public static void playNum(int num) {
    playNum(num, 1.0F);
  }

  public static void playNum(String num) {
    int n = Integer.parseInt(num);
    playNum(n, 1.0F);
  }

  public static int playSound(int id) {
    return SingletonHolder.instance.play(id);
  }
}
