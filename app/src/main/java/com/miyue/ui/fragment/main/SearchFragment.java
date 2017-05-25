package com.miyue.ui.fragment.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.miyue.R;

/**
 * Created by zhangzhendong on 16/3/31.
 */
public class SearchFragment extends Fragment {
    private TextView tv_to_frag;
    private int mCurrentlyShowingFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_find_music, null);

//        tv_to_frag = (TextView) view.findViewById(R.id.tv_to_frag);
//        tv_to_frag.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {
//            getChildFragmentManager().beginTransaction().
//                    replace(R.id.rl_for_add, new MyMusicFragment()).commit();
//            mCurrentlyShowingFragment = 0;
        } else {
//            mCurrentlyShowingFragment = savedInstanceState.getInt("currently_showing_fragment");

        }
        super.onViewCreated(view, savedInstanceState);
    }

    private void showNextScreen() {
//        mCurrentlyShowingFragment = 1;
//        getChildFragmentManager().beginTransaction().replace(R.id.rl_for_add, new Fragment0()).addToBackStack(null).commit();
//        adjustButtonText();
    }

    private void showPreviousScreen() {
//        mCurrentlyShowingFragment = 0;
        getChildFragmentManager().popBackStackImmediate();
//        adjustButtonText();
    }
}
