package com.jisj.archtools.cmd;

import java.nio.file.Path;

/**
 * Common interface for extract commands of archive utils
 */
public interface CmdExtractUtil extends CmdUtil {
    static String encloseInQuotations(String str) {
        return "\"" + str + "\"";
    }

    /**
     * Creates string command for extract all files from archive to destination folder
     *
     * @param archive     source archive
     * @param destination destination folder
     * @return command string
     */
    String extractToDestinationCmd(Path archive, Path destination);

    /**
     * Creates string command for get archive file list
     *
     * @param archive source archive
     * @return command string
     */
    String getFileListCmd(Path archive);

}
