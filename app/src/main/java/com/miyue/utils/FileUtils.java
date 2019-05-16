package com.miyue.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.miyue.R;
import com.miyue.application.MiYueApp;
import com.miyue.application.MiYueConstans;
import com.miyue.bean.LrcRow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by zhangzhendong on 17/5/27.
 */

public class FileUtils {

    public static final String TAG = "FileUtils";

    private static final String MP3 = ".mp3";
    private static final String LRC = ".lrc";



    public static boolean isFileExist(String  path){
        File file = new File(path);
        return file.exists();
    }


    public static boolean deleteMusic(String path){
        File file = new File(path);
        if(!file.exists()){
           return true;
        }
        return file.delete();
    }

    public static boolean deleteLrc(String songName, String artist){
        File file = new File(MiYueConstans.LRC_PATH +songName+"-"+artist+".lrc");
        if(!file.exists()){
            return true;
        }
        return file.delete();
    }

    /**
     * 保存歌词到本地
     * */
    public static void downLrc(String lrc, String filename){
        File file = new File(MiYueConstans.LRC_PATH + filename);
        FileWriter fwLrc = null;
        try {
            if(!creteDir(file)){
                UtilLog.e(TAG, "文件夹创建失败！");
                return;
            }
            if(!file.exists()){
                file.createNewFile();
            }
            fwLrc = new FileWriter(file);
            fwLrc.write(lrc);
            fwLrc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 保存图片到本地
     * */
    public static void downPic(Bitmap bitmap, String urlHasncode){
        File file = new File(MiYueConstans.ALBUM_CACHE_PATH + urlHasncode);
        FileOutputStream fos = null;
        try {
            if(!creteDir(file)){
                UtilLog.e(TAG, "文件夹创建失败！");
                return;
            }
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 保存音乐到本地
     *
     * @param str str[0] 歌名 str1[1] 歌手
     * @return  0表示失败， 大于0表示成功
     * */
    public static int downloadMusic(InputStream is, String[] str) {
        File file = new File(MiYueConstans.MUSIC_DOWN_PATH + getMp3Name(str[0], str[1]));

        if(!creteDir(file)){
            UtilLog.e(TAG, "文件夹创建失败！");
            return 0;
        }

        FileOutputStream fos = null;
        byte[] buf = new byte[2048];
        int len = 0;
        int size = 0;
        try {
            if(!file.exists()){
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
            while ((len = is.read(buf)) != -1) {
                fos.write(buf, 0, len);
                size += len;
            }
            fos.flush();
            fos.close();
            return size;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }
    public static boolean isLRCFileExists(String name){
        File file = new File(MiYueConstans.LRC_PATH + name);
        return file.exists();
    }

    public static boolean isMp3FileExists(String name){
        File file = new File(MiYueConstans.MUSIC_DOWN_PATH + name);
        return file.exists();
    }

    public static Bitmap[] getArtPic(String filename){
        File file = new File(MiYueConstans.ALBUM_CACHE_PATH + filename);
        if(!file.exists()){
            return null;
        }
        try {
            InputStream inputStream = new FileInputStream(file);
            Bitmap bigPic = BitmapFactory.decodeStream(inputStream);
            Bitmap smallPic = BitmapUtils.getScaleBitmap(bigPic, 3);
            Bitmap[] bitmaps = new Bitmap[]{bigPic, smallPic};
            return bitmaps;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**读取歌词*/
    public static  List<LrcRow> getLrcRows(String name) {

        File file = new File(MiYueConstans.LRC_PATH + name);
        if(!file.exists()){
            UtilLog.e(TAG, "歌词文件不存在");
            return null;
        }
        List<LrcRow> rows = null;
        InputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (is == null) {
                return null;
            }
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuilder sb = new StringBuilder();
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            rows = LrcParseUitls.getIstance().getLrcRows(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rows;
    }


    /**
     *  获取Mp3的名字
     * */
    public static String getMp3Name(String title, String artist){
        artist = StringUtils.stringFilter(artist);
        title = StringUtils.stringFilter(title);
        if (StringUtils.isNullOrEmpty(artist)) {
            artist = MiYueApp.getInstance().getString(R.string.unknown);
        }
        if (StringUtils.isNullOrEmpty(title)) {
            title = MiYueApp.getInstance().getString(R.string.unknown);
        }
        return title + " - " + artist + MP3;
    }

    /**
     *  获取Mp3的名字
     * */
    public static String getLrcName(String title, String artist){
        artist = StringUtils.stringFilter(artist);
        title = StringUtils.stringFilter(title);
        if (StringUtils.isNullOrEmpty(artist)) {
            artist = MiYueApp.getInstance().getString(R.string.unknown);
        }
        if (StringUtils.isNullOrEmpty(title)) {
            title = MiYueApp.getInstance().getString(R.string.unknown);
        }
        return title + " - " + artist + LRC;
    }
    private static boolean creteDir(File file){
        if(!file.getParentFile().exists()) {
            if(!file.getParentFile().mkdirs()) {
                UtilLog.e(TAG, "创建目标文件所在目录失败！");
                return false;
            }else{
                return true;
            }
        }
        return true;
    }
    public static String mkdirs(String dir) {
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        return dir;
    }
}
