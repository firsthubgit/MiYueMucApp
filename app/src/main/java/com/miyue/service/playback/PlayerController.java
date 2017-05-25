package com.miyue.service.playback;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.miyue.R;
import com.miyue.application.MiYueConstans;
import com.miyue.common.listener.Playback;
import com.miyue.dao.MusicProvider;
import com.miyue.utils.UtilLog;

/**
 * Created by zhangzhendong on 17/5/20.
 */

public class PlayerController implements  Playback.Callback{

    private static final String TAG = "PlayerController";

    private MusicProvider mMusicProvider;
    private QueueManager mQueueManager;
    private RemoteMusicPlayer mMusicPlayer;
    private MusicServiceCallback mServiceCallback;
    private MediaSessionCallback mMediaSessionCallback;

    // Action to thumbs up a media item
    private static final String CUSTOM_ACTION_THUMBS_UP = "com.example.android.uamp.THUMBS_UP";


    public PlayerController(MusicServiceCallback serviceCallback,
                           MusicProvider musicProvider,
                           QueueManager queueManager,
                           RemoteMusicPlayer musicPlayer) {
        mServiceCallback = serviceCallback;
        mMusicProvider = musicProvider;
        mQueueManager = queueManager;
        mMediaSessionCallback = new MediaSessionCallback();
        mMusicPlayer = musicPlayer;
        mMusicPlayer.setCallback(this);
    }

    public MediaSessionCompat.Callback getMediaSessionCallback() {
        return mMediaSessionCallback;
    }

    public RemoteMusicPlayer getMusicPlayer() {
        return mMusicPlayer;
    }
    /**
     * 更新当前media player的状态
     */
    public void updatePlaybackState() {
        long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        if (mMusicPlayer != null) {
            position = mMusicPlayer.getCurrentStreamPosition();
        }

        //noinspection ResourceType
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(getAvailableActions());

        setCustomAction(stateBuilder);
        int state = mMusicPlayer.getState();

        //noinspection ResourceType
        stateBuilder.setState(state, position, 1.0f, SystemClock.elapsedRealtime());

        // Set the activeQueueItemId if the current index is valid.
        MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
        if (currentMusic != null) {
            stateBuilder.setActiveQueueItemId(currentMusic.getQueueId());
        }

        mServiceCallback.onPlaybackStateUpdated(stateBuilder.build());

        if (state == PlaybackStateCompat.STATE_PLAYING ||
                state == PlaybackStateCompat.STATE_PAUSED) {
            mServiceCallback.onNotificationRequired();
        }
    }

    private long getAvailableActions() {
        long actions =
                PlaybackStateCompat.ACTION_PLAY_PAUSE |
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                        PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT|
                        PlaybackStateCompat.ACTION_SET_REPEAT_MODE;
        if (mMusicPlayer.isPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        } else {
            actions |= PlaybackStateCompat.ACTION_PLAY;
        }
        return actions;
    }


    private void setCustomAction(PlaybackStateCompat.Builder stateBuilder) {
        MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
        if (currentMusic == null) {
            return;
        }
        String mediaId = currentMusic.getDescription().getMediaId();
        if (mediaId == null) {
            return;
        }
        int favoriteIcon = mMusicProvider.isFavorite(mediaId) ?
                R.drawable.ic_star_on : R.drawable.ic_star_off;
        UtilLog.e(TAG, "updatePlaybackState, setting Favorite custom action of music " +
                mediaId + " current favorite=" + mMusicProvider.isFavorite(mediaId));
        Bundle customActionExtras = new Bundle();
        stateBuilder.addCustomAction(new PlaybackStateCompat.CustomAction.Builder(
                CUSTOM_ACTION_THUMBS_UP, "最爱", favoriteIcon)
                .setExtras(customActionExtras)
                .build());
    }

    public void setCurrentMediaId(String mediaId) {
        mQueueManager.setQueueFromMusic(mediaId);
    }
////////////////////////////////////////////////////////////////
//处理播放相关


    public void handlePlayRequest(){
        UtilLog.e(TAG, "handlePlayRequest: mState=" + mMusicPlayer.getState());
        MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
        if (currentMusic != null) {
            mServiceCallback.onPlaybackStart();
            mMusicPlayer.play(currentMusic.getDescription());
        }
    }

    public void handlePauseRequest() {
        UtilLog.e(TAG, "handlePauseRequest: mState=" + mMusicPlayer.getState());
        if (mMusicPlayer.isPlaying()) {
            mMusicPlayer.pause();
            mServiceCallback.onPlaybackStop();
        }
    }

