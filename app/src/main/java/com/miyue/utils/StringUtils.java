package com.miyue.utils;

import com.miyue.application.MiYueConstans;
import com.miyue.service.playback.MusicProvider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhangzhendong on 17/5/31.
 */

public class StringUtils {

    /**
     * 判断是否为空
     *
     * @param text
     * @return
     */
    public static boolean isNullOrEmpty(String text) {
        if (text == null || "".equals(text.trim()) || text.trim().length() == 0
                || "null".equals(text.trim())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取播放链接
     * */
    public static String getPlayUrl(String songF){
        if(songF.contains("@@")){
            String[] song = songF.split("@@");
            for(String playUrl : song){
                if(playUrl.startsWith("http")){
                    //播放连接
                   return playUrl;
                }
            }
            return  null;
        } else {
            String[] song = songF.split("\\|");
            String songid = song[0];
            return MiYueConstans.QQ_PLAY_URL.replace("SONGID", songid);
        }
    }

    /**
     * 过滤特殊字符(\/:*?"<>|)
     */
    public static String stringFilter(String str) {
        if (str == null) {
            return null;
        }
        String regEx = "[\\/:*?\"<>|]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }
}
