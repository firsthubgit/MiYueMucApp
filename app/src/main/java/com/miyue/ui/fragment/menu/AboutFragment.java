package com.miyue.ui.fragment.menu;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.miyue.R;
import com.miyue.common.base.BaseSwipeBackFragment;
import com.miyue.ui.activity.MainActivity;
import com.miyue.utils.FragmentControl;
import com.miyue.utils.SkinUtils;


/**
*
* @author ZZD
* @time 17/6/5 上午10:47
*/
public class AboutFragment extends BaseSwipeBackFragment {

    private RelativeLayout rl_about_background;

    private MainActivity mActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.frag_about_author, null);
        rl_about_background = (RelativeLayout) view.findViewById(R.id.rl_about_background);
        SkinUtils.initBackground(mActivity, rl_about_background);
        return view;
    }

    @Override
    public void onBackFinish() {
        FragmentControl.getFragConInstance().closeFragment(this, 0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mActivity.setDrawerUnLock();
    }
}
