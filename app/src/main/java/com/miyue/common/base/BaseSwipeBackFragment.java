package com.miyue.common.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.miyue.R;
import com.miyue.utils.FragmentControl;
import com.miyue.utils.SwipeBackHelper;
import com.miyue.widgets.SwipeBackLayout;

/**
 * Created by zhangzhendong on 16/4/7.
 */
public abstract class BaseSwipeBackFragment extends BaseFragment implements SwipeBackLayout.OnFinishListener{

    public SwipeBackHelper mHelper;
    private boolean mEnable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHelper = new SwipeBackHelper(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mHelper.initView();
    }

    /**设为true滑动,false不滑动*/
    public void setSwipeEnabled(boolean mEnable){
        mHelper.getSwipeBackLayout().setSwipeEnable(mEnable);
    }

    public abstract void onBackFinish();

    @Override
    public void finish() {
        onBackFinish();
    }
}

