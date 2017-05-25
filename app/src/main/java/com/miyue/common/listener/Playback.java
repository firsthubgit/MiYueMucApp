package com.miyue.common.listener;

/**
 * Created by zhangzhendong on 17/5/19.
 */

public interface Playback  {


    interface Callback {

        void onCompletion();

        void onPlaybackStatusChanged(int state);

        void onError(String error);

    }
}
