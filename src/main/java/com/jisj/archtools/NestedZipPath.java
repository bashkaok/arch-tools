package com.jisj.archtools;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.ClosedFileSystemException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


/**
 * {@code Closable} object with the root {@code Path} to specified ZIP in nested archive<br>
 * Usage: <pre>{@code
 *      try (var zp = NestedZipPath.newPath(Path.of("nested-archive.zip"), "file1.zip", "file2.zip", "file3.zip")) {
 *             assertEquals("Text from file3.txt", Files.readString(zp.getPath().resolve("file3.txt")));
 *         } catch (IOException e) {
 *             ...
 *         }
 *  nested-archive.zip
 *      ┖ file1.zip
 *          ┖ file2.zip
 *              ┖ file3.zip
 *                  ┖ file3.txt
 * }</pre>
 */
public class NestedZipPath implements Closeable {
    private final Path rootPath;
    private final List<FileSystem> fileSystems = new ArrayList<>();
    private final Path path;

    private NestedZipPath(Path rootPath, String... paths) throws IOException {
        this.rootPath = rootPath;
        path = getFileSystemPath(paths);
    }

    private Path getFileSystemPath(String... paths) throws IOException {
        FileSystem fs = openFS(rootPath);
        fileSystems.add(fs);
        for (var path : paths) {
            Path p = fs.getPath(path);
            fs = openFS(p);
            fileSystems.add(fs);
        }
        return fs.getPath("/");
    }

    /**
     * Returns {@code Closable} object with the root {@code Path} to specified ZIP in nested archive
     * @param rootPath {@code Path} of ZIP in OS file system
     * @param paths string paths of nested file systems
     * @return object with {@code Path}
     * @throws IOException when new {@link FileSystem} opens or closes
     * @see #getPath()
     */
    public static NestedZipPath newPath(Path rootPath, String... paths) throws IOException {
        return  new NestedZipPath(rootPath, paths);
    }

    /**
     * Overloaded method
     * @param nestedPath path to nest archive
     * @return object with {@code Path}
     * @throws IOException when new {@link FileSystem} opens or closes
     * @see NestedZipPath
     * @see NestedPath
     */
    public static NestedZipPath newPath(NestedPath nestedPath) throws IOException {
        return  new NestedZipPath(nestedPath.rootPath(), nestedPath.paths());
    }

    /**
     * Return built {@code Path} to nest archive
     * @return {@code Path} with opened {@code FileSystem}
     */
    public Path getPath() {
        return path;
    }

    private FileSystem openFS(Path path) throws IOException {
        try {
            return FileSystems.newFileSystem(path);

        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        var li = fileSystems.listIterator(fileSystems.size());
        while(li.hasPrevious()) {
            FileSystem fs = li.previous();
            try {
                if (fs.isOpen()) fs.close();
            } catch (ClosedFileSystemException e) {
                throw new IOException(e);
            }
        }
        fileSystems.clear();
    }
}
