package com.miyue.utils;

import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.greendao.MusicBean;
import com.miyue.application.MiYueConstans;
import com.miyue.bean.QQSong;
import com.miyue.bean.SongsInfo;
import com.miyue.service.playback.MusicProvider;

import java.util.ArrayList;

/**
* 各种封装的音乐类型的数据的转换
* @author ZZD
* @time 17/6/1 下午1:44
*/

public class MusicUtils {

    /**把MediaMetadata 转成 QueueItem*/
    public static MediaSessionCompat.QueueItem metadataToQueueItem(MediaMetadataCompat metadata, int id){
        return new MediaSessionCompat.QueueItem(metadata.getDescription(), id);
    }

    /**
     * @param isDown true表示是下载完成创建的Bundle
     * */
    public static Bundle creSongBundle(QQSong qqSong, boolean isDown){
        Bundle bundle = new Bundle();

        String playUrl = MiYueConstans.QQ_PLAY_URL.replace("MEDIAID", qqSong.getSongmid())
                .replace("AK", MiYueConstans.KEY);
        bundle.putString(MusicProvider.MEDIA_NET_PLAY_URL, playUrl);
        //songid
        bundle.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, qqSong.getSongid());
        //歌曲名字
        bundle.putString(MediaMetadataCompat.METADATA_KEY_TITLE, qqSong.getFsong());
        //歌手
        bundle.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, qqSong.getFsinger());
        //时长
        bundle.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, Long.parseLong(qqSong.getInterval())*1000);
        //图片地址
        String pic_url = MiYueConstans.QQ_PIC_URL.replace("专辑mid", qqSong.getAlbummid());
        bundle.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, pic_url);
        //专辑名称
        bundle.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, qqSong.getAlbumname());
        if(isDown){
            String fileName = FileUtils.getMp3Name(qqSong.getFsong(), qqSong.getFsinger());
            bundle.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,
                    MiYueConstans.MUSIC_DOWN_PATH + fileName);
            bundle.putString(MusicProvider.MEDIA_FILE_NAME, fileName);
        }
        return bundle;
    }

    //注意：这里返回的MediaID没有加table名字
    public static MediaMetadataCompat createMetadataFromQQSong(Bundle bundle){
        //这里返回的MediaID没有加table名字
        String mediaID = bundle.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
        String title = bundle.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
        String album = bundle.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
        String artist = bundle.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
        String path = bundle.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI);
        String subtitle = artist + "·" + album;
        Long duration = bundle.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        String pic_url = bundle.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);
        String playurl = bundle.getString(MusicProvider.MEDIA_NET_PLAY_URL);

        String fileSize = bundle.getString(MusicProvider.MEDIA_FILE_SIZE);
        String fileName = bundle.getString(MusicProvider.MEDIA_FILE_NAME);


        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaID)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE,subtitle)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, path)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, pic_url)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .putString(MusicProvider.MEDIA_FILE_SIZE, fileSize)
                .putString(MusicProvider.MEDIA_FILE_NAME, fileName)
                .putString(MusicProvider.MEDIA_NET_PLAY_URL, playurl)
                .build();
    }

    /***/
    public static MusicBean buildBeanFromMetadata(MediaMetadataCompat metadata){
        MusicBean bean = new  MusicBean();
        Bundle bundle = metadata.getBundle();
        String mediaID = bundle.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
        if(mediaID.contains(":")){
            mediaID = mediaID.split(":")[1];
        }
        bean.setMediaID(mediaID);
        bean.setTitle(bundle.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
        bean.setArtist(bundle.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
        bean.setAlbum(bundle.getString(MediaMetadataCompat.METADATA_KEY_ALBUM));
        String path = bundle.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI);
        bean.setPath(path);
        bean.setDuration(bundle.getLong(MediaMetadataCompat.METADATA_KEY_DURATION) +"");

        bean.setFile_size(bundle.getString(MusicProvider.MEDIA_FILE_SIZE));
        bean.setFile_name(bundle.getString(MusicProvider.MEDIA_FILE_NAME));
        bean.setPlay_url(bundle.getString(MusicProvider.MEDIA_NET_PLAY_URL));
        bean.setPic_url(bundle.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI));
        bean.setId(Long.valueOf(path.hashCode()));
        return bean;
    }

    /**最近播放的音乐不带Id一列*/
    public static MusicBean buildBeanNoIdFromMeta(MediaMetadataCompat metadata){
        MusicBean bean = new  MusicBean();
        Bundle bundle = metadata.getBundle();
        String mediaID = bundle.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
        if(mediaID.contains(":")){
            mediaID = mediaID.split(":")[1];
        }
        bean.setMediaID(mediaID);
        bean.setTitle(bundle.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
        bean.setArtist(bundle.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
        bean.setAlbum(bundle.getString(MediaMetadataCompat.METADATA_KEY_ALBUM));
        String path = bundle.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI);
        bean.setPath(path);
        bean.setDuration(bundle.getLong(MediaMetadataCompat.METADATA_KEY_DURATION) +"");

        bean.setFile_size(bundle.getString(MusicProvider.MEDIA_FILE_SIZE));
        bean.setFile_name(bundle.getString(MusicProvider.MEDIA_FILE_NAME));
        bean.setPlay_url(bundle.getString(MusicProvider.MEDIA_NET_PLAY_URL));
        bean.setPic_url(bundle.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI));
        return bean;
    }

    /**从MusicBean中获取MediaMetadataCompat*/
    public static MediaMetadataCompat buildFromMusicBean(MusicBean bean, String whichtable){
        String title = bean.getTitle();
        String subtitle = (bean.getArtist()+"-"+bean.getAlbum());
        String album = bean.getAlbum();
        String artist = bean.getArtist();
        String path = bean.getPath();
        Long duration = Long.valueOf(bean.getDuration());
        String file_size = bean.getFile_size();
        String file_name = bean.getFile_name();
        String mediaID = whichtable + ":" + bean.getMediaID();
        String play_url = bean.getPlay_url();
        String pic_url = bean.getPic_url();

        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaID)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE,subtitle)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, path)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .putString(MusicProvider.MEDIA_FILE_SIZE, file_size)
                .putString(MusicProvider.MEDIA_FILE_NAME, file_name)
                .putString(MusicProvider.MEDIA_NET_PLAY_URL, play_url)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, pic_url)
                .build();
    }

}
