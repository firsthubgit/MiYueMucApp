package com.miyue.ui.fragment.menu;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.miyue.R;
import com.miyue.common.base.BaseSwipeBackFragment;
import com.miyue.ui.activity.MainActivity;
import com.miyue.utils.BitmapUtils;
import com.miyue.utils.DisplayUtils;
import com.miyue.utils.FragmentControl;
import com.miyue.utils.PreferenUtils;
import com.miyue.utils.SkinUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.miyue.utils.BitmapUtils.getImageSmall;

/**
 * Created by zhangzhendong on 17/5/25.
 */

public class SwitchBgFragment extends BaseSwipeBackFragment implements AdapterView.OnItemClickListener{

    private MyAdapter mAdapter;
    private List<BgEntity> mBgList;
    /**默认的路径，给个小图标*/
    private String mDefaultBgPath;
    private GridView mGridView;
    private TextView tv_bckg_title;
    private ImageView iv_mannag_skin;
    private MainActivity mContext;
    private RelativeLayout rl_all_background;
    private RelativeLayout rl_title;

    private boolean mIsCanDelete = false;

    private Bitmap mBackgBitmap;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = (MainActivity)context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_menu_background, null);
        getData();
        initView(view);
        initListener();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBackgBitmap = SkinUtils.initBackground(mContext, rl_all_background);
    }

    private void initView(View view) {
        int barHeight = DisplayUtils.getStatusBarHeight(mContext);
        rl_all_background = (RelativeLayout)view.findViewById(R.id.rl_all_background);
        rl_title = (RelativeLayout) view.findViewById(R.id.rl_title);
        rl_title.setPadding(0,
                barHeight + DisplayUtils.dip2px(mContext,10),0,0);
        iv_mannag_skin = (ImageView) view.findViewById(R.id.iv_mannag_skin);
        mGridView = (GridView) view.findViewById(R.id.grid_content);
        int space = ((DisplayUtils.getScreenWidth(mContext)/4) - 40)/2;
        //初始化背景
        //设置GridView
        mGridView.setHorizontalSpacing(space);
        mGridView.setVerticalSpacing(space);
        mAdapter = new MyAdapter(mBgList);
        mGridView.setOnItemClickListener(this);
        mGridView.setAdapter(mAdapter);
    }
    private void initListener() {
        iv_mannag_skin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mIsCanDelete){
                    mIsCanDelete = true;
                    iv_mannag_skin.setBackgroundResource(R.drawable.skin_complete_normal);
                    mAdapter.setDeleteState(true);
                    mAdapter.notifyDataSetChanged();
                } else {
                    mIsCanDelete = false;
                    iv_mannag_skin.setBackgroundResource(R.drawable.skin_edit_normal);
                    mAdapter.setDeleteState(false);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(position==0 || mIsCanDelete){
            return;
        }
        mDefaultBgPath = mAdapter.getItem(position-1).path;
        SkinUtils.setBackground(mContext, rl_all_background, mDefaultBgPath);
        PreferenUtils.getInstance(mContext).putBgPath(mDefaultBgPath).exit();
        mAdapter.notifyDataSetChanged();
        mContext.udpateBackground();
    }

    @Override
    public void onBackFinish() {
        FragmentControl.getFragConInstance().closeFragment(this, 0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mContext.setDrawerUnLock();
        if(mBgList != null && mBgList.size()>0){
            for(BgEntity bgbean : mBgList){
                bgbean.bitmap.recycle();
                bgbean.bitmap = null;
            }
            mBgList.clear();
        }
        if(mBackgBitmap != null){
            mBackgBitmap.recycle();
            mBackgBitmap = null;
        }
    }

    private void getData() {
        //测试1.2.1
        Bitmap bitmap = null;
        AssetManager am = getActivity().getAssets();
        try {
            String[] drawableList = am.list("bkgs");
            this.mBgList = new ArrayList();
            for (String path : drawableList) {
                BgEntity bg = new BgEntity();
                InputStream is = am.open("bkgs/" + path);
                bitmap = BitmapUtils.getImageSmall(is,
                        DisplayUtils.getScreenWidth(mContext)/4+5,
                        DisplayUtils.getScreenHeight(mContext)/4+5);
                bg.path = path;
                bg.bitmap = bitmap;
                mBgList.add(bg);
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class MyAdapter extends BaseAdapter {
        private List<BgEntity> bgList;

        /**是否是删除状态*/
        private boolean mIsDeleteState = false;

        private class ViewHolder {
            ImageView iv_item_bg;
            ImageView iv_item_checked;
            TextView tv_skin_delete;
        }

        public MyAdapter(List<BgEntity> list) {
            bgList = list;
        }

        public void setDeleteState(boolean state){
            mIsDeleteState = state;
        }
        public int getCount() {
            return mIsDeleteState ? bgList.size() : bgList.size()+1;
        }

        public BgEntity getItem(int position) {
            return mIsDeleteState ? bgList.get(position) : bgList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();

                convertView = MainActivity.getActivity().getLayoutInflater().inflate(R.layout.item_backgroud_gridview, null);
                convertView.setLayoutParams(new ViewGroup.LayoutParams(
                        DisplayUtils.getScreenWidth(mContext)/4+5,
                        DisplayUtils.getScreenHeight(mContext)/4+5));
                viewHolder.iv_item_bg = (ImageView) convertView.findViewById(R.id.iv_item_bg);
                viewHolder.iv_item_checked = (ImageView) convertView.findViewById(R.id.iv_item_checked);
                viewHolder.tv_skin_delete = (TextView) convertView.findViewById(R.id.tv_skin_delete);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }


            if(position==0 && !mIsDeleteState){//非删除模式下的第一张
                viewHolder.iv_item_bg.setImageResource(R.drawable.add_skin);
                viewHolder.iv_item_bg.setScaleType(ImageView.ScaleType.CENTER);
                viewHolder.iv_item_bg.setBackgroundResource(R.drawable.skin_background);
                viewHolder.iv_item_checked.setVisibility(View.GONE);
                viewHolder.tv_skin_delete.setVisibility(View.GONE);
            } else {
                if(mIsDeleteState){//表示删除模式
                    viewHolder.iv_item_bg.setImageBitmap(getItem(position).bitmap);
                    viewHolder.iv_item_bg.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    viewHolder.iv_item_checked.setVisibility(View.GONE);
                    viewHolder.tv_skin_delete.setVisibility(View.VISIBLE);
                } else {// 非删除模式，会多一个item
                    viewHolder.iv_item_bg.setImageBitmap(getItem(position-1).bitmap);
                    viewHolder.iv_item_bg.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    viewHolder.tv_skin_delete.setVisibility(View.GONE);
                    if (getItem(position-1).path.equals(mDefaultBgPath)) {
                        viewHolder.iv_item_checked.setVisibility(View.VISIBLE);
                    } else {
                        viewHolder.iv_item_checked.setVisibility(View.GONE);
                    }
                }
            }
            return convertView;
        }
    }

    private class BgEntity {
        Bitmap bitmap;
        String path;
        private BgEntity() {
        }
    }


}
