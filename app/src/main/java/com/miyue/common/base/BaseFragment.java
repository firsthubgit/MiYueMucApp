package com.miyue.common.base;


import android.app.Fragment;
import com.miyue.utils.FragmentControl;


/**
 * Created by zhangzhendong on 16/4/7.
 */
public class BaseFragment extends Fragment {

    public FragmentControl.FragType fragType;

    public  void setFragType(FragmentControl.FragType mFragType){
        fragType = mFragType;
    }

    public FragmentControl.FragType getFragType(){
        return this.fragType;
    }

}
