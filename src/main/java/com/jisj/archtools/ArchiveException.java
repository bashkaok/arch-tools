package com.jisj.archtools;

import java.io.IOException;

/**
 * Archive tools exception
 */
public class ArchiveException extends IOException {

    public ArchiveException(String message) {
        super(message);
    }

    public ArchiveException(Throwable cause) {
        super(cause);
    }

    public ArchiveException(String message, Throwable cause) {
        super(message, cause);
    }
}
