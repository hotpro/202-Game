package edu.sjsu.cmpe202.timer;



/**
 * Created by yutao on 11/20/15.
 */
public interface GameCountDown {
    public void start();

    public void cancel();

    public void register(GameCountDownListener listener);

    public void unregister(GameCountDownListener listener);

    public void onTick(long second);

    public void onFinish();
}
