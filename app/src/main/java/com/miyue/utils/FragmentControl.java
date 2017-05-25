package com.miyue.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

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
        FragmentManager fm = MainActivity.getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        if(anim != 0){
            transaction.setCustomAnimations(anim, 0);
        }
        UtilLog.e("FragmentControl","Type_Main_Flag");
        transaction.add(R.id.rl_main_frag,mFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        return true;
    }

    public boolean showMyFragment(Fragment mFragment,Fragment fragment){
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

    public boolean closeFragment(Fragment fragment, int anim){
        FragmentManager fm = MainActivity.getActivity().getSupportFragmentManager();
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
