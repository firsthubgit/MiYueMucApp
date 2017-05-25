package com.miyue.widgets;


import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.miyue.R;

/**
 * Created by zhangzhendong on 16/4/25.
 */
public class MorePopupWindow extends PopupWindow {
    private Context mContext;
    private View view;
    private TextView tv_scan_music;
    private RelativeLayout rl_background;

    public MorePopupWindow(Context mContext,View.OnClickListener clicker){
        super(mContext);
        this.mContext = mContext;
        view = LayoutInflater.from(mContext).inflate(R.layout.pop_more_function,null);
        tv_scan_music = (TextView) view.findViewById(R.id.tv_scan_music);
        rl_background = (RelativeLayout) view.findViewById(R.id.rl_background);

        // 设置PopupWindow的View
        this.setContentView(view);
        // 设置PopupWindow弹出窗体的宽
        this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        // 设置PopupWindow弹出窗体的高
        this.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        // 设置PopupWindow弹出窗体可点击
        this.setFocusable(true);
        // 设置PopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.AnimBottom);
        this.setOutsideTouchable(true);

        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0x00000000);
        // 设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);

        tv_scan_music.setOnClickListener(clicker);
        rl_background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

}
