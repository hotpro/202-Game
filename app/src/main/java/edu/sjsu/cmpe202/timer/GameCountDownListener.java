package edu.sjsu.cmpe202.timer;

/**
 * Created by yutao on 11/20/15.
 */
public interface GameCountDownListener {
    public void onTick(long second);

    public void onFinish();
}
