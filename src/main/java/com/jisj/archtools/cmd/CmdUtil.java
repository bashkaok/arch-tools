package com.jisj.archtools.cmd;

import java.nio.file.Path;

/**
 * Common interface for commands of archives utils
 */
public interface CmdUtil {
    /**
     * Encloses the specified string in quoter marks
     * @param str source {@code String}
     * @return string in quoter marks: {@code "str"}
     */
    static String encloseInQuotations(String str) {
        return "\"" + str + "\"";
    }

    /**
     * Returns {@code Path} of the executive util file
     * @return util path
     */
    Path getUtilPath();
}
