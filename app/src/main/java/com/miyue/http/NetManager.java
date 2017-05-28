package com.miyue.http;

import android.text.TextUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by zhangzhendong on 17/5/28.
 */

public class NetManager {
    private static NetManager install;

    private OkHttpClient client;


    public static NetManager getInstall(){
        if(install == null){
            install = new NetManager();
        }
        return install;
    }

    public NetManager() {;
        if (client==null){
            client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS).build();
        }
    }

    public String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}
