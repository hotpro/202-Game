package edu.sjsu.cmpe202.log;

/**
 * Created by yutao on 11/16/15.
 */
public abstract class AbstractLogger {
    public static final String TAG = AbstractLogger.class.getSimpleName();
    public static final int INFO = 1;
    public static final int DEBUG = INFO + 1;
    public static final int ERROR = DEBUG + 1;

    protected int level;
    protected AbstractLogger nextLogger;

    public void setNextLogger(AbstractLogger nextLogger) {
        this.nextLogger = nextLogger;
    }

    public void logMessage(int level, String message) {
        if (this.level <= level) {
            write(message);
        }
        if (nextLogger != null) {
            nextLogger.logMessage(level, message);
        }
    }

    protected abstract void write(String message);
}
