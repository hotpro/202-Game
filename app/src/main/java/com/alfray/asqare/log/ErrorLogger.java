package com.alfray.asqare.log;

/**
 * Created by yutao on 11/16/15.
 */
public class ErrorLogger extends AbstractLogger {
    public ErrorLogger(int level) {
        this.level = level;
    }

    @Override
    protected void write(String message) {

    }
}
