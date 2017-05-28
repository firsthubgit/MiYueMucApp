package com.miyue.utils;

import com.miyue.application.MiYueConstans;
import com.miyue.bean.LrcRow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by zhangzhendong on 17/5/27.
 */

public class FileUtils {

    public static final String TAG = "FileUtils";
    /**
     * 保存歌词到本地
     * */
    public static void downLrc(String lrc, String path){
        File file =new File(path);
        FileWriter fwLrc = null;
        try {
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

    /**读取歌词*/
    public static  List<LrcRow> getLrcRows(String name) {

        File file = new File(MiYueConstans.LRC_PATH+ name + ".lrc");
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

}
