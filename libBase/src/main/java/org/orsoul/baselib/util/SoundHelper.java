package org.orsoul.baselib.util;

import android.content.Context;

import org.orsoul.baselib.R;

/** 常用音效、数字音效工具类. */
public class SoundHelper extends SoundPoolUtil {

  private static class SingletonHolder {
    private static SoundHelper instance = new SoundHelper();
  }

  public static SoundHelper getInstance() {
    return SoundHelper.SingletonHolder.instance;
  }

  private static final char[] MONEY_STR =
      { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '9' + 1 };
  private static final char[] MONEY_UNIT_BASE = { '9' + 1, '9' + 2, '9' + 3 };

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
    numSoundIdArr = new int[12];
    numSoundIdArr[0] = mSoundPool.load(context, R.raw.c0, DEFAULT_PRIORITY);
    numSoundIdArr[1] = mSoundPool.load(context, R.raw.c1, DEFAULT_PRIORITY);
    numSoundIdArr[2] = mSoundPool.load(context, R.raw.c2, DEFAULT_PRIORITY);
    numSoundIdArr[3] = mSoundPool.load(context, R.raw.c3, DEFAULT_PRIORITY);
    numSoundIdArr[4] = mSoundPool.load(context, R.raw.c4, DEFAULT_PRIORITY);
    numSoundIdArr[5] = mSoundPool.load(context, R.raw.c5, DEFAULT_PRIORITY);
    numSoundIdArr[6] = mSoundPool.load(context, R.raw.c6, DEFAULT_PRIORITY);
    numSoundIdArr[7] = mSoundPool.load(context, R.raw.c7, DEFAULT_PRIORITY);
    numSoundIdArr[8] = mSoundPool.load(context, R.raw.c8, DEFAULT_PRIORITY);
    numSoundIdArr[9] = mSoundPool.load(context, R.raw.c9, DEFAULT_PRIORITY);
    numSoundIdArr[10] = mSoundPool.load(context, R.raw.cshi, DEFAULT_PRIORITY);
    numSoundIdArr[11] = mSoundPool.load(context, R.raw.cbai, DEFAULT_PRIORITY);
  }

  public static void loadSounds(Context context) {
    SingletonHolder.instance.loadTone(context);
    SingletonHolder.instance.loadNum(context);
  }

  public static int playReply(int id, float percentVolume) {
    return SingletonHolder.instance.play(id, getInstance().getVolume(percentVolume), 0, 1);
  }

  /** 播放成功声. */
  public static int playToneSuccess() {
    return SingletonHolder.instance.play(TONE_SUCCESS);
  }

  /** 播放 正常按键声音 */
  public static int playToneDrop() {
    return SingletonHolder.instance.play(TONE_DROP);
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

  public static void playNum(int num, float rate) {
    String s = MoneyConvert.convertThousand(num, MONEY_STR, MONEY_UNIT_BASE);
    char[] chars = s.toCharArray();
    int[] ids = new int[chars.length];
    for (int i = 0; i < ids.length; i++) {
      ids[i] = numSoundIdArr[chars[i] - '0'];
    }
    SingletonHolder.instance.playArr(rate, 400, ids);
  }

  public static void playNum(int num) {
    playNum(num, 1.0F);
  }
}
