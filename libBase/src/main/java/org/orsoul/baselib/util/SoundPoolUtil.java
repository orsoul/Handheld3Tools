package org.orsoul.baselib.util;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.SystemClock;

import com.apkfuns.logutils.LogUtils;
import com.blankj.utilcode.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SoundPoolUtil {

  private static final String TAG = SoundPoolUtil.class.getSimpleName();

  /** SoundPool对象中允许同时存在的最大流的数量 */
  protected static final int MAX_STREAMS = 32;

  /** 声道音量. */
  protected static final float DEFAULT_VOLUME = 1.0F;
  /** 声道音量. */
  protected static final int DEFAULT_PRIORITY = 1;
  /** 指定是否循环播放。0表示无限循环，n播放n次. */
  protected static final int DEFAULT_TIMES = 1;
  /** 播放速率0.5~2。1.0为原始频率,2.0 为两倍播放. */
  protected static final float DEFAULT_RATE = 1;

  protected SoundPool mSoundPool;
  protected float volume = DEFAULT_VOLUME;
  protected boolean isSilence;

  protected boolean isLoadC = false;

  private Map<String, Integer> idCache;
  private List<Integer> sidCache;

  protected SoundPoolUtil() {
    idCache = new HashMap<>();
    sidCache = new ArrayList<>();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      AudioAttributes aab = new AudioAttributes.Builder()
          .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
          .setUsage(AudioAttributes.USAGE_MEDIA)
          .build();
      mSoundPool = new SoundPool.Builder()
          .setMaxStreams(MAX_STREAMS)
          .setAudioAttributes(aab)
          .build();
    } else {
      mSoundPool = new SoundPool(MAX_STREAMS, AudioManager.USE_DEFAULT_STREAM_TYPE, 0);
    }
    //mSoundPool = new SoundPool(MAX_STREAMS, AudioManager.USE_DEFAULT_STREAM_TYPE, 0);
    mSoundPool.setOnLoadCompleteListener(new MyOnLoadCompleteListener());
  }

  private static class SingletonHolder {
    private static SoundPoolUtil instance = new SoundPoolUtil();
  }

  public static SoundPoolUtil getInstance() {
    return SingletonHolder.instance;
  }

  private class MyOnLoadCompleteListener implements SoundPool.OnLoadCompleteListener {
    @Override
    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
      isLoadC = true;
    }
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

  /**
   * 设置 soundPool 播放音量.
   *
   * @param volume 0~1f
   */
  public void setVolume(float volume) {
    this.volume = volume;
  }

  /**
   * 按百分比 获取当前音量.
   *
   * @param percent 0~2.0
   */
  public float getVolume(float percent) {
    return this.volume * percent;
  }

  public void setSilence(boolean silence) {
    this.isSilence = silence;
  }

  /**
   * 加载指定资源
   */
  public void loadR(String name, String path) {
    if (checkSoundPool()) {
      if (!idCache.containsKey(name)) {
        idCache.put(name, mSoundPool.load(path, 1));
      }
    }
  }

  /**
   * 加载指定路径列表的资源
   */
  public void loadR(Map<String, String> map) {
    Set<Map.Entry<String, String>> entries = map.entrySet();
    for (Map.Entry<String, String> entry : entries) {
      String key = entry.getKey();
      if (checkSoundPool()) {
        if (!idCache.containsKey(key)) {
          idCache.put(key, mSoundPool.load(entry.getValue(), 1));
        }
      }
    }
  }

  /**
   * 加载指定AssetFileDescriptor的资源
   */
  public void loadRF(String name, AssetFileDescriptor afd) {
    if (checkSoundPool()) {
      if (!idCache.containsKey(name)) {
        idCache.put(name,
            mSoundPool.load(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength(), 1));
      }
    }
  }

  /**
   * 加载指定AssetFileDescriptor列表的资源
   */
  public void loadRF(Map<String, AssetFileDescriptor> map) {
    Set<Map.Entry<String, AssetFileDescriptor>> entries = map.entrySet();
    for (Map.Entry<String, AssetFileDescriptor> entry : entries) {
      String key = entry.getKey();
      if (checkSoundPool()) {
        if (!idCache.containsKey(key)) {
          idCache.put(key, mSoundPool
              .load(entry.getValue().getFileDescriptor(), entry.getValue().getStartOffset(),
                  entry.getValue().getLength(), 1));
        }
      }
    }
  }

  /**
   * 加载指定列表资源
   */
  public void loadR(Context context, Map<String, Integer> map) {
    Set<Map.Entry<String, Integer>> entries = map.entrySet();
    for (Map.Entry<String, Integer> entry : entries) {
      String key = entry.getKey();
      if (checkSoundPool()) {

        if (!idCache.containsKey(key)) {
          idCache.put(key, mSoundPool.load(context, entry.getValue(), 1));
          LogUtils.d("loadR: " + key);
          LogUtils.d("loadR------------" + idCache.size());
        }
      }
    }
  }

  /**
   * 加载单个音频
   */
  public void loadR(Context context, String name, int res) {
    if (checkSoundPool()) {
      if (!idCache.containsKey(name)) {
        idCache.put(name, mSoundPool.load(context, res, 1));
      }
    }
  }

  public int load(Context context, int res) {
    if (checkSoundPool()) {
      return mSoundPool.load(context, res, DEFAULT_PRIORITY);
    }
    return -1;
  }

  /**
   * 播放指定音频，并返用于停止、暂停、恢复的StreamId
   */
  public int play(String name, int times) {

    LogUtils.i("play: ------------------------：" + name);
    return this.play(name, 1, 1, 1, times, 1);
  }

  /**
   * 播放指定音频，并指定播放次数和频率
   */
  public int play(String name, int times, int rate) {
    return this.play(name, 1, 1, 1, times, rate);
  }

  /**
   * 播放指定音频，并指定优先级和播放频率
   */
  public int play(String name, int property, int times, int rate) {
    return this.play(name, 1, 1, property, times, rate);
  }

  /**
   * 播放指定音频，并指定左右声道、优先级、播放次数、播放频率
   */
  public int play(String name, float leftVolume, float rightVolume, int property, int times,
      int rate) {
    int streamId = -1;
    if (checkSoundPool()) {
      if (idCache.containsKey(name) && isLoadC) {
        streamId =
            mSoundPool.play(idCache.get(name), leftVolume, rightVolume, property, times, rate);
        sidCache.add(streamId);
      }
    }
    return streamId;
  }

  /**
   * @param leftVolume 左声道音量：0~1.0
   * @param rightVolume 右声道音量：0~1.0
   * @param times 播放次数，0播放无限次。
   * @param rate 播放速度，范围：0.5~2倍速
   */
  public int play(int id, float leftVolume, float rightVolume, int times, float rate) {
    if (checkSoundPool() && !isSilence) {
      return mSoundPool.play(id, leftVolume, rightVolume, DEFAULT_PRIORITY, times - 1, rate);
    }
    return -1;
  }

  public int play(int id, float volume, int times, float rate) {
    return play(id, volume, volume, times, rate);
  }

  public int play(int id, int times, float rate) {
    return play(id, volume, times, rate);
  }

  public int play(int id, int times) {
    return play(id, times, DEFAULT_RATE);
  }

  public int play(int id) {
    return play(id, DEFAULT_TIMES);
  }

  public void playArr(float rate, int interval, int... ids) {
    if (isSilence) {
      return;
    }
    for (int i = 0; i < ids.length; i++) {
      play(ids[i], 1, rate);
      // 时间 隔
      if (i < ids.length - 1) {
        SystemClock.sleep(Math.round(interval * rate));
      }
    }
  }

  public void playArr(int interval, int... ids) {
    playArr(1, interval, ids);
  }

  /**
   * 播放指定列表的音频，并返回并返用于停止、暂停、恢复的StreamId列表
   */
  public List<Integer> play(List<String> names, int times) {

    return this.play(names, 1, 1, 1, times, 1);
  }

  /**
   * 播放指定列表的音频，并返回并返用于停止、暂停、恢复的StreamId列表，指定次数和频率
   */
  public List<Integer> play(List<String> names, int times, int rate) {

    return this.play(names, 1, 1, 1, times, rate);
  }

  /**
   * 播放指定列表的音频，并返回并返用于停止、暂停、恢复的StreamId列表，指定所有参数
   */
  public List<Integer> play(List<String> names, int leftVolume, int rightVolume, int property,
      int times, int rate) {
    List<Integer> streamIds = new ArrayList<>();
    if (checkSoundPool()) {
      for (String name : names) {
        if (idCache.containsKey(name) && isLoadC) {
          int a =
              mSoundPool.play(idCache.get(name), leftVolume, rightVolume, property, times, rate);
          streamIds.add(a);
          sidCache.add(a);
        }
      }
    }
    return streamIds;
  }

  /**
   * 停止指定id音频
   */
  public void stop(int r) {
    if (checkSoundPool()) {
      mSoundPool.stop(r);
    }
  }

  /**
   * 停止指定列表音频
   */
  public void stopAll() {
    if (checkSoundPool()) {
      for (int r : sidCache) {
        mSoundPool.stop(r);
      }
    }
  }

  /**
   * 暂停指定音效
   */
  public void pause(int r) {
    if (checkSoundPool()) {
      mSoundPool.pause(r);
    }
  }

  /**
   * 暂停指定列表音频
   */
  public void pause(List<Integer> list) {
    if (checkSoundPool()) {
      for (int r : list) {
        mSoundPool.pause(r);
      }
    }
  }

  /**
   * 暂停所有音效
   */
  public void pauseAll() {
    mSoundPool.autoPause();
  }

  /**
   * 恢复指定音频播放
   */
  public void resume(int r) {
    if (checkSoundPool()) {
      mSoundPool.resume(r);
    }
  }

  /**
   * 恢复指定列表的音频
   */
  public void resume(List<Integer> list) {
    if (checkSoundPool()) {
      for (int r : list) {
        mSoundPool.resume(r);
      }
    }
  }

  /**
   * 恢复所有暂停的音频
   */
  public void resumeAll() {
    if (checkSoundPool()) {
      mSoundPool.autoResume();
    }
  }

  /**
   * 卸载指定音频
   */
  public void unLoad(String name) {
    if (checkSoundPool()) {
      if (idCache.containsKey(name)) {
        mSoundPool.unload(idCache.get(name));
        idCache.remove(name);
      }
    }
  }

  /**
   * 卸载指定列表的音频
   */
  public void unLoad(List<String> names) {
    if (checkSoundPool()) {
      for (String name : names) {
        if (idCache.containsKey(name)) {
          mSoundPool.unload(idCache.get(name));
          idCache.remove(name);
        }
      }
    }
  }

  /**
   * 释放所有资源，如果想继续播放，需要重新加载资源
   */
  public void release() {
    if (checkSoundPool()) {
      mSoundPool.release();
      idCache.clear();
    }
  }

  private boolean checkSoundPool() {
    if (mSoundPool != null) {
      return true;
    }
    return false;
  }
}