package com.jisj.archtools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

public class Utils {
    /**
     * Deletes the specified folder and all children folders and files
     * @param folder folder {@code Path}
     * @throws IOException during file operations
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void removeNotEmptyFolder(Path folder) throws IOException {
        try (Stream<Path> paths = Files.walk(folder).sorted(Comparator.reverseOrder())) {
            paths.map(Path::toFile).forEach(File::delete);
        }
    }

    /**
     * Gets file extension. Extension excludes from last chars after dot in the file name
     * @param fileName file name
     * @param dotInclude {@code true} returns extension with leading dot
     * @return file extension
     * @see #getFileExtension(Path, boolean)
     */
    public static String getFileExtension(String fileName, boolean dotInclude) {
        int dotIndex = fileName.lastIndexOf('.') + (dotInclude ? 0 : 1);
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex).toLowerCase();
    }

    /**
     * Gets file extension. Extension excludes from last chars after dot in the file name
     * @param fileName file name
     * @param dotInclude {@code true} returns extension with leading dot
     * @return file extension
     * @see #getFileExtension(String, boolean)
     */

    public static String getFileExtension(Path fileName, boolean dotInclude) {
        return getFileExtension(fileName.getFileName().toString(), dotInclude);
    }

    public static String getNoExtName(Path path) {
        return path.getFileName().toString()
                .substring(0, path.getFileName().toString().lastIndexOf(".")).trim();
    }
}
