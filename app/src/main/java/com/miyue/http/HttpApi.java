package com.miyue.http;


import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.miyue.application.MiYueApp;
import com.miyue.application.MiYueConstans;
import com.miyue.bean.QQSong;
import com.miyue.bean.SongsInfo;
import com.miyue.utils.FileUtils;
import com.miyue.utils.MusicUtils;
import com.miyue.utils.StringUtils;
import com.miyue.utils.UtilLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;


/**
*
* @author ZZD
* @time  17/5/27 下午1:59
*/

public class HttpApi {
    private static final String TAG = "HttpApi";

    private static final String HTTP_LRC_URL = "http://geci.me/api/lyric/";

    private static final String TTPOD_LRC = "http://lp.music.ttpod.com/lrc/down?artist=歌手&title=歌曲名";

//    private static final String QQ_SEARCH_URL = "http://s.music.qq.com/fcgi-bin/music_search_new_platform?t=0&%20amp;n=10&aggr=1&cr=1&loginUin=0&format=json&inCharset=GB2312&outCharset=utf-8¬ice=0&platform=jqminiframe.json&needNewCode=0&p=页数&catZhida=0&remoteplace=sizer.newclient.next_song&w=周杰伦";
    private static final String QQ_SEARCH_URL = "https://c.y.qq.com/soso/fcgi-bin/client_search_cp?aggr=1&cr=1&flag_qc=0&p=页数&n=20&w=周杰伦";


    private static final String QQ_LRC_URL = "https://c.y.qq.com/lyric/fcgi-bin/fcg_query_lyric_yqq.fcg?nobase64=0&musicid=SONGID&-=jsonp1&g_tk=857640282&hostUin=0&format=json&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq.json&needNewCode=0";


    /**在输入前判断用户时候输入全，强制歌曲名和歌手都不为空*/
    public static String getLrc(String...param){
        String lrc = null;
        NetManager manager = NetManager.getInstall();
        String lrcURL = null;
        if(null != param[0] && null != param[1]){
             lrcURL = TTPOD_LRC.replace("歌曲名", param[0]).replace("歌手", param[1]);
            UtilLog.url(lrcURL);
        } else {
            return null;
        }
        try {
            String lrcJson = manager.run(lrcURL);
            lrc =  JsonParser.parseJsonForLyric(lrcJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lrc;
    }

    public static SongsInfo<QQSong> getQQSongKeyword(String keyword, int page){
        NetManager manager = new NetManager();

        String keywordURL = QQ_SEARCH_URL.replace("周杰伦", keyword)
                .replace("页数", page + "");
        UtilLog.url("URL:" + keywordURL);
        try {
            String songJson = manager.run(keywordURL);
            if(songJson.startsWith("callback")){
                songJson = songJson.substring(9, songJson.length()-1);
            }
            SongsInfo<QQSong> songsInfo = JsonParser.parseQQForSong(songJson);
            return songsInfo;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getQQLRC(String songId){
        NetManager manager = new NetManager();
        String qqSongLRC = QQ_LRC_URL.replace("SONGID",songId);
        UtilLog.e(TAG, qqSongLRC);
        try {
            String lrcData = manager.getQQLRC(qqSongLRC);
            String lrc = JsonParser.parseJsonForQQLyric(lrcData);
            UtilLog.e(TAG, "QQ歌词：" + lrc);
            return lrc;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getAlbumPic(String artUrl){
        NetManager manager = new NetManager();

        try {
            InputStream is = manager.getStream(artUrl);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param  song song[0]代表歌名 song[1]代表歌手
     * */
    public static int downMusic(String playUrl, String[] song){
        NetManager manager = new NetManager();
        try {
            InputStream is = manager.getStream(playUrl);
            int size = FileUtils.downloadMusic(is, song);
            return size;//下载成功
        } catch (IOException e) {
            e.printStackTrace();
            return 0;//下载失败
        }
    }

    public static String downKey(String url){
        NetManager manager = new NetManager();
        try {
            Reader reader = manager.getReaderStream(url);
            BufferedReader read = new BufferedReader(reader);
            String keyCuan = "";
            String line = "";
            while((line = read.readLine()) != null){
                keyCuan += line;
            }
            return keyCuan;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
