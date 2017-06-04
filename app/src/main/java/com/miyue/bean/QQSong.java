package com.miyue.bean;

/**
*
* @author ZZD
* @time 17/5/30 下午9:54
*/

public class QQSong {

    private String f;
    /**周杰伦*/
    private String fsinger;
    /**告白气球*/
    private String fsong;
    /**搜索歌词*/
    private String lyric;
    private String lyric_hilight;


    public String getLyric() {
        return lyric;
    }

    public void setLyric(String lyric) {
        this.lyric = lyric;
    }

    public String getLyric_hilight() {
        return lyric_hilight;
    }

    public void setLyric_hilight(String lyric_hilight) {
        this.lyric_hilight = lyric_hilight;
    }


    public String getF() {
        return f;
    }

    public void setF(String f) {
        this.f = f;
    }

    public String getFsinger() {
        return fsinger;
    }

    public void setFsinger(String fsinger) {
        this.fsinger = fsinger;
    }

    public String getFsong() {
        return fsong;
    }

    public void setFsong(String fsong) {
        this.fsong = fsong;
    }

    @Override
    public String toString() {
        return "Song{" +
                "f='" + f + '\'' +
                ", fsinger='" + fsinger + '\'' +
                ", fsong='" + fsong + '\'' +
                '}';
    }
}
