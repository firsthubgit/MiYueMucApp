package com.miyue.ui.activity;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.miyue.R;
import com.miyue.common.base.BaseActivity;
import com.miyue.service.PlayerService;
import com.miyue.ui.fragment.main.LrcFragment;
import com.miyue.ui.fragment.main.MyMusicFragment;
import com.miyue.ui.fragment.main.PlayFragment;
import com.miyue.ui.fragment.main.SearchFragment;
import com.miyue.utils.DisplayUtils;
import com.miyue.utils.UtilLog;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private static MainActivity mainActivity;

    private ViewPager mViewPager;

    private DrawerLayout mDrawerLayout;

    public static MainActivity getActivity(){
        return mainActivity ;
    }

    private PlayFragment mPlayFragment = new PlayFragment();
    private LrcFragment mLrcFragment = new LrcFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mainActivity = this;

        setWindowTopTrans();
        initDrawerLayout();
        initViewPager();
        TabLayout tabLayout = (TabLayout) findViewById(R.id.main_tablayout);
        tabLayout.setPadding(0,DisplayUtils.getStatusBarHeight(this),0,5);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setupWithViewPager(mViewPager);
        mPlayFragment.setSeekBarChangeListener(mLrcFragment);
    }

    private void initDrawerLayout(){
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        mDrawerLayout.setScrimColor(Color.TRANSPARENT);
        toggle.syncState();
    }
    private void initViewPager(){
        mViewPager = (ViewPager) findViewById(R.id.main_viewpager);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(new SectionPagerAdapter(getFragmentManager()));
    }
    /**
     * 设置顶栏透明
     * */
    private void setWindowTopTrans(){
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
                requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        666);
                return;
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    public class SectionPagerAdapter extends FragmentPagerAdapter {

        public SectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new MyMusicFragment();
                case 1:
                    return mLrcFragment;
                case 2:
                    return mPlayFragment;
                case 3:
                    return new SearchFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "我的";
                case 1:
                    return "歌词";
                case 2:
                    return "播放";
                case 3:
                    return "搜索";
                default:
                    return "RecyclerView";
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(this, PlayerService.class);
        stopService(intent);
    }


    @Override
    public void onMediaControllerConnected(MediaControllerCompat mcc){
        mLrcFragment.setMediaController(mcc);
        mLrcFragment.registCallback();
        mPlayFragment.setMediaController(mcc);
        mPlayFragment.registCallback();
    }

    public void setDrawerLock(){
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
    }
    public void setDrawerUnLock(){
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

    }
    public void closeDrawer(){
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }
}
