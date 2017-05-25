package com.miyue.common.base;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

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
