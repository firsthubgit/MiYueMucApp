package com.miyue.ui.fragment.main;


import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.AppCompatSeekBar;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.miyue.application.MiYueConstans;
import com.miyue.R;
import com.miyue.common.base.BaseMediaFragment;
import com.miyue.utils.UtilLog;
import com.miyue.widgets.RoundImageView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhangzhendong on 16/3/31.
 */
public class PlayFragment extends BaseMediaFragment {

    private static final String TAG = "PlayFragment";
    private static final long PROGRESS_UPDATE_INTERNAL = 1000;
    private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;
    private ObjectAnimator mRotateAnim;


    private TextView tv_music_start_time,tv_music_end_time;
    private AppCompatSeekBar aps_seekbar;
    private ImageView iv_play_mode;
    private ImageView iv_previous_music;
    private ImageView iv_next_music;
    private ImageView iv_play_menu;
    private ImageView iv_pause_music;
    private ImageView iv_play_music;
    private TextView tv_track_title, tv_track_subtitle;
    private RoundImageView riv_track_pic;

    private boolean isFirstRunAnim = true;
    private int mMode = MiYueConstans.MODE_ORDER;
    private final ScheduledExecutorService mExecutorService =
        Executors.newSingleThreadScheduledExecutor();

    private ScheduledFuture<?> mScheduleFuture;
    private final Handler mHandler = new Handler();
    private final Runnable mUpdateProgressTask = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };
    private PlaybackStateCompat mLastPlaybackState;

    private SeekBarChangeListener mSeekBarChangeListener;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_playing,null);
        tv_track_title = (TextView)view.findViewById(R.id.tv_track_title);
        tv_track_subtitle = (TextView)view.findViewById(R.id.tv_track_subtitle);
        tv_music_start_time = (TextView) view.findViewById(R.id.tv_music_start_time);
        tv_music_end_time = (TextView) view.findViewById(R.id.tv_music_end_time);
        aps_seekbar = (AppCompatSeekBar) view.findViewById(R.id.aps_seekbar);
        iv_play_mode = (ImageView) view.findViewById(R.id.iv_play_mode);
        iv_previous_music = (ImageView) view.findViewById(R.id.iv_previous_music);
        iv_pause_music = (ImageView) view.findViewById(R.id.iv_pause_music);
        iv_play_music = (ImageView)view.findViewById(R.id.iv_play_music);
        iv_next_music = (ImageView) view.findViewById(R.id.iv_next_music);
        iv_play_menu = (ImageView) view.findViewById(R.id.iv_play_menu);
        riv_track_pic = (RoundImageView) view.findViewById(R.id.riv_track_pic);
        initListener();
        createRotationAnim();
        return view;
    }
    private void initListener(){
        iv_play_mode.setOnClickListener(listener);
        iv_previous_music.setOnClickListener(listener);
        iv_pause_music.setOnClickListener(listener);
        iv_play_music.setOnClickListener(listener);
        iv_next_music.setOnClickListener(listener);
        iv_play_menu.setOnClickListener(listener);
        aps_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_music_start_time.setText(DateUtils.formatElapsedTime(progress / 1000));
                mSeekBarChangeListener.onSeekBarChanged(progress, fromUser);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopSeekbarUpdate();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mMediaControllerCompat.getTransportControls().seekTo(seekBar.getProgress());
                scheduleSeekbarUpdate();
            }
        });

    }
    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.iv_play_mode:
                    switchMode(mMode, mMediaControllerCompat);
                    break;
                case R.id.iv_previous_music:
                    mMediaControllerCompat.getTransportControls().skipToPrevious();;
                    riv_track_pic.setImageResource(R.drawable.roundimageview2);
                    break;
                case R.id.iv_next_music:
                    mMediaControllerCompat.getTransportControls().skipToNext();
                    riv_track_pic.setImageResource(R.drawable.roundimage);
                    break;
                case R.id.iv_play_music:
