package com.miyue.utils;

import android.content.Context;
import android.media.AudioFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

    /**
     * <功能详细描述>判断网络是否可用<br>
     *
     * @param context
     * @return<br>
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断是否联网
     *
     * @param context
     * @return
     */
    public static boolean isNetConn(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {
                String name = info.getTypeName();
                UtilLog.e("net", "联网方式" + name);
                return true;
            } else {
                UtilLog.e("net", "断网");
                return false;
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return false;
    }
}
