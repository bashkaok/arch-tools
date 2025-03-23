package com.jisj.archtools;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

/**
 * Immutable record with nested archive path<br>
 * Nested path: {@code rootPath/archive1/archive2...archiveN/file_name}<br>
 */
public class NestedPath {
    private final Path rootPath;
    private final String[] nestFiles;
    private FileTime lastModifiedTime;

    /**
     * @param rootPath  {@code Path} with archive in OS file system. {@link java.nio.file.spi.FileSystemProvider FileSystemProvider} : jar
     * @param nestFiles string paths to nest archive or file in archive
     * @see NestedZipPath
     */
    public NestedPath(Path rootPath, String... nestFiles) {
        this.rootPath = rootPath;
        this.nestFiles = nestFiles;
    }

    /**
     * Return the root path
     * @return Path of archive in OS file system
     */
    public Path getRootPath() {
        return rootPath;
    }

    public String[] getNestFiles() {
        return nestFiles;
    }

    public String ofNestFiles() {
        return String.join("/", nestFiles);
    }

    public FileTime getLastModifiedTime() {
        return lastModifiedTime;
    }

    /**
     * Sets time for the object, not for a file
     * @param lastModifiedTime FileTime
     */
    public void setLastModifiedTime(FileTime lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    /**
     * Returns file name.
     * @return root path file name if nest files list is empty or last of nest files
     */
    public String getFileName() {
        if (!isNested()) return getRootPath().getFileName().toString();
        return nestFiles[nestFiles.length-1];
    }

    /**
     * Nested file
     * @return true if the file is nested into archive
     */
    public boolean isNested() {
        return nestFiles.length>0;
    }

    public String toAbsolutePath() {
        if (isNested()) return rootPath.toAbsolutePath() + "?" + ofNestFiles();
        return rootPath.toAbsolutePath().toString();

    }

    @Override
    public String toString() {
        return "NestedPath{" + toAbsolutePath() +
                "}";
    }
}
