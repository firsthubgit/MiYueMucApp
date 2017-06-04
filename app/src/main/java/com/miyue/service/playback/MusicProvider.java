package com.miyue.service.playback;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.text.TextUtils;

import com.greendao.DBHelper;
import com.greendao.MusicBean;
import com.miyue.application.DbConstans;
import com.miyue.application.MiYueApp;
import com.miyue.application.MiYueConstans;
import com.miyue.utils.FileUtils;
import com.miyue.utils.MusicUtils;
import com.miyue.utils.UtilLog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
*
* @author ZZD
* @time 17/5/19 下午5:46
 *
 * 数据库中mediaID不带 "DbConstants.table :",开启app取出时候，添加表名和冒号
*/
public class MusicProvider {

    private static final String TAG = "MusicProvider";

    public static final String MEDIA_FILE_SIZE = "com.miyue.dao.FILE_SIZE";
    public static final String MEDIA_FILE_NAME = "com.miyue.dao.FILE_NAME";
    public static final String MEDIA_NET_PLAY_URL = "com.miyue.dao.NET_PLAY_URL";

    private LinkedHashMap<String, MediaMetadataCompat> mLocalMusics;
    private LinkedHashMap<String, MediaMetadataCompat> mDownMusics;
    private LinkedHashMap<String, MediaMetadataCompat> mRecentMusics;
    private LinkedHashMap<String, MediaMetadataCompat> mFavoriteMusic;

    private final Set<String> mFavoriteTracks;

    public MediaMetadataCompat getCurernNetMeta() {
        return mCurernNetMeta;
    }

    public void setCurernNetMeta(MediaMetadataCompat curernNetMeta) {
        mCurernNetMeta = curernNetMeta;
    }

