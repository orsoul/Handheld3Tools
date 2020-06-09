package com.fanfull.libhard.rd.barcodeScanTest;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundPoolAudioClip {
    private SoundPool soundpool;
    private int musicID;

    public SoundPoolAudioClip(Context ctx, int resID) {
        soundpool = new SoundPool(1, AudioManager.STREAM_MUSIC, 100);
        if (soundpool != null) {
            musicID = soundpool.load(ctx, resID, 1);
        }
    }

    public synchronized void play() {
        if (soundpool != null) {
            soundpool.play(musicID,// 播放的声音资�?
                    1.0f,// 左声道，范围�?0--1.0
                    1.0f,// 右声道，范围�?0--1.0
                    0, // 优先级，0为最低优先级
                    0,// 循环次数,0为不循环
                    1);// 播放速率�?1为正常�?�率
        }
    }

    public void release() {
        if (soundpool != null) {
            soundpool.release();
        }
    }
}