//                    PlaybackStateCompat state = mMediaControllerCompat.getPlaybackState();
//                    if( state.getState() == PlaybackStateCompat.STATE_PAUSED
//                            || state.getState() == PlaybackStateCompat.STATE_BUFFERING){
                        stopSeekbarUpdate();
                        iv_play_music.setVisibility(View.GONE);
                        iv_pause_music.setVisibility(View.VISIBLE);
                        mMediaControllerCompat.getTransportControls().play();
//                    }
                    break;
                case R.id.iv_pause_music:
                    PlaybackStateCompat state2 = mMediaControllerCompat.getPlaybackState();
                    if(state2.getState() == PlaybackStateCompat.STATE_PLAYING
                            || state2.getState() == PlaybackStateCompat.STATE_BUFFERING){
                        scheduleSeekbarUpdate();
                        iv_play_music.setVisibility(View.VISIBLE);
                        iv_pause_music.setVisibility(View.GONE);
                        mMediaControllerCompat.getTransportControls().pause();
                    }
                    break;
                case R.id.iv_play_menu:
                    mActivity.showText("敬请期待");
                    break;
            }
        }
    };


    public void switchMode(int mode, MediaControllerCompat controller){
        switch (mode){
            case MiYueConstans.MODE_ORDER:
                mMode = MiYueConstans.MODE_LOOPER;
                controller.getTransportControls().setRepeatMode(MiYueConstans.MODE_LOOPER);
                iv_play_mode.setImageResource(R.mipmap.modecircle_normal);
                mActivity.showText("循环播放");
                break;
            case MiYueConstans.MODE_LOOPER:
                mMode = MiYueConstans.MODE_RANDOM;
                controller.getTransportControls().setRepeatMode(MiYueConstans.MODE_RANDOM);
                iv_play_mode.setImageResource(R.mipmap.moderandom_normal);
                mActivity.showText("随机播放");
                break;
            case MiYueConstans.MODE_RANDOM:
                mMode = MiYueConstans.MODE_SINGLE;
                controller.getTransportControls().setRepeatMode(MiYueConstans.MODE_SINGLE);
                iv_play_mode.setImageResource(R.mipmap.modesingle_normal);
                mActivity.showText("单曲循环");
                break;
            case MiYueConstans.MODE_SINGLE:
                mMode = MiYueConstans.MODE_ORDER;
                controller.getTransportControls().setRepeatMode(MiYueConstans.MODE_ORDER);
                iv_play_mode.setImageResource(R.mipmap.modeorder_normal);
                mActivity.showText("顺序播放");
                break;
        }
    }


    /**创建Fragment初始化的时候对UI更新*/
    private void updateUI(){
        PlaybackStateCompat state = mMediaControllerCompat.getPlaybackState();
        updatePlaybackState(state);
        MediaMetadataCompat metadata = mMediaControllerCompat.getMetadata();
        if (metadata != null) {
            updateMediaDescription(metadata.getDescription());
            updateDuration(metadata);
        }
        updateProgress();
        if (state != null && (state.getState() == PlaybackStateCompat.STATE_PLAYING ||
                state.getState() == PlaybackStateCompat.STATE_BUFFERING)) {
            scheduleSeekbarUpdate();
        }
    }

    private void updateMediaDescription(MediaDescriptionCompat description) {
        if (description == null) {
            return;
        }
       //更新title
        tv_track_title.setText(description.getTitle());
        tv_track_subtitle.setText(description.getSubtitle());
    }

    /**更新初始化时长*/
    private void updateDuration(MediaMetadataCompat metadata) {
        if (metadata == null) {
            return;
        }
        int duration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        aps_seekbar.setMax(duration);
        aps_seekbar.setProgress(0);
        tv_music_end_time.setText(DateUtils.formatElapsedTime(duration/1000));
    }
    private void updatePlaybackState(PlaybackStateCompat state){
        if (state == null) {
            return;
        }
        mLastPlaybackState = state;
        switch (state.getState()) {
            case PlaybackStateCompat.STATE_PLAYING:
                iv_pause_music.setVisibility(View.VISIBLE);
                iv_play_music.setVisibility(View.GONE);
                if(isFirstRunAnim){
                    mRotateAnim.start();
                    isFirstRunAnim = false;
                }else{
                    mRotateAnim.resume();
                }
                scheduleSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                mRotateAnim.pause();
                iv_play_music.setVisibility(View.VISIBLE);
                iv_pause_music.setVisibility(View.GONE);
                stopSeekbarUpdate();
                mRotateAnim.pause();
                break;
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_STOPPED:
                iv_play_music.setVisibility(View.VISIBLE);
                iv_pause_music.setVisibility(View.GONE);
                stopSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                iv_play_music.setVisibility(View.VISIBLE);
                iv_pause_music.setVisibility(View.GONE);
                stopSeekbarUpdate();
                break;
            default:
                UtilLog.e(TAG, "Unhandled state " + state.getState());
        }
    }

    private void scheduleSeekbarUpdate() {
        stopSeekbarUpdate();
        if (!mExecutorService.isShutdown()) {
            mScheduleFuture = mExecutorService.scheduleAtFixedRate(
                    new Runnable() {
                        @Override
                        public void run() {
                            mHandler.post(mUpdateProgressTask);
                        }
                    }, PROGRESS_UPDATE_INITIAL_INTERVAL,
                    PROGRESS_UPDATE_INTERNAL, TimeUnit.MILLISECONDS);
        }
    }

    private void stopSeekbarUpdate() {
        if (mScheduleFuture != null) {
            mScheduleFuture.cancel(false);
        }
    }

    /**更新进度条*/
    private void updateProgress() {
        if (mLastPlaybackState == null) {
            return;
        }
        long currentPosition = mLastPlaybackState.getPosition();
        if (mLastPlaybackState.getState() != PlaybackStateCompat.STATE_PAUSED) {
            //用（当前时间减去发来state的时间）乘以（速度）计算播放进度，
            //这样做不用多次调用getPlaybackState()
            long timeDelta = SystemClock.elapsedRealtime() -
                    mLastPlaybackState.getLastPositionUpdateTime();
            currentPosition += (int) timeDelta * mLastPlaybackState.getPlaybackSpeed();
            tv_music_start_time.setText(DateUtils.formatElapsedTime(currentPosition/1000));
        }
        aps_seekbar.setProgress((int) currentPosition);
    }


    public void registCallback(){
        registBaseCallback();
        updateUI();
    }

    private void createRotationAnim(){
        mRotateAnim = ObjectAnimator.ofFloat(riv_track_pic, "rotation", new float[]{0.0F, 360F});
        mRotateAnim.setRepeatCount(ValueAnimator.INFINITE);
        mRotateAnim.setDuration(20000L);
        mRotateAnim.setInterpolator(new LinearInterpolator());
    }

    @Override
    public void onPause() {
        super.onPause();
        mRotateAnim.pause();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(mRotateAnim.isStarted() && mRotateAnim.isPaused()
                && mLastPlaybackState.getState()==PlaybackStateCompat.STATE_PLAYING){
            mRotateAnim.resume();
        }
    }

    //////////////////////////////////////////////////////////
// 实现BaseFragment里抽象方法
    @Override
    public void onPlaybackStateChangedForClien(@NonNull PlaybackStateCompat state) {
        updatePlaybackState(state);
    }

    @Override
    public void onMetadataChangedForClien(MediaMetadataCompat metadata) {
        if (metadata != null) {
            updateMediaDescription(metadata.getDescription());
            updateDuration(metadata);
        }
    }

    @Override
    public void onConnectedForClien() {
        //四大主Fragment可能不会走这里的
        UtilLog.e(TAG, "启动时候连接上了");
    }

    public void setSeekBarChangeListener(SeekBarChangeListener sbclistener){
        mSeekBarChangeListener = sbclistener;
    }
    interface SeekBarChangeListener{
        void onSeekBarChanged(int progress, boolean fromUser);
    }

}
