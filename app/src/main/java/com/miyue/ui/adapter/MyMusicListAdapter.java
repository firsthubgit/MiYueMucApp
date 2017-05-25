package com.miyue.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.miyue.R;

import java.util.ArrayList;

/**
 * Created by zhangzhendong on 16/4/5.
 */
public class MyMusicListAdapter extends BaseAdapter{

    ArrayList<String> list;
    Context mContext;

    public MyMusicListAdapter(ArrayList<String> list,Context mContext){
         this.list = list;
         this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(mContext).inflate(R.layout.custom_album_item, null);
        return convertView;
    }
}
