package com.miyue.ui.fragment.my;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.miyue.R;
import com.miyue.common.base.BaseSwipeBackFragment;
import com.miyue.utils.FragmentControl;

/**
 * Created by zhangzhendong on 16/4/13.
 */
public class RecentPlayFragment extends BaseSwipeBackFragment {

      private TextView tv_back;

      private CallBack callBack;

//    private FragmentControl.FragType fType = FragmentControl.FragType.Type_MY_Flag;
//
//
//    public RecentPlayFragment(){
//        setFragType(fType);
//    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBackFinish() {
        FragmentControl.getFragConInstance().closeFragment(this, 0);
        callBack.call();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_recent_play, null);
        tv_back = (TextView) view.findViewById(R.id.tv_back);
        View v_click = (View)view.findViewById(R.id.v_click);
        return view;
    }

    public  interface CallBack{
        void call();
    }

    public void setOnBackListener(CallBack cb){
        callBack = cb;
    }


}
