package com.miyue.ui.fragment.my;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.miyue.R;
import com.miyue.common.base.BaseMediaFragment;
import com.miyue.ui.adapter.BrowseAdapter;
import com.miyue.utils.UtilLog;
import com.miyue.widgets.MorePopupWindow;

import java.util.List;

/**
 * Created by zhangzhendong on 16/4/12.
 */
public class LocalMusicFragment extends BaseMediaFragment implements AdapterView.OnItemClickListener{

    private static final String TAG = "LocalMusicFragment";

    public static final String FROM_LOCAL_MUSIC = "FROM_LOCAL_MUSIC";

    private CallBack callBack;
    private ImageButton ib_my_back;
    private View rootView;
    private ListView lv_my_music_list;
    private ImageView iv_my_more;
    private MorePopupWindow morePop;

    private String whichList;

    private BrowseAdapter mBrowseAdapter;

    private Activity mContext;

    public static LocalMusicFragment newInstance(String whichList) {
        LocalMusicFragment localMusicFragment = new LocalMusicFragment();
        Bundle bundle = new Bundle();
        bundle.putString(FROM_LOCAL_MUSIC, whichList);
        localMusicFragment.setArguments(bundle);
        return localMusicFragment;
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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.frag_local_music,null);
        lv_my_music_list = (ListView) rootView.findViewById(R.id.lv_my_music_list);
        iv_my_more = (ImageView) rootView.findViewById(R.id.iv_my_more);
        ib_my_back = (ImageButton) rootView.findViewById(R.id.ib_my_back);

        iv_my_more.setOnClickListener(clickListener);
        ib_my_back.setOnClickListener(clickListener);
        mBrowseAdapter = new BrowseAdapter(mActivity);
        lv_my_music_list.setAdapter(mBrowseAdapter);
        lv_my_music_list.setOnItemClickListener(this);
        return  rootView;
    }


    /**onStart时候，onConnected，进行注册*/
    @Override
    public void onStop() {
        super.onStop();
        MediaBrowserCompat mediaBrowser = mActivity.getMediaBrowser();
        if (mediaBrowser != null && mediaBrowser.isConnected() && whichList != null) {
            mediaBrowser.unsubscribe(whichList);
        }
    }


    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.iv_my_more:
                    if(morePop == null){
                        morePop = new MorePopupWindow(mActivity,clickListener);
                    }
                    morePop.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
                    break;
                case R.id.ib_my_back:
                    callBack.call();
                    break;
//                case R.id.tv_scan_music:
//
//                    break;
            }


        }
    };


    public void setOnBackListener(CallBack cb){
        callBack = cb;

    }

    private void checkForUserVisibleErrors(boolean forceError) {
        boolean showError = forceError;

        // otherwise, if state is ERROR and metadata!=null, use playback state error message:
//        MediaControllerCompat controller = ((FragmentActivity) getActivity())
//                .getSupportMediaController();
//        if (controller != null
//                && controller.getMetadata() != null
//                && controller.getPlaybackState() != null
//                && controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_ERROR
//                && controller.getPlaybackState().getErrorMessage() != null) {
//            mErrorMessage.setText(controller.getPlaybackState().getErrorMessage());
//            showError = true;
//        } else if (forceError) {
//            // Finally, if the caller requested to show error, show a generic message:
//            mErrorMessage.setText(R.string.error_loading_media);
//            showError = true;
//        }
//
//        mErrorView.setVisibility(showError ? View.VISIBLE : View.GONE);
    }

    public String getMediaId() {
        Bundle args = getArguments();
        if (args != null) {
            return args.getString(FROM_LOCAL_MUSIC);
        }
        return null;
    }
////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////
//实现的回调

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        boolean b = mActivity.getMediaBrowser().isConnected();
        UtilLog.e(TAG, String.valueOf(b));
        MediaBrowserCompat.MediaItem item =
                (MediaBrowserCompat.MediaItem)mBrowseAdapter.getItem(position);
        mMediaControllerCompat.getTransportControls()
                .playFromMediaId(item.getMediaId(), null);
    }
    @Override
    public void onPlaybackStateChangedForClien(@NonNull PlaybackStateCompat state) {
        checkForUserVisibleErrors(false);
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
        whichList = getMediaId();

        // Unsubscribing before subscribing is required if this mediaId already has a subscriber
        // on this MediaBrowser instance. Subscribing to an already subscribed mediaId will replace
        // the callback, but won't trigger the initial callback.onChildrenLoaded.
        //
        // This is temporary: A bug is being fixed that will make subscribe
        // consistently call onChildrenLoaded initially, no matter if it is replacing an existing
        // subscriber or not. Currently this only happens if the mediaID has no previous
        // subscriber or if the media content changes on the service side, so we need to
        // unsubscribe first.
        //在
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

/////////////////////////////////////////////////////////////////////
    public  interface CallBack{
         void call();
    }
}
