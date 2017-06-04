package com.miyue.ui.fragment.main;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.greendao.DBHelper;
import com.greendao.SearchHis;
import com.miyue.R;
import com.miyue.application.MiYueConstans;
import com.miyue.bean.QQSong;
import com.miyue.bean.SongsInfo;
import com.miyue.common.base.BaseActivity;
import com.miyue.common.base.BaseMediaFragment;
import com.miyue.http.HttpApi;
import com.miyue.service.playback.MusicProvider;
import com.miyue.ui.adapter.OnLineMusicAdapter;
import com.miyue.utils.FileUtils;
import com.miyue.utils.MusicUtils;
import com.miyue.utils.NetWorkUtils;
import com.miyue.utils.StringUtils;
import com.miyue.utils.UtilLog;

import java.util.ArrayList;
import java.util.List;

/**
*
* @author ZZD
* @time 16/3/31 上午10:26
*/
public class SearchFragment extends BaseMediaFragment implements OnLineMusicAdapter.OnDownClickListener{

    private static final String TAG = "SearchFragment";
    private EditText et_find_music;
    private ImageView iv_clear_edit;

    private DBHelper dbHelper;
    private Context mContext;
    private ListView lv_search_histroy;
    private ListView lv_online_music;
    private RelativeLayout rl_search_histroy;
    private TextView tv_clearall_histroy;

    /**历史记录实体类*/
    private List<SearchHis> mSeaList;
    /**搜索历史记录StringHistroy*/
    private List<String> mStList = new ArrayList<String>();

    private ArrayAdapter mHistroyAdapter;

    /**分页加载，当前页数*/
    private int mCurrentPage = 1;
    private List<QQSong> mQQSongs = new ArrayList<>();
    private OnLineMusicAdapter mOnLineMusicAdapter;

    private FeatchQQSongTask mFeatchQQSongTask;

    private DownMusicTask mDownMusicTask;
    private boolean isLoading;

    private String mKeyword;

    private int mTotalCount;



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        dbHelper = DBHelper.getInstance(mContext);
        getHistroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_find_music, null);
        et_find_music = (EditText) view.findViewById(R.id.et_find_music);
        iv_clear_edit = (ImageView) view.findViewById(R.id.iv_clear_edit);
        iv_clear_edit.setVisibility(View.INVISIBLE);
        tv_clearall_histroy = (TextView) view.findViewById(R.id.tv_clearall_histroy);

        rl_search_histroy = (RelativeLayout) view.findViewById(R.id.rl_search_histroy);

        if(mStList.size() == 0){
            rl_search_histroy.setVisibility(View.GONE);
        }else{
            rl_search_histroy.setVisibility(View.VISIBLE);
        }

        lv_search_histroy = (ListView) view.findViewById(R.id.lv_search_histroy);
        mHistroyAdapter = new ArrayAdapter<String>(mContext,R.layout.item_seahist_list,R.id.tv_seahistroy,mStList);
        lv_search_histroy.setAdapter(mHistroyAdapter);

        lv_online_music = (ListView) view.findViewById(R.id.lv_online_music);
        mOnLineMusicAdapter = new OnLineMusicAdapter(mContext,mQQSongs);
        mOnLineMusicAdapter.setOnDownClickListener(this);
        lv_online_music.setAdapter(mOnLineMusicAdapter);
        lv_online_music.setVisibility(View.GONE);

        initListener();
        return view;
    }

    private void initListener() {
        tv_clearall_histroy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.clearAllHistroy();
                mStList.clear();
                mHistroyAdapter.notifyDataSetChanged();
                rl_search_histroy.setVisibility(View.GONE);
            }
        });
        iv_clear_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_clear_edit.setVisibility(View.GONE);
                et_find_music.setText("");
                mCurrentPage = 1;
                mTotalCount = 0;
                mKeyword = "";

                updateStList();
                mHistroyAdapter.notifyDataSetChanged();
                rl_search_histroy.setVisibility(View.VISIBLE);

                lv_online_music.setVisibility(View.GONE);
                mQQSongs.clear();
                mOnLineMusicAdapter.notifyDataSetChanged();
            }
        });

        et_find_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_find_music.setCursorVisible(true);
            }
        });
        et_find_music.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    mKeyword = et_find_music.getText().toString().trim();
                    if(StringUtils.isNullOrEmpty(mKeyword)){
                        return false;
                    }
                    doSearchKeyword(mKeyword);
                }
                return false;
            }
        });
        lv_search_histroy.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mKeyword = (String) mHistroyAdapter.getItem(position);
                doSearchKeyword(mKeyword);
                et_find_music.setText(mKeyword);
            }
        });
        /**点击音乐item*/
        lv_online_music.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QQSong song = mOnLineMusicAdapter.getItem(position);
                Bundle bundle = MusicUtils.creSongBundle(song, false);
                UtilLog.url("播放URL" + bundle.getString(MusicProvider.MEDIA_NET_PLAY_URL));
                Uri songUri = Uri.parse(bundle.getString(MusicProvider.MEDIA_NET_PLAY_URL));
                mMediaControllerCompat.getTransportControls().playFromUri(songUri, bundle);
            }
        });
        lv_online_music.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // 移动到最后一条开始执行加载更多的操作
                // 如果当前已滑到最后一条，并且当前条目数小于总条目数，并且当前没有在请求数据(便面重复请求)，则进行新的请求
                if (firstVisibleItem + visibleItemCount - mQQSongs.size() >= 0 && !isLoading
                        && mQQSongs.size() < mTotalCount) {
                    if(!StringUtils.isNullOrEmpty(mKeyword)){
                        isLoading = true;
                        mCurrentPage++;
                        getQQSongTask(mKeyword);
                    }
                }
            }
        });
    }


    @Override
    public void onPause() {
        super.onPause();
        et_find_music.clearFocus();
    }



    private void getHistroy(){
        mSeaList = dbHelper.getSearchHistroy();
        for(int i = mSeaList.size()-1; i>=0; i--){
            mStList.add(mSeaList.get(i).getSmitem());
        }
    }

    private void updateStList(){
        mStList.clear();
        mSeaList = dbHelper.getSearchHistroy();
        for(int i = mSeaList.size()-1; i>=0; i--){
            mStList.add(mSeaList.get(i).getSmitem());
        }

    }

    /**搜索前页面UI的变化*/
    private void doSearchKeyword(String keyword){
        iv_clear_edit.setVisibility(View.VISIBLE);
        et_find_music.clearFocus();
        mQQSongs.clear();
        mCurrentPage = 1;
        mTotalCount = 0;
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et_find_music.getWindowToken(), 0); //强制隐藏键盘
        SearchHis hisbean = new SearchHis();
        hisbean.setSmitem(keyword);
        dbHelper.addHistroy(hisbean);
        rl_search_histroy.setVisibility(View.GONE);
        getQQSongTask(keyword);
    }

    public void registCallback(){
        registBaseCallback();
    }