    public void handleStopRequest(String withError) {
        UtilLog.e(TAG, "handleStopRequest: mState=" + mMusicPlayer.getState());
        mMusicPlayer.stop(true);
        mServiceCallback.onPlaybackStop();
        updatePlaybackState();
    }
///////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////
//前台的UI里的操作会走到这里处理

    private class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            UtilLog.e(TAG, "play");
            if (mQueueManager.getCurrentMusic() == null) {
                mQueueManager.setRandomQueue();
            }
            handlePlayRequest();
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            UtilLog.e(TAG, "playFromMediaId mediaId:"+ mediaId + "  extras=" + extras);
            if(null !=mQueueManager.getCurrentMusic()
                    && (mediaId.equals(mQueueManager.getCurrentMusic().getDescription().getMediaId()))){
                if(mMusicPlayer.getState() == PlaybackStateCompat.STATE_PLAYING){
                    handlePauseRequest();
                } else if(mMusicPlayer.getState() == PlaybackStateCompat.STATE_PAUSED){
                    handlePlayRequest();
                }
                return;
            }
            mQueueManager.setQueueFromMusic(mediaId);
            handlePlayRequest();
        }

        @Override
        public void onPause() {
            UtilLog.e(TAG, "pause. current state=" + mMusicPlayer.getState());
            handlePauseRequest();
        }

        @Override
        public void onStop() {
            UtilLog.e(TAG, "stop. current state=" + mMusicPlayer.getState());
            handleStopRequest(null);
        }

        @Override
        public void onSkipToNext() {
            UtilLog.e(TAG, "skipToNext");
            if (mQueueManager.skipNextOperation(0)) {
                handlePlayRequest();
            } else {
                handleStopRequest("Cannot skip");
            }
            mQueueManager.updateMetadata();
        }

        @Override
        public void onSkipToPrevious() {
            if (mQueueManager.skipPreOperation()) {
                handlePlayRequest();
            } else {
                handleStopRequest("Cannot skip");
            }
            mQueueManager.updateMetadata();
        }

        @Override
        public void onSeekTo(long position) {
            UtilLog.e(TAG, "onSeekTo:" + position);
            mMusicPlayer.seekTo((int) position);
        }

        @Override
        public void onSkipToQueueItem(long queueId) {
            UtilLog.d(TAG, "OnSkipToQueueItem:" + queueId);
            mQueueManager.setCurrentQueueItem(queueId);
            mQueueManager.updateMetadata();
        }

        @Override
        public void onSetRepeatMode(int repeatMode){
            mQueueManager.setRepeatMode(repeatMode);
        }
        @Override
        public void onCustomAction(@NonNull String action, Bundle extras) {
            UtilLog.e(TAG, "onCustomAction action:" + action);
            if (CUSTOM_ACTION_THUMBS_UP.equals(action)) {
                UtilLog.e(TAG, "onCustomAction: favorite for current track");
                MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
                if (currentMusic != null) {
                    String mediaId = currentMusic.getDescription().getMediaId();
                    if (mediaId != null) {
                        mMusicProvider.setFavorite(mediaId, !mMusicProvider.isFavorite(mediaId));
                    }
                }
                // playback state needs to be updated because the "Favorite" icon on the
                // custom action will change to reflect the new favorite state.
                updatePlaybackState();
            } else {
                UtilLog.e(TAG, "Unsupported action: " + action);
            }
        }

        /**
         * 此方法是运行在主线程的
         * 通过与MediaControllerCompat连接，所有Android上的语音搜索自动发送到这个方法
         * 搜索最为一个耗时的操作，应启用线程异步处理，例如AsyncTask
         **/
        @Override
        public void onPlayFromSearch(final String query, final Bundle extras) {
            mMusicPlayer.setState(PlaybackStateCompat.STATE_CONNECTING);
//            boolean successSearch = mQueueManager.setQueueFromSearch(query, extras);
//            if (successSearch) {
//                handlePlayRequest();
//                mQueueManager.updateMetadata();
//            } else {
//                updatePlaybackState("Could not find music");
//            }
        }
    }


//////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////
//好多回调

    @Override
    public void onCompletion() {
        UtilLog.e(TAG, "播完一曲,然后skipToNext");
        if (mQueueManager.skipNextOperation(9)) {
            handlePlayRequest();
        } else {
            handleStopRequest("Cannot skip");
        }
        mQueueManager.updateMetadata();
    }
    //RemoteMusicPlayer调用
    @Override
    public void onPlaybackStatusChanged(int state) {
        updatePlaybackState();
    }

    @Override
    public void onError(String error) {

    }




    public interface MusicServiceCallback {

        void onPlaybackStart();

        void onNotificationRequired();

        void onPlaybackStop();

        void onPlaybackStateUpdated(PlaybackStateCompat newState);
    }
}
