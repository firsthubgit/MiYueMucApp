package com.miyue.utils;

import android.media.AudioManager;
import android.media.MediaPlayer;

import com.miyue.application.MiYueConstans;
import com.miyue.service.PlayerService;

import java.io.IOException;
import java.util.Random;

/**
 * Created by zhangzhendong on 16/5/4.
 */
public class PlayerHelper {
    private static volatile PlayerHelper playerHelper;
    private static MediaPlayer mediaPlayer;

    private PlayerHelper(){}

    public static PlayerHelper getInstance(){
        if(playerHelper == null){
            synchronized (PlayerHelper.class){
                if(playerHelper == null){
                    playerHelper = new PlayerHelper();
                }
            }
        }
        if(mediaPlayer == null){
            mediaPlayer = new MediaPlayer();
        }
        return playerHelper;
    }

    /**
     * 拿到mediaPlayer对象后,要初始化
     * */
    public void initPlayer(){
        mediaPlayer.setOnCompletionListener(new PlayerOnCompletionListener());
    }

    /**
     * 播放音乐
     * @param musicPath 文件的路径
     * */
    public void play(String musicPath){
        try {
            mediaPlayer.reset();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(musicPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 暂停音乐
     * */
    public void pause(){
        mediaPlayer.pause();
    }

    /**
     * 继续播放
     * */
    public void continuePlay(){
        mediaPlayer.start();
    }
    /**
     * 停止音乐
     * */
    public void stop(){
        mediaPlayer.stop();
    }
    /**
     * 释放播放器
     * */
    public void release(){
        mediaPlayer.release();
    }

    /**
     * 获得当前音乐的播放位置
     * */
    public int getPlayCurrentTime(){
        return mediaPlayer.getCurrentPosition();
    }

    /**
     * 获取歌曲的时长
     * */
    public int getPlayDuration(){
        return mediaPlayer.getDuration();
    }
    /**
     * 播放歌曲的指定位置
     * @param seek 指定的位置
     * */
    public void seekToMusicAndPlay(int seek){
        mediaPlayer.seekTo(seek);
        mediaPlayer.start();
    }

    /**
     * 判断音乐是否正在播放
     * */
    public boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }



    private  class PlayerOnCompletionListener implements
            MediaPlayer.OnCompletionListener{

        @Override
        public void onCompletion(MediaPlayer mp) {
            switch (MiYueConstans.MODE_ORDER){
                case MiYueConstans.MODE_ORDER:

                    break;
                case MiYueConstans.MODE_RANDOM:

                    break;
                case MiYueConstans.MODE_SINGLE:
                    break;
                case MiYueConstans.MODE_LOOPER:
                    break;
            }
        }
    }
}
