package com.miyue.http;

import android.util.Base64;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.miyue.bean.QQSong;
import com.miyue.bean.Singer;
import com.miyue.bean.SongsInfo;
import com.miyue.bean.TrackLrc;
import com.miyue.utils.Base64Util;
import com.miyue.utils.StringUtils;
import com.miyue.utils.UtilLog;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhangzhendong on 17/5/27.
 */

public class JsonParser {

    public static final String TAG = "JsonParser";

    public static List<TrackLrc> parseJsonForLRC(String responseStr) {

        List<TrackLrc> result;
        try {
            JSONObject object = JSON.parseObject(responseStr);
            JSONObject data = (JSONObject) object.get("result");
            JSONArray jsonArray = data.getJSONArray("TrackLrc");
            result = JSON.parseArray(jsonArray.toJSONString(), TrackLrc.class);

        } catch (Exception e) {
            result = new ArrayList<>();
            UtilLog.e(TAG, "parseResponseData()中解析json出现异常");
        }
        return result;
    }

    public static String parseJsonForQQLyric(String responseStr){
        try {
            JSONObject object = JSON.parseObject(responseStr);
            String data = object.getString("lyric");
            if(data == null){
                return null;
            }
            //正则替换Unicode编码为ASCII码
            Pattern r = Pattern.compile("\\&\\#\\d{2};");
            Matcher matcher  = r.matcher(data);
            StringBuffer sb=new StringBuffer();
            boolean result = matcher.find();
            while(result){
                char c = StringUtils.unicode2ASCII(matcher.group());
                matcher.appendReplacement(sb, String.valueOf(c));
                result = matcher.find();//继续下一步匹配
            }
            matcher.appendTail(sb);
            //也可能转换QQ音乐Base64后歌词
//            byte[] bytelrc = Base64.decode(data, Base64.NO_WRAP);
//            String lrc = new String(bytelrc, "UTF-8");
            return sb.toString();
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }

    }

    public static String  parseJsonForLyric(String responseStr){
        try {
            JSONObject object = JSON.parseObject(responseStr);
            JSONObject data = (JSONObject) object.get("data");
            if(data == null){
                return null;
            }
            String lrc = (String) data.get("lrc");
            return lrc;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static SongsInfo<QQSong> parseQQForSong(String responseStr){
        List<QQSong> result = null;
        SongsInfo<QQSong> songsInfo = new SongsInfo<>();
        try {
            JSONObject object = JSON.parseObject(responseStr);
            JSONObject data = (JSONObject) object.get("data");
            if(data != null){
                JSONObject songs = (JSONObject) data.get("song");
                songsInfo.setTotalnum(songs.getString("totalnum"));
                if(songs != null){
                    JSONArray jsonArray = songs.getJSONArray("list");
                    result = JSON.parseArray(jsonArray.toJSONString(), QQSong.class);
                    for(int i=0; i<jsonArray.size(); i++){
                        JSONObject  song = (JSONObject) jsonArray.get(i);
                        JSONArray singers = song.getJSONArray("singer");
                        List<Singer> singerBeans = JSON.parseArray(singers.toJSONString(), Singer.class);
                        result.get(i).setFsinger(singerBeans);
                    }
                    songsInfo.setList((ArrayList<QQSong>) result);
                }
            }
        } catch (Exception e) {
            UtilLog.e(TAG,"parseResponseData()中解析json出现异常");
        }
        return songsInfo;
    }
}
