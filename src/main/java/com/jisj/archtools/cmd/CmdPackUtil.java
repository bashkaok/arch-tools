package com.jisj.archtools.cmd;

import java.nio.file.Path;

/**
 * Common interface for extract commands of archive utils
 */
public interface CmdPackUtil extends CmdUtil {

    /**
     * Creates string command for archive creation of files from specified folder
     *
     * @param archive     target archive
     * @param sourceFolder folder with files
     * @return command string
     */
    String packOfFolderCmd(Path archive, Path sourceFolder);

}
