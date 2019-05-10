package com.miyue.ui.fragment.main;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
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
import android.widget.Toast;

import com.greendao.DBHelper;
import com.greendao.SearchHis;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.miyue.R;
import com.miyue.application.MiYueConstans;
import com.miyue.bean.QQSong;
import com.miyue.bean.SongsInfo;
import com.miyue.common.base.BaseActivity;
import com.miyue.common.base.BaseMediaFragment;
import com.miyue.http.Downkey;
import com.miyue.http.HttpApi;
import com.miyue.service.playback.MusicProvider;
import com.miyue.ui.adapter.OnLineMusicAdapter;
import com.miyue.utils.FileUtils;
import com.miyue.utils.JsonParser;
import com.miyue.utils.MusicUtils;
import com.miyue.utils.NetWorkUtils;
import com.miyue.utils.StringUtils;
import com.miyue.utils.UtilLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

    private ListView lv_search_histroy;
    private ListView lv_online_music;
    private RelativeLayout rl_search_histroy;
    private TextView tv_clearall_histroy;
    private ImageView iv_recognize_sound;

    // 语音听写对象
    private SpeechRecognizer mIat;

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

    private String mEngineType;
    // 语音听写UI
    private RecognizerDialog mIatDialog;

    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();


    public String key_url = "http://c.y.qq.com/base/fcgi-bin/fcg_music_express_mobile3.fcg?g_tk=556936094&loginUin=0&hostUin=0&format=json&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0&cid=205361747&uin=0&songmid=003a1tne1nSz1Y&filename=C400003a1tne1nSz1Y.m4a&guid=joe";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // 引擎类型
        mEngineType = SpeechConstant.TYPE_CLOUD;
        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(mActivity, mInitListener);
        mIat = SpeechRecognizer.createRecognizer(mActivity, mInitListener);
        getHistroy();
    }

    /*SDK API<23时，onAttach(Context)不执行，需要使用onAttach(Activity)。Fragment自身的Bug，v4的没有此问题*/
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // 引擎类型
            mEngineType = SpeechConstant.TYPE_CLOUD;

            // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
            // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
            mIatDialog = new RecognizerDialog(mActivity, mInitListener);
            mIat = SpeechRecognizer.createRecognizer(mActivity, mInitListener);

            getHistroy();
        }
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
        iv_recognize_sound = (ImageView) view.findViewById(R.id.iv_recognize_sound);
        if(mStList.size() == 0){
            rl_search_histroy.setVisibility(View.GONE);
        }else{
            rl_search_histroy.setVisibility(View.VISIBLE);
        }

        lv_search_histroy = (ListView) view.findViewById(R.id.lv_search_histroy);
        mHistroyAdapter = new ArrayAdapter<String>(mActivity,R.layout.item_seahist_list,R.id.tv_seahistroy,mStList);
        lv_search_histroy.setAdapter(mHistroyAdapter);

        lv_online_music = (ListView) view.findViewById(R.id.lv_online_music);
        mOnLineMusicAdapter = new OnLineMusicAdapter(mActivity,mQQSongs);
        mOnLineMusicAdapter.setOnDownClickListener(this);
        lv_online_music.setAdapter(mOnLineMusicAdapter);
        lv_online_music.setVisibility(View.GONE);

        initListener();
        return view;
    }


    int ret = 0; // 函数调用返回值

    private void initListener() {
        iv_recognize_sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.showText("请开始说话111...");
                // 设置参数
                boolean isShowDialog = true;
                setParam();
                if (isShowDialog) {
                    // 显示听写对话框
                    mIatDialog.setListener(mRecognizerDialogListener);
                    mIatDialog.show();
                    mActivity.showText("请开始说话...");
                } else {
                    // 不显示听写对话框
                    ret = mIat.startListening(mRecognizerListener);
                    if (ret != ErrorCode.SUCCESS) {
                        mActivity.showText("听写失败,错误码：" + ret);
                    } else {
                        mActivity.showText("请开始说话…");
                    }
                }
            }
        });

        tv_clearall_histroy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDBHelper.clearAllHistroy();
                mStList.clear();
                mHistroyAdapter.notifyDataSetChanged();
                rl_search_histroy.setVisibility(View.GONE);
            }
        });
        iv_clear_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_recognize_sound.setVisibility(View.GONE);
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
                    if(StringUtils.isNullOrEmpty(MiYueConstans.KEY)){
                        new Downkey().execute(key_url);
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
        mSeaList = mDBHelper.getSearchHistroy();
        for(int i = mSeaList.size()-1; i>=0; i--){
            mStList.add(mSeaList.get(i).getSmitem());
        }
    }

    private void updateStList(){
        mStList.clear();
        mSeaList = mDBHelper.getSearchHistroy();
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
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et_find_music.getWindowToken(), 0); //强制隐藏键盘
        SearchHis hisbean = new SearchHis();
        hisbean.setSmitem(keyword);
        mDBHelper.addHistroy(hisbean);
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
                mActivity.showText("没有网络或者没有数据！");
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
                mActivity.showText("没有网络或者没有数据！");
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
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getActivity(), "开始下载", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Integer doInBackground(QQSong... params) {
            mQQSong = params[0];
            String mp3Name = FileUtils.getMp3Name(mQQSong.getFsong(), mQQSong.getFsinger());
            if(FileUtils.isMp3FileExists(mp3Name)){
                return -1;
            }
            String playUrl = MiYueConstans.QQ_PLAY_URL.replace("MEDIAID", mQQSong.getSongmid())
                    .replace("AK", MiYueConstans.KEY);
            UtilLog.e("kkk", playUrl);
            return HttpApi.downMusic(playUrl, new String[]{params[0].getFsong(), params[0].getFsinger()});
        }

        @Override
        protected void onPostExecute(Integer size) {
            super.onPostExecute(size);
            if(size>0){
                mActivity.showText("下载成功！");
                Bundle bundle = MusicUtils.creSongBundle(mQQSong, true);
                bundle.putString(MusicProvider.MEDIA_FILE_SIZE, size + "");
                mMediaControllerCompat.getTransportControls()
                        .sendCustomAction(MiYueConstans.CUSTOM_ACTION_DOWNLOAD_SUCCESS,bundle);
            } else if(-1 == size){
                mActivity.showText("歌曲存在，不用重复下载！");
            } else {
                mActivity.showText("下载失败！");
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


    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                mActivity.showText("初始化失败，错误码：" + code);
            }
        }
    };


    /**
     * 参数设置
     *
     * @return
     */
    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        // 设置语言
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置语言区域
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin");


        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/iat.wav");
    }

    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            printResult(results);
        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            mActivity.showText(error.getPlainDescription(true));
        }

    };

    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //存起来
        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        if(!StringUtils.isNullOrEmpty(resultBuffer.toString())){
            et_find_music.setText(resultBuffer.toString());
            et_find_music.setSelection(et_find_music.length());

            iv_recognize_sound.setVisibility(View.GONE);
            iv_clear_edit.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            mActivity.showText("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
            mActivity.showText(error.getPlainDescription(true));

        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            mActivity.showText("结束说话");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.d(TAG, results.getResultString());
            printResult(results);
            if (isLast) {
                // TODO 最后的结果
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            mActivity.showText("当前正在说话，音量大小：" + volume);
            Log.d(TAG, "返回音频数据："+data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if( null != mIat ){
            // 退出时释放连接
            mIat.cancel();
            mIat.destroy();
        }
    }


}
