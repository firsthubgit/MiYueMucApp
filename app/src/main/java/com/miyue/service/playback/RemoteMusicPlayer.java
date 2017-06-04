package com.miyue.service.playback;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import com.miyue.application.MiYueConstans;
import com.miyue.common.listener.Playback.Callback;
import com.miyue.service.PlayerService;
import com.miyue.utils.FileUtils;
import com.miyue.utils.StringUtils;
import com.miyue.utils.UtilLog;

import java.io.IOException;

/**
 * Created by zhangzhendong on 17/5/19.
 */

public class RemoteMusicPlayer implements AudioManager.OnAudioFocusChangeListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener {

    private static final String TAG = "RemoteMusicPlayer";


    /**当时去焦点，当仍然可以播放的时候，降低声音的值*/
    public static final float VOLUME_DUCK = 0.2f;
    /**正常播放时候的声音值*/
    public static final float VOLUME_NORMAL = 1.0f;

    /**没有焦点，也不能低音量播放*/
    private static final int AUDIO_NO_FOCUS_NO_DUCK = 0;
    /**没有焦点，但是可以低音量播放*/
    private static final int AUDIO_NO_FOCUS_CAN_DUCK = 1;
    /**拥有audio焦点*/
    private static final int AUDIO_FOCUSED  = 2;

    private final Context mContext;
    private final WifiManager.WifiLock mWifiLock;
    /**播放状态*/
    private int mState;
    private Callback mCallback;
    private final MusicProvider mMusicProvider;

    /**当前的焦点处于那种情况*/
    private int mAudioFocus = AUDIO_NO_FOCUS_NO_DUCK;
    /**获得audio焦点的过程*/
    private boolean mPlayOnFocusGain;

    private boolean isPlayFromNet;
    private final AudioManager mAudioManager;
    private CompatMediaPlayer mMediaPlayer;
    private volatile boolean mAudioNoisyReceiverRegistered;
    private volatile int mCurrentPosition;
    private volatile String mCurrentMediaId;

    private final IntentFilter mAudioNoisyIntentFilter =
            new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private final BroadcastReceiver mAudioNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                UtilLog.d(TAG, "Headphones disconnected.");
                if (isPlaying()) {
                    Intent i = new Intent(context, PlayerService.class);
                    i.setAction(MiYueConstans.ACTION_CMD);
                    i.putExtra(MiYueConstans.CMD_NAME, MiYueConstans.CMD_PAUSE);
                    mContext.startService(i);
                }
            }
        }
    };
    public RemoteMusicPlayer(Context context, MusicProvider musicProvider) {
        this.mContext = context;
        this.mMusicProvider = musicProvider;
        this.mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        // 创建 Wifi lock
        this.mWifiLock = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "uAmp_lock");
        this.mState = PlaybackStateCompat.STATE_NONE;
    }


