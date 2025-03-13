package com.jisj.archtools;

import java.nio.file.Path;
import java.util.List;

import static com.jisj.archtools.ExtractUtil.encloseInQuotations;

/**
 * Native extract util for RAR archives
 * <pre>{@code unrar <command> -<switch 1> -<switch N> <archive> <files...>
 *                <@listfiles...> <path_to_extract\>}
 * </pre>
 *
 * @param utilPath path to extract util. Default: {@code C:/Program Files/WinRAR/unrar.exe}
 */
public record RarExtractUtil(Path utilPath) implements ExtractUtil {

    /**
     * Default constructor: "C:/Program Files/WinRAR/unrar.exe"
     */
    RarExtractUtil() {
        this(Path.of("C:/Program Files/WinRAR/unrar.exe"));
    }

    /**
     * {@inheritDoc}
     * <p>Additional keys:
     * <ul style="list-style-type:none">
     * <li>-y Assume Yes on all queries</li>
     * </ul>
     *
     * @param archive     source archive
     * @param destination destination folder
     * @return {@code unrar x -y <archive> <destination>}
     */
    @Override
    public String extractToDestinationCmd(Path archive, Path destination) {
        return encloseInQuotations(utilPath.toAbsolutePath().toString()) + " x -y %s %s"
                .formatted(encloseInQuotations(archive.toAbsolutePath().toString()),
                        encloseInQuotations(destination.toAbsolutePath().toString()));
    }

    public String getFileListCmd(Path archive) {
        return encloseInQuotations(utilPath.toAbsolutePath().toString()) + " lb %s"
                .formatted(encloseInQuotations(archive.toAbsolutePath().toString()));
    }
}
