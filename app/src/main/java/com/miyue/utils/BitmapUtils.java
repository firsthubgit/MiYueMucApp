package com.miyue.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zhangzhendong on 17/5/27.
 */

public class BitmapUtils {


    /**
     * 对图片宽高和分表率压缩
     * */
    public static Bitmap getImageThumbnail(String imagePath, int maxWidth,
                                           int maxHeight, boolean isDeleteFile) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        options.inJustDecodeBounds = false;
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
        FileInputStream inputStream = null;
        File file = new File(imagePath);
        try {
            if(file != null && file.exists()){
                if (file.length() < 20480) { // 0-20k
                    options.inSampleSize = 1;
                } else if (file.length() < 51200) { // 20-50k
                    options.inSampleSize = 2;
                } else if (file.length() < 307200) { // 50-300k
                    options.inSampleSize = 4;
                } else if (file.length() < 819200) { // 300-800k
                    options.inSampleSize = 6;
                } else if (file.length() < 1048576) { // 800-1024k
                    options.inSampleSize = 8;
                } else {
                    options.inSampleSize = 10;
                }
                inputStream = new FileInputStream(file);
                bitmap = BitmapFactory.decodeStream(inputStream, null,options);
                if(isDeleteFile){
                    file.delete();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }
    }

    /**
     * 对图片宽高和分表率压缩
     * */
    public static Bitmap getImageSmall(InputStream inputStream, int maxWidth,
                                       int maxHeight) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inJustDecodeBounds = false;
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
        try {
            int size = inputStream.available();
            if (size < 20480) { // 0-20k
                options.inSampleSize = 1;
            } else if (size < 51200) { // 20-50k
                options.inSampleSize = 2;
            } else if (size < 307200) { // 50-300k
                options.inSampleSize = 4;
            } else if (size < 819200) { // 300-800k
                options.inSampleSize = 6;
            } else if (size < 1048576) { // 800-1024k
                options.inSampleSize = 8;
            } else {
                options.inSampleSize = 10;
            }
            bitmap = BitmapFactory.decodeStream(inputStream, null,options);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return bitmap;
        }
    }
    /**
     * 计算像素压缩的缩放比例
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;


        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }
}
