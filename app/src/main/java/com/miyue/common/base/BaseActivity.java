package com.miyue.common.base;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.miyue.service.PlayerService;
import com.miyue.ui.activity.MainActivity;
import com.miyue.utils.UtilLog;

/**
 * Created by zhangzhendong on 17/5/21.
 */

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";

    private MediaBrowserCompat mMediaBrowser;

    private Toast mToast;

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    UtilLog.e(TAG, "onConnected");
                    try {
                        connectToSession(mMediaBrowser.getSessionToken());
                    } catch (RemoteException e) {
                        UtilLog.e(TAG, "could not connect media controller");
                    }
                }

                @Override
                public void onConnectionFailed() {
                    UtilLog.e(TAG, "onConnectedFailed");
                }
            };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToast = Toast.makeText(BaseActivity.this, "", Toast.LENGTH_SHORT);
        mMediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, PlayerService.class), mConnectionCallback, null);
        mMediaBrowser.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaBrowser.disconnect();
    }

    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {
        MediaControllerCompat mediaController = new MediaControllerCompat(this, token);
        MediaControllerCompat.setMediaController(this, mediaController);
        onMediaControllerConnected(mediaController);
    }

    protected void onMediaControllerConnected(MediaControllerCompat mediaController) {
    }

    public MediaBrowserCompat getMediaBrowser(){
        return mMediaBrowser;
    }

    public void showText(String text){
        mToast.setText(text);
        LinearLayout ll = (LinearLayout) mToast.getView();
        TextView tv = (TextView) ll.getChildAt(0);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        mToast.show();
    }

}
