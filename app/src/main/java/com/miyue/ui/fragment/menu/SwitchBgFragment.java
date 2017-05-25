package com.miyue.ui.fragment.menu;

import android.content.res.AssetManager;
import android.content.res.Resources;
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
import android.widget.TextView;

import com.miyue.R;
import com.miyue.common.base.BaseSwipeBackFragment;
import com.miyue.ui.activity.MainActivity;
import com.miyue.utils.FragmentControl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangzhendong on 17/5/25.
 */

public class SwitchBgFragment extends BaseSwipeBackFragment implements AdapterView.OnItemClickListener, View.OnClickListener {

    private MyAdapter mAdapter;
    private TextView mBackBtn;
    private List<BgEntity> mBgList;
    private String mDefaultBgPath;
    private GridView mGridView;
//    private SPStorage mSp;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_menu_background, null);
//        mSp = new SPStorage(getActivity());
//        mDefaultBgPath = this.mSp.getPath();
//        getData();
//        initView(view);
        return view;
    }

    private void initView(View view) {
//        mBackBtn = (TextView) view.findViewById(R.id.backBtn);
//        mBackBtn.setOnClickListener(this);
//        mGridView = (GridView) view.findViewById(R.id.grid_content);
//        mAdapter = new MyAdapter(mBgList);
//        mGridView.setOnItemClickListener(this);
//        mGridView.setAdapter(mAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String path = this.mAdapter.getItem(position).path;
//        mSp.savePath(path);
        mDefaultBgPath = path;
        mAdapter.notifyDataSetChanged();
//        Intent intent = new Intent(MiYueConstans.BROADCAST_CHANGEBG);
//        intent.putExtra("path", path);
//        getActivity().sendBroadcast(intent);
    }

    @Override
    public void onBackFinish() {
        FragmentControl.getFragConInstance().closeFragment(this, 0);
    }

    private class BgEntity {
            Bitmap bitmap;
            String path;

            private BgEntity() {
            }
        }

    private class MyAdapter extends BaseAdapter {
        private List<BgEntity> bgList;
        private Resources resources;

        private class ViewHolder {
            ImageView backgroundIv;
            ImageView checkedIv;

            private ViewHolder() {
            }
        }

        public MyAdapter(List<BgEntity> list) {
            this.bgList = list;
            this.resources = SwitchBgFragment.this.getActivity().getResources();
        }

        public int getCount() {
            return this.bgList.size();
        }

        public BgEntity getItem(int position) {
            return (BgEntity) this.bgList.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = MainActivity.getActivity().getLayoutInflater().inflate(R.layout.item_backgroud_gridview, null);
                viewHolder.backgroundIv = (ImageView) convertView.findViewById(R.id.gridview_item_iv);
                viewHolder.checkedIv = (ImageView) convertView.findViewById(R.id.gridview_item_checked_iv);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.backgroundIv.setBackgroundDrawable(new BitmapDrawable(this.resources, getItem(position).bitmap));
            if (getItem(position).path.equals(SwitchBgFragment.this.mDefaultBgPath)) {
                viewHolder.checkedIv.setVisibility(View.VISIBLE);
            } else {
                viewHolder.checkedIv.setVisibility(View.GONE);
            }
            return convertView;
        }
    }



    private void getData() {
        AssetManager am = getActivity().getAssets();
        try {
            String[] drawableList = am.list("bkgs");
            this.mBgList = new ArrayList();
            for (String path : drawableList) {
                BgEntity bg = new BgEntity();
                InputStream is = am.open("bkgs/" + path);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                bg.path = path;
                bg.bitmap = bitmap;
                this.mBgList.add(bg);
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public void onClick(View v) {

    }
}
