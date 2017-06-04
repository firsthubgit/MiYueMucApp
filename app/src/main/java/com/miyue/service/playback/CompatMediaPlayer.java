package com.miyue.service.playback;

import android.media.MediaPlayer;
import android.os.SystemClock;

/**
*
* @author ZZD
* @time 17/5/17 下午4:41
 *
 * 兼容API 16以下的系统
*/
public class CompatMediaPlayer extends MediaPlayer implements MediaPlayer.OnCompletionListener {

    private boolean mCompatMode = true;
    private MediaPlayer mNextPlayer;
    private OnCompletionListener mCompletion;

    public CompatMediaPlayer() {
        try {
            MediaPlayer.class.getMethod("setNextMediaPlayer", MediaPlayer.class);
            mCompatMode = false;
        } catch (NoSuchMethodException e) {
            mCompatMode = true;
            super.setOnCompletionListener(this);
        }
    }

    public void setNextMediaPlayer(MediaPlayer next) {
        if (mCompatMode) {
            mNextPlayer = next;
        } else {
            super.setNextMediaPlayer(next);
        }
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {
        if (mCompatMode) {
            mCompletion = listener;
        } else {
            super.setOnCompletionListener(listener);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mNextPlayer != null) {
            SystemClock.sleep(50);
            mNextPlayer.start();
        }
        mCompletion.onCompletion(this);
    }
}