//////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////
//其他操作


    public void setState(int state) {
        this.mState = state;
    }

    public int getState(){
        return mState;
    }
    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }


    public void setPlayerCurrentMediaId(String mediaId) {
        this.mCurrentMediaId = mediaId;
    }

    public String getPlayerCurrentMediaId() {
        return mCurrentMediaId;
    }

    private void tryToGetAudioFocus() {
        int result = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mAudioFocus = AUDIO_FOCUSED;
        } else {
            mAudioFocus = AUDIO_NO_FOCUS_NO_DUCK;
        }
    }

    private void registerAudioNoisyReceiver() {
        if (!mAudioNoisyReceiverRegistered) {
            mContext.registerReceiver(mAudioNoisyReceiver, mAudioNoisyIntentFilter);
            mAudioNoisyReceiverRegistered = true;
        }
    }

    private void unregisterAudioNoisyReceiver() {
        if (mAudioNoisyReceiverRegistered) {
            mContext.unregisterReceiver(mAudioNoisyReceiver);
            mAudioNoisyReceiverRegistered = false;
        }
    }

    private void configMediaPlayerState() {
        UtilLog.e(TAG, "configMediaPlayerState. mAudioFocus="+ mAudioFocus);
        if (mAudioFocus == AUDIO_NO_FOCUS_NO_DUCK) {
            if (mState == PlaybackStateCompat.STATE_PLAYING) {
                pause();
            }
        } else {
            //拥有焦点
            registerAudioNoisyReceiver();
            if (mAudioFocus == AUDIO_NO_FOCUS_CAN_DUCK) {
                mMediaPlayer.setVolume(VOLUME_DUCK, VOLUME_DUCK);
            } else {
                if (mMediaPlayer != null) {
                    mMediaPlayer.setVolume(VOLUME_NORMAL, VOLUME_NORMAL);
                } else {
                    //当mMediaPlayer为空，可以在客户端提示异常等操作
                    UtilLog.e(TAG, "mMediaPlayer为空了啊！");
                }
            }

            if (mPlayOnFocusGain) {
                if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
                    UtilLog.e(TAG,"configMediaPlayerState startMediaPlayer. seeking to "+
                            mCurrentPosition);
                    if (mCurrentPosition == mMediaPlayer.getCurrentPosition()) {
                        mMediaPlayer.start();
                        mState = PlaybackStateCompat.STATE_PLAYING;
                    } else {
                        mMediaPlayer.seekTo(mCurrentPosition);
                        mState = PlaybackStateCompat.STATE_BUFFERING;
                    }
                }
            }
        }
        if (mCallback != null) {
            mCallback.onPlaybackStatusChanged(mState);
        }
    }

    /**
     * 释放当前服务所使用的资源。
     *这包括“前台服务”的状态，唤醒锁和可能的媒体播放器。
     *
     * @param releaseMediaPlayer 是否把MediaPlayer也释放掉
     */
    private void relaxResources(boolean releaseMediaPlayer) {
        UtilLog.d(TAG, "relaxResources. releaseMediaPlayer="+releaseMediaPlayer);

        if (releaseMediaPlayer && mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        // 如果持有 Wifi lock, 释放掉
        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        }
    }


    private void giveUpAudioFocus() {
        if (mAudioManager.abandonAudioFocus(this) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mAudioFocus = AUDIO_NO_FOCUS_NO_DUCK;
        }
    }

    public int getCurrentStreamPosition() {
        return mMediaPlayer != null ?
                mMediaPlayer.getCurrentPosition() : mCurrentPosition;
    }

    /**
     * 确保mediaplayer存在且是初始状态，
     */
    private void createMediaPlayerIfNeeded() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new CompatMediaPlayer();

            // Make sure the media player will acquire a wake-lock while
            // playing. If we don't do that, the CPU might go to sleep while the
            // song is playing, causing playback to stop.
            mMediaPlayer.setWakeMode(mContext.getApplicationContext(),
                    PowerManager.PARTIAL_WAKE_LOCK);

            // we want the media player to notify us when it's ready preparing,
            // and when it's done playing:
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnSeekCompleteListener(this);
        } else {
            mMediaPlayer.reset();
        }
    }
 //////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////
