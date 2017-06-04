package com.miyue.service.playback;

import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.miyue.application.MiYueConstans;
import com.miyue.common.listener.Playback;
import com.miyue.utils.MusicUtils;
import com.miyue.utils.UtilLog;

/**
*
* @author ZZD
* @time 17/5/20. 下午4:41
*/
public class PlayerController implements Playback.Callback{

    private static final String TAG = "PlayerController";

    private MusicProvider mMusicProvider;
    private QueueManager mQueueManager;
    private RemoteMusicPlayer mMusicPlayer;
    private MusicServiceCallback mServiceCallback;
    private MediaSessionCallback mMediaSessionCallback;



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
    public void updatePlaybackState(String error, String action) {

        long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        if (mMusicPlayer != null) {
            position = mMusicPlayer.getCurrentStreamPosition();
        }

        //noinspection ResourceType
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(getAvailableActions());
        if(null != action){
            switch (action){
                case MiYueConstans.CUSTOM_ACTION_DOWNLOAD_SUCCESS:
                    setCusActAddSuccess(stateBuilder);
                    break;
                case MiYueConstans.CUSTOM_ACTION_DELETE_CMD:
                    setCusActDelete(stateBuilder);
                    break;
                case MiYueConstans.CUSTOM_ACTION_THUMBS_UP:
                    setCustomAction(stateBuilder);
                    break;
                default:
                    break;
            }
        }
        int state = mMusicPlayer.getState();

        if (error != null) {
            // Error states are really only supposed to be used for errors that cause playback to
            // stop unexpectedly and persist until the user takes action to fix it.
            stateBuilder.setErrorMessage(error);
            state = PlaybackStateCompat.STATE_ERROR;
        }

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

    private void setCusActAddSuccess(PlaybackStateCompat.Builder stateBuilder){
        Bundle customActionExtras = new Bundle();
        stateBuilder.addCustomAction(new PlaybackStateCompat.CustomAction.Builder(
                MiYueConstans.CUSTOM_ACTION_DOWNLOAD_SUCCESS, "下载成功", 1)
                .setExtras(customActionExtras)
                .build());
    }

    private void setCusActDelete(PlaybackStateCompat.Builder stateBuilder){
        Bundle customActionExtras = new Bundle();
        stateBuilder.addCustomAction(new PlaybackStateCompat.CustomAction.Builder(
                MiYueConstans.CUSTOM_ACTION_DELETE_CMD, "删除成功", 1)
                .setExtras(customActionExtras)
                .build());
    }

    private void setCustomAction(PlaybackStateCompat.Builder stateBuilder) {

        Bundle customActionExtras = new Bundle();
        stateBuilder.addCustomAction(new PlaybackStateCompat.CustomAction.Builder(
                MiYueConstans.CUSTOM_ACTION_THUMBS_UP, "最爱", 1)
                .setExtras(customActionExtras)
                .build());
    }

    public void setCurrentMediaId(String mediaId) {
        mQueueManager.setQueueFromMusic(mediaId);
    }

///****************处理播放相关******************************************************///////


    public void handlePlayRequest(){
        UtilLog.e(TAG, "handlePlayRequest: mState=" + mMusicPlayer.getState());
        if(mQueueManager.isNetPlay){
            mServiceCallback.onPlaybackStart();
            mMusicPlayer.play(mQueueManager.getCurretNetMedia().getDescription());
            //在线音乐先不添加到最近播放
//            mMusicProvider.addRecentMusic(mQueueManager.getCurretNetMedia());
        } else{
            MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
            if (currentMusic != null) {
                mServiceCallback.onPlaybackStart();
                mMusicPlayer.play(currentMusic.getDescription());
                mMusicProvider.addRecentMusic(currentMusic.getDescription().getMediaId());
            }
        }
    }

    public void handlePlayFromNet(MediaMetadataCompat mediaData){
        mServiceCallback.onPlaybackStart();
        mMusicPlayer.play(mediaData.getDescription());
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
        updatePlaybackState(withError, null);
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
            if(mQueueManager.isNetPlay){
                mQueueManager.switchtoLocal();
                mQueueManager.setQueueFromMusic(mediaId);
                handlePlayRequest();
                return;
            }
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
            mQueueManager.switchtoLocal();
            if (mQueueManager.skipNextOperation(0)) {
                handlePlayRequest();
            } else {
                handleStopRequest("Cannot skip");
            }
            mQueueManager.updateMetadata();
        }

        @Override
        public void onSkipToPrevious() {
            mQueueManager.switchtoLocal();
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
        public void onPlayFromUri(Uri uri, Bundle extras) {
            super.onPlayFromUri(uri, extras);
            MediaMetadataCompat mediaData = MusicUtils.createMetadataFromQQSong(extras);
            mQueueManager.updateNetMetadata(mediaData);
            handlePlayFromNet(mediaData);
        }

        @Override
        public void onCustomAction(@NonNull String action, Bundle extras) {
            UtilLog.e(TAG, "onCustomAction action:" + action);
            if (MiYueConstans.CUSTOM_ACTION_THUMBS_UP.equals(action)) {
                String mediaID = extras.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
                if (mediaID != null) {
                    mMusicProvider.setFavorite(mediaID, !mMusicProvider.isFavorite(mediaID));
                }
                updatePlaybackState(null, MiYueConstans.CUSTOM_ACTION_THUMBS_UP);
            } else if(MiYueConstans.CUSTOM_ACTION_DOWNLOAD_SUCCESS.equals(action)){
                MediaMetadataCompat metaData = MusicUtils.createMetadataFromQQSong(extras);
                mMusicProvider.addDownMusic(metaData);
                mQueueManager.refreshQueue();
                updatePlaybackState(null, MiYueConstans.CUSTOM_ACTION_DOWNLOAD_SUCCESS);
            } else if(MiYueConstans.CUSTOM_ACTION_DELETE_CMD.equals(action)){
                String mediaID = extras.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
                mMusicProvider.deleteMusic(mediaID);
                mQueueManager.refreshQueue();
                updatePlaybackState(null, MiYueConstans.CUSTOM_ACTION_DELETE_CMD);
            }else{
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


//********好多回调****************************************************************//

    @Override
    public void onCompletion() {
        UtilLog.e(TAG, "播完一曲,然后skipToNext");
        mQueueManager.switchtoLocal();
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
        updatePlaybackState(null, null);
    }

    @Override
    public void onError(String error) {
        UtilLog.e(TAG, "error: " + error);
        updatePlaybackState(error, null);
    }




    public interface MusicServiceCallback {

        void onPlaybackStart();

        void onNotificationRequired();

        void onPlaybackStop();

        void onPlaybackStateUpdated(PlaybackStateCompat newState);
    }
}
