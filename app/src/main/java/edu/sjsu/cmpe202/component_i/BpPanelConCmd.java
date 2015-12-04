package edu.sjsu.cmpe202.component_i;

/**
 * Created by ivanybma on 11/17/15.
 */
public class BpPanelConCmd implements BpPanelCmd {

    private BpPanelCmdRcv cmdrcv;

    @Override
    public void execute() {
        cmdrcv.doAction();
    }

    @Override
    public void setReceiver(BpPanelCmdRcv target) {
        this.cmdrcv=target;
    }
}
