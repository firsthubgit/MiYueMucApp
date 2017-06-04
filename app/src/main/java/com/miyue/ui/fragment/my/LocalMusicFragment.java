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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.greendao.DBHelper;
import com.miyue.R;
import com.miyue.application.MiYueConstans;
import com.miyue.common.base.BaseMediaFragment;
import com.miyue.common.listener.CallBack;
import com.miyue.ui.adapter.BrowseAdapter;
import com.miyue.utils.FileUtils;
import com.miyue.utils.UtilLog;
import com.miyue.widgets.MorePopupWindow;

import java.util.List;

/**
*
 * 重复的Fragment业务逻辑没有封装到另一个fragment中，
 * 怕以后可能会要改动
* @author ZZD
* @time 17/6/4 下午8:20
*/
public class LocalMusicFragment extends BaseMediaFragment implements
        AdapterView.OnItemClickListener, BrowseAdapter.OnMoreClickListener{

    public static final String TAG = "LocalMusicFragment";

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

    private String mPopMediaID;

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
        mBrowseAdapter.setOnMoreClickListener(this);
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
                    break;
            }


        }
    };

    @Override
    public void onMoreClick(String popMediaID) {
        mPopMediaID = popMediaID;

        boolean isFavorite = mDBHelper.isFavoriteMusic(popMediaID.split(":")[1]);
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

    public String getTableName() {
        Bundle args = getArguments();
        if (args != null) {
            return args.getString(FROM_LOCAL_MUSIC);
        }
        return null;
    }


//********实现的回调*****************************************************************///

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        boolean b = mActivity.getMediaBrowser().isConnected();
        UtilLog.e(TAG, String.valueOf(b));
        MediaBrowserCompat.MediaItem item =
                (MediaBrowserCompat.MediaItem)mBrowseAdapter.getItem(position);
        String path = item.getDescription().getMediaUri().toString();
        if(!FileUtils.isFileExist(path)){
            mActivity.showText("本地文件不存在，请手动删除");
            return;
        }
        mMediaControllerCompat.getTransportControls()
                .playFromMediaId(item.getMediaId(), null);
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


}
