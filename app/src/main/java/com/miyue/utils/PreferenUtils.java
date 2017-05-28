package com.miyue.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by zhangzhendong on 17/5/27.
 */

public class PreferenUtils {

    public static final String BACKGROUND_PATH = "pic_background_path";

    private static PreferenUtils sInstance;

    private static SharedPreferences mPreferences;

    public PreferenUtils(final Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static final PreferenUtils getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new PreferenUtils(context.getApplicationContext());
        }
        return sInstance;
    }

    public void exit(){
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong("exit_time", System.currentTimeMillis());
        editor.commit();
    }

    public PreferenUtils putBgPath(String path){
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(BACKGROUND_PATH, path);
        editor.apply();
        return sInstance;
    }

    public String getBgPath(){
        return mPreferences.getString(BACKGROUND_PATH, "");
    }
}
