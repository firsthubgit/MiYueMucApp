package com.miyue.http;

import android.os.Environment;

import com.miyue.common.listener.ReqCallBack;
import com.miyue.utils.UtilLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by zhangzhendong on 17/5/27.
 */

public class HttpApi {
    private static final String TAG = "HttpApi";

    private static final String HTTP_LRC_URL = "http://geci.me/api/lyric/";

    private static final String TTPOD_LRC = "http://lp.music.ttpod.com/lrc/down?artist=歌手&title=歌曲名";



    /**在输入前判断用户时候输入全，强制歌曲名和歌手都不为空*/
    public static String getLrc(String...param){
        String lrc = null;
        NetManager manager = NetManager.getInstall();
        String lrcURL = null;
        if(null != param[0] && null != param[1]){
             lrcURL = TTPOD_LRC.replace("歌曲名", param[0]).replace("歌手", param[1]);
            UtilLog.e(TAG, lrcURL);
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
}
