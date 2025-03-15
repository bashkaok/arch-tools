package com.jisj.archtools.cmd;

import java.nio.file.Path;

import static com.jisj.archtools.cmd.CmdExtractUtil.encloseInQuotations;

/**
 * Native util for ZIP archives
 * <pre>{@code 7z <command> [<switches>...] <archive_name> [<file_names>...] [@listfile]
 *                  <path_to_extract\>}
 * </pre>
 *
 * @param utilPath path to extract util. Default: {@code C:/Program Files/7-Zip/7z.ex}
 */
public record ZipCmd(Path utilPath) implements CmdExtractUtil, CmdPackUtil {

    /**
     * Default constructor: "C:/Program Files/WinRAR/unrar.exe"
     */
    public ZipCmd() {
        this(Path.of("C:/Program Files/7-Zip/7z.exe"));
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
     * @return {@code 7z x -y <archive> <destination>}
     */
    @Override
    public String extractToDestinationCmd(Path archive, Path destination) {
        return encloseInQuotations(utilPath.toAbsolutePath().toString()) + " x -y %s %s"
                .formatted(encloseInQuotations(archive.toAbsolutePath().toString()),
                        encloseInQuotations(destination.toAbsolutePath().toString()));
    }

    /**
     * {@inheritDoc}
     *
     * @param archive source archive
     * @return {@code 7z l -ba <archive>}
     */
    public String getFileListCmd(Path archive) {
        return encloseInQuotations(utilPath.toAbsolutePath().toString()) + " l -ba %s"
                .formatted(encloseInQuotations(archive.toAbsolutePath().toString()));
    }

    @Override
    public Path getUtilPath() {
        return utilPath;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code 7z a -y <archive> <sourceFolder>/*}
     */
    @Override
    public String packOfFolderCmd(Path archive, Path sourceFolder) {
        return encloseInQuotations(utilPath.toAbsolutePath().toString()) + " a -y %s %s"
                .formatted(encloseInQuotations(archive.toAbsolutePath().toString()),
                        encloseInQuotations(sourceFolder.toAbsolutePath() + "\\*"));
    }
}
