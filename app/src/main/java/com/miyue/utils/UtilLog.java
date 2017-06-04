package com.miyue.utils;

import android.util.Log;

/**
 * Created by zhangzhendong on 16/4/7.
 */
public class UtilLog {

    /**
     * 是否开启日志
     */
    public static boolean isTest = true;

    public static void d(String tag, String value){
        if(isTest){
            Log.d(tag,value);
        }
    }
    public static void i(String tag, String value){
        if(isTest){
            Log.i(tag,value);
        }
    }
    public static void e(String tag, String value){
        if(isTest){
            Log.e(tag,value);
        }
    } public static void w(String tag, String value){
        if(isTest){
            Log.w(tag,value);
        }
    }
    public static void m(String tag, String value){
        if(isTest){
            Log.d(tag,value);
        }
    }

    public static void url(String value){
        if(isTest){
            Log.e("url",value);
        }
    }
}
