package com.miyue.utils;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.ViewGroup;

import com.miyue.R;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zhangzhendong on 17/5/28.
 */

public class SkinUtils {

    public static Bitmap initBackground(Activity activity, ViewGroup viewbg) {
        AssetManager am = activity.getAssets();
        String bgPath = PreferenUtils.getInstance(activity).getBgPath();
        Bitmap bitmap = null;
        InputStream is = null;
        try {
            if("".equals(bgPath) || bgPath == null){
                is = am.open("bkgs/006.png");
            }else{
                is = am.open("bkgs/"+bgPath);
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            bitmap = BitmapFactory.decodeStream(is, null, options);
            viewbg.setBackground(new BitmapDrawable(activity.getResources(), bitmap));
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap setBackground(Activity activity, ViewGroup viewbg, String picPath){
        AssetManager am = activity.getAssets();
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            is = am.open("bkgs/"+picPath);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            bitmap = BitmapFactory.decodeStream(is, null, options);
            viewbg.setBackground(new BitmapDrawable(activity.getResources(), bitmap));
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
