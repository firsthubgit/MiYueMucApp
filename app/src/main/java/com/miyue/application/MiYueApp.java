package com.miyue.application;

import android.app.Application;

import com.greendao.DBHelper;

/**
 * Created by zhangzhendong on 16/4/25.
 */
public class MiYueApp extends Application {
    private static MiYueApp instance;

    private DBHelper mDBHelper;

    public static MiYueApp getInstance(){
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initData();
    }

    private void initData(){
        //写一些网络的判断,软件要初始化的
    }

    public  DBHelper getDBHelper(){
        if(null == mDBHelper){
            mDBHelper = DBHelper.getInstance(this);
        }
        return mDBHelper;
    }

}
