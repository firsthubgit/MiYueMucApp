package com.miyue.dao;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.text.TextUtils;

import com.greendao.DBHelper;
import com.greendao.MusicBean;
import com.miyue.application.DbConstans;
import com.miyue.application.MiYueApp;
import com.miyue.utils.UtilLog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
*
* @author ZZD
* @time 17/5/19 下午5:46
*/
public class MusicProvider {

    private static final String TAG = "MusicProvider";

    public static final String MEDIA_FILE_SIZE = "com.miyue.dao.FILE_SIZE";
    public static final String MEDIA_FILE_NAME = "com.miyue.dao.FILE_NAME";

    private ConcurrentHashMap<String, MediaMetadataCompat> mLocalMusics;
    private ConcurrentHashMap<String, MediaMetadataCompat> mDownMusics;
    private ConcurrentHashMap<String, MediaMetadataCompat> mRecentMusics;

    private final Set<String> mFavoriteTracks;

    enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED;
    }

    private DBHelper mDbHelper = DBHelper.getInstance(MiYueApp.getInstance());

    private volatile State mCurrentState = State.NON_INITIALIZED;



    public MusicProvider(){
        mFavoriteTracks = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
        retrieveMediaAsync();
    }



    public void retrieveMediaAsync() {
        UtilLog.d(TAG, "retrieveMediaAsync called");
        if (mCurrentState == State.INITIALIZED) {
            //如果初始化过了
            return;
        }

        //后台扫描本地音乐
        new AsyncTask<Void, Void, State>() {
            @Override
            protected State doInBackground(Void... params) {
                long number = mDbHelper.getLocalMusicCount();
                if(0 == number){
                    initMusic();
                }
                retrieveMedia();
                return mCurrentState;
            }

            @Override
            protected void onPostExecute(State current) {

            }
        }.execute();
    }

    public void initMusic(){
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            ContentResolver conRes = MiYueApp.getInstance().getContentResolver();
            Cursor cs = conRes.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null,
                    MediaStore.Audio.Media.DURATION + ">?",
                    new String[]{"30000"},
                    MediaStore.Audio.Media.TITLE
            );

            int titleIndex = cs.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistIndex = cs.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int album = cs.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int pathIndex = cs.getColumnIndex(MediaStore.Audio.Media.DATA);
            int durationIndex = cs.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int sizeIndex = cs.getColumnIndex(MediaStore.Audio.Media.SIZE);
            int fileNameIndex = cs.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);

            if(cs.getCount()!=0){
                cs.moveToFirst();
                do{
                    File musicFile = new File(cs.getString(pathIndex));
                    if(musicFile.exists()){
                        MusicBean bean = new MusicBean();
                        String title = cs.getString(titleIndex);
                        bean.setTitle(title);
                        String artist = cs.getString(artistIndex);
                        bean.setArtist(artist);
                        bean.setAlbum(cs.getString(album));
                        bean.setPath(cs.getString(pathIndex));
                        bean.setDuration(cs.getString(durationIndex));
                        String filesize = cs.getString(sizeIndex);
                        bean.setFile_size(filesize);
                        String filename = cs.getString(fileNameIndex);
                        bean.setFile_name(filename);
                        String mID = cs.getString(pathIndex);
                        bean.setMediaID(String.valueOf(mID.hashCode()));
                        mDbHelper.addMusic(bean, DbConstans.LOCAL_MUSIC);
                        cs.moveToNext();
                    }else{
                        cs.moveToNext();
                    }
                }while(! cs.isAfterLast());
            }
            cs.close();
        }
    }

    private synchronized void retrieveMedia() {
        try {
            if (mCurrentState == State.NON_INITIALIZED) {
                mCurrentState = State.INITIALIZING;
                mLocalMusics = buildSyncList(mDbHelper.getLocalMusicList(), DbConstans.LOCAL_MUSIC);
                mDownMusics = buildSyncList(mDbHelper.getLocalMusicList(), DbConstans.DOWNLOAD);
                mRecentMusics = buildSyncList(mDbHelper.getLocalMusicList(), DbConstans.RECENT);
                mCurrentState = State.INITIALIZED;
            }
        } finally {
            if (mCurrentState != State.INITIALIZED) {
                // 有时候意外发生，我们需要重置State为NON_INITIALIZED
                mCurrentState = State.NON_INITIALIZED;
            }
        }
    }



    public boolean isInitialized() {
        return mCurrentState == State.INITIALIZED;
    }
    /**
     * 搜索本地歌曲的功能
     * */
    Iterable<MediaMetadataCompat> searchMusic(String metadataField, String query) {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        ArrayList<MediaMetadataCompat> result = new ArrayList<>();
        for (MediaMetadataCompat metaDate : mLocalMusics.values()) {
            if (metaDate.getString(metadataField).contains(query)) {
                result.add(metaDate);
            }
        }
        return result;
    }

    public boolean isFavorite(String mediaID){
        return mFavoriteTracks.contains(mediaID);
    }

    public void setFavorite(String mediaID, boolean favorite) {
        if (favorite) {
            mFavoriteTracks.add(mediaID);
        } else {
            mFavoriteTracks.remove(mediaID);
        }
    }
    /**
     * 创建本地音乐，下载歌曲，最近歌曲等列表
     * */
    private ConcurrentHashMap<String, MediaMetadataCompat> buildSyncList(List<MusicBean> musicBeans,
                                                                         String whichTable){
        ConcurrentHashMap<String, MediaMetadataCompat> conBeans =
                new ConcurrentHashMap<>();
        for(int i=0; i<musicBeans.size(); i++){
            MusicBean bean = musicBeans.get(i);
            conBeans.put(whichTable + ":" + bean.getMediaID(),
                    buildFromMusicBean(bean, whichTable));
        }
        return conBeans;
    }

    public MediaMetadataCompat getMusic(String mediaID, String whichdb){
        if(TextUtils.equals(DbConstans.LOCAL_MUSIC,whichdb) && mLocalMusics != null){
           return mLocalMusics.get(mediaID);
        } else if(TextUtils.equals(DbConstans.RECENT,whichdb)  && mRecentMusics != null){
            return mRecentMusics.get(mediaID);
        } else if(TextUtils.equals(DbConstans.DOWNLOAD,whichdb)  && mDownMusics != null){
            return mDownMusics.get(mediaID);
        }
        return null;
    }

    public ConcurrentHashMap<String, MediaMetadataCompat> getMusic(String whichdb){
        if(TextUtils.equals(DbConstans.LOCAL_MUSIC,whichdb) && mLocalMusics != null){
            return mLocalMusics;
        } else if(TextUtils.equals(DbConstans.RECENT,whichdb)  && mRecentMusics != null){
            return mRecentMusics;
        } else if(TextUtils.equals(DbConstans.DOWNLOAD,whichdb)  && mDownMusics != null){
            return mDownMusics;
        }
        return null;
    }
    public List<MediaBrowserCompat.MediaItem> getMediaItemList(String whichdb) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        ConcurrentHashMap<String, MediaMetadataCompat> whichList;
        //判断当前是否有list
