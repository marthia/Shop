package com.poonehmedia.app;

public class CrashReportException extends Exception {

    public CrashReportException(String message, Throwable cause) {
        super("TryCatch -> " + message, cause);
    }
}
