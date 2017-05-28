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
import com.miyue.service.PlayerService;

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
    private long adDuration = 2000;

    private Timer mTimer;
    private TimerTask mTimerTask;

    private Handler mHander = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ANIMATION_END_MSG:
                    cancelTimer();
                    Intent newIntent = new Intent();
                    newIntent.setClass(SplashActivity.this, MainActivity.class);
                    startActivity(newIntent);
                    finish();
                    break;
            }
        }
    };

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

//        startService(new Intent(this, PlayerService.class));
        registListenerAndAnimation();
    }


    private void registListenerAndAnimation() {
        // 透明动画（从完全透明到不透明，分别对应第一个参数和第二个参数）
        final AlphaAnimation animation = new AlphaAnimation(0.8f, 1.0f);
        // 动画效果时间为3秒
        animation.setDuration(3000);
        // 动画监听
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { // 动画开始时执行此方法
            }

            @Override
            public void onAnimationRepeat(Animation animation) { // 动画重复调用时执行此方法
            }

            @Override
            public void onAnimationEnd(Animation animation) { // 动画结束时执行此方法
                mTimer= new Timer(true);
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        mHander.obtainMessage(ANIMATION_END_MSG).sendToTarget();
                    }
                };
                mTimer.schedule(timerTask, adDuration);
            }
        });
        // 设置开始动画
        rl_splash.startAnimation(animation);

        rl_splash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果不添加下面的代码会在动画结束时再次向handler发送信息。
                animation.setAnimationListener(null);
                // 结束这个页面
                mHander.sendEmptyMessage(ANIMATION_END_MSG);
                // 跳转到广告页
                //............
            }
        });
        btn_skip_ad.setOnClickListener(new View.OnClickListener() {
            // 点击跳过按钮直接发送动画结束信息，跳过广告
            @Override
            public void onClick(View v) {
                animation.setAnimationListener(null);
                mHander.sendEmptyMessage(ANIMATION_END_MSG);
            }
        });
    }

    private void cancelTimer(){
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }
}
