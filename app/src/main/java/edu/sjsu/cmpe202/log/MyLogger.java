package edu.sjsu.cmpe202.log;

/**
 * Created by yutao on 11/16/15.
 */
public class MyLogger {
    private static MyLogger instance;

    private final ConsoleLogger consoleLogger;

    private MyLogger() {
        consoleLogger = new ConsoleLogger(AbstractLogger.INFO);
        AbstractLogger fileLogger = new FileLogger(AbstractLogger.DEBUG);
        AbstractLogger errorLogger = new ErrorLogger(AbstractLogger.ERROR);

        consoleLogger.setNextLogger(fileLogger);
        fileLogger.setNextLogger(errorLogger);
    }

    public static MyLogger instance() {
        if (instance == null) {
            instance = new MyLogger();
        }
        return instance;
    }

    public void d(String message) {
        consoleLogger.logMessage(AbstractLogger.INFO, message);
    }
}
