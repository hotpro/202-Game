package edu.sjsu.cmpe202.component_i;

import android.os.Handler;

import edu.sjsu.cmpe202.AsqareActivity;
import edu.sjsu.cmpe202.engine.AnimThread;

/**
 * Created by ivanybma on 11/17/15.
 */
public class BpPanelCmdCdDbpRcv implements BpPanelCmdRcv{

    private AnimThread mAnimThread;
    public Handler timer;
    private AsqareActivity mActivity;

    public BpPanelCmdCdDbpRcv(AnimThread mAnimThread, Handler timer, AsqareActivity mActivity){
        this.mAnimThread=mAnimThread;
        this.timer=timer;
        this.mActivity=mActivity;
    }

    @Override
    public void doAction() {

        mAnimThread.pauseThread(false);
        timer countdown = new timer(timer, mActivity.getContext());
        Thread timethread = new Thread(countdown, "countdown_thread");
        timethread.start();

    }
}
