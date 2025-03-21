package com.jisj.archtools;

public class UnsupportedCommand extends ArchiveException {
    /**
     * Default constructor
     */
    public UnsupportedCommand() {
        super("Unsupported command");
    }
}
