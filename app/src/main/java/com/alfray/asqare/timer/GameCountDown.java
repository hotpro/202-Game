package com.alfray.asqare.timer;



/**
 * Created by yutao on 11/20/15.
 */
public interface GameCountDown {
    public void start();

    public void register(GameCountDownListener listener);

    public void unregister(GameCountDownListener listener);

    public void onTick(long second);

    public void onFinish();
}
