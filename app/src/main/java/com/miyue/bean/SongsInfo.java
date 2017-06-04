package com.miyue.bean;

import java.util.ArrayList;

/**
 * Created by zhangzhendong on 17/5/31.
 */

public class SongsInfo<T> {

    /**总数*/
    private String totalnum;

    private ArrayList<T> list;

    public String getTotalnum() {
        return totalnum;
    }

    public void setTotalnum(String totalnum) {
        this.totalnum = totalnum;
    }

    public ArrayList<T> getList() {
        return list;
    }
    public void setList(ArrayList<T> list) {
        this.list = list;
    }
}
