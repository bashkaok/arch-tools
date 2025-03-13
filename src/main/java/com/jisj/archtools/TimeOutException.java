package com.jisj.archtools;

public class TimeOutException extends ArchiveException {
    public TimeOutException(String message) {
        super(message);
    }

    public TimeOutException(Throwable cause) {
        super(cause);
    }
}
