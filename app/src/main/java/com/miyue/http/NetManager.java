package com.miyue.http;

import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
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

    public String getGB2312(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        byte[] b = response.body().bytes(); //获取数据的bytes
        String info = new String(b, "GB2312"); //然后将其转为gb2312
        return info;
    }

    public InputStream getStream(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        InputStream is = response.body().byteStream();
        return is;
    }
}
