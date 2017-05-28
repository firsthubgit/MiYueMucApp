package com.miyue.utils;

import android.view.LayoutInflater;

import com.miyue.R;
import com.miyue.common.base.BaseSwipeBackFragment;
import com.miyue.widgets.SwipeBackLayout;

/**
 * Created by zhangzhendong on 16/4/12.
 */
public class SwipeBackHelper {
    private BaseSwipeBackFragment swipeBackFragment;
    private SwipeBackLayout swipeBackLayout;

    public SwipeBackHelper(BaseSwipeBackFragment swipeBackFragment){
        this.swipeBackFragment = swipeBackFragment;
        swipeBackLayout = (SwipeBackLayout) LayoutInflater.from(
                swipeBackFragment.getActivity()).inflate(R.layout.swipeback_layout, null);
        swipeBackLayout.setOnFinishListener(swipeBackFragment);
    }

    public void initView(){
        swipeBackLayout.attachToFragment(swipeBackFragment);
    }

    public void detachView(){
        swipeBackLayout.detachView(swipeBackFragment);
        swipeBackLayout = null;
    }
    public SwipeBackLayout getSwipeBackLayout(){
        return swipeBackLayout;
    }

}
