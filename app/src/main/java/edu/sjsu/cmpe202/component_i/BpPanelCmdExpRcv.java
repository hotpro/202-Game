package edu.sjsu.cmpe202.component_i;

import android.os.Handler;

import edu.sjsu.cmpe202.AsqareActivity;
import edu.sjsu.cmpe202.engine.AnimThread;

/**
 * Created by ivanybma on 11/17/15.
 */
public class BpPanelCmdExpRcv implements BpPanelCmdRcv {

    private AnimThread mAnimThread;
    public Handler timer;
    private AsqareActivity mActivity;
    public static int bpincrease =0;

    public BpPanelCmdExpRcv(AnimThread mAnimThread, Handler timer, AsqareActivity mActivity){
        this.mAnimThread=mAnimThread;
        this.timer=timer;
        this.mActivity=mActivity;
    }
    @Override
    public void doAction() {
        mAnimThread.pauseThread(false);
        bpincrease=1;
        System.out.println("u just pressed option B");

    }
}
