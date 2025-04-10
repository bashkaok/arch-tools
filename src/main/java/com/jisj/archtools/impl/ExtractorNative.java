package com.jisj.archtools.impl;

import com.jisj.archtools.ArchiveException;
import com.jisj.archtools.Extractor;
import com.jisj.archtools.TimeOutException;
import com.jisj.archtools.UnsupportedCommand;
import com.jisj.archtools.cmd.CmdExtractUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Class with commands for extract files from archives using native archive utils
 */
public class ExtractorNative implements Extractor {
    private final CmdExtractUtil util;
    private int breakTimeOutSec = 120;
    private Path archive;
    private Path logFile;
    private boolean appendLog = false;
    private Consumer<Long> progressListener;
    private Consumer<String> messageListener;
    private long progressCount = 0;
    private boolean debugMode = false;

    /**
     * Creates new {@link Extractor} object
     * @param util command interface {@link CmdExtractUtil}
     */
    public ExtractorNative(CmdExtractUtil util) {
        this.util = util;
        if (!Files.exists(util.getUtilPath()))
            throw new IllegalArgumentException("Archive util not found: " + util.getUtilPath());
    }

    /**
     * Sets break extracting timeout in sec
     *
     * @param breakTimeOutSec new value in sec. Default: 10sec
     */
    public void setBreakTimeOutSec(int breakTimeOutSec) {
        this.breakTimeOutSec = breakTimeOutSec;
    }

    /**
     * Sets path to log file. Default log file is created in destination folder with name of archive: {@code archive_name.rar.log}
     *
     * @param logFile name of log file
     */
    public void setLogFile(Path logFile) {
        this.logFile = logFile;
    }

    /**
     * Next extraction log will be added to exist log
     *
     * @param appendLog {@code true} for append log. Default {@code false}
     */
    public void setAppendLog(boolean appendLog) {
        this.appendLog = appendLog;
    }

    @Override
    public void setProgressListener(Consumer<Long> progressListener) {
        this.progressListener = progressListener;
    }
    @Override
    public void setMessageListener(Consumer<String> messageListener) {
        this.messageListener = messageListener;
    }

    /**
     * Sets output to console. Reading of input stream and error stream is unavailable
     * Use this flag to debug
     * @param debugMode {@code true} sets output to console. Default {@code false}
     */
    @SuppressWarnings("unused")
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ArchiveException      {@inheritDoc}
     * @throws TimeOutException      on timeout breaking
     */
    @Override
    public void extractTo(Path archive, Path destination) throws TimeOutException, ArchiveException {
        assertFileNotFound(archive);
        assertFileNotFound(destination);

        if (!Files.isDirectory(destination))
            throw new ArchiveException("Destination path is not directory: " + destination);

        init(archive);

        ProcessBuilder builder = new ProcessBuilder(util.extractToDestinationCmd(archive, destination));
        debugMode(builder);
        try {
            if (logFile == null) createLog(archive, destination);
            Process process = builder.start();
            saveToLog(process.getInputStream());
            saveToLog(process.getErrorStream());
            wait(process);
            if (process.exitValue() != 0) {
                throw new ArchiveException("Extraction error. ExitValue=" + process.exitValue() + " See log file " + logFile);
            }
            if (!appendLog) deleteLog();
        } catch (TimeOutException e) {
            throw new TimeOutException(e);
        } catch (IOException ex) {
            throw new ArchiveException(ex);
        }
    }

    private void debugMode(ProcessBuilder builder) {
        if (debugMode) {
            builder.inheritIO();
            System.out.println(builder.command());
        }
    }

    private void init(Path archive) {
        this.archive = archive;
        progressCount = 0;
    }

    private void wait(Process process) throws IOException {
        try {
            if (!process.waitFor(breakTimeOutSec, TimeUnit.SECONDS)) {
                process.destroy();
                if (process.isAlive()) {
                    process.destroyForcibly();
                }
                String message = "Extract timeout exception after " + breakTimeOutSec + "sec in archive: " + archive;
                if (logFile != null) saveToLog(message);
                throw new TimeOutException(message + "\nSee log file " + logFile);
            }
        } catch (InterruptedException e) {
            throw new ArchiveException(e);
        } catch (TimeOutException e) {
            throw new TimeOutException(e);
        }
        if (process.exitValue() != 0) {
            throw new ArchiveException("Extraction error. ExitValue=" + process.exitValue() + " See log file " + logFile);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws ArchiveException      {@inheritDoc}
     */
    @Override
    public List<String> getFileList(Path archive) throws ArchiveException {
        assertFileNotFound(archive);
        init(archive);
        List<String> result;
        ProcessBuilder builder = new ProcessBuilder(util.getFileListCmd(archive));
        debugMode(builder);
        Process process;
        try {
            process = builder.start();

            result = getFileList(getReader(process.getInputStream())
                    .lines()
                    .peek(this::updateListeners)
                    .toList());
            List<String> errors = getReader(process.getErrorStream()).lines().toList();

            wait(process);
            if (process.exitValue() != 0) {
                if (logFile != null) saveToLog(String.join("\n", errors));
                throw new ArchiveException(String.join("\n", errors));
            }
        } catch (TimeOutException e) {
            throw new TimeOutException(e);
        } catch (IOException e) {
            throw new ArchiveException(e);
        }

        return result;
    }

    @Override
    public void test(Path archive) throws ArchiveException {
        throw new UnsupportedCommand();
    }

    /**
     * For overload in subclass
     *
     * @param fileList file list for mapping
     * @return archive file list
     */
    protected List<String> getFileList(List<String> fileList) {
        return fileList;
    }

    private BufferedReader getReader(InputStream is) {
        return new BufferedReader(new InputStreamReader(is));
    }

    private void updateListeners(String nextElement) {
        if (progressListener != null) progressListener.accept(++progressCount);
        if (messageListener != null) messageListener.accept(nextElement);
    }

    private void assertFileNotFound(Path file) throws ArchiveException {
        if (!Files.exists(file)) throw new ArchiveException("File/Directory not found: " + file);
    }

    private void createLog(Path archive, Path destination) throws IOException {
        this.logFile = Files.createFile(destination.resolve(archive.getFileName().toString() + ".log"));
    }

    private void saveToLog(String message) throws IOException {
        Files.writeString(logFile, message + "\n", StandardOpenOption.APPEND, StandardOpenOption.CREATE);
    }

    private void saveToLog(InputStream inputStream) throws IOException {
        BufferedReader reader = getReader(inputStream);
        String line;
        while ((line = reader.readLine()) !=null) {
            updateListeners(line);
            saveToLog(line);
        }
    }

    private void deleteLog() throws IOException {
        Files.delete(logFile);
        logFile = null;
    }

    @Override
    public String toString() {
        return "ExtractorImpl{" +
                "util=" + util +
                ", breakTimeOutSec=" + breakTimeOutSec +
                ", archive=" + archive +
                ", logFile=" + logFile +
                ", appendLog=" + appendLog +
                ", progress=" + progressListener +
                ", progressCount=" + progressCount +
                ", debugMode=" + debugMode +
                '}';
    }
}
