package com.alfray.asqare.timer;

/**
 * Created by yutao on 11/20/15.
 */
public interface GameCountDownListener {
    public void onTick(long second);

    public void onFinish();
}
