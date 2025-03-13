package com.jisj.archtools;

import java.nio.file.Path;

public interface ExtractUtil {
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
    String getFileListCmd(Path archive);

}
