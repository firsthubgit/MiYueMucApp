package com.miyue.ui.fragment.menu;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.miyue.R;
import com.miyue.ui.activity.MainActivity;
import com.miyue.utils.BitmapUtils;
import com.miyue.utils.DisplayUtils;
import com.miyue.utils.FragmentControl;
import com.miyue.utils.UtilLog;

/**
 * Created by zhangzhendong on 17/5/17.
 */

public class MenuFragment extends Fragment implements View.OnClickListener{

    private TextView tv_menu_scan;
    private TextView tv_menu_background;
    private TextView txt_sleep;
    private TextView tv_menu_preference;
    private TextView tv_menu_about;

    private RelativeLayout rl_sliding_menu;


    private MainActivity mActivity;

    private SwitchBgFragment mSwitchBgFragment;

    private AboutFragment mAboutFragment;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof MainActivity){
            mActivity = (MainActivity)context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sliding_menu, null);
        initView(view);
        registerListener();
        return view;
    }

    private void registerListener() {
        tv_menu_scan.setOnClickListener(this);
        tv_menu_background.setOnClickListener(this);
        txt_sleep.setOnClickListener(this);
        tv_menu_preference.setOnClickListener(this);
        tv_menu_about.setOnClickListener(this);
    }

    private void initView(View view) {
        rl_sliding_menu = (RelativeLayout) view.findViewById(R.id.rl_sliding_menu);
        tv_menu_scan = ((TextView)view.findViewById(R.id.tv_menu_scan));
        tv_menu_background = ((TextView)view.findViewById(R.id.tv_menu_background));
        txt_sleep = ((TextView)view.findViewById(R.id.txt_sleep));
        tv_menu_preference = ((TextView)view.findViewById(R.id.tv_menu_preference));
        tv_menu_about = ((TextView)view.findViewById(R.id.tv_menu_about));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_menu_scan:
                mActivity.showText(getString(R.string.todo_toast));
                break;
            case R.id.tv_menu_background:
                UtilLog.e("MenuFragment","show background");
                mActivity.setDrawerLock();
                mSwitchBgFragment = (SwitchBgFragment) FragmentControl.getFragConInstance().getMainFragment("SwitchBgFragment");
                if(mSwitchBgFragment == null){
                    mSwitchBgFragment = new SwitchBgFragment();
                }
                FragmentControl.getFragConInstance().showMainFragment(mSwitchBgFragment,"SwitchBgFragment",0);
                break;
            case R.id.txt_sleep:
                mActivity.showText(getString(R.string.todo_toast));
                break;
            case R.id.tv_menu_preference:
                mActivity.showText(getString(R.string.todo_toast));
                break;
            case R.id.tv_menu_about:
                UtilLog.e("MenuFragment","show background");
                mActivity.setDrawerLock();
                mAboutFragment = (AboutFragment) FragmentControl.getFragConInstance().getMainFragment("AboutFragment");
                if(mAboutFragment == null){
                    mAboutFragment = new AboutFragment();
                }
                FragmentControl.getFragConInstance().showMainFragment(mAboutFragment,"AboutFragment",0);
                break;
        }
        mActivity.closeDrawer();
    }
}
