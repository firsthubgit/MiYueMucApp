package com.miyue.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.miyue.R;

/**
 * Created by zhangzhendong on 17/5/31.
 */

public class NetWorkUtils {

    private static ConnectivityManager sConnManager = null;


    public static boolean isConnected(Context context) {
        ConnectivityManager connManager = getConnManager(context);
        if(connManager != null) {
            try {
                NetworkInfo e = connManager.getActiveNetworkInfo();
                if(e != null) {
                    return e.isConnected();
                }
            } catch (Exception e) {
                UtilLog.e("NetworkUtils", e.toString());
            }
        } else {
            UtilLog.e("NetworkUtils", "connManager is null!");
        }

        return false;
    }

    public static ConnectivityManager getConnManager(Context context) {
        if(context == null) {
            UtilLog.e("NetworkUtils", "context is null!");
            return null;
        } else {
            if(sConnManager == null) {
                sConnManager = (ConnectivityManager)context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
            }

            return sConnManager;
        }
    }
}
