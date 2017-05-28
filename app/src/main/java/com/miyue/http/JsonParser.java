package com.miyue.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.miyue.bean.TrackLrc;
import com.miyue.utils.UtilLog;

import java.util.ArrayList;
import java.util.List;

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
}