/*****************获取音乐列表*******************************************************/

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
        private String keyword;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isLoading = true;
        }

        @Override
        protected SongsInfo<QQSong> doInBackground(String... params) {
            keyword = params[0];
            return HttpApi.getQQSongKeyword(params[0], mCurrentPage);
        }

        @Override
        protected void onPostExecute(SongsInfo<QQSong> qqSongs) {
            super.onPostExecute(qqSongs);
            isLoading = false;
            if(qqSongs == null){
                ((BaseActivity)mContext).showText("没有网络或者没有数据！");
                return;
            }
            if (qqSongs.getList() != null && qqSongs.getList().size()>0){
                if(1 == mCurrentPage ){
                    mTotalCount = Integer.parseInt(qqSongs.getTotalnum());
                    mQQSongs.addAll(qqSongs.getList());
                    if(mQQSongs.size() < mTotalCount){
                        mCurrentPage++;
                        getQQSongTask(keyword);
                    }
                } else {
                    mQQSongs.addAll(qqSongs.getList());
                }
                lv_online_music.setVisibility(View.VISIBLE);
                mOnLineMusicAdapter.notifyDataSetChanged();
            } else {
                ((BaseActivity)mContext).showText("没有网络或者没有数据！");
            }
        }
    }


/*********************************下载音乐********************************************/
    @Override
    public void onDownClick(QQSong qqSong) {
        startDownMsicTask(qqSong);
    }

    private void startDownMsicTask(QQSong qqSong){
        if(!NetWorkUtils.isConnected(mActivity)){
            mActivity.showText("你没有联网呢！");
            return;
        }
        if (mDownMusicTask != null && (mDownMusicTask.getStatus().equals(AsyncTask.Status.RUNNING)
                || mDownMusicTask.getStatus().equals(AsyncTask.Status.PENDING))) {
            mDownMusicTask.cancel(true);
        }
        mDownMusicTask = new DownMusicTask();
        mDownMusicTask.execute(qqSong);
    }

    public class DownMusicTask extends AsyncTask<QQSong, Void, Integer>{

        private QQSong mQQSong;

        @Override
        protected Integer doInBackground(QQSong... params) {
            mQQSong = params[0];
            String mp3Name = FileUtils.getMp3Name(mQQSong.getFsong(), mQQSong.getFsinger());
            if(FileUtils.isMp3FileExists(mp3Name)){
                return -1;
            }
            String playUrl = StringUtils.getPlayUrl(params[0].getF());
            return HttpApi.downMusic(playUrl, new String[]{params[0].getFsong(), params[0].getFsinger()});
        }

        @Override
        protected void onPostExecute(Integer size) {
            super.onPostExecute(size);
            if(size>0){
                ((BaseActivity)mContext).showText("下载成功！");
                Bundle bundle = MusicUtils.creSongBundle(mQQSong, true);
                bundle.putString(MusicProvider.MEDIA_FILE_SIZE, size + "");
                mMediaControllerCompat.getTransportControls()
                        .sendCustomAction(MiYueConstans.CUSTOM_ACTION_DOWNLOAD_SUCCESS,bundle);
            } else if(-1 == size){
                ((BaseActivity)mContext).showText("歌曲存在，不用重复下载！");
            } else {
                ((BaseActivity)mContext).showText("下载失败！");
            }
        }
    }

    @Override
    public void onPlaybackStateChangedForClien(@NonNull PlaybackStateCompat state) {
    }

    @Override
    public void onMetadataChangedForClien(MediaMetadataCompat metadata) {
    }

    @Override
    public void onConnectedForClien() {
    }

}
