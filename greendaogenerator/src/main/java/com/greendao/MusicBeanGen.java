package com.greendao;

/**
 * Created by zhangzhendong on 16/5/4.
 */
public interface MusicBeanGen {
    Long getId();
    void setId(Long id);

    String getTitle();
    void setTitle(String title);

    String getArtist();
    void setArtist(String artist);

    String getAlbum();
    void setAlbum(String album);

    String getPath();
    void setPath(String path);

    String getDuration();
    void setDuration(String duration);

    String getFile_size();
    void setFile_size(String file_size);

    String getFile_name();
    void setFile_name(String file_name);

    String getMediaID();
    void setMediaID(String mediaID);

    String getPlay_url();
    void setPlay_url(String play_url);

    String getPic_url();
    void setPic_url(String pic_url);

}
