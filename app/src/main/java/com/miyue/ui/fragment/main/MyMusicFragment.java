package com.miyue.ui.fragment.main;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.miyue.R;
import com.miyue.application.DbConstans;
import com.miyue.application.MiYueConstans;
import com.miyue.common.base.BaseMediaFragment;
import com.miyue.common.listener.CallBack;
import com.miyue.ui.adapter.MyMusicListAdapter;
import com.miyue.ui.fragment.my.CommenListFragment;
import com.miyue.ui.fragment.my.DownloadFragment;
import com.miyue.ui.fragment.my.LocalMusicFragment;
import com.miyue.ui.fragment.my.RecentPlayFragment;
import com.miyue.utils.FragmentControl;
import com.miyue.utils.UtilLog;

import java.util.ArrayList;
import java.util.List;

/**
* @author ZZD
* @time 16/3/31
*/
public class MyMusicFragment extends BaseMediaFragment {

    private static final String TAG = "MyMusicFragment";
    private View my_music_frg;
    private ListView music_list_list;
    private View headerView;
    private LinearLayout ll_local_music, ll_download_manage, ll_recent_play;
    private FragmentControl fragmentControl;
    private RelativeLayout rl_my_frag;
    private TextView tv_local_num, tv_download_num, tv_recent_num;
    private LocalMusicFragment localMusicFragment;
    private DownloadFragment mDownloadFragment;
    private Context mContext;
    private RecentPlayFragment mRecentPlayFragment;

    private CommenListFragment mCommenListFragment;
    private MyMusicListAdapter mAlbumAdapter;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        my_music_frg = inflater.inflate(R.layout.layout_my_music,null);
        headerView = inflater.inflate(R.layout.my_music_headerview,null);

