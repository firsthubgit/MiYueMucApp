package com.miyue.service.playback;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.miyue.application.DbConstans;
import com.miyue.application.MiYueConstans;
import com.miyue.utils.MusicUtils;
import com.miyue.utils.StringUtils;
import com.miyue.utils.UtilLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

/**
*
* @author ZZD
* @time 17/5/20 下午1:59
*/
public class QueueManager {
    private static final String TAG = "QueueManager";

    private MusicProvider mMusicProvider;
    private MetadataUpdateListener mListener;

    /**播放顺序*/
    private int mRepeatMode = MiYueConstans.MODE_ORDER;
    /**表示正在播放的列表*/
    private List<MediaSessionCompat.QueueItem> mPlayingQueue;
    private int mCurrentIndex;

    public boolean isNetPlay = false;


    /**
     * 他的值有可能是
     *  DbConstans.LOCAL_MUSIC
     *  DbConstans.FAVORITES
     *  DbConstans.DOWNLOAD
     *  DbConstans.Recent
     *
     *  参考 {@link MusicProvider#},
     *  初始化MediaID的时候，拼接了一个DbConstans.**
     *  数据库中不带
     * */
    private String mCurrentTable = "";

    public QueueManager(@NonNull MusicProvider musicProvider,
                        @NonNull Resources resources,
                        @NonNull MetadataUpdateListener listener) {
        this.mMusicProvider = musicProvider;
        this.mListener = listener;

        mPlayingQueue = Collections.synchronizedList(new ArrayList<MediaSessionCompat.QueueItem>());
        mCurrentIndex = 0;
    }

    public void setRepeatMode(int mode){
        mRepeatMode = mode;
    }

    public void setQueueFromMusic(String mediaId) {
        UtilLog.e(TAG, "setQueueFromMusic: " + mediaId);
        String[] str = mediaId.split(":");
        if(mCurrentTable.equals(str[0])){
            mCurrentIndex = getMusicIndexOnQueue(mPlayingQueue, mediaId);
        } else {
            mCurrentTable = str[0];
            setCurrentQueue(str[0], getPlayingQueue(mediaId, mMusicProvider));
        }
        updateMetadata();
    }


    protected void setCurrentQueue(String title, List<MediaSessionCompat.QueueItem> newQueue) {
        mPlayingQueue = newQueue;
        mListener.onQueueUpdated(title, newQueue);
    }


    /**
     * 第一次启动点击播放按钮没有音乐的时候
     * 从本地音乐中随机一个音乐
     * */
    public void setRandomQueue() {
        List<MediaSessionCompat.QueueItem> randomList = getPlayingQueue(DbConstans.LOCAL_MUSIC, mMusicProvider);
        setCurrentQueue(DbConstans.LOCAL_MUSIC, randomList);
        mCurrentTable = DbConstans.LOCAL_MUSIC;
        mCurrentIndex = new Random().nextInt(randomList.size());
        updateMetadata();
    }

    private void setCurrentQueueIndex(int index) {
        if (index >= 0 && index < mPlayingQueue.size()) {
            mCurrentIndex = index;
            mListener.onCurrentQueueIndexUpdated(mCurrentIndex);
        }
    }

    public boolean setCurrentQueueItem(long queueId) {
        // set the current index on queue from the queue Id:
        int index = getMusicIndexOnQueue(mPlayingQueue, queueId);
        setCurrentQueueIndex(index);
        return index >= 0;
    }

    public boolean setCurrentQueueItem(String mediaId) {
        // set the current index on queue from the music Id:
        int index = getMusicIndexOnQueue(mPlayingQueue, mediaId);
        setCurrentQueueIndex(index);
        return index >= 0;
    }

    /**
     * 点击下一首按钮
     * */
    public boolean skipNextOperation(int from){
        if(null == mPlayingQueue || mPlayingQueue.size()==0){
            return false;
        }
        switch (mRepeatMode){
            case MiYueConstans.MODE_ORDER:
                return skipQueuePosition(1);
            case MiYueConstans.MODE_LOOPER:
                return skipQueuePosition(1);
            case MiYueConstans.MODE_RANDOM:
                Random ram = new Random();
                mCurrentIndex = ram.nextInt(mPlayingQueue.size());
                return true;
            case MiYueConstans.MODE_SINGLE://单曲循环时
                 if(from == 9){
                     //表示是自动播放完的，还用当前的index，循环播放
                     return true;
                 } else {//点击下一首时
                     return skipQueuePosition(1);
                 }
            default:
                break;
        }
        return true;
    }

