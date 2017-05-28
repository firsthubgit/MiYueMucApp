package com.miyue.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.miyue.R;

import java.io.IOException;
import java.io.InputStream;


public class DisplayUtils {

    public static int statusBarHeight = 0;

    public static int screenWidth = 0;

    public static int screenHeight = 0;

    /**获取屏幕的宽度*/
    public static int getScreenWidth(Context context){
        if(screenWidth != 0){
            return screenWidth;
        }
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        //取得窗口属性
        wm.getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        return screenWidth;
    }
    /**获取屏幕的高度*/
    public static int getScreenHeight(Context context){
        if(screenHeight != 0){
            return screenHeight;
        }
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        //取得窗口属性
        wm.getDefaultDisplay().getMetrics(dm);
        screenHeight = dm.heightPixels;
        return screenHeight;
    }

    /**获取状态栏的高度*/
    public static int getStatusBarHeight(Context context) {
        if (statusBarHeight != 0)
            return statusBarHeight;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    /**
     * 设置背景图片
     * */
    public void initBackground(Context context,RelativeLayout allback) {
        AssetManager am = context.getAssets();
        String path = PreferenUtils.getInstance(context).getBgPath();

        if("".equals(path) || path == null){
            allback.setBackgroundResource(R.drawable.backgroud);
            return;
        }
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            is = am.open("bkgs/"+path);
            bitmap = BitmapFactory.decodeStream(is);
            allback.setBackground(new BitmapDrawable(context.getResources(), bitmap));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
