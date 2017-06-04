package com.miyue.ui.fragment.my;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.miyue.R;
import com.miyue.application.MiYueConstans;
import com.miyue.common.base.BaseMediaFragment;
import com.miyue.common.listener.CallBack;
import com.miyue.ui.adapter.BrowseAdapter;
import com.miyue.utils.UtilLog;
import com.miyue.widgets.MorePopupWindow;

import java.util.List;

/**
*
* @author ZZD
* @time  17/6/3 下午12:50
*/


public class DownloadFragment extends BaseMediaFragment implements BrowseAdapter.OnMoreClickListener{

    private static final String TAG = "DownloadFragment";
    private static final String FROM_DOWNLOAD_MUSIC = "FROM_DOWNLOAD_MUSIC";


    private View rootView;
    private ListView lv_my_music_list;
    private ImageButton ib_my_back;

    private Activity mContext;

    private CallBack callBack;

    private String whichList;

    private BrowseAdapter mBrowseAdapter;

    private String mPopMediaID;
    private MorePopupWindow morePop;

    public static DownloadFragment newInstance(String whichList) {
        DownloadFragment downloadFragment = new DownloadFragment();
        Bundle bundle = new Bundle();
        bundle.putString(FROM_DOWNLOAD_MUSIC, whichList);
        downloadFragment.setArguments(bundle);
        return downloadFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = (Activity)context;
        //初始化必须要设置cntoller和监听
        setMediaController(MediaControllerCompat.getMediaController(mContext));
        registBaseCallback();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.frag_down_music,null);
        lv_my_music_list = (ListView) rootView.findViewById(R.id.lv_my_music_list);
        ib_my_back = (ImageButton) rootView.findViewById(R.id.ib_my_back);
        mBrowseAdapter = new BrowseAdapter(mActivity);
        mBrowseAdapter.setOnMoreClickListener(this);
        lv_my_music_list.setAdapter(mBrowseAdapter);
        initListener();
        return rootView;
    }

    private void initListener() {
        ib_my_back.setOnClickListener(clickListener);
        lv_my_music_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean b = mActivity.getMediaBrowser().isConnected();
                UtilLog.e(TAG, String.valueOf(b));
                MediaBrowserCompat.MediaItem item =
                        (MediaBrowserCompat.MediaItem)mBrowseAdapter.getItem(position);
                mMediaControllerCompat.getTransportControls()
                        .playFromMediaId(item.getMediaId(), null);
            }
        });
    }

    View.OnClickListener clickListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            Bundle bundle = null;
            switch (v.getId()){
                case R.id.ib_my_back:
                    callBack.call();
                    break;
                case R.id.iv_like_or_unlike:
                    bundle = new Bundle();
                    bundle.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mPopMediaID);
                    mMediaControllerCompat.getTransportControls()
                            .sendCustomAction(MiYueConstans.CUSTOM_ACTION_THUMBS_UP, bundle);                    morePop.dismiss();
                    break;
                case R.id.iv_delete_music:
                    bundle = new Bundle();
                    bundle.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mPopMediaID);
                    mMediaControllerCompat.getTransportControls()
                            .sendCustomAction(MiYueConstans.CUSTOM_ACTION_DELETE_CMD, bundle);
                    morePop.dismiss();
                default:
                    break;
            }
        }
    };

    /**onStart时候，onConnected，进行注册*/
    @Override
    public void onStop() {
        super.onStop();
        MediaBrowserCompat mediaBrowser = mActivity.getMediaBrowser();
        if (mediaBrowser != null && mediaBrowser.isConnected() && whichList != null) {
            mediaBrowser.unsubscribe(whichList);
        }
    }

    public void setOnBackListener(CallBack cb){
        callBack = cb;
    }

    public String getTableName() {
        Bundle args = getArguments();
        if (args != null) {
            return args.getString(FROM_DOWNLOAD_MUSIC);
        }
        return null;
    }

    private void checkForUserVisibleErrors(boolean forceError) {
        boolean showError = forceError;
        UtilLog.e(TAG, "DownloadFragment出错了：" + forceError);
    }

    private void dealAction(List<PlaybackStateCompat.CustomAction> actions) {

        for(PlaybackStateCompat.CustomAction customAction : actions){
            String command = customAction.getAction();
            switch (command){
                case MiYueConstans.CUSTOM_ACTION_DELETE_CMD:
                    mActivity.showText("删除成功");
                    mActivity.getMediaBrowser().unsubscribe(whichList);
                    mActivity.getMediaBrowser().subscribe(whichList, mSubscriptionCallback);
                    break;
                case MiYueConstans.CUSTOM_ACTION_DOWNLOAD_SUCCESS:
                    mActivity.getMediaBrowser().unsubscribe(whichList);
                    mActivity.getMediaBrowser().subscribe(whichList, mSubscriptionCallback);
                    break;
            }
        }
    }

    @Override
    public void onPlaybackStateChangedForClien(@NonNull PlaybackStateCompat state) {
        checkForUserVisibleErrors(false);
        List<PlaybackStateCompat.CustomAction> actions= state.getCustomActions();
        if(actions != null && actions.size()>0){
            dealAction(actions);
        }
        mBrowseAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMetadataChangedForClien(MediaMetadataCompat metadata) {
        mBrowseAdapter.notifyDataSetChanged();
    }

    @Override
    public void onConnectedForClien() {
        if (isDetached()) {
            return;
        }
        whichList = getTableName();
        mActivity.getMediaBrowser().unsubscribe(whichList);
        mActivity.getMediaBrowser().subscribe(whichList, mSubscriptionCallback);
    }

    private final MediaBrowserCompat.SubscriptionCallback mSubscriptionCallback =
            new MediaBrowserCompat.SubscriptionCallback() {
                @Override
                public void onChildrenLoaded(@NonNull String whichList,
                                             @NonNull List<MediaBrowserCompat.MediaItem> children) {
                    try {
                        checkForUserVisibleErrors(children.isEmpty());
                        mBrowseAdapter.clear();
                        for (MediaBrowserCompat.MediaItem item : children) {
                            mBrowseAdapter.add(item);
                        }
                        mBrowseAdapter.notifyDataSetChanged();
                    } catch (Throwable t) {
                        t.printStackTrace();
                        UtilLog.e(TAG, "Error on childrenloaded" + t);
                    }
                }

                @Override
                public void onError(@NonNull String id) {
                    Toast.makeText(getActivity(), id, Toast.LENGTH_SHORT).show();
                    checkForUserVisibleErrors(true);
                }
            };

    @Override
    public void onMoreClick(String mediaID) {
        mPopMediaID = mediaID;

        boolean isFavorite = mDBHelper.isFavoriteMusic(mediaID.split(":")[1]);
        if(morePop == null){
            morePop = new MorePopupWindow(mActivity,clickListener);
        }
        if(isFavorite){
            morePop.setFavoriteSrc(R.drawable.selector_unlike_music);
        }else{
            morePop.setFavoriteSrc(R.drawable.selector_like_music);
        }
        morePop.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
    }
}
