package com.jisj.archtools;

import java.io.FileNotFoundException;
import java.nio.file.Path;

public interface UnPacker {
    /**
     * Extracts files from archive to specified destination directory
     * @param archive archive file path
     * @param destination destination directory path
     * @throws FileNotFoundException when archive file not found or destination path not found or not is directory
     * @throws ArchiveException on archive extracting errors
     */
    void extractTo(Path archive, Path destination) throws FileNotFoundException, ArchiveException;
}
