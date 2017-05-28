package com.miyue.common.listener;

/**
 * Created by zhangzhendong on 17/5/27.
 */

public interface ReqCallBack<T> {
    void success(T t);
    void failed();
}
