package com.alfray.asqare.component_i;

/**
 * Created by ivanybma on 11/17/15.
 */
public class BpPanelMenuItem implements BpPanelCmdIvok {

    private BpPanelCmd bpPanelCmd;

    @Override
    public void setCommand(BpPanelCmd cmd) {
        this.bpPanelCmd=cmd;
    }

    @Override
    public void invoke() {
        bpPanelCmd.execute();
    }
}
