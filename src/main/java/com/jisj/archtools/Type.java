package com.jisj.archtools;

import java.nio.file.Path;
import java.util.Arrays;

/**
 * Types of supported archives
 */
public enum Type {
    RAR("rar"),
    ZIP("zip"),
    /**
     * 7z archive format
     */
    S7Z("7z"),
    UNKNOWN("");

    private final String ext;

    Type(String ext) {
        this.ext = ext;
    }

    /**
     * Gets type of archive by file extension
     * @param fileName {@code Path} of archive
     * @return archive {@link Type}
     */
    public static Type getType(Path fileName) {
        return Arrays.stream(values())
                .filter(value -> Utils.getFileExtension(fileName, false).equalsIgnoreCase(value.ext))
                .findFirst().orElse(UNKNOWN);
    }

    /**
     * Returns extension of archive file with dot leading
     * @return extension: {@code .ext}
     */
    public String getExt() {
        return "." + ext;
    }

}