//播放动作相关事宜

    public boolean isPlaying() {
        return mPlayOnFocusGain || (mMediaPlayer != null && mMediaPlayer.isPlaying());
    }

    public void pause() {
        if (mState == PlaybackStateCompat.STATE_PLAYING) {
            // Pause media player and cancel the 'foreground service' state.
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentPosition = mMediaPlayer.getCurrentPosition();
            }
            // 暂停时，保留MediaPlayer放弃焦点
            relaxResources(false);
        }
        mState = PlaybackStateCompat.STATE_PAUSED;
        if (mCallback != null) {
            mCallback.onPlaybackStatusChanged(mState);
        }
        unregisterAudioNoisyReceiver();
    }


    public void stop(boolean notifyListeners) {
        mState = PlaybackStateCompat.STATE_STOPPED;
        if (notifyListeners && mCallback != null) {
            mCallback.onPlaybackStatusChanged(mState);
        }
        mCurrentPosition = getCurrentStreamPosition();
        // 释放焦点
        giveUpAudioFocus();
        unregisterAudioNoisyReceiver();
        //释放MediaPlayer和wifi锁
        relaxResources(true);
    }

    public void play(MediaDescriptionCompat description){
        mPlayOnFocusGain = true;
        tryToGetAudioFocus();
        registerAudioNoisyReceiver();
        String mediaId = description.getMediaId();
        boolean mediaHasChanged = !TextUtils.equals(mediaId, mCurrentMediaId);
        if (mediaHasChanged) {
            mCurrentPosition = 0;
            mCurrentMediaId = mediaId;
        }
        //从暂停状态，并且mediaID没有变
        if (mState == PlaybackStateCompat.STATE_PAUSED && !mediaHasChanged && mMediaPlayer != null) {
            configMediaPlayerState();
        } else {
            mState = PlaybackStateCompat.STATE_STOPPED;
            //除了MediaPlayer都释放掉
            relaxResources(false);
            try {
                createMediaPlayerIfNeeded();
                mState = PlaybackStateCompat.STATE_BUFFERING;
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                Uri uri = description.getMediaUri();
                if(uri != null && !StringUtils.isNullOrEmpty(uri.toString())){
                    mMediaPlayer.setDataSource(description.getMediaUri().toString());
                } else {
                    mMediaPlayer.setDataSource(
                            mMusicProvider.getCurernNetMeta().getString(MusicProvider.MEDIA_NET_PLAY_URL));
                }


                // Starts preparing the media player in the background. When
                // it's done, it will call our OnPreparedListener (that is,
                // the onPrepared() method on this class, since we set the
                // listener to 'this'). Until the media player is prepared,
                // we *cannot* call start() on it!
                mMediaPlayer.prepareAsync();

                // If we are streaming from the internet, we want to hold a
                // Wifi lock, which prevents the Wifi radio from going to
                // sleep while the song is playing.
                mWifiLock.acquire();

                if (mCallback != null) {
                    mCallback.onPlaybackStatusChanged(mState);
                }

            } catch (IOException ex) {
                ex.printStackTrace();
                if (mCallback != null) {
                    mCallback.onError(ex.getMessage());
                }
            }
        }
    }

    public void seekTo(int position) {
        UtilLog.e(TAG, "seekbar位置：" + position);
        if (mMediaPlayer == null) {
            mCurrentPosition = position;
        } else {
            if (mMediaPlayer.isPlaying()) {
                mState = PlaybackStateCompat.STATE_BUFFERING;
            }
            registerAudioNoisyReceiver();
            mMediaPlayer.seekTo(position);
            if (mCallback != null) {
                mCallback.onPlaybackStatusChanged(mState);
            }
        }
    }
///*********实现的相关回调******************************************************************////////


    @Override
    public void onAudioFocusChange(int focusChange) {
        UtilLog.e(TAG, "onAudioFocusChange. focusChange="+focusChange);
        switch (focusChange){
            case AudioManager.AUDIOFOCUS_GAIN:
                // Resume playback or Raise it back to normal
                mAudioFocus = AUDIO_FOCUSED;
//                mPlayOnFocusGain = true; 重新获得焦点不播放

                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // am.unregisterMediaButtonEventReceiver(RemoteControlReceiver);
                // am.abandonAudioFocus(afChangeListener);
                // Stop playback
                mAudioFocus = AUDIO_NO_FOCUS_NO_DUCK;
                mPlayOnFocusGain = false;

                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Pause playback
                mAudioFocus = AUDIO_NO_FOCUS_NO_DUCK;
                mPlayOnFocusGain = false;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lower the volume
                mAudioFocus = AUDIO_NO_FOCUS_CAN_DUCK;
                mPlayOnFocusGain = false;
                break;
            default:
                UtilLog.e(TAG, "onAudioFocusChange:unsupported focusChange: "+ focusChange);
                break;
        }
        configMediaPlayerState();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mCurrentPosition = 0;
        if (mCallback != null) {
            mCallback.onCompletion();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (mCallback != null) {
            UtilLog.e(TAG, "onError1: " + what);
            if(-38 == what){
                mCallback.onError(MiYueConstans.ERROR_NO_FILE);
            } else {
                mCallback.onError("MediaPlayer error " + what + " (" + extra + ")");
            }
        }
        return false;//true表示我们处理了这条消息
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        configMediaPlayerState();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        UtilLog.e(TAG, "onSeekComplete from MediaPlayer:" + mp.getCurrentPosition());
        mCurrentPosition = mp.getCurrentPosition();
        if (mState == PlaybackStateCompat.STATE_BUFFERING) {
            registerAudioNoisyReceiver();
            mMediaPlayer.start();
            mState = PlaybackStateCompat.STATE_PLAYING;
        }
        if (mCallback != null) {
            mCallback.onPlaybackStatusChanged(mState);
        }
    }
}
