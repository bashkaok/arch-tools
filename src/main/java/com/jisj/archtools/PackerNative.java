package com.jisj.archtools;

import com.jisj.archtools.cmd.CmdPackUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Class with commands for packing files to archives using native archive util
 */
public class PackerNative implements Packer {

    private final CmdPackUtil util;
    private Consumer<Integer> progressListener;
    private Consumer<String> messageListener;
    private int progressCount = 0;
    private int breakTimeOutSec = 60;
    private boolean debugMode = false;

    /**
     * Creates new {@link Packer} object
     *
     * @param util command interface {@link CmdPackUtil}
     */
    public PackerNative(CmdPackUtil util) {
        this.util = util;
    }

    @Override
    public void setProgressListener(Consumer<Integer> progressListener) {
        this.progressListener = progressListener;
    }

    @Override
    public void setMessageListener(Consumer<String> messageListener) {
        this.messageListener = messageListener;
    }

    private void updateListeners(String nextLine) {
        if (progressListener != null) progressListener.accept(++progressCount);
        if (messageListener != null) messageListener.accept(nextLine);
    }

    /**
     * Sets break extracting timeout in sec
     *
     * @param breakTimeOutSec new value in sec. Default: 60sec
     */
    @SuppressWarnings("unused")
    public void setBreakTimeOutSec(int breakTimeOutSec) {
        this.breakTimeOutSec = breakTimeOutSec;
    }

    /**
     * Sets output to console. Reading of input stream and error stream is unavailable
     * Use this flag to debug
     *
     * @param debugMode {@code true} sets output to console. Default {@code false}
     */
    @SuppressWarnings("unused")
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ArchiveException         {@inheritDoc}
     * @throws IllegalArgumentException if the filesFolder is not directory; target archive overlaps with the source folder
     * @throws TimeOutException         on timeout breaking
     * @implNote This implementation creates all directories in path of archive file
     */
    @Override
    public void packOfFolder(Path archive, Path filesFolder) throws ArchiveException {
        if (!Files.exists(filesFolder))
            throw new ArchiveException("Folder with files not found: " + filesFolder.toAbsolutePath());
        if (!Files.isDirectory(filesFolder))
            throw new IllegalArgumentException("Source files folder is not a directory: " + filesFolder.toAbsolutePath());
        if (archive.getParent().startsWith(filesFolder))
            throw new IllegalArgumentException("Target archive overlaps with the source folder: <" + archive + "> and <" + filesFolder + ">");
        try {
            Files.createDirectories(archive.getParent());
        } catch (IOException e) {
            throw new ArchiveException("Cannot create target folder" + archive.getParent(), e);
        }

        try {
            ProcessBuilder builder = new ProcessBuilder(util.packOfFolderCmd(archive, filesFolder));
            debugMode(builder);
            Process process = builder.start();
            readStream(process.getInputStream(), this::updateListeners);
            List<String> errors = new ArrayList<>();
            readStream(process.getErrorStream(), line -> {
                updateListeners(line);
                errors.add(line);
            });
            if (!process.waitFor(breakTimeOutSec, TimeUnit.SECONDS)) {
                process.destroy();
                if (process.isAlive()) {
                    process.destroyForcibly();
                }
                throw new TimeOutException("Archiver timeout exception after " + breakTimeOutSec + "sec in archive: " + archive + "\n" + this);
            }
            if (process.exitValue() != 0) {
                throw new ArchiveException("Archiving errors: " + String.join("\n", errors));
            }
        } catch (InterruptedException | IOException e) {
            throw new ArchiveException(e);
        }
    }

    private void debugMode(ProcessBuilder builder) {
        if (debugMode) {
            builder.inheritIO();
            System.out.println(this);
            System.out.println(builder.command());
        }
    }

    private void readStream(InputStream inputStream, Consumer<String> consumer) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            consumer.accept(line);
        }

    }


}
