package com.jisj.archtools;

import java.nio.file.Path;

/**
 * The interface for archives packer utils
 */
public interface Packer extends Archiver {
    /**
     * Creates archive of files in the specified folder
     * @param archive archive name
     * @param filesFolder folder with files for archiving
     * @throws ArchiveException if folder with files for archiving not found, during files operations
     */
    void packOfFolder(Path archive, Path filesFolder) throws ArchiveException;
}