    private MediaMetadataCompat mCurernNetMeta;

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
                return mCurrentState;
            }

            @Override
            protected void onPostExecute(State current) {
                retrieveMedia();
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
                do{ //读取系统数据库往自己表中添加数据时，用path的ashcode作为表id和mediaID
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
                        bean.setId(Long.valueOf(mID.hashCode()));
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
                mDownMusics = buildSyncList(mDbHelper.getDownLoadMusicList(), DbConstans.DOWNLOAD);
                mRecentMusics = buildSyncList(mDbHelper.getRecentMusicList(), DbConstans.RECENT);
                mFavoriteMusic = buildSyncList(mDbHelper.getFavoriteList(), DbConstans.FAVORITES);

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
        String newId = mediaID.split(":")[1];
        return mFavoriteMusic.containsKey(DbConstans.FAVORITES + ":" + newId);
    }

    public void setFavorite(String mediaID, boolean favorite) {
        if (favorite) {
            addFavoriteMusic(mediaID);
        } else {
            deleteFavoriteMusin(mediaID);
        }
    }
    /**
     * 创建本地音乐，下载歌曲，最近歌曲等列表
     * */
    private LinkedHashMap<String, MediaMetadataCompat> buildSyncList(List<MusicBean> musicBeans,
                                                                   String whichTable){
        LinkedHashMap<String, MediaMetadataCompat> conBeans;
        if(DbConstans.RECENT.equals(whichTable)){
            conBeans = new LinkedHashMap<>(32,0.75f,true);//按照访问顺序排
            for(int i=musicBeans.size()-1; i>=0; i--){
                MusicBean bean = musicBeans.get(i);
                conBeans.put(whichTable + ":" + bean.getMediaID(),
                        MusicUtils.buildFromMusicBean(bean, whichTable));
            }
        } else {
            conBeans = new LinkedHashMap<>();
            for(int i=0; i<musicBeans.size(); i++){
                MusicBean bean = musicBeans.get(i);
                conBeans.put(whichTable + ":" + bean.getMediaID(),
                        MusicUtils.buildFromMusicBean(bean, whichTable));
            }
        }

        return conBeans;
    }

    public MediaMetadataCompat getMusic(String mediaID, String whichdb){
        if(TextUtils.equals(DbConstans.LOCAL_MUSIC,whichdb) && mLocalMusics != null){
           return mLocalMusics.get(mediaID);
        } else if(TextUtils.equals(DbConstans.RECENT,whichdb) && mRecentMusics != null){
            return mRecentMusics.get(mediaID);
        } else if(TextUtils.equals(DbConstans.DOWNLOAD,whichdb) && mDownMusics != null){
            return mDownMusics.get(mediaID);
        }else if(TextUtils.equals(DbConstans.FAVORITES,whichdb) && mFavoriteMusic != null){
            return mFavoriteMusic.get(mediaID);
        }
        return null;
    }

    public LinkedHashMap<String, MediaMetadataCompat> getMusic(String whichdb){
        if(TextUtils.equals(DbConstans.LOCAL_MUSIC,whichdb) && mLocalMusics != null){
            return mLocalMusics;
        } else if(TextUtils.equals(DbConstans.RECENT,whichdb)  && mRecentMusics != null){
            return mRecentMusics;
        } else if(TextUtils.equals(DbConstans.DOWNLOAD,whichdb)  && mDownMusics != null){
            return mDownMusics;
        } else if(TextUtils.equals(DbConstans.FAVORITES,whichdb)  && mFavoriteMusic != null){
            return mFavoriteMusic;
        }
        return null;
    }
    public List<MediaBrowserCompat.MediaItem> getMediaItemList(String whichdb) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        LinkedHashMap<String, MediaMetadataCompat> whichList;
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
        } else if(DbConstans.FAVORITES.equals(whichdb) && mFavoriteMusic != null){
            whichList = mFavoriteMusic;
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


    /***
     *  移除喜爱的音乐
     */
    public void deleteFavoriteMusin(String mediaID){
        String[] media = mediaID.split(":");
        mDbHelper.deleteFavorite(media[1]);
        mFavoriteMusic.remove(DbConstans.FAVORITES+":"+media[1]);
    }

    /**
     *  添加喜爱的音乐
     * */
    public void addFavoriteMusic(String mediaID){
        String[] media = mediaID.split(":");
        MediaMetadataCompat metadata = getMusic(mediaID,media[0]);
        MusicBean bean = MusicUtils.buildBeanFromMetadata(metadata);
        mDbHelper.addMusic(bean, DbConstans.FAVORITES);

        String newId = DbConstans.FAVORITES + ":" + media[1];
        Bundle bundle = metadata.getBundle();
        bundle.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, newId);
        MediaMetadataCompat newdata = MusicUtils.createMetadataFromQQSong(bundle);
        mFavoriteMusic.put(newId, newdata);
    }

    /**删除某首歌*/
    public void deleteMusic(String mediaID){
        String[] media = mediaID.split(":");
        MediaMetadataCompat metadata = getMusic(mediaID,media[0]);
        FileUtils.deleteMusic(metadata.getDescription().getMediaUri().toString());
        mDbHelper.deleteDownload(media[1]);
        mDbHelper.deleteLocal(media[1]);
        mDbHelper.deleteRecent(media[1]);
        mDbHelper.deleteFavorite(media[1]);

        refereshCurrentMap(DbConstans.DOWNLOAD);
        refereshCurrentMap(DbConstans.LOCAL_MUSIC);
        refereshCurrentMap(DbConstans.RECENT);
        refereshCurrentMap(DbConstans.FAVORITES);
    }

    /**下载完成添加到列表*/
    public void addDownMusic(MediaMetadataCompat metadata){
        MusicBean bean = MusicUtils.buildBeanFromMetadata(metadata);
        mDbHelper.addMusic(bean, DbConstans.DOWNLOAD);
        mDbHelper.addMusic(bean, DbConstans.LOCAL_MUSIC);

        refereshCurrentMap(DbConstans.DOWNLOAD);
        refereshCurrentMap(DbConstans.LOCAL_MUSIC);
    }


    /**把网络添加到最近的歌曲，暂时不做*/
    public void addRecentMusic(MediaMetadataCompat metadata){
        MusicBean bean = MusicUtils.buildBeanNoIdFromMeta(metadata);
        if(mRecentMusics.size()>10){
            //更新数据库中的数据
            mDbHelper.deleteFirstRecent();
        }
        mDbHelper.addMusic(bean, DbConstans.RECENT);
        refereshCurrentMap(DbConstans.RECENT);
    }

    /**把本地的添加到最近播放里*/
    public void addRecentMusic(String mediaID){
        String[] media = mediaID.split(":");
        if(DbConstans.RECENT.equals(media[0])){
            return;
        }
        MediaMetadataCompat metadata = getMusic(mediaID, media[0]);
        MusicBean bean = MusicUtils.buildBeanNoIdFromMeta(metadata);
        if(mRecentMusics.size()>100){
            //删除数据库中的第一条数据
            mDbHelper.deleteFirstRecent();
        }
        mDbHelper.addMusic(bean, DbConstans.RECENT);
        refereshCurrentMap(DbConstans.RECENT);
    }

    /**刷新当前的数据*/
    private void refereshCurrentMap(String whichmap){
        if(DbConstans.DOWNLOAD.equals(whichmap)){
            mDownMusics.clear();
            mDownMusics = null;
            mDownMusics = buildSyncList(mDbHelper.getDownLoadMusicList(), whichmap);
        } else if(DbConstans.LOCAL_MUSIC.equals(whichmap)){
            mLocalMusics.clear();
            mLocalMusics = null;
            mLocalMusics = buildSyncList(mDbHelper.getLocalMusicList(), whichmap);
        } else if(DbConstans.RECENT.equals(whichmap)){
            mRecentMusics.clear();
            mRecentMusics = null;
            mRecentMusics = buildSyncList(mDbHelper.getRecentMusicList(), whichmap);
        } else if(DbConstans.FAVORITES.equals(whichmap)){
            mFavoriteMusic.clear();
            mFavoriteMusic = null;
            mFavoriteMusic = buildSyncList(mDbHelper.getFavoriteList(), whichmap);
        }
    }
}
