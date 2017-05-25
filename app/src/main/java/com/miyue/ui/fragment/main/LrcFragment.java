package com.miyue.ui.fragment.main;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.miyue.R;
import com.miyue.common.base.BaseMediaFragment;
import com.miyue.common.bean.LrcRow;
import com.miyue.dao.MusicProvider;
import com.miyue.utils.LrcParseUitls;
import com.miyue.widgets.LrcView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
*
* @author ZZD
* @time 16/3/31 上午11:35
*/
public class LrcFragment extends BaseMediaFragment implements PlayFragment.SeekBarChangeListener{

    private LrcView mLrc_view;
    private TextView tv_down_lrc;
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
    }


    public void registCallback(){
        registBaseCallback();
    }

    public void updateLrc(String name) {
        List<LrcRow> list = getLrcRows(name);
        if (list != null && list.size() > 0) {
            tv_down_lrc.setVisibility(View.INVISIBLE);
            mLrc_view.setLrcRows(list);
        } else {
            tv_down_lrc.setVisibility(View.VISIBLE);
            mLrc_view.reset();
        }
    }

    private List<LrcRow> getLrcRows(String name) {

        List<LrcRow> rows = null;
        InputStream is = null;
        try {
            is = new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/Music/Lrc/" + name + ".lrc");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (is == null) {
                return null;
            }
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuilder sb = new StringBuilder();
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            rows = LrcParseUitls.getIstance().getLrcRows(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rows;
    }
////////////////////////////////////////////////////////////
    @Override
    public void onPlaybackStateChangedForClien(@NonNull PlaybackStateCompat state) {
    }

    @Override
    public void onMetadataChangedForClien(MediaMetadataCompat metadata) {
        if (metadata != null) {
            String name = metadata.getBundle().getString(MusicProvider.MEDIA_FILE_NAME);
            String realname;
            if("".equals(name) || name == null){
                realname = (String) metadata.getDescription().getTitle();
            } else {
                int pos = name.lastIndexOf(".");
                if(pos == -1){
                    realname = name;
                } else {
                    realname = name.substring(0,name.lastIndexOf("."));
                }
            }
            updateLrc(realname);
        }
    }

    @Override
    public void onConnectedForClien() {
    }

    @Override
    public void onSeekBarChanged(int progress, boolean fromUser) {
        mLrc_view.seekTo(progress, true, fromUser);
    }
}
