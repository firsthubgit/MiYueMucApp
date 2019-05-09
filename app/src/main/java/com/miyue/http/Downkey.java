package com.miyue.http;

import android.os.AsyncTask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.miyue.application.MiYueConstans;
import com.miyue.bean.KeyItem;
import com.miyue.utils.StringUtils;
import com.miyue.utils.UtilLog;

import java.util.List;

public class Downkey extends AsyncTask<String, Void, String> {

    private String url;

    @Override
    protected String doInBackground(String... params) {
        url = params[0];
        return HttpApi.downKey(url);
    }

    @Override
    protected void onPostExecute(String keyString) {
        super.onPostExecute(keyString);
        UtilLog.e("kkk", keyString);
        if(!StringUtils.isNullOrEmpty(keyString)){
            JSONObject object = JSON.parseObject(keyString);
            JSONObject data = (JSONObject) object.get("data");
            if(data != null){
                JSONArray jsonArray = data.getJSONArray("items");

                List<KeyItem> result = null;
                result = JSON.parseArray(jsonArray.toJSONString(), KeyItem.class);
                if(result != null && result.size()>0){
                    MiYueConstans.KEY = result.get(0).getVkey();
                }
            }
        }
    }
}