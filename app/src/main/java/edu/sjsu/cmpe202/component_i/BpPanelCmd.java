package edu.sjsu.cmpe202.component_i;

/**
 * Created by ivanybma on 11/17/15.
 */
public interface BpPanelCmd {

    public void execute();
    public void setReceiver(BpPanelCmdRcv target);
}
