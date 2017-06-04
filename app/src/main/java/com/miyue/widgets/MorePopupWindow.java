package com.miyue.widgets;


import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.miyue.R;

/**
*
* @author ZZD
* @time 16/4/25 上午10:00
*/
public class MorePopupWindow extends PopupWindow {
    private Context mContext;
    private View view;
    private ImageView iv_like_or_unlike, iv_delete_music;
    private RelativeLayout rl_background;

    public MorePopupWindow(Context mContext,View.OnClickListener clicker){
        super(mContext);
        this.mContext = mContext;
        view = LayoutInflater.from(mContext).inflate(R.layout.pop_more_function,null);
        iv_like_or_unlike = (ImageView) view.findViewById(R.id.iv_like_or_unlike);
        iv_delete_music = (ImageView) view.findViewById(R.id.iv_delete_music);
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

        iv_like_or_unlike.setOnClickListener(clicker);
        iv_delete_music.setOnClickListener(clicker);
        rl_background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void setFavoriteSrc(int id){
        iv_like_or_unlike.setImageResource(id);
    }

}
