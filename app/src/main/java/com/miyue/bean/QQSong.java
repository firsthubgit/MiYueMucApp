package com.miyue.bean;

import java.util.List;

/**
*
* @author ZZD
* @time 17/5/30 下午9:54
*/

public class QQSong {


    private String songid;
    /**周杰伦*/
//    private String fsinger;
    /**告白气球*/
    private String songname;
    /**搜索歌词*/
    private String lyric;
    private String lyric_hilight;

    //时间长度
    private String interval;
    //专辑名称
    private String albumname;

    private List<Singer> singer;

    private String songmid;

    private String albummid;


    public String getAlbummid() {
        return albummid;
    }

    public void setAlbummid(String albummid) {
        this.albummid = albummid;
    }



    public void setSongmid(String songmid) {
        this.songmid = songmid;
    }

    public String getSongmid() {
        return songmid;
    }


    public void setInterval(String interval) {
        this.interval = interval;
    }

    public void setAlbumname(String albumname) {
        this.albumname = albumname;
    }

    public String getInterval() {
        return interval;
    }

    public String getAlbumname() {
        return albumname;
    }


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

    public String getFsinger() {
        String singerName = "";
        if(singer !=null && singer.size()>0){
            singerName =  singer.get(0).name;
            for(int i=1; i<singer.size(); i++){
                singerName += ("|" + singer.get(i).name);
            }
        }
        return singerName;
    }

    public void setFsinger(List<Singer> singer) {
        this.singer = singer;
    }

    public String getFsong() {
        return songname;
    }

    public void setSongname(String songname) {
        this.songname = songname;
    }

    public String getSongid() {
        return songid;
    }

    public void setSongid(String songid) {
        this.songid = songid;
    }

}
