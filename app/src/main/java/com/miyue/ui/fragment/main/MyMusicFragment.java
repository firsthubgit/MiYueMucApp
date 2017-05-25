package com.miyue.ui.fragment.main;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.greendao.DBHelper;
import com.greendao.MusicBean;
import com.miyue.R;
import com.miyue.application.DbConstans;
import com.miyue.dao.MusicProvider;
import com.miyue.ui.adapter.MyMusicListAdapter;
import com.miyue.ui.fragment.my.LocalMusicFragment;
import com.miyue.ui.fragment.my.RecentPlayFragment;
import com.miyue.utils.FragmentControl;
import com.miyue.utils.UtilLog;

import java.io.File;
import java.util.ArrayList;

/**
* @author ZZD
* @time 16/3/31
*/
public class MyMusicFragment extends Fragment {
    private View my_music_frg;
    private ListView music_list_list;
    private View headerView;
    private LinearLayout ll_local_music, ll_download_manage, ll_recent_play;
    private FragmentControl fragmentControl;
    private DBHelper dbHelper;
    private RelativeLayout rl_my_frag;
    private TextView tv_local_num;
    private LocalMusicFragment localMusicFragment;
    private Context mContext;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        my_music_frg = inflater.inflate(R.layout.layout_my_music,null);
        rl_my_frag = (RelativeLayout) my_music_frg.findViewById(R.id.rl_my_frag);
        headerView = inflater.inflate(R.layout.my_music_headerview,null);
        tv_local_num = (TextView) headerView.findViewById(R.id.tv_local_num);
        fragmentControl = FragmentControl.getFragConInstance();
        dbHelper = DBHelper.getInstance(mContext);
        initView();
        new CheckTask().execute();
        return my_music_frg;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private void initView(){
        music_list_list = (ListView)my_music_frg.findViewById(R.id.lv_my_music_list);
        music_list_list.addHeaderView(headerView, null, false);

        ll_local_music = (LinearLayout) headerView.findViewById(R.id.ll_local_music);
        ll_download_manage = (LinearLayout) headerView.findViewById(R.id.ll_download_manage);
        ll_recent_play = (LinearLayout) headerView.findViewById(R.id.ll_recent_play);


        ll_local_music.setOnClickListener(onClickListener);
        ll_download_manage.setOnClickListener(onClickListener);
        ll_recent_play.setOnClickListener(onClickListener);


        MyMusicListAdapter listAdapter = new MyMusicListAdapter(initData(), getActivity());
        music_list_list.setAdapter(listAdapter);
    }

    View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {

            switch (v.getId()){
                case R.id.ll_local_music:
                    UtilLog.e("MyMusicFrag","本地歌曲点了");
                    music_list_list.setVisibility(View.GONE);
                    if(null == localMusicFragment){
                        UtilLog.e("MyMusicFrag","New Fragment");
                        localMusicFragment = LocalMusicFragment.newInstance(DbConstans.LOCAL_MUSIC);
                        localMusicFragment.setOnBackListener(new LocalMusicFragment.CallBack() {
                            @Override
                            public void call() {
                                popback();
                            }
                        });
                    }
                    fragmentControl.showMyFragment(MyMusicFragment.this,localMusicFragment);

                    break;
                case R.id.ll_download_manage:
                    break;
                case R.id.ll_recent_play:
                    UtilLog.e("MyMusicFrag","最近播放");
                    RecentPlayFragment rpf = new RecentPlayFragment();
                    rpf.setOnBackListener(new RecentPlayFragment.CallBack() {
                        @Override
                        public void call() {
                        }
                    });
                    fragmentControl.showMainFragment(rpf,"",0);

//                    fc.showFragment(new RecentPlayFragment(),"RencentPlayFragment",0);
//                    FragmentManager fm = getChildFragmentManager();
//                    FragmentTransaction ft = fm.beginTransaction();
//                    ft.replace(R.id.rl_my_frag, rpf);
//                    ft.addToBackStack(null);
//                    ft.commit();
                    break;
            }
        }
    };


    public ArrayList<String> initData(){
        ArrayList<String> list = new ArrayList<String>();
        list.add("ha");
        list.add("nihao");
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
                    long musicNum = (long)msg.obj;
                    tv_local_num.setText(musicNum+"");
                    break;
            }
            return false;
        }
    });


    public class CheckTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            long  number = dbHelper.getLocalMusicCount();
            Message msg = Message.obtain();
            msg.obj = number;
            msg.what = 1;
            mHandler.sendMessage(msg);
            return null;
        }
    }
}

