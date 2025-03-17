package com.jisj.archtools;

import java.nio.file.Path;
import java.util.List;

/**
 * Functions for existing archive content manipulations
 */
public interface Controller extends Archiver {
    /**
     * Creates new empty ZIP archive
     *
     * @param newArchive path of new archive
     * @throws ArchiveException cause by IOException
     */
    void create(Path newArchive) throws ArchiveException;

    /**
     * Adds specified file list to archive
     *
     * @param archive       path to archive
     * @param files         file list
     * @param pathInArchive path in archive. If the path is empty - all files will be added in root directory, otherwise - into specified directory
     * @param option        of {@link CopyOptions}
     * @return empty {@code List} if all files was added. Otherwise - {@code List} not added files
     * @throws ArchiveException when archive or files not found, archive errors
     */
    List<Path> addFiles(Path archive, List<Path> files, String pathInArchive, CopyOptions option) throws ArchiveException;

    /**
     * Deletes specified file from archive
     *
     * @param archive        path to archive
     * @param pathsInArchive list of string paths of files in archive directory from root
     * @return empty {@code List} if all files was deleted. Otherwise - {@code List} not deleted files
     * @throws ArchiveException when archive or file not found, archive errors. Exception will not throw if file in archive not found
     */
    List<String> removeFiles(Path archive, List<String> pathsInArchive) throws ArchiveException;

    enum CopyOptions {
        /**
         * Replace existing file
         */
        REPLACE_EXISTING,
        /**
         * Skip adding file if same exists in archive
         */
        OMIT_SAME
    }
}
