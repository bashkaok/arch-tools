package com.jisj.archtools;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Class with commands for extract files from archives using native archive utils
 */
public class Extractor implements UnPacker {
    private final ExtractUtil util;
    private int breakTimeOutSec = 10;
    private Path logFile;
    private boolean appendLog = false;

    public Extractor(ExtractUtil util) {
        this.util = util;
    }

    /**
     * Sets break extracting timeout in sec
     *
     * @param breakTimeOutSec new value in sec. Default: 120sec
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

    /**
     * {@inheritDoc}
     *
     * @throws FileNotFoundException {@inheritDoc}
     * @throws ArchiveException      {@inheritDoc}
     * @throws TimeOutException      on timeout breaking
     */
    @Override
    public void extractTo(Path archive, Path destination) throws TimeOutException, FileNotFoundException, ArchiveException {
        assertFileNotFound(archive);
        assertFileNotFound(destination);

        if (!Files.isDirectory(destination))
            throw new FileNotFoundException("Destination path is not directory: " + destination);

        ProcessBuilder processBuilder = new ProcessBuilder(util.extractToDestinationCmd(archive, destination));
        try {
            if (logFile == null) createLog(archive, destination);
            processBuilder.redirectErrorStream(true);
            if (appendLog) processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile.toFile()));
            else processBuilder.redirectOutput(logFile.toFile());

            Process process = processBuilder.start();
            if (!process.waitFor(breakTimeOutSec, TimeUnit.SECONDS)) {
                process.destroy();
                if (process.isAlive()) {
                    process.destroyForcibly();
                }
                String message = "Extract timeout exception after " + breakTimeOutSec + "sec in archive: " + archive;
                saveToLog(message);
                throw new TimeOutException(message + "\nSee log file " + logFile);
            }
            if (process.exitValue() != 0) {
                throw new ArchiveException("Extraction error. ExitValue=" + process.exitValue() + " See log file " + logFile);
            }
            if (!appendLog) deleteLog();
        } catch (TimeOutException e) {
            throw new TimeOutException(e);
        } catch (IOException | InterruptedException ex) {
            throw new ArchiveException(ex);
        }
    }

    public List<String> getFileList(Path archive) throws FileNotFoundException, ArchiveException {
        assertFileNotFound(archive);
        try {
            Process proc = new ProcessBuilder(new RarExtractUtil().getFileListCmd(archive))
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .start();

            BufferedReader input =
                    new BufferedReader(new InputStreamReader(proc.getInputStream()));
//            BufferedReader error =
//                    new BufferedReader(new InputStreamReader(proc.getErrorStream()));

            String line = null;
//            while ((line = input.readLine()) != null) {
//                System.out.println(line);
//            }
            line = null;
//            while ((line = error.readLine()) != null) {
//                System.out.println(line);
//            }

            System.out.println(proc.waitFor());
        }
        catch(Exception e) {e.printStackTrace();}
        return List.of();
    }

    private void assertFileNotFound(Path file) throws FileNotFoundException {
        if (!Files.exists(file)) throw new FileNotFoundException("File/Directory not found: " + file);
    }


    private void createLog(Path archive, Path destination) throws IOException {
        this.logFile = Files.createFile(destination.resolve(archive.getFileName().toString() + ".log"));
    }

    private void saveToLog(String message) throws IOException {
        Files.writeString(logFile, message);
    }

    private void deleteLog() throws IOException {
        Files.delete(logFile);
        logFile = null;
    }
}
