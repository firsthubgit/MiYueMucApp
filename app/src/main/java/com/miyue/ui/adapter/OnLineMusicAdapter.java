package com.miyue.ui.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.miyue.R;
import com.miyue.application.MiYueConstans;
import com.miyue.bean.QQSong;
import com.miyue.common.base.BaseActivity;
import com.miyue.http.HttpApi;
import com.miyue.utils.StringUtils;

import java.util.List;

/**
 * Created by zhangzhendong on 17/5/30.
 */

public class OnLineMusicAdapter extends BaseAdapter {

    private Context mContext;
    private List<QQSong> songList;
    private Holder holder;

    private Drawable mDrawable;

    private OnDownClickListener listener;
    public OnLineMusicAdapter(Context context, List<QQSong> songs){
        this.mContext = context;
        this.songList = songs;
    }

    @Override
    public int getCount() {
        return songList.size();
    }

    @Override
    public QQSong getItem(int position) {
        return songList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if(null == convertView){
            holder = new Holder();
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.item_musiclist, parent, false);
            holder.tv_music_order = (TextView) convertView.findViewById(R.id.tv_music_order);
            holder.tv_music_name = (TextView) convertView.findViewById(R.id.tv_music_name);
            holder.tv_artist = (TextView) convertView.findViewById(R.id.tv_music_singer);
            holder.tv_lrc_sou = (TextView) convertView.findViewById(R.id.tv_lrc_sou);
            holder.iv_list_icon = (ImageView) convertView.findViewById(R.id.iv_list_icon);
            holder.iv_list_icon.setImageResource(R.drawable.search_download);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        holder.tv_music_order.setVisibility(View.GONE);
        QQSong song = songList.get(position);

        holder.tv_music_name.setText(song.getFsong());
        holder.tv_artist.setText(song.getFsinger() + "·" + song.getAlbumname());


        if(mDrawable == null){
            mDrawable = mContext.getResources().getDrawable(R.drawable.lrc_search);
            mDrawable.setBounds(0, 0, mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight());
        }

        if(!StringUtils.isNullOrEmpty(song.getLyric_hilight())
                && song.getLyric_hilight().equals(song.getLyric())){
            holder.tv_lrc_sou.setText(song.getLyric());
            holder.tv_lrc_sou.setVisibility(View.GONE);
            holder.tv_lrc_sou.setCompoundDrawables(null, null, null, null);
        } else if(!StringUtils.isNullOrEmpty(song.getLyric_hilight())){
            holder.tv_lrc_sou.setText(Html.fromHtml(song.getLyric_hilight()));
            holder.tv_lrc_sou.setVisibility(View.VISIBLE);
            if(song.getLyric_hilight().contains("译名") ||
                    song.getLyric_hilight().contains("《")){
                holder.tv_lrc_sou.setCompoundDrawables(null, null, null, null);
            }else{
                holder.tv_lrc_sou.setCompoundDrawables(mDrawable, null, null, null);
            }
        } else if(!StringUtils.isNullOrEmpty(song.getLyric())){
            holder.tv_lrc_sou.setText(song.getLyric());
            holder.tv_lrc_sou.setVisibility(View.VISIBLE);
            if(song.getLyric_hilight().contains("译名") ||
                    song.getLyric_hilight().contains("《")){
                holder.tv_lrc_sou.setCompoundDrawables(null, null, null, null);
            }else{
                holder.tv_lrc_sou.setCompoundDrawables(mDrawable, null, null, null);
            }
        } else {
            holder.tv_lrc_sou.setVisibility(View.GONE);
        }

        holder.iv_list_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDownClick(getItem(position));
            }
        });

        return convertView;
    }



    private class Holder{
        TextView tv_music_order;
        TextView tv_music_name;
        TextView tv_artist;
        TextView tv_lrc_sou;
        ImageView iv_list_icon;
        private Holder(){

        }
    }

    public interface OnDownClickListener{
        void onDownClick(QQSong qqSong);
    }

    public void setOnDownClickListener(OnDownClickListener listener){
        this.listener = listener;
    }

}
