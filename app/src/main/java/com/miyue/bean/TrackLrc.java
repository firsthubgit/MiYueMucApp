package com.miyue.bean;

/**
 * Created by zhangzhendong on 17/5/27.
 */

public class TrackLrc {

    private String aid;

    private String artist_id;

    /**LRC下载地址*/
    private String lrc;

    private String sid;
    /**歌曲名字*/
    private String song;


    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getArtist_id() {
        return artist_id;
    }

    public void setArtist_id(String artist_id) {
        this.artist_id = artist_id;
    }

    public String getLrc() {
        return lrc;
    }

    public void setLrc(String lrc) {
        this.lrc = lrc;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getSong() {
        return song;
    }

    public void setSong(String song) {
        this.song = song;
    }


}
