package org.orsoul.baselib.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.SoundPool;
import com.apkfuns.logutils.LogUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SoundPoolUtil {

    private static final String TAG = SoundPoolUtil.class.getSimpleName();

    /** SoundPool对象中允许同时存在的最大流的数量 */
    private static final int MAX_STREAMS = 32;

    private SoundPool mSoundPool;
    private boolean isLoadC = false;
    private Map<String, Integer> idCache;
    private List<Integer> sidCache;

    private SoundPoolUtil() {
        idCache = new HashMap<>();
        sidCache = new ArrayList<>();
        //        if(Build.VERSION.SDK_INT >=  Build.VERSION_CODES.LOLLIPOP){
        //            AudioAttributes aab = new AudioAttributes.Builder()
        //                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        //                    .setUsage(AudioAttributes.USAGE_MEDIA)
        //                    .build() ;
        //            mSoundPool = new SoundPool.Builder()
        //                    .setMaxStreams(MAX_STREAMS)
        //                    .setAudioAttributes(aab)
        //                    .build() ;
        //        }else{
        //           mSoundPool = new SoundPool(MAX_STREAMS, AudioManager.USE_DEFAULT_STREAM_TYPE, 0);
        //        }
        mSoundPool = new SoundPool(MAX_STREAMS, AudioManager.USE_DEFAULT_STREAM_TYPE, 0);
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
     * 加载指定资源
     * @param name
     * @param path
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
     * @param map
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
     * @param name
     * @param afd
     */
    public void loadRF(String name, AssetFileDescriptor afd) {
        if (checkSoundPool()) {
            if (!idCache.containsKey(name)) {
                idCache.put(name, mSoundPool.load(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength(), 1));
            }
        }
    }

    /**
     * 加载指定AssetFileDescriptor列表的资源
     * @param map
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
     * @param context
     * @param map
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
     * @param context
     * @param name
     * @param res
     */
    public void loadR(Context context, String name, int res) {
        if (checkSoundPool()) {
            if (!idCache.containsKey(name)) {
                idCache.put(name, mSoundPool.load(context, res, 1));
            }
        }
    }

    /**
     * 播放指定音频，并返用于停止、暂停、恢复的StreamId
     * @param name
     * @param times
     * @return
     */
    public int play(String name, int times) {

      LogUtils.i("play: ------------------------：" + name);
        return this.play(name, 1, 1, 1, times, 1);
    }

    /**
     * 播放指定音频，并指定播放次数和频率
     * @param name
     * @param times
     * @param rate
     * @return
     */
    public int play(String name, int times, int rate) {
        return this.play(name, 1, 1, 1, times, rate);
    }

    /**
     * 播放指定音频，并指定优先级和播放频率
     * @param name
     * @param property
     * @param times
     * @param rate
     * @return
     */
    public int play(String name, int property, int times, int rate) {
        return this.play(name, 1, 1, property, times, rate);
    }

    /**
     * 播放指定音频，并指定左右声道、优先级、播放次数、播放频率
     */
    public int play(String name, float leftVolume, float rightVolume, int property, int times, int rate) {
        int streamId = -1;
        if (checkSoundPool()) {
            if (idCache.containsKey(name) && isLoadC) {
                streamId = mSoundPool.play(idCache.get(name), leftVolume, rightVolume, property, times, rate);
                sidCache.add(streamId);
            }
        }
        return streamId;
    }

    /**
     * 播放指定列表的音频，并返回并返用于停止、暂停、恢复的StreamId列表
     */
    public List<Integer> play(List<String> names, int times) {

        return this.play(names, 1, 1, 1, times, 1);
    }

    /**
     * 播放指定列表的音频，并返回并返用于停止、暂停、恢复的StreamId列表，指定次数和频率
     * @param names
     * @param times
     * @param rate
     * @return
     */
    public List<Integer> play(List<String> names, int times, int rate) {

        return this.play(names, 1, 1, 1, times, rate);
    }

    /**
     * 播放指定列表的音频，并返回并返用于停止、暂停、恢复的StreamId列表，指定所有参数
     * @param names
     * @param leftVolume
     * @param rightVolume
     * @param property
     * @param times
     * @param rate
     * @return
     */
    public List<Integer> play(List<String> names, int leftVolume, int rightVolume, int property, int times, int rate) {
        List<Integer> streamIds = new ArrayList<>();
        if (checkSoundPool()) {
            for (String name : names) {
                if (idCache.containsKey(name) && isLoadC) {
                    int a = mSoundPool.play(idCache.get(name), leftVolume, rightVolume, property, times, rate);
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
     * @param r
     */
    public void pause(int r) {
        if (checkSoundPool()) {
            mSoundPool.pause(r);
        }
    }

    /**
     * 暂停指定列表音频
     * @param list
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
     * @param r
     */
    public void resume(int r) {
        if (checkSoundPool()) {
            mSoundPool.resume(r);
        }
    }

    /**
     * 恢复指定列表的音频
     * @param list
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
     * @param name
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
     * @param names
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