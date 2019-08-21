package com.miyue.application;

import android.os.Environment;

/**
 * Created by zhangzhendong on 16/4/13.
 */
public class MiYueConstans {

     /**状态栏的高度*/
     public static final int STATUS_BAR_HEIGHT = 0;

     /**随机播放*/
     public static final int MODE_RANDOM = 0;
     /**单曲循环*/
     public static final int MODE_SINGLE = 1;
     /**顺序播放*/
     public static final int MODE_ORDER = 2;
     /**循环播放*/
     public static final int MODE_LOOPER = 3;

     public static String KEY = "";

     public static final String ABSOLUTE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

     public static final String LRC_PATH = ABSOLUTE_PATH + "/Music/Lrc/";

     public static final String ALBUM_CACHE_PATH = ABSOLUTE_PATH + "/Music/Album/";

     public static final String MUSIC_DOWN_PATH = ABSOLUTE_PATH+ "/Music/Download/";

     public static final String QQ_PIC_URL = "https://y.gtimg.cn/music/photo_new/T002R300x300M000专辑mid.jpg?max_age=2592000";

     public static final String QQ_PLAY_URL = "http://124.193.228.150/amobile.music.tc.qq.com/C400MEDIAID.m4a?vkey=AK&guid=joe&uin=0&fromtag=8";



     // 下载音乐成功
     public static final String CUSTOM_ACTION_DOWNLOAD_SUCCESS = "com.miyue.DOWNLOAD_SUCESS";
     // 删除音乐命令
     public static final String CUSTOM_ACTION_DELETE_CMD = "com.miyue.DELETE_CMD ";
     // 最爱音乐
     public static final String CUSTOM_ACTION_THUMBS_UP = "com.miyue.THUMBS_UP";



     //本地文件已经被删除
     public static final String ERROR_NO_FILE = "com.miyue.NO_FILE";

     // Extra on MediaSession that contains the Cast device name currently connected to
     public static final String EXTRA_CONNECTED_CAST = "com.example.android.uamp.CAST_NAME";
     // The action of the incoming Intent indicating that it contains a command
     // to be executed (see {@link #onStartCommand})
     public static final String ACTION_CMD = "com.example.android.uamp.ACTION_CMD";
     // The key in the extras of the incoming Intent indicating the command that
     // should be executed (see {@link #onStartCommand})
     public static final String CMD_NAME = "CMD_NAME";
     // A value of a CMD_NAME key in the extras of the incoming Intent that
     // indicates that the music playback should be paused (see {@link #onStartCommand})
     public static final String CMD_PAUSE = "CMD_PAUSE";
     // A value of a CMD_NAME key that indicates that the music playback should switch
     // to local playback from cast playback.
     public static final String CMD_STOP_CASTING = "CMD_STOP_CASTING";
}
