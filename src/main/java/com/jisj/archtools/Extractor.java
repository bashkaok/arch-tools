package com.jisj.archtools;

import java.nio.file.Path;
import java.util.List;

/**
 * The interface for archives extractors
 */
public interface Extractor extends Archiver {
    /**
     * Extracts files from archive to specified destination directory
     * @param archive archive file path
     * @param destination destination directory path
     * @throws ArchiveException when archive file not found or destination path not found or not is directory, on archive extracting errors
     * @throws TimeOutException on timeout breaking
     */
    void extractTo(Path archive, Path destination) throws ArchiveException;

    /**
     * Gets file list from specified archive
     * @param archive archive file path
     * @return archive file list. If the extract util incompatible with archive type will be return empty {@link List}
     * @throws ArchiveException on archive extracting errors, archive file not found, on timeout breaking
     */
    List<String> getFileList(Path archive) throws ArchiveException;

    /**
     * Tests specified archive
     * @param archive archive file path
     * @throws ArchiveException when archive file not found, or archive extracting errors
     */
    void test(Path archive) throws ArchiveException;
}