//        if (!MediaIDHelper.isBrowseable(mediaId)) {
//            return mediaItems;
//        }
        if(DbConstans.LOCAL_MUSIC.equals(whichdb) && mLocalMusics != null){
            whichList = mLocalMusics;
        } else if(DbConstans.RECENT.equals(whichdb) && mRecentMusics != null){
            whichList = mRecentMusics;
        } else if(DbConstans.DOWNLOAD.equals(whichdb) && mDownMusics != null){
            whichList = mDownMusics;
        } else {
            return null;
        }
        for(MediaMetadataCompat metadate : whichList.values()){
            mediaItems.add(createMediaItem(metadate));
        }
        return mediaItems;
    }


    private MediaBrowserCompat.MediaItem createMediaItem(MediaMetadataCompat metadata) {
        return new MediaBrowserCompat.MediaItem(metadata.getDescription(),
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);

    }

    private MediaMetadataCompat buildFromMusicBean(MusicBean bean, String whichTable){
        String title = bean.getTitle();
        String subtitle = (bean.getArtist()+"-"+bean.getAlbum());
        String album = bean.getAlbum();
        String artist = bean.getArtist();
        String path = bean.getPath();
        Long duration = Long.valueOf(bean.getDuration());
        String file_size = bean.getFile_size();
        String file_name = bean.getFile_name();
        String mediaID = whichTable + ":" + bean.getMediaID();


        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaID)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE,subtitle)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, path)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .putString(MusicProvider.MEDIA_FILE_SIZE, file_size)
                .putString(MusicProvider.MEDIA_FILE_NAME, file_name)
                .build();
    }
}
