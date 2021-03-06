package org.orsoul.baselib.util;

import android.app.Application;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.SystemClock;

import com.blankj.utilcode.util.Utils;

import org.orsoul.baselib.R;

/**
 * 播放声音的工具类.
 */
public class SoundUtils {

  /** SoundPool对象中允许同时存在的最大流的数量 */
  private static final int MAX_STREAMS = 32;

  /** 声道音量 */
  private static final float DEFAULT_VOLUME = 1.0f;
  private static float volume = DEFAULT_VOLUME;
  private static final int PRIORITY = 1; // 指定播放声音的优先级，数值越高，优先级越大。
  /** 指定是否循环。-1表示无限循环，0播放1次,n 表示 循环 n次 */
  private static final int LOOP = 0;
  /** 播放速率0.5~2。1.0为原始频率,2.0 为两倍播放. */
  private static final float RATE = 1; // 指定

  private static boolean isSilence;

  /** 连续播放声音的 时间隔 */
  private static final int PLAY_INTERVAL = 400;

  private static final int SOUND_TEN = 10; // 声音 '十'
  private static final int SOUND_HUNDRED = 11; // 声音 '百'

  public static int DROP_SOUND; // 操作正确的声音 id
  public static int FAILED_SOUND; // // 操作错误的声音 id
  public static int SCAN_START_SOUND; // 开始扫捆 提示声
  public static int SCAN_FINISH_SOUND; // 扫捆 结束 提示声
  public static int WRITE_ING_DATA;// 正在写数据
  public static int TONE_SCAN_ONE;

  public static int REFRESH_SCREEN; // 刷新屏
  public static int SCAN_BAG; // 扫描袋
  public static int SCAN_PILE_BAG; // 扫描堆袋
  public static boolean FLAG = true;
  public static int INIT_SUCCESS;
  // 声音池
  private static final SoundPool mSoundPool =
      new SoundPool(MAX_STREAMS, AudioManager.USE_DEFAULT_STREAM_TYPE, 0);
  public static int[] mSoundIds = new int[12]; // 数字声音 的 播放id

  // 从资源文件 加载 声音资源
  // SoundPool 载入音乐文件使用了独立的线程，不会阻塞UI主线程的操作
  // 但如果音效文件过大没有载入完成，我们调用play方法时可能产生严重的后果
  public static void loadSounds(Context context) {
    // mSoundIds[0] = mSoundPool.load(context, R.raw.a0, PRIORITY);
    // mSoundIds[1] = mSoundPool.load(context, R.raw.a1, 5); //
    // 使用频率比较高的声音优先级设5
    // mSoundIds[2] = mSoundPool.load(context, R.raw.a2, PRIORITY);
    // mSoundIds[3] = mSoundPool.load(context, R.raw.a3, PRIORITY);
    // mSoundIds[4] = mSoundPool.load(context, R.raw.a4, PRIORITY);
    // mSoundIds[5] = mSoundPool.load(context, R.raw.a5, PRIORITY);
    // mSoundIds[6] = mSoundPool.load(context, R.raw.a6, PRIORITY);
    // mSoundIds[7] = mSoundPool.load(context, R.raw.a7, PRIORITY);
    // mSoundIds[8] = mSoundPool.load(context, R.raw.a8, PRIORITY);
    // mSoundIds[9] = mSoundPool.load(context, R.raw.a9, PRIORITY);
    // mSoundIds[10] = mSoundPool.load(context, R.raw.shi, 5);

    // mSoundIds[0] = mSoundPool.load(context, R.raw.b0, PRIORITY);
    // mSoundIds[1] = mSoundPool.load(context, R.raw.b1, 5); //
    // 使用频率比较高的声音优先级设5
    // mSoundIds[2] = mSoundPool.load(context, R.raw.b2, PRIORITY);
    // mSoundIds[3] = mSoundPool.load(context, R.raw.b3, PRIORITY);
    // mSoundIds[4] = mSoundPool.load(context, R.raw.b4, PRIORITY);
    // mSoundIds[5] = mSoundPool.load(context, R.raw.b5, PRIORITY);
    // mSoundIds[6] = mSoundPool.load(context, R.raw.b6, PRIORITY);
    // mSoundIds[7] = mSoundPool.load(context, R.raw.b7, PRIORITY);
    // mSoundIds[8] = mSoundPool.load(context, R.raw.b8, PRIORITY);
    // mSoundIds[9] = mSoundPool.load(context, R.raw.b9, PRIORITY);
    // mSoundIds[10] = mSoundPool.load(context, R.raw.b10, 5);
    // mSoundIds[11] = mSoundPool.load(context, R.raw.bai, PRIORITY);

    mSoundIds[0] = mSoundPool.load(context, R.raw.c0, PRIORITY);
    mSoundIds[1] = mSoundPool.load(context, R.raw.c1, PRIORITY); // 使用频率比较高的声音优先级设5
    mSoundIds[2] = mSoundPool.load(context, R.raw.c2, PRIORITY);
    mSoundIds[3] = mSoundPool.load(context, R.raw.c3, PRIORITY);
    mSoundIds[4] = mSoundPool.load(context, R.raw.c4, PRIORITY);
    mSoundIds[5] = mSoundPool.load(context, R.raw.c5, PRIORITY);
    mSoundIds[6] = mSoundPool.load(context, R.raw.c6, PRIORITY);
    mSoundIds[7] = mSoundPool.load(context, R.raw.c7, PRIORITY);
    mSoundIds[8] = mSoundPool.load(context, R.raw.c8, PRIORITY);
    mSoundIds[9] = mSoundPool.load(context, R.raw.c9, PRIORITY);
    mSoundIds[10] = mSoundPool.load(context, R.raw.cshi, PRIORITY);
    mSoundIds[11] = mSoundPool.load(context, R.raw.cbai, PRIORITY);

    DROP_SOUND = mSoundPool.load(context, R.raw.tone_drop, PRIORITY); // 操作正确的声音
    FAILED_SOUND = mSoundPool.load(context, R.raw.tone_failed, PRIORITY); // //操作错误的 声音
    INIT_SUCCESS = mSoundPool.load(context, R.raw.tone_success, PRIORITY);// 初始化成功
    TONE_SCAN_ONE = mSoundPool.load(context, R.raw.tone_biu, PRIORITY); // //操作错误的 声音
    WRITE_ING_DATA = mSoundPool.load(context, R.raw.dida1018, PRIORITY);

    SCAN_START_SOUND = mSoundPool.load(context, R.raw.scan_bunch_start, PRIORITY);
    SCAN_FINISH_SOUND = mSoundPool.load(context, R.raw.scan_bunch_finish, PRIORITY);
    REFRESH_SCREEN = mSoundPool.load(context, R.raw.please_refresh_screen, PRIORITY);
    SCAN_BAG = mSoundPool.load(context, R.raw.please_scan_bag, PRIORITY);
    SCAN_PILE_BAG = mSoundPool.load(context, R.raw.please_scan_pile_bag, PRIORITY);
  }

