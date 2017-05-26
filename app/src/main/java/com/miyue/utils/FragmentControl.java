package com.miyue.utils;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import com.miyue.R;
import com.miyue.common.base.BaseSwipeBackFragment;
import com.miyue.ui.activity.MainActivity;

/**
 * Created by zhangzhendong on 16/4/7.
 */
public class FragmentControl {

    static final String TAG = "FragmentControl";

    private FragmentControl(){}

    private static class FragControl{
        private static final FragmentControl instance = new FragmentControl();
    }

    public static FragmentControl getFragConInstance(){
        UtilLog.e("FragmentControl", "getFragConInstance()");
        return FragControl.instance;
    }


    public boolean showMainFragment(BaseSwipeBackFragment mFragment, String Tag, int anim){
        UtilLog.e("FragmentControl","Type_Main_Flag");
        FragmentManager fm = MainActivity.getActivity().getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        if(anim != 0){
            transaction.setCustomAnimations(
                    R.animator.slide_in_from_right, R.animator.slide_out_to_left,
                    R.animator.slide_in_from_left, R.animator.slide_out_to_right);
        }else{
            transaction.setCustomAnimations(R.animator.slide_in_from_right, anim,
                    R.animator.slide_in_from_left, R.animator.slide_out_to_right);
        }
        transaction.add(R.id.rl_main_frag,mFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        return true;
    }

    public boolean showMyFragment(Fragment mFragment, Fragment fragment){
        FragmentManager fm2 = mFragment.getChildFragmentManager();
        FragmentTransaction ft2 = fm2.beginTransaction();
        ft2.replace(R.id.rl_my_frag, fragment);
        ft2.addToBackStack(null);
        ft2.commit();
        return false;
    }

    public boolean closeFragment(){

        return false;
    }

    public Fragment getMainFragment(String tag){
        FragmentManager fm = MainActivity.getActivity().getFragmentManager();
        return fm.findFragmentByTag(tag);
    }

    public boolean closeFragment(Fragment fragment, int anim){
        FragmentManager fm = MainActivity.getActivity().getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        if (fragment.isAdded()) {
            transaction.setCustomAnimations(0, anim);
            transaction.remove(fragment).commit();
        }
        return true;
    }


    public enum FragType{
        Type_Main_Flag("主Frag"), Type_MY_Flag("我的frag");
        private String name;

        FragType(String name){
            this.name = name;
        }
        @Override
        public String toString() {
            return name;
        }
    }

}
