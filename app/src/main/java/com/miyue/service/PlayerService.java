package com.miyue.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.miyue.R;
import com.miyue.application.MiYueConstans;
import com.miyue.service.playback.MusicProvider;
import com.miyue.notification.MediaNotificationManager;
import com.miyue.service.playback.PlayerController;
import com.miyue.service.playback.QueueManager;
import com.miyue.service.playback.RemoteMusicPlayer;
import com.miyue.ui.activity.MainActivity;
import com.miyue.utils.UtilLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by zhangzhendong on 17/5/19.
 */

public class PlayerService extends MediaBrowserServiceCompat implements
        PlayerController.MusicServiceCallback{

    public static final String TAG = "PlayerService";

    // 延迟30秒停止Service
    private static final int STOP_DELAY = 30000;

    private MusicProvider mMusicProvider;
    private PlayerController mPlayerController;

    private MediaSessionCompat mSession;
    private final DelayedStopHandler mDelayedStopHandler = new DelayedStopHandler(this);

    private MediaNotificationManager mMediaNotificationManager;

    private QueueManager mQueueManager;
    @Override
    public void onCreate() {
        super.onCreate();
        UtilLog.e(TAG, "onCreate");
        mMusicProvider = new MusicProvider();
        mQueueManager = new QueueManager(mMusicProvider, getResources(),
                new QueueManager.MetadataUpdateListener() {
                    @Override
                    public void onMetadataChanged(MediaMetadataCompat metadata) {
                        mSession.setMetadata(metadata);
                    }

                    @Override
                    public void onMetadataRetrieveError() {
                        mPlayerController.updatePlaybackState("扫描数据没有成功", null);
                    }

                    @Override
                    public void onCurrentQueueIndexUpdated(int queueIndex) {
                        mPlayerController.handlePlayRequest();
                    }

                    @Override
                    public void onQueueUpdated(String title,
                                               List<MediaSessionCompat.QueueItem> newQueue) {
                        mSession.setQueue(newQueue);
                        mSession.setQueueTitle(title);
                    }
                });


        RemoteMusicPlayer musicPlayer = new RemoteMusicPlayer(this, mMusicProvider);
        mPlayerController = new PlayerController(this,mMusicProvider, mQueueManager,
                musicPlayer);

        // 创建一个session
        mSession = new MediaSessionCompat(this, "MusicService");
        setSessionToken(mSession.getSessionToken());
        mSession.setCallback(mPlayerController.getMediaSessionCallback());
        //处理耳机按键等事件
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        Context context = getApplicationContext();
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 99 /*request code*/,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mSession.setSessionActivity(pi);

        mPlayerController.updatePlaybackState(null, null);

        try {
            mMediaNotificationManager = new MediaNotificationManager(this);
        } catch (RemoteException e) {
            throw new IllegalStateException("Could not create a MediaNotificationManager", e);
        }
    }


    /**
     * (non-Javadoc)
     * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
     */
    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {

        if (startIntent != null) {
            String action = startIntent.getAction();
            String command = startIntent.getStringExtra(MiYueConstans.CMD_NAME);
            if (MiYueConstans.ACTION_CMD.equals(action)) {
                if (MiYueConstans.CMD_PAUSE.equals(command)) {
                    mPlayerController.handlePauseRequest();
                }
            } else {
                // Try to handle the intent as a media button event wrapped by MediaButtonReceiver
                MediaButtonReceiver.handleIntent(mSession, startIntent);
            }
        }
        // Reset the delay handler to enqueue a message to stop the service if
        // nothing is playing.
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        UtilLog.e(TAG, "onDestroy");
        // 服务被干掉后，释放掉所有资源
        mPlayerController.handleStopRequest(null);
        mMediaNotificationManager.stopNotification();
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mSession.release();
    }



    private static class DelayedStopHandler extends Handler {
        private final WeakReference<PlayerService> mWeakReference;

        private DelayedStopHandler(PlayerService service) {
            mWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            PlayerService service = mWeakReference.get();
            if (service != null && service.mPlayerController.getMusicPlayer() != null) {
                if (service.mPlayerController.getMusicPlayer().isPlaying()) {
                    UtilLog.e(TAG, "Ignoring delayed stop since the media player is in use.");
                    return;
                }
                UtilLog.e(TAG, "Stopping service with delay handler.");
                service.stopSelf();
            }
        }
    }


/////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////
//回调方法

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
//       如果在BaseActivity的onStop方法中进行disconnect()，这里的
//        BrowserRoot()第二个参数不能为null，
//        并且要返回父类中mHanlder持有的serviceBinder(MessengerImpl)
//        Bundle bundle = new Bundle();
//        bundle.putString("PlayerService", "可以连接");
//        IBinder binder = (IBinder) getBrowserRootHints().get("extra_messenger");//error
//        BundleCompat.putBinder(bundle, "extra_messenger", binder);

        //返回为null 表示其他的MediaBrowser不能连接，所以只有有返回就好
        return new BrowserRoot(getString(R.string.app_name), null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        UtilLog.e(TAG, "OnLoadChildren: parentMediaId=" + parentId);
        if ("".equals(parentId)) {
            result.sendResult(new ArrayList<MediaBrowserCompat.MediaItem>());
        } else if (mMusicProvider.isInitialized()) {
            result.sendResult(mMusicProvider.getMediaItemList(parentId));
        } else {
            // otherwise, only return results when the music library is retrieved
//            result.detach();
//            mMusicProvider.retrieveMediaAsync(new MusicProvider.Callback() {
//                @Override
//                public void onMusicCatalogReady(boolean success) {
//                    result.sendResult(mMusicProvider.getChildren(parentMediaId, getResources()));
//                }
//            });
        }
    }

    @Override
    public void onPlaybackStart() {
        mSession.setActive(true);
        mDelayedStopHandler.removeCallbacksAndMessages(null);

        startService(new Intent(getApplicationContext(), PlayerService.class));
    }

    @Override
    public void onNotificationRequired() {
        mMediaNotificationManager.startNotification();
    }


    @Override
    public void onPlaybackStop() {
        mSession.setActive(false);
        // Reset the delayed stop handler, so after STOP_DELAY it will be executed again,
        // potentially stopping the service.
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
//        stopForeground(true); 即使后台暂停了，我也不想销毁我的service，因为我一般
    }

    @Override
    public void onPlaybackStateUpdated(PlaybackStateCompat newState) {
        mSession.setPlaybackState(newState);
    }
}
