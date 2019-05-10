package com.miyue.ui.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.miyue.R;
import com.miyue.utils.FileUtils;
import com.miyue.utils.UtilLog;

import java.util.ArrayList;
import java.util.Collection;



/**
*
* @author ZZD
* @time  17/5/21 上午10:35
*/
public class BrowseAdapter extends BaseAdapter {

    private static String TAG = "BrowseAdapter";

    public static final int STATE_INVALID = -1;
    public static final int STATE_NONE = 0;
    public static final int STATE_PLAYABLE = 1;
    public static final int STATE_PAUSED = 2;
    public static final int STATE_PLAYING = 3;
    public static final int STATE_UNPLAYABLE = 4;


    private ArrayList<MediaBrowserCompat.MediaItem> musicList = new ArrayList<>();

    private final Object mLock = new Object();

    private Activity mActivity;
    private Holder holder;

    private MediaControllerCompat mMediaControllerCompat;

    private OnMoreClickListener listener;

    public BrowseAdapter(Activity context) {
        mActivity = context;
        mMediaControllerCompat = MediaControllerCompat.getMediaController(context);
    }

    @Override
    public int getCount() {
        return musicList.size();
    }

    @Override
    public MediaBrowserCompat.MediaItem getItem(int position) {
        return musicList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Integer cachedState = STATE_INVALID;
        final MediaDescriptionCompat description = musicList.get(position).getDescription();
        if(null == convertView){
            holder = new Holder();
            convertView = LayoutInflater.from(mActivity)
                    .inflate(R.layout.item_musiclist, parent, false);
            holder.tv_music_order = (TextView) convertView.findViewById(R.id.tv_music_order);
            holder.tv_music_name = (TextView) convertView.findViewById(R.id.tv_music_name);
            holder.tv_artist = (TextView) convertView.findViewById(R.id.tv_music_singer);
            holder.iv_list_icon = (ImageView) convertView.findViewById(R.id.iv_list_icon);

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
            cachedState = (Integer) convertView.getTag(R.id.tag_mediaitem_state_cache);
        }

        holder.iv_list_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaBrowserCompat.MediaItem item = getItem(position);
                listener.onMoreClick(item.getMediaId());
            }
        });



        holder.tv_music_name.setText(description.getTitle());
        holder.tv_artist.setText(description.getSubtitle() + " - " + description.getDescription());
        //根据文件是否存在设置颜色
        if(FileUtils.isFileExist(description.getMediaUri().toString())){
            //根据播放状态设置字体颜色
            int state = getMediaItemState(musicList.get(position));
            if (cachedState == null || cachedState != state) {
                int color = getColorByState(state);
                holder.tv_music_name.setTextColor(color);
                holder.tv_artist.setTextColor(ContextCompat.getColor(mActivity,R.color.my_white_alpha_60));
                convertView.setTag(R.id.tag_mediaitem_state_cache, state);
            }
        } else {//文件不存在
            int grayColorId = ContextCompat.getColor(mActivity,R.color.my_gray_10);
            holder.tv_music_name.setTextColor(grayColorId);
            holder.tv_artist.setTextColor(grayColorId);
            convertView.setTag(R.id.tag_mediaitem_state_cache, STATE_UNPLAYABLE);
        }

        if(position<10){
            holder.tv_music_order.setText("0"+position+".");
        } else {
            holder.tv_music_order.setText(position+".");
        }


        return convertView;
    }


    public void add(MediaBrowserCompat.MediaItem object) {
        synchronized (mLock) {
            if (musicList != null) {
                musicList.add(object);
            } else {
                UtilLog.e(TAG, "ArrayList为空了");
            }
        }
    }

    public void addAll(Collection<MediaBrowserCompat.MediaItem> collection) {
        synchronized (mLock) {
            if (musicList != null) {
                musicList.addAll(collection);
            } else {
                UtilLog.e(TAG, "ArrayList为空了");
            }
        }
    }

    public void clear(){
        synchronized (mLock) {
            if (musicList != null) {
                musicList.clear();
            } else {
                UtilLog.e(TAG, "ArrayList为空了");
            }
        }
    }

    public void remove(MediaBrowserCompat.MediaItem object) {
        synchronized (mLock) {
            if (musicList != null) {
                musicList.remove(object);
            } else {
                UtilLog.e(TAG, "ArrayList为空了");
            }
        }
    }
    public class Holder{
        TextView tv_music_order;
        TextView tv_music_name;
        TextView tv_artist;
        ImageView iv_list_icon;
    }


    public int getColorByState(int state) {

        switch (state) {
            case STATE_PLAYING:
                return ContextCompat.getColor(mActivity,R.color.my_orange);
            case STATE_PLAYABLE:
            case STATE_PAUSED:
            default:
                return ContextCompat.getColor(mActivity,R.color.my_common_white);
        }
    }

    public int getMediaItemState(MediaBrowserCompat.MediaItem mediaItem) {
        int state = STATE_NONE;
        if (mediaItem.isPlayable()) {
            state = STATE_PLAYABLE;
            if (isMediaItemPlaying(mediaItem)) {
                state = STATE_PLAYING;
            }
        }
        return state;
    }

//    public int getStateFromController() {
//        PlaybackStateCompat pbState = mMediaControllerCompat.getPlaybackState();
//        if (pbState == null ||
//                pbState.getState() == PlaybackStateCompat.STATE_ERROR) {
//            return STATE_NONE;
//        } else if (pbState.getState() == PlaybackStateCompat.STATE_PLAYING) {
//            return STATE_PLAYING;
//        } else {
//            return STATE_PAUSED;
//        }
//    }


    public boolean isMediaItemPlaying(MediaBrowserCompat.MediaItem mediaItem) {
        // 通过controller获取当前播放的MediaMetadate，与Item比较
        if (mMediaControllerCompat != null && mMediaControllerCompat.getMetadata() != null) {
            String currentPlayingMediaId = mMediaControllerCompat.getMetadata().getDescription()
                    .getMediaId();
            String itemMusicId =
                    mediaItem.getDescription().getMediaId();
            if (currentPlayingMediaId != null
                    && TextUtils.equals(currentPlayingMediaId, itemMusicId)) {
                return true;
            }
        }
        return false;
    }

    public interface OnMoreClickListener{
        void onMoreClick(String mediaID);
    }

    public void setOnMoreClickListener(OnMoreClickListener listener){
        this.listener = listener;
    }

}
