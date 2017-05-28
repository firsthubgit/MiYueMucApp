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
import com.miyue.common.base.BaseMediaFragment;
import com.miyue.bean.LrcRow;
import com.miyue.http.HttpApi;
import com.miyue.utils.FileUtils;
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

    private QureyLRCTask mQureyLRCTask;
    /**当前歌曲查找歌词，第一次点击*/
    private boolean isFirstClick = true;
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
                if(isFirstClick){
                    isFirstClick = false;
                    getQueryLrcTask();
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
            updateLrc(mTitle + ":" + mArtist);
        }
    }

    @Override
    public void onConnectedForClien() {
    }

    @Override
    public void onSeekBarChanged(int progress, boolean fromUser) {
        mLrc_view.seekTo(progress, true, fromUser);
    }


    public void getQueryLrcTask(){
        if (mQureyLRCTask != null && (mQureyLRCTask.getStatus().equals(AsyncTask.Status.RUNNING)
                || mQureyLRCTask.getStatus().equals(AsyncTask.Status.PENDING))) {
            mQureyLRCTask.cancel(true);
        }
        mQureyLRCTask = new QureyLRCTask();
        mQureyLRCTask.execute(mTitle, mArtist);
    }


    public class QureyLRCTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            String lrc = HttpApi.getLrc(params);
            if(lrc == null){
                return null;
            }
            FileUtils.downLrc(lrc, MiYueConstans.LRC_PATH+ params[0]+ ":"+ params[1] + ".lrc");
            return lrc;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(TextUtils.isEmpty(s)){
                mActivity.showText("没有找到歌词");
            }else{
                updateLrc(mTitle + ":" + mArtist);
            }
        }
    }
}
