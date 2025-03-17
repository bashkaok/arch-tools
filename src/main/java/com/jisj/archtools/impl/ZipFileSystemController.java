package com.jisj.archtools.impl;

import com.jisj.archtools.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.jisj.archtools.Controller.CopyOptions.REPLACE_EXISTING;
import static java.lang.Integer.MAX_VALUE;

/**
 * Implements {@link Controller}, {@link Extractor} interfaces by {@link FileSystems} and <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/io/fsp/zipfilesystemprovider.html">Zip File System Provider</a>
 */
public class ZipFileSystemController implements Controller, Extractor {
    private boolean silentMode = true;

    /**
     * Sets "silent" mode for {@link #removeFiles(Path, List)}.
     * In the silent mode an exceptions like {@link FileNotFoundException}, {@link InvalidPathException}, {@link DirectoryNotEmptyException} will not throw
     *
     * @param silentMode {@code false} switch off silent mode. Default {@code true}
     */
    public void setSilentMode(boolean silentMode) {
        this.silentMode = silentMode;
    }

    @Override
    public void create(Path zipArchive) throws ArchiveException {
        final Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        final URI uri = URI.create("jar:" + zipArchive.toUri());

        //noinspection EmptyTryBlock
        try (var ignored = FileSystems.newFileSystem(uri, env)) {
        } catch (IOException e) {
            throw new ArchiveException(e);
        }
    }

        /**
     * @throws ArchiveException when: <ul>
     *                          <li>{@link FileSystem} open exception</li>
     *                          <li>archive path not found</li>
     *                          <li>files to add {@code List} is empty</li>
     *
     *                          </ul><br>
     *                          If {@link #setSilentMode(boolean) SilentMode} is switched off {@code ArchiveException} will be caused: <ul>
     *                          <li>{@link InvalidPathException}</li>
     *                          <li>{@link FileAlreadyExistsException} on try save same file with option {@link com.jisj.archtools.Controller.CopyOptions#OMIT_SAME OMIT_SAME}</li>
     *                          <li>{@link NoSuchFileException} on try to save file into not existing directory</li>
     *                          </ul>
     */
    @Override
    public List<Path> addFiles(Path archive, List<Path> files, String toPath, CopyOptions option) throws ArchiveException {
        if (!silentMode)
            System.out.println(getClass().getName() + ".addFiles: Silent mode is switched off" +
                    "\nAdd to: " + archive +
                    "\nFiles : " + files);
        if (files.isEmpty())
            throw new ArchiveException("Nothing to add");
        List<Path> errors = new ArrayList<>();
        try (var zipFS = FileSystems.newFileSystem(archive)) {
            Path pathInZip;
            for (var file : files) {
                try {
                    pathInZip = zipFS.getPath(toPath.isEmpty() ? "/" : toPath, file.getFileName().toString());
                } catch (InvalidPathException e) {
                    errors.add(file);
                    if (!silentMode) throw new ArchiveException(e);
                    continue;
                }
                if (option == REPLACE_EXISTING)
                    try {
                        Files.copy(file, pathInZip, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        errors.add(file);
                        if (!silentMode) throw new ArchiveException(e);
                    }
                else
                    try {
                        Files.copy(file, pathInZip);
                    } catch (IOException e) {
                        errors.add(file);
                        if (!silentMode) throw new ArchiveException(e);
                    }
            }
        } catch (IOException e) {
            throw new ArchiveException(e);
        }
        if (!silentMode) System.out.println(this.getClass().getName() + ".addFiles: results=" + errors);
        return errors;
    }

    /**
     * @throws ArchiveException when {@link FileSystem} opens
     *                          If {@link #setSilentMode(boolean) SilentMode} is switched off {@code ArchiveException} will be caused:
     *                          <ul>
     *                               <li>{@link InvalidPathException}</li>
     *                               <li>{@link FileNotFoundException} on try to delete not existing file</li>
     *                               <li>{@link DirectoryNotEmptyException}} on try to delete not existing directory</li>
     *                          </ul>
     */
    @Override
    public List<String> removeFiles(Path archive, List<String> pathsInArchive) throws ArchiveException {
        List<String> errors = new ArrayList<>();
        try (var zipFS = FileSystems.newFileSystem(archive)) {
            Path pathInZip;
            for (var file : pathsInArchive) {
                try {
                    pathInZip = zipFS.getPath(file);
                } catch (InvalidPathException e) {
                    if (silentMode) {
                        errors.add(file);
                        continue;
                    }
                    throw new ArchiveException(e);
                }
                try {
                    if (!Files.deleteIfExists(pathInZip)) {
                        if (silentMode) errors.add(file);
                        else throw new ArchiveException("File not found: " + file);
                    }
                } catch (IOException e) {
                    errors.add(file);
                    if (!silentMode) throw new ArchiveException(e);
                }
            }
        } catch (IOException e) {
            throw new ArchiveException(e);
        }
        return errors;
    }


    @Override
    public void setProgressListener(Consumer<Long> consumer) {

    }

    @Override
    public void setMessageListener(Consumer<String> consumer) {

    }

    @Override
    public void extractTo(Path archive, Path destination) throws ArchiveException {
        throw new UnsupportedCommand();
    }

    @Override
    public List<String> getFileList(Path archive) throws ArchiveException {
        try (var zipFS = FileSystems.newFileSystem(archive)) {
            Path root = zipFS.getPath("/");
            try (var files = Files.walk(root, MAX_VALUE)) {
                return files
                        .filter(path -> !path.equals(root))
                        .map(Path::toString)
                        .toList();
            }
        } catch (IOException e) {
            throw new ArchiveException(e);
        }
    }

    @Override
    public void test(Path archive) throws ArchiveException {
        throw new UnsupportedCommand();

    }
}