        fragmentControl = FragmentControl.getFragConInstance();
        initView();
        new CheckTask().execute();
        return my_music_frg;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private void initView(){
        rl_my_frag = (RelativeLayout) my_music_frg.findViewById(R.id.rl_my_frag);
        music_list_list = (ListView)my_music_frg.findViewById(R.id.lv_my_music_list);
        music_list_list.addHeaderView(headerView, null, false);

        ll_local_music = (LinearLayout) headerView.findViewById(R.id.ll_local_music);
        ll_download_manage = (LinearLayout) headerView.findViewById(R.id.ll_download_manage);
        ll_recent_play = (LinearLayout) headerView.findViewById(R.id.ll_recent_play);
        tv_local_num = (TextView) headerView.findViewById(R.id.tv_local_num);
        tv_download_num = (TextView) headerView.findViewById(R.id.tv_download_num);
        tv_recent_num = (TextView) headerView.findViewById(R.id.tv_recent_num);

        ll_local_music.setOnClickListener(onClickListener);
        ll_download_manage.setOnClickListener(onClickListener);
        ll_recent_play.setOnClickListener(onClickListener);


        mAlbumAdapter = new MyMusicListAdapter(initData(), getActivity());
        music_list_list.setAdapter(mAlbumAdapter);
        music_list_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                music_list_list.setVisibility(View.GONE);
                if(null == mCommenListFragment){
                    UtilLog.e(TAG,"New CommenListFragment");
                    mCommenListFragment = CommenListFragment.newInstance(DbConstans.FAVORITES);
                    mCommenListFragment.setOnBackListener(new CallBack() {
                        @Override
                        public void call() {
                            popback();
                        }
                    });
                }
                fragmentControl.showMyFragment(MyMusicFragment.this,mCommenListFragment);
            }
        });
    }

    View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {

            switch (v.getId()){
                case R.id.ll_local_music:
                    UtilLog.e(TAG,"本地歌曲点了");
                    music_list_list.setVisibility(View.GONE);
                    if(null == localMusicFragment){
                        UtilLog.e(TAG,"New LocalMusicFragment");
                        localMusicFragment = LocalMusicFragment.newInstance(DbConstans.LOCAL_MUSIC);
                        localMusicFragment.setOnBackListener(new CallBack() {
                            @Override
                            public void call() {
                                popback();
                            }
                        });
                    }
                    fragmentControl.showMyFragment(MyMusicFragment.this,localMusicFragment);

                    break;
                case R.id.ll_download_manage:
                    UtilLog.e(TAG,"下载歌曲");
                    music_list_list.setVisibility(View.GONE);
                    if(null == mDownloadFragment){
                        UtilLog.e(TAG,"New DownloadFragment");
                        mDownloadFragment = DownloadFragment.newInstance(DbConstans.DOWNLOAD);
                        mDownloadFragment.setOnBackListener(new CallBack() {
                            @Override
                            public void call() {
                                popback();
                            }
                        });
                    }
                    fragmentControl.showMyFragment(MyMusicFragment.this,mDownloadFragment);
                    break;
                case R.id.ll_recent_play:
                    UtilLog.e(TAG,"最近播放");
                    music_list_list.setVisibility(View.GONE);
                    if(null == mRecentPlayFragment){
                        UtilLog.e(TAG,"New RecentPlayFragment");
                        mRecentPlayFragment = RecentPlayFragment.newInstance(DbConstans.RECENT);
                        mRecentPlayFragment.setOnBackListener(new CallBack() {
                            @Override
                            public void call() {
                                popback();
                            }
                        });
                    }
                    fragmentControl.showMyFragment(MyMusicFragment.this,mRecentPlayFragment);
                    break;
            }
        }
    };


    public void registCallback(){
        registBaseCallback();
    }

    public ArrayList<String> initData(){
        ArrayList<String> list = new ArrayList<String>();
        list.add("我喜欢的");
        return list;
    }

    public void popback(){
        music_list_list.setVisibility(View.VISIBLE);
        getChildFragmentManager().popBackStackImmediate();
    }
    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    Bundle bundle = msg.getData();
                    tv_local_num.setText(bundle.getString(DbConstans.LOCAL_MUSIC));
                    tv_download_num.setText(bundle.getString(DbConstans.DOWNLOAD));
                    tv_recent_num.setText(bundle.getString(DbConstans.RECENT));
                    mAlbumAdapter.setMyLikeNum(bundle.getString(DbConstans.FAVORITES));
                    break;
            }
            return false;
        }
    });


    public class CheckTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            long  localCount = mDBHelper.getLocalMusicCount();
            long  downCount = mDBHelper.getDownLoadMusicCount();
            long  recentCount = mDBHelper.getRecnetMusicCount();
            long  favorite = mDBHelper.getFavoriteMusicCount();
            Message msg = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putString(DbConstans.LOCAL_MUSIC, String.valueOf(localCount));
            bundle.putString(DbConstans.DOWNLOAD, String.valueOf(downCount));
            bundle.putString(DbConstans.RECENT, String.valueOf(recentCount));
            bundle.putString(DbConstans.FAVORITES, String.valueOf(favorite));
            msg.setData(bundle);
            msg.what = 1;
            mHandler.sendMessage(msg);
            return null;
        }
    }

    private void dealAction(List<PlaybackStateCompat.CustomAction> actions) {

        for(PlaybackStateCompat.CustomAction customAction : actions){
            String command = customAction.getAction();
            switch (command){
                case MiYueConstans.CUSTOM_ACTION_DELETE_CMD:
                case MiYueConstans.CUSTOM_ACTION_DOWNLOAD_SUCCESS:
                    new CheckTask().execute();
                    break;
                case MiYueConstans.CUSTOM_ACTION_THUMBS_UP:
                    long favoriteCount = mDBHelper.getFavoriteMusicCount();
                    mAlbumAdapter.setMyLikeNum(favoriteCount+"");
                    break;
            }
        }
    }

    @Override
    public void onPlaybackStateChangedForClien(@NonNull PlaybackStateCompat state) {
        List<PlaybackStateCompat.CustomAction> actions= state.getCustomActions();
        if(actions != null && actions.size()>0){
            dealAction(actions);
        }
    }

    @Override
    public void onMetadataChangedForClien(MediaMetadataCompat metadata) {
        long  recentCount = mDBHelper.getRecnetMusicCount();
        tv_recent_num.setText(recentCount+"");
    }

    @Override
    public void onConnectedForClien() {

    }
}