  /**
   * 设置 系统多媒体音量.同时显示音量控制UI和播放声音
   *
   * @param raise true增加音量
   */
  public static void setVolume(boolean raise) {
    Application app = Utils.getApp();
    if (app == null) {
      return;
    }
    AudioManager audio = (AudioManager) app.getSystemService(Context.AUDIO_SERVICE);
    if (audio == null) {
      return;
    }
    int dire = raise ? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_LOWER;
    audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, dire,
        AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
    //int volume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
    //int volumeMax = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    //LogUtils.d("%s / %s", volume, volumeMax);
  }

  /**
   * 设置 系统多媒体音量.
   *
   * @param volume 0 ~ 15, 若小于0，设为最大音量的70%
   * @param showUi true显示音量控制UI
   */
  public static void setVolume(int volume, boolean showUi) {
    Application app = Utils.getApp();
    if (app == null) {
      return;
    }
    AudioManager audio = (AudioManager) app.getSystemService(Context.AUDIO_SERVICE);
    if (audio == null) {
      return;
    }
    if (volume < 0) {
      int volumeMax = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
      volume = (int) (volumeMax * 0.7F);
    }
    int flag = showUi ? AudioManager.FLAG_SHOW_UI : 0;
    audio.setStreamVolume(AudioManager.STREAM_MUSIC, volume, flag);
  }

  /**
   * 设置 soundPool 播放音量.
   *
   * @param volume 0~1f
   */
  public static void setVolume(float volume) {
    SoundUtils.volume = volume;
  }

  public static void setVolumeSilence(boolean silence) {
    if (silence) {
      isSilence = true;
      //setVolume(0);
    } else {
      isSilence = false;
      //setVolume(DEFAULT_VOLUME);
    }
  }

  /**
   * 播放指定id的声音
   *
   * @param id soundpool中声音流的id
   */
  private static void play(int id) {
    if (isSilence) {
      return;
    }
    mSoundPool.play(id, volume, volume, PRIORITY, LOOP, RATE);
  }

  /**
   * 按传入的 参数, 依次 播放
   *
   * @param indexs 可变参数, 从 mSoundIds[indexs] 获得 声音流 的id
   */
  private static void playArr(int... indexs) {
    if (isSilence) {
      return;
    }
    for (int i = 0; i < indexs.length; i++) {
      SoundUtils.play(mSoundIds[indexs[i]]);
      // 连续 报数的 时间 隔
      if (i < indexs.length - 1) {
        SystemClock.sleep(PLAY_INTERVAL);
      }
    }
  }

