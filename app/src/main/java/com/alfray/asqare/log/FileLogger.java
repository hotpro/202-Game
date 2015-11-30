package com.alfray.asqare.log;

import android.util.Log;

/**
 * Created by yutao on 11/16/15.
 */
public class FileLogger extends AbstractLogger {
    public FileLogger(int level) {
        this.level = level;
    }

    @Override
    protected void write(String message) {
        Log.d(TAG, "FileLogger: " + message);
    }
}
