package com.miyue.http;

import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
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

    public String getQQLRC(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .removeHeader("User-Agent")
                .removeHeader("Referer")
                .addHeader("User-Agent",
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36")
                .addHeader("Referer", "https://y.qq.com/portal/player.html")
                .build();
        Response response = client.newCall(request).execute();
        String lrcData = response.body().string();
        return lrcData;
    }

    public InputStream getStream(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        InputStream is = response.body().byteStream();
        return is;
    }

    public Reader getReaderStream(String url) throws IOException{
        Request request = new Request.Builder()
                .url(url).removeHeader("User-Agent").addHeader("User-Agent",
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36")
                .build();
        Response response = client.newCall(request).execute();
        Reader reader = response.body().charStream();
        return reader;
    }
}
