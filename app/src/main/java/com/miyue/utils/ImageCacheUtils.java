package com.miyue.utils;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.LruCache;

import com.miyue.http.HttpApi;
/**
*
* @author ZZD
* @time 17/6/1 下午5:17
*/
public class ImageCacheUtils {
    public static final String TAG = "ImageCacheUtils" ;

    private static final int MAX_ALBUM_ART_CACHE_SIZE = 10*1024*1024;  // 10 MB

    private static final int BIG_BITMAP_INDEX = 0;
    private static final int SMALL_BITMAP_INDEX = 1;


    private final LruCache<String, Bitmap[]>  mCache;

    private static final ImageCacheUtils sInstance = new ImageCacheUtils();

    public static ImageCacheUtils getInstance() {
        return sInstance;
    }


    public ImageCacheUtils(){
        int maxSize = Math.min(MAX_ALBUM_ART_CACHE_SIZE,
                (int) (Math.min(Integer.MAX_VALUE, Runtime.getRuntime().maxMemory()/4)));
        mCache = new LruCache<String, Bitmap[]>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap[] value) {
                return value[BIG_BITMAP_INDEX].getByteCount()
                        + value[SMALL_BITMAP_INDEX].getByteCount();
            }
        };
    }


    public Bitmap getBigImage(String artUrl) {
        Bitmap[] result = mCache.get(artUrl);
        return result == null ? null : result[BIG_BITMAP_INDEX];
    }

    public Bitmap getSmallImage(String artUrl) {
        Bitmap[] result = mCache.get(artUrl);
        return result == null ? null : result[SMALL_BITMAP_INDEX];
    }

    public void fetch(final String artUrl, final FetchListener listener) {
        UtilLog.e("url", "AlbumURL: " + artUrl);
        Bitmap[] bitmap = mCache.get(artUrl);
        if(bitmap == null){
            bitmap = FileUtils.getArtPic(artUrl.hashCode()+"");
            if(bitmap != null){
                mCache.put(artUrl, bitmap);
                listener.onFetched(artUrl, bitmap[BIG_BITMAP_INDEX], bitmap[SMALL_BITMAP_INDEX]);
                return;
            }
        }else{
            listener.onFetched(artUrl, bitmap[BIG_BITMAP_INDEX], bitmap[SMALL_BITMAP_INDEX]);
            return;
        }
        new AsyncTask<Void, Void, Bitmap[]>() {
            @Override
            protected Bitmap[] doInBackground(Void[] objects) {
                Bitmap[] bitmaps;
                Bitmap bigBitmap = HttpApi.getAlbumPic(artUrl);
                if(bigBitmap == null){
                    return null;
                }
                Bitmap smalBitmap = BitmapUtils.getScaleBitmap(bigBitmap, 3);
                bitmaps = new Bitmap[] {bigBitmap, smalBitmap};
                FileUtils.downPic(bigBitmap, artUrl.hashCode()+"");
                mCache.put(artUrl, bitmaps);
                UtilLog.e(TAG, "当前mCacheSize: " + mCache.size());
                return bitmaps;
            }

            @Override
            protected void onPostExecute(Bitmap[] bitmaps) {
                if (bitmaps == null) {
                    listener.onError(new IllegalArgumentException("获取空图片了"));
                } else {
                    listener.onFetched(artUrl,
                            bitmaps[BIG_BITMAP_INDEX], bitmaps[SMALL_BITMAP_INDEX]);
                }
            }
        }.execute();
    }

    public static abstract class FetchListener {
        public abstract void onFetched(String artUrl, Bitmap bigImage, Bitmap iconImage);
        public abstract void onError(Exception e);
    }
}