    /**
     * 点击上一首按钮
     * */
    public boolean skipPreOperation(){
        switch (mRepeatMode){
            case MiYueConstans.MODE_ORDER:
                return skipQueuePosition(-1);
            case MiYueConstans.MODE_LOOPER:
                return skipQueuePosition(-1);
            case MiYueConstans.MODE_RANDOM:
                Random ram = new Random();
                mCurrentIndex = ram.nextInt(mPlayingQueue.size());
                return true;
            case MiYueConstans.MODE_SINGLE:
                return skipQueuePosition(-1);
            default:
                break;
        }
        return true;
    }

    /**使当前列表mCurrentIndex前进或后退*/
    public boolean skipQueuePosition(int amount) {
        int index = mCurrentIndex + amount;
        if (index < 0) {
            index = mPlayingQueue.size()-1;
        } else {
            index %= mPlayingQueue.size();
        }
        if (!isIndexPlayable(index, mPlayingQueue)) {
            UtilLog.e(TAG, "Cannot increment queue index by " + amount +
                    ". Current=" + mCurrentIndex + " queue length=" + mPlayingQueue.size());
            return false;
        }
        mCurrentIndex = index;
        return true;
    }

    public MediaMetadataCompat getCurretNetMedia(){
        return mMusicProvider.getCurernNetMeta();
    }

    public MediaSessionCompat.QueueItem getCurrentMusic() {
        if(isNetPlay){
            return  MusicUtils.metadataToQueueItem(getCurretNetMedia(), 0);
        }
        if (!isIndexPlayable(mCurrentIndex, mPlayingQueue)) {
            return null;
        }
        return mPlayingQueue.get(mCurrentIndex);
    }

    public int getCurrentQueueSize() {
        if (mPlayingQueue == null) {
            return 0;
        }
        return mPlayingQueue.size();
    }


    /**刷新当前的列表*/
    public void refreshQueue(){
        if(!StringUtils.isNullOrEmpty(mCurrentTable)){
            mPlayingQueue.clear();
            LinkedHashMap<String, MediaMetadataCompat>
                    current = mMusicProvider.getMusic(mCurrentTable);
            int count = 0;
            for(MediaMetadataCompat metadata : current.values()){
                MediaSessionCompat.QueueItem item =
                        MusicUtils.metadataToQueueItem(metadata, count++);
                mPlayingQueue.add(item);
            }
        }
    }


    public void switchtoLocal(){
        //第一次直接播放网络的时候，播放完切换到本地
        if(isNetPlay){
            isNetPlay = false;
            if(mPlayingQueue == null || mPlayingQueue.size()==0){
                setRandomQueue();
            }
        }
    }


    public void updateNetMetadata(MediaMetadataCompat mediaData){
        isNetPlay = true;
        mMusicProvider.setCurernNetMeta(mediaData);
        mListener.onMetadataChanged(mediaData);
    }


    public void updateMetadata() {
        MediaSessionCompat.QueueItem currentMusic = getCurrentMusic();
        if (currentMusic == null) {
            mListener.onMetadataRetrieveError();
            return;
        }
        final String mediaID = currentMusic.getDescription().getMediaId();
        MediaMetadataCompat metadata = mMusicProvider.getMusic(mediaID, mCurrentTable);
        if (metadata == null) {
            throw new IllegalArgumentException("Invalid musicId " + mediaID);
        }

        mListener.onMetadataChanged(metadata);

        // Set the proper album artwork on the media session, so it can be shown in the
        // locked screen and in other places.
        if (metadata.getDescription().getIconBitmap() == null &&
                metadata.getDescription().getIconUri() != null) {
//            String albumUri = metadata.getDescription().getIconUri().toString();
//            AlbumArtCache.getInstance().fetch(albumUri, new AlbumArtCache.FetchListener() {
//                @Override
//                public void onFetched(String artUrl, Bitmap bitmap, Bitmap icon) {
//                    mMusicProvider.updateMusicArt(musicId, bitmap, icon);
//
//                    // If we are still playing the same music, notify the listeners:
//                    MediaSessionCompat.QueueItem currentMusic = getCurrentMusic();
//                    if (currentMusic == null) {
//                        return;
//                    }
//                    String currentPlayingId = MediaIDHelper.extractMusicIDFromMediaID(
//                            currentMusic.getDescription().getMediaId());
//                    if (musicId.equals(currentPlayingId)) {
//                        mListener.onMetadataChanged(mMusicProvider.getMusic(currentPlayingId));
//                    }
//                }
//            });
        }
    }