  /**
   * 报数
   *
   * @param n 需要 播报 的数字, 范围应在 0~999
   */
  public static void playNum(int n) {
    if (isSilence) {
      return;
    }
    if (n < 0 || 999 < n) {
      return;
    }

    // 报 一位数
    if (n <= 10) {
      SoundUtils.playArr(n);
      return;
    }

    // 报 两位数
    if (n < 100) {
      int bit = n % 10; // 个位上的 数字
      int bitTen = n / 10; // 十位上的 数字

      if (0 == bit) {
        // 报 整十
        SoundUtils.playArr(bitTen, SOUND_TEN);
      } else {
        // 报 十x
        if (1 == bitTen) {
          SoundUtils.playArr(SOUND_TEN, bit);
        } else {
          // 报 x十x
          SoundUtils.playArr(bitTen, SOUND_TEN, bit);
        }
      }
      return;
    }

    // 报 三位数
    int bit = n % 10; // 个位上的 数字
    int bitTen = n % 100 / 10; // 十位上的 数字
    int bitHundred = n / 100; // 百位上的 数字
    if (0 == bit) { // 个位数 为 0
      if (0 == bitTen) {
        // 报 整百
        SoundUtils.playArr(bitHundred, SOUND_HUNDRED); // 11 = '百'的 音效
      } else {
        // 报 x百x十
        SoundUtils
            .playArr(bitHundred, SOUND_HUNDRED, bitTen, SOUND_TEN);
      }
    } else { // 个位数 不为 0
      if (0 == bitTen) {
        // 报 x百零x
        SoundUtils.playArr(bitHundred, SOUND_HUNDRED, 0, bit);
      } else {
        // 报 x百x十x
        SoundUtils.playArr(bitHundred, SOUND_HUNDRED, bitTen,
            SOUND_TEN, bit);
      }
    }
  }

  /**
   * 报数
   *
   * @param n 需要 播报 的 字符串形式的 数字, 范围应在 1~999
   */
  public static void playNum(String n) {
    playNum(Integer.parseInt(n));
  }

  /** 播放成功声. */
  public static void playToneSuccess() {
    SoundUtils.play(INIT_SUCCESS);
  }

  /** 播放 正常按键声音 */
  public static void playToneDrop() {
    SoundUtils.play(DROP_SOUND);
  }

  /** 播放 错误声音. */
  public static void playToneFailed() {
    SoundUtils.play(FAILED_SOUND);
  }

  /** 播放扫描音效. */
  public static void playToneScanOne() {
    SoundUtils.play(TONE_SCAN_ONE);
  }

  /** 播放 滴答声. */
  public static void playToneDiDa() {
    SoundUtils.play(WRITE_ING_DATA);
  }

  /**
   * 播放 开始 扫捆
   */
  public static void playScanStartSound() {
    SoundUtils.play(SCAN_START_SOUND);
  }

  /**
   * 播放 扫捆 结束
   */
  public static void playScanFinishSound() {
    SoundUtils.play(SCAN_FINISH_SOUND);
  }

  /**
   * 播放 请扫描袋声
   */
  public static void playScanBag() {
    SoundUtils.play(SCAN_BAG);
  }

  /**
   * 播放 请扫描堆袋声
   */
  public static void playScanPileBag() {
    SoundUtils.play(SCAN_PILE_BAG);
  }

  /**
   * 播放 请刷新屏声
   */
  public static void playRefreshScreen() {
    SoundUtils.play(REFRESH_SCREEN);
  }

  /**
   * 设置 系统多媒体音量.同时显示音量控制UI和播放声音
   *
   * @param raise true增加音量
   */
  public static void setAudioVolume(boolean raise) {
    Application app = Utils.getApp();
    if (app == null) {
      return;
    }
    AudioManager audio = (AudioManager) app.getSystemService(Context.AUDIO_SERVICE);
    if (audio == null) {
      return;
    }
    int dire = raise ? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_LOWER;
    audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, dire,
        AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
    //int volume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
    //int volumeMax = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    //LogUtils.d("%s / %s", volume, volumeMax);
  }

  /**
   * 设置 系统多媒体音量.
   *
   * @param volume 0 ~ 15, 若小于0，设为最大音量的70%
   * @param showUi true显示音量控制UI
   */
  public static void setAudioVolume(int volume, boolean showUi) {
    setAudioVolume(volume, showUi, false);
  }

  public static void setAudioVolume(int volume, boolean showUi, boolean playSound) {
    Application app = Utils.getApp();
    if (app == null) {
      return;
    }
    AudioManager audio = (AudioManager) app.getSystemService(Context.AUDIO_SERVICE);
    if (audio == null) {
      return;
    }
    if (volume < 0) {
      int volumeMax = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
      volume = (int) (volumeMax * 0.7F);
    }
    int flag = 0;
    if (showUi) {
      flag |= AudioManager.FLAG_SHOW_UI;
    }
    if (playSound) {
      flag |= AudioManager.FLAG_PLAY_SOUND;
    }
    audio.setStreamVolume(AudioManager.STREAM_MUSIC, volume, flag);
  }

  /**
   * 获取多媒体 音量.
   *
   * @param isMax true 获取最大音量，否则获取 当前音量.
   */
  public static int getAudioVolume(boolean isMax) {
    Application app = Utils.getApp();
    AudioManager audio = (AudioManager) app.getSystemService(Context.AUDIO_SERVICE);
    if (audio == null) {
      return -1;
    }
    int volume;
    if (isMax) {
      volume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    } else {
      volume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
    }
    return volume;
  }
}
