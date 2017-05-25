package com.miyue.utils;

import android.content.Context;
import android.media.AudioFormat;
import android.os.Build;
import android.widget.Toast;

/**
 * Created by zhangzhendong on 16/5/13.
 */
public class CommentUtils {

    /**
     * toast （默认 时间Toast.LENGTH_SHORT）
     *
     * @param context
     * @param msg  内容
     */
    public static void toast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 当前系统版本不小于21
     * @param versioncode 当前版本
     * */
    public static boolean sdkNotSmallerThan21(int versioncode){
        if(versioncode >= Build.VERSION_CODES.LOLLIPOP){
            return true;
        }
        return false;
    }
}
