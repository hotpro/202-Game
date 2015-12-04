package edu.sjsu.cmpe202.log;

import android.util.Log;

/**
 * Created by yutao on 11/16/15.
 */
public class ErrorLogger extends AbstractLogger {
    public ErrorLogger(int level) {
        this.level = level;
    }

    @Override
    protected void write(String message) {
        Log.d(TAG, "ErrorLogger: " + message);
    }
}
