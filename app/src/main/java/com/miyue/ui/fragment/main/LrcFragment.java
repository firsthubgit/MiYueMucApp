package com.miyue.ui.fragment.main;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.miyue.R;
import com.miyue.application.MiYueConstans;
import com.miyue.bean.QQSong;
import com.miyue.bean.SongsInfo;
import com.miyue.common.base.BaseMediaFragment;
import com.miyue.bean.LrcRow;
import com.miyue.http.HttpApi;
import com.miyue.utils.FileUtils;
import com.miyue.utils.LrcParseUitls;
import com.miyue.utils.NetWorkUtils;
import com.miyue.utils.StringUtils;
import com.miyue.widgets.LrcView;

import java.util.List;

/**
*
* @author ZZD
* @time 16/3/31 上午11:35
*/
public class LrcFragment extends BaseMediaFragment implements PlayFragment.SeekBarChangeListener{

    public static final String TAG = "LrcFragment";
    private LrcView mLrc_view;
    private TextView tv_down_lrc;

    private String mTitle;
    private String mArtist;
    private String mMediaID;

    private QueryLRCFromQQ mQueryLRCFromQQ;
    /**当前歌曲查找歌词，第一次点击*/
    private boolean isFirstClick = true;

    private FeatchQQSongTask mFeatchQQSongTask;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_lrc,null);
        mLrc_view = (LrcView) view.findViewById(R.id.lrc_view);
        tv_down_lrc = (TextView) view.findViewById(R.id.tv_down_lrc);
        initListener();
        return view;
    }

    private void initListener() {
        mLrc_view.setOnArrowClickListener(new LrcView.OnArrowClickListener() {
            @Override
            public void onArrowClick(int progress) {
                mMediaControllerCompat.getTransportControls().seekTo(progress);
            }
        });
        tv_down_lrc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mTitle == null || mArtist == null){
                    mActivity.showText("播放一首歌试试");
                    return;
                }
                if(isFirstClick){

                } else {
                    mActivity.showText("没有歌词!\n再戳，再戳我就自爆!!!");
                }
            }
        });
    }


    public void registCallback(){
        registBaseCallback();
    }

    public void updateLrc(String name) {
        List<LrcRow> list = FileUtils.getLrcRows(name);
        if (list != null && list.size() > 0) {
            tv_down_lrc.setVisibility(View.GONE);
            mLrc_view.setLrcRows(list);
        } else {
            tv_down_lrc.setVisibility(View.VISIBLE);
            mLrc_view.reset();
        }
    }

    public void udpateLrcFromLrc(String lrc){
        List<LrcRow> list = LrcParseUitls.getIstance().getLrcRows(lrc);
        if (list != null && list.size() > 0) {
            tv_down_lrc.setVisibility(View.GONE);
            mLrc_view.setLrcRows(list);
        } else {
            tv_down_lrc.setVisibility(View.VISIBLE);
            mLrc_view.reset();
        }
    }





    /*****************************************************************************************/
    @Override
    public void onPlaybackStateChangedForClien(@NonNull PlaybackStateCompat state) {
    }

    @Override
    public void onMetadataChangedForClien(MediaMetadataCompat metadata) {
        if (metadata != null) {
            isFirstClick = true;
            Bundle metaBundle = metadata.getBundle();
            mTitle = metaBundle.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
            mArtist = metaBundle.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
            mMediaID = metaBundle.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
            mLrc_view.reset();
            if(!FileUtils.isLRCFileExists(FileUtils.getLrcName(mTitle, mArtist))){
                //从搜索列表过来的只有id,其他有分号
//                String[] args = mMediaID.split(":");
//                getQueryQQLRCTask(args.length>1? args[1]:args[0]);
                String  searchKey = mTitle + " " + mArtist;
                getQQSongTask(searchKey);
            } else {
                updateLrc(FileUtils.getLrcName(mTitle, mArtist));
            }
        }
    }

    @Override
    public void onConnectedForClien() {
    }

    @Override
    public void onSeekBarChanged(int progress, boolean fromUser) {
        mLrc_view.seekTo(progress, true, fromUser);
    }
////////////////////////////////////////////////////////////////////////////////////

    private void getQQSongTask(String keyword){
        if(!NetWorkUtils.isConnected(mActivity)){
            mActivity.showText("你没有联网呢！");
            return;
        }
        if (mFeatchQQSongTask != null && (mFeatchQQSongTask.getStatus().equals(AsyncTask.Status.RUNNING)
                || mFeatchQQSongTask.getStatus().equals(AsyncTask.Status.PENDING))) {
            mFeatchQQSongTask.cancel(true);
        }
        mFeatchQQSongTask = new FeatchQQSongTask();
        mFeatchQQSongTask.execute(keyword);
    }


    /**获取QQ歌曲列表*/
    private class FeatchQQSongTask extends AsyncTask<String, Void, SongsInfo<QQSong>>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected SongsInfo<QQSong> doInBackground(String... params) {
            return HttpApi.getQQSongKeyword(params[0], 1);
        }

        @Override
        protected void onPostExecute(SongsInfo<QQSong> qqSongs) {
            super.onPostExecute(qqSongs);
            if(qqSongs == null){
                mActivity.showText("没有网络或者没有歌词！");
                return;
            }
            if (qqSongs.getList() != null && qqSongs.getList().size()>0){
                QQSong song = qqSongs.getList().get(0);
                getQueryQQLRCTask(song.getSongid());
            } else {
                mActivity.showText("没有网络或者没有歌词！");
            }
        }
    }


    /**从QQ音乐获取歌词*/
    public void getQueryQQLRCTask(String songID){
        if (mQueryLRCFromQQ != null && (mQueryLRCFromQQ.getStatus().equals(AsyncTask.Status.RUNNING)
                || mQueryLRCFromQQ.getStatus().equals(AsyncTask.Status.PENDING))) {
            mQueryLRCFromQQ.cancel(true);
        }
        mQueryLRCFromQQ = new QueryLRCFromQQ();
        mQueryLRCFromQQ.execute(songID);
    }

    /**从QQ音乐获取歌词*/
    public class QueryLRCFromQQ extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            String qqLRC = HttpApi.getQQLRC(params[0]);
            if(qqLRC == null){
                return null;
            }
            FileUtils.downLrc(qqLRC, FileUtils.getLrcName(mTitle, mArtist));
            return qqLRC;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(StringUtils.isNullOrEmpty(s)){
                mActivity.showText("没有找到歌词");
            }
            udpateLrcFromLrc(s);
        }
    }
}
