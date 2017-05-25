package com.miyue.ui.adapter;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.miyue.R;
import com.miyue.utils.UtilLog;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Created by zhangzhendong on 17/5/21.
 */

public class BrowseAdapter extends BaseAdapter {

    private static String TAG = "BrowseAdapter";

    public static final int STATE_INVALID = -1;
    public static final int STATE_NONE = 0;
    public static final int STATE_PLAYABLE = 1;
    public static final int STATE_PAUSED = 2;
    public static final int STATE_PLAYING = 3;


    private ArrayList<MediaBrowserCompat.MediaItem> musicList = new ArrayList<>();

    private final Object mLock = new Object();

    private Activity mActivity;
    private Holder holder;

    private MediaControllerCompat mMediaControllerCompat;

    public BrowseAdapter(Activity context) {
        mActivity = context;
        mMediaControllerCompat = MediaControllerCompat.getMediaController(context);
    }

    @Override
    public int getCount() {
        return musicList.size();
    }

    @Override
    public Object getItem(int position) {
        return musicList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Integer cachedState = STATE_INVALID;

        if(null == convertView){
            holder = new Holder();
            convertView = LayoutInflater.from(mActivity)
                    .inflate(R.layout.item_musiclist, parent, false);
            holder.tv_music_order = (TextView) convertView.findViewById(R.id.tv_music_order);
            holder.tv_music_name = (TextView) convertView.findViewById(R.id.tv_music_name);
            holder.tv_artist = (TextView) convertView.findViewById(R.id.tv_music_singer);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
            cachedState = (Integer) convertView.getTag(R.id.tag_mediaitem_state_cache);

        }
        holder.tv_music_name.setText(musicList.get(position).getDescription().getTitle());
        holder.tv_artist.setText(musicList.get(position).getDescription().getSubtitle());
        if(position<10){
            holder.tv_music_order.setText("0"+position+".");
        } else {
            holder.tv_music_order.setText(position+".");
        }

        int state = getMediaItemState(musicList.get(position));
        if (cachedState == null || cachedState != state) {
            int color = getColorByState(state);
            holder.tv_music_name.setTextColor(color);
            holder.tv_artist.setTextColor(color);
            convertView.setTag(R.id.tag_mediaitem_state_cache, state);
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
}
