package com.miyue.ui.fragment.menu;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.miyue.R;
import com.miyue.ui.activity.MainActivity;
import com.miyue.ui.fragment.my.RecentPlayFragment;
import com.miyue.utils.DisplayUtils;
import com.miyue.utils.FragmentControl;
import com.miyue.utils.UtilLog;

/**
 * Created by zhangzhendong on 17/5/17.
 */

public class MenuFragment extends Fragment implements View.OnClickListener{

    private TextView txt_scan;
    private TextView txt_background;
    private TextView txt_sleep;
    private TextView preference_text;
    private TextView txt_exit;

    private RelativeLayout rl_sliding_menu;


    private MainActivity mContext;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof MainActivity){
            mContext = (MainActivity)context;
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
        txt_scan.setOnClickListener(this);
        txt_background.setOnClickListener(this);
        txt_sleep.setOnClickListener(this);
        preference_text.setOnClickListener(this);
        txt_exit.setOnClickListener(this);
    }

    private void initView(View view) {
        rl_sliding_menu = (RelativeLayout) view.findViewById(R.id.rl_sliding_menu);
        txt_scan = ((TextView)view.findViewById(R.id.txt_scan));
        txt_background = ((TextView)view.findViewById(R.id.txt_background));
        txt_sleep = ((TextView)view.findViewById(R.id.txt_sleep));
        preference_text = ((TextView)view.findViewById(R.id.preference_text));
        txt_exit = ((TextView)view.findViewById(R.id.txt_exit));

        rl_sliding_menu.setPadding(0, DisplayUtils.getStatusBarHeight(mContext),0,5);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.txt_scan:

                break;
            case R.id.txt_background:
                UtilLog.e("MenuFragment","show background");
                SwitchBgFragment sbf = new SwitchBgFragment();
                FragmentControl.getFragConInstance().showMainFragment(sbf,"",0);
                break;
            case R.id.txt_sleep:

                break;
            case R.id.preference_text:

                break;
            case R.id.txt_exit:

                break;

        }
        mContext.closeDrawer();
    }
}
