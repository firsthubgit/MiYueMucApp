package com.miyue.common.base;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.greendao.DBHelper;

/**
 * Created by zhangzhendong on 17/5/21.
 */

public abstract class BaseMediaFragment extends BaseFragment {

    private static final String TAG = "BaseMediaFragment";

    protected MediaControllerCompat mMediaControllerCompat;

    protected BaseActivity mActivity;

    protected DBHelper  mDBHelper;


    private final MediaControllerCompat.Callback mMediaControllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
                    onPlaybackStateChangedForClien(state);
                }

                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    onMetadataChangedForClien(metadata);
                }
            };


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (BaseActivity) context;
        mDBHelper = DBHelper.getInstance(context);
    }

    /*SDK API<23时，onAttach(Context)不执行，需要使用onAttach(Activity)。Fragment自身的Bug，v4的没有此问题*/
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mActivity = (BaseActivity) getActivity();
            mDBHelper = DBHelper.getInstance(activity);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        MediaBrowserCompat mediaBrowser;
        if(mActivity != null){
             mediaBrowser = mActivity.getMediaBrowser();
        } else {
            mActivity = (BaseActivity) getActivity();
            mediaBrowser = mActivity.getMediaBrowser();
        }
        if (mediaBrowser.isConnected()) {
            onConnectedForClien();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(mMediaControllerCompat != null){
            mMediaControllerCompat.unregisterCallback(mMediaControllerCallback);
        }
        mActivity = null;
    }

    public void setMediaController(MediaControllerCompat troller){
        mMediaControllerCompat = troller;
    }

    protected void registBaseCallback(){
        if(mMediaControllerCompat != null) {
            mMediaControllerCompat.registerCallback(mMediaControllerCallback);
        }
    }

    public abstract void onPlaybackStateChangedForClien(@NonNull PlaybackStateCompat state);
    public abstract void onMetadataChangedForClien(MediaMetadataCompat metadata);
    public abstract void onConnectedForClien();
}
