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
     * unicode 转字韩文
     */
    public static String unicode2Korea(String unicode) {
        StringBuffer string = new StringBuffer();
        String[] hex = unicode.split(";");
        for (int i = 0; i < hex.length; i++) {
            String temp = hex[i].trim();
            if(temp.startsWith("&#")){
                temp = temp.substring(2);
                // 转换出每一个代码点
                int data = Integer.parseInt(temp, 10);
                string.append((char) data);
            } else{
                string.append(temp);
            }

        }
        return string.toString();
    }

    /**
     * 专辑 unicode 转字韩文
     */
    public static String unicodeToKoreaForAlbum(String unicode) {
        StringBuffer string = new StringBuffer();
        String[] hex = unicode.split("&amp;");
        for (int i = 0; i < hex.length; i++) {
            String temp = hex[i].trim();
            if(temp.startsWith("#")){
                int pos = temp.indexOf(";");
                temp = temp.substring(1, pos);
                int data = Integer.parseInt(temp, 10);
                string.append((char) data);
            } else{
                string.append(temp);
            }

        }
        return string.toString();
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