    public int getMusicIndexOnQueue(Iterable<MediaSessionCompat.QueueItem> queue,
                             String mediaId) {
        int index = 0;
        for (MediaSessionCompat.QueueItem item : queue) {
            if (mediaId.equals(item.getDescription().getMediaId())) {
                return index;
            }
            index++;
        }
        return -1;
    }


    public List<MediaSessionCompat.QueueItem>
    getPlayingQueue(String mediaId, MusicProvider musicProvider) {
        String[] str = mediaId.split(":");
        LinkedHashMap<String, MediaMetadataCompat>
                current = musicProvider.getMusic(str[0]);
        ArrayList<MediaSessionCompat.QueueItem> itemList =
                new ArrayList<>();
        int count = 0;
        for(MediaMetadataCompat metadata : current.values()){
            if(mediaId.equals(metadata.getDescription().getMediaId())){
                mCurrentIndex = count;
            }
            MediaSessionCompat.QueueItem item =
                    MusicUtils.metadataToQueueItem(metadata, count++);
            itemList.add(item);
        }
        return itemList;
    }

    public interface MetadataUpdateListener {
        void onMetadataChanged(MediaMetadataCompat metadata);
        void onMetadataRetrieveError();
        void onCurrentQueueIndexUpdated(int queueIndex);
        void onQueueUpdated(String title, List<MediaSessionCompat.QueueItem> newQueue);
    }


//*********************************************************************************************/


    public static List<MediaSessionCompat.QueueItem> getPlayingQueueFromSearch(String query,
                                                                               Bundle queryParams, MusicProvider musicProvider) {

//        LogHelper.d(TAG, "Creating playing queue for musics from search: ", query,
//                " params=", queryParams);
//
//        VoiceSearchParams params = new VoiceSearchParams(query, queryParams);
//
//        LogHelper.d(TAG, "VoiceSearchParams: ", params);
//
//        if (params.isAny) {
//            // If isAny is true, we will play anything. This is app-dependent, and can be,
//            // for example, favorite playlists, "I'm feeling lucky", most recent, etc.
//            return getRandomQueue(musicProvider);
//        }
//
//        Iterable<MediaMetadataCompat> result = null;
//        if (params.isAlbumFocus) {
//            result = musicProvider.searchMusicByAlbum(params.album);
//        } else if (params.isGenreFocus) {
//            result = musicProvider.getMusicsByGenre(params.genre);
//        } else if (params.isArtistFocus) {
//            result = musicProvider.searchMusicByArtist(params.artist);
//        } else if (params.isSongFocus) {
//            result = musicProvider.searchMusicBySongTitle(params.song);
//        }
//
//        // If there was no results using media focus parameter, we do an unstructured query.
//        // This is useful when the user is searching for something that looks like an artist
//        // to Google, for example, but is not. For example, a user searching for Madonna on
//        // a PodCast application wouldn't get results if we only looked at the
//        // Artist (podcast author). Then, we can instead do an unstructured search.
//        if (params.isUnstructured || result == null || !result.iterator().hasNext()) {
//            // To keep it simple for this example, we do unstructured searches on the
//            // song title only. A real world application could search on other fields as well.
//            result = musicProvider.searchMusicBySongTitle(query);
//        }

//        return convertToQueue(result, MEDIA_ID_MUSICS_BY_SEARCH, query);
        return null;
    }


    public int getMusicIndexOnQueue(Iterable<MediaSessionCompat.QueueItem> queue,
                                    long queueId) {
        int index = 0;
        for (MediaSessionCompat.QueueItem item : queue) {
            if (queueId == item.getQueueId()) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public boolean isIndexPlayable(int index, List<MediaSessionCompat.QueueItem> queue) {
        return (queue != null && index >= 0 && index < queue.size());
    }
}
