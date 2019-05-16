package com.miyue.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.RelativeLayout;


import com.miyue.R;

import com.miyue.http.Downkey;
import com.miyue.service.PlayerService;


import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zhangzhendong on 17/5/17.
 */

public class SplashActivity extends Activity {

    public static final int ANIMATION_END_MSG = 1;


    private Button btn_skip_ad;
    private RelativeLayout rl_splash;

    /**广告时长*/
    private long adDuration = 500;

    private Timer mTimer;
    private TimerTask mTimerTask;

    private Handler mHander = new MyHandler(this);

    private AlphaAnimation animation;

    private String key_url = "http://c.y.qq.com/base/fcgi-bin/fcg_music_express_mobile3.fcg?g_tk=556936094&loginUin=0&hostUin=0&format=json&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0&cid=205361747&uin=0&songmid=003a1tne1nSz1Y&filename=C400003a1tne1nSz1Y.m4a&guid=joe";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
        if(Build.VERSION.SDK_INT >= 23){
            int hasPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 666);
                return;
            }
        }
        setContentView(R.layout.splash_ad);
        btn_skip_ad = (Button) findViewById(R.id.btn_skip_ad);
        rl_splash = (RelativeLayout) findViewById(R.id.rl_splash);

        startService(new Intent(this, PlayerService.class));
        registListenerAndAnimation();

        setKey();
    }

    private void setKey() {
        new Downkey().execute(key_url);
    }


    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.rl_splash:
                case R.id.btn_skip_ad:
                    //如果不添加下面的代码会在动画结束时再次向handler发送信息。
                    animation.setAnimationListener(null);
                    // 结束这个页面
                    mHander.sendEmptyMessage(ANIMATION_END_MSG);
                    // 跳转到广告页
                    break;
            }
        }
    };

    Animation.AnimationListener mAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mTimer= new Timer(true);
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    mHander.obtainMessage(ANIMATION_END_MSG).sendToTarget();
                }
            };
            mTimer.schedule(mTimerTask, adDuration);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };
    private void registListenerAndAnimation() {
        // 透明动画（从完全透明到不透明，分别对应第一个参数和第二个参数）
        animation = new AlphaAnimation(0.8f, 1.0f);
        // 动画效果时间为3秒
        animation.setDuration(1000);
        // 动画监听
        animation.setAnimationListener(mAnimationListener);
        // 设置开始动画
        rl_splash.startAnimation(animation);

        rl_splash.setOnClickListener(mOnClickListener);
        btn_skip_ad.setOnClickListener(mOnClickListener);
    }

    public void clearAnima(){
        rl_splash.clearAnimation();
        animation.reset();
    }
    public void cancelTimer(){
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    @Override
    protected void onDestroy() {
        rl_splash.clearAnimation();
        mHander.removeCallbacksAndMessages(null);
        mHander = null;
        super.onDestroy();
    }

    private static class MyHandler extends Handler {
        private final WeakReference<SplashActivity> mActivity;

        public MyHandler(SplashActivity activity) {
            mActivity = new WeakReference<SplashActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mActivity.get() == null) {
                return;
            }
            SplashActivity activity = (SplashActivity)mActivity.get();
            switch (msg.what) {
                case ANIMATION_END_MSG:
                    activity.clearAnima();
                    activity.cancelTimer();
                    Intent newIntent = new Intent();
                    newIntent.setClass(activity, MainActivity.class);
                    activity.startActivity(newIntent);
                    activity.finish();
                    break;
            }
            mActivity.clear();
        }
    }
}
