package com.jisj.archtools.conv;

import com.jisj.archtools.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static com.jisj.archtools.Utils.removeNotEmptyFolder;
import static com.jisj.archtools.Utils.getNoExtName;

/**
 * Converts an archive to another format
 */
public class Converter {
    private static final Logger log = Logger.getLogger(Converter.class.getName());
    private static final int MAX_PROGRESS_CORRECTION = 10;

    private Path sourceArchive;
    private Path destinationFolder;
    private Type targetFormat;
    private Path temporaryRootFolder;

    private UtilProvider provider;
    private Extractor extractor;
    private Packer packer;
    private Path destinationArchive;
    private Path temporaryArchiveFolder;

    private final Set<Options> options = new HashSet<>();
    private Consumer<String> stepMessageListener;
    private Consumer<String> messageListener;
    private Consumer<Long> progressListener;
    private long maxProgressCount;
    private State state;
    private int sourceArchiveFilesCount;


    Converter() {
    }

    void setSourceArchive(Path sourceArchive) {
        this.sourceArchive = sourceArchive;
    }

    public Path getSourceArchive() {
        return sourceArchive;
    }

    void setDestinationFolder(Path destinationFolder) {
        this.destinationFolder = destinationFolder;
    }

    Path getDestinationFolder() {
        return destinationFolder;
    }

    void setTargetFormat(Type targetFormat) {
        this.targetFormat = targetFormat;
    }

    Type getTargetFormat() {
        return targetFormat;
    }

    void setTemporaryRootFolder(Path temporaryRootFolder) {
        this.temporaryRootFolder = temporaryRootFolder;
    }

    Path getTemporaryRootFolder() {
        return temporaryRootFolder;
    }

    void setProvider(UtilProvider provider) {
        this.provider = provider;
    }

    void setExtractor(Extractor extractor) {
        this.extractor = extractor;
    }

    public Extractor getExtractor() {
        return extractor;
    }

    void setPacker(Packer packer) {
        this.packer = packer;
    }

    @SuppressWarnings("unused")
    public Packer getPacker() {
        return packer;
    }

    public Path getDestinationArchive() {
        return destinationArchive;
    }

    public Path getTemporaryArchiveFolder() {
        return temporaryArchiveFolder;
    }

    void setOptions(Options[] options) {
        this.options.addAll(List.of(options));
    }

    public Set<Options> getOptions() {
        return options;
    }

    void build() {
        temporaryArchiveFolder = getTemporaryRootFolder()
                .resolve(getNoExtName(getSourceArchive()));

        destinationArchive = getDestinationFolder()
                .resolve(getNoExtName(getSourceArchive().getFileName()) +
                        getTargetFormat().getExt());

        extractor.setMessageListener(this::messageTranslator);
        extractor.setProgressListener(this::progressTranslator);
        packer.setMessageListener(this::messageTranslator);
        packer.setProgressListener(this::progressTranslator);
    }

    private void progressTranslator(long counter) {
        if (progressListener != null) progressListener.accept(counter);

    }

    private void messageTranslator(String s) {
        if (messageListener != null) messageListener.accept(s);
    }

    private void stepMessageTranslator(String s) {
        if (stepMessageListener != null) stepMessageListener.accept(s);
    }

    private void assertFiles() throws ArchiveException {
        if (!Files.exists(sourceArchive))
            throw new ArchiveException("Source archive file not found: " + sourceArchive.toAbsolutePath());

        if (Type.getType(sourceArchive) == Type.UNKNOWN)
            throw new IllegalArgumentException("Unsupported archive format: " + sourceArchive);

        if (Type.getType(sourceArchive) == targetFormat)
            throw new IllegalStateException("Same source and target format: " + sourceArchive + " to " + targetFormat);

        if (sourceArchive.equals(destinationArchive))
            throw new IllegalStateException("Source ant target archives are same: " + sourceArchive + " : " + destinationArchive);

        if (Files.exists(destinationArchive))
            throw new ArchiveException("Target archive file already exists: " + destinationArchive.toAbsolutePath());

    }

    /**
     * Sets message listener. Listener gets each new string from output stream of {@code Archiver}
     *
     * @param messageListener {@code Consumer<String>}
     */
    public void setMessageListener(Consumer<String> messageListener) {
        this.messageListener = messageListener;
    }

    /**
     * Sets progress listener. Progress increases with each new string from output stream of {@code Archiver}
     *
     * @param progressListener {@code Consumer<Integer>}
     */
    public void setProgressListener(Consumer<Long> progressListener) {
        this.progressListener = progressListener;
    }

    /**
     * Sets listener for message with conversion step
     *
     * @param stepMessageListener {@code Consumer<String>}
     */
    public void setStepMessageListener(Consumer<String> stepMessageListener) {
        this.stepMessageListener = stepMessageListener;
    }

    /**
     * Gets max count of progress
     * @return count
     */
    public long getMaxProgressCount() {
        return maxProgressCount;
    }

    protected boolean testSourceArchive() {
        try {
            stepMessageTranslator("Converting : Testing - " + sourceArchive.getFileName());
            extractor.test(sourceArchive);
            return true;
        } catch (Exception e) {
            setState(Options.TEST_BEFORE, e);
            return false;
        }
    }

    /**
     * Tests target (result) archive
     *
     * @return true if successfully
     */
    protected boolean testTargetArchive() {
        try {
            stepMessageTranslator("Converting : Testing - " + destinationArchive.getFileName());
            throw new UnsupportedCommand();
        } catch (ArchiveException e) {
            setState(Options.TEST_AFTER, e);
            return false;
        }
    }

    /**
     * Compares the source and target archives by the count of contained files
     *
     * @return {@code true} if equals
     */
    protected boolean compare() {
        stepMessageTranslator("Converting : Comparing - " + sourceArchive.getFileName() + " and " + destinationArchive.getFileName());
        try {
            final int resultArchiveFilesCont = getTargetFilesCount();
            if (sourceArchiveFilesCount != resultArchiveFilesCont) {
                throw new ArchiveException("Source and target archives files count is differ:" +
                        "\n<" + sourceArchiveFilesCount + "> in " + sourceArchive.toAbsolutePath() +
                        "\n<" + resultArchiveFilesCont + "> in " + destinationArchive.toAbsolutePath());
            }
        } catch (ArchiveException e) {
            setState(Options.COMPARE, e);
            return false;
        }
        return true;
    }

    private int getTargetFilesCount() throws ArchiveException {
        if (packer instanceof Extractor targetExtractor) {
            return targetExtractor.getFileList(destinationArchive).size();
        } else {
            return provider.getExtractor(targetFormat)
                    .orElseThrow(()-> new ArchiveException("Extractor not found for " + destinationArchive.toAbsolutePath()))
                    .getFileList(destinationArchive)
                    .size();
        }
    }

    protected boolean unPack() {
        try {
            assertFiles();
            createTemporaryFolder();
            stepMessageTranslator("Converting : Extracting - " + sourceArchive.getFileName());
            extractor.extractTo(sourceArchive, temporaryArchiveFolder);
            return true;
        } catch (IOException e) {
            setState(Options.EXTRACTING, e);
            return false;
        }
    }

    private void setState(Options step, Throwable e) {
        state = new State(step, e);
    }

    public State getState() {
        return state;
    }

    protected boolean pack() {
        try {
            assertFiles();
            stepMessageTranslator("Converting : Packing - " + destinationArchive.getFileName());
            packer.packOfFolder(destinationArchive, temporaryArchiveFolder);
            return true;
        } catch (ArchiveException e) {
            setState(Options.PACKING, e);
            return false;
        }
    }

    /**
     * Converts source archive to target format
     */
    public boolean convert() {
        try {
            assertFiles();
        } catch (ArchiveException e) {
            setState(Options.START, e);
            return false;
        }

        if (options.contains(Options.TEST_BEFORE))
            if (!testSourceArchive()) return false;

        try {
            sourceArchiveFilesCount = extractor.getFileList(sourceArchive).size();
            if (sourceArchiveFilesCount == 0) {
                throw new ArchiveException("No files to extract in " + sourceArchive.toAbsolutePath());
            }
            maxProgressCount = sourceArchiveFilesCount + MAX_PROGRESS_CORRECTION;
        } catch (ArchiveException e) {
            setState(Options.START, e);
            return false;
        }


        if (!unPack()) return false;
        if (!pack()) return false;

        if (options.contains(Options.TEST_AFTER))
            if (!testTargetArchive()) return false;
        if (options.contains(Options.COMPARE))
            if (!compare()) return false;
        try {
            removeTemporaryFolder();
        } catch (IOException e) {
            log.warning("Cannot delete temporary folder " + getTemporaryArchiveFolder());
        }
        setState(Options.ALL, null);
        return true;
    }

    private void removeTemporaryFolder() throws IOException {
        removeNotEmptyFolder(temporaryArchiveFolder);
    }

    private void createTemporaryFolder() throws IOException {
        Files.createDirectories(temporaryArchiveFolder);
    }

    public static ConverterBuilder builder() {
        return new ConverterBuilder();
    }

    /**
     * The builder for {@code Converter} class
     * @param provider archive utility provider
     * @return {@code ConverterBuilder} object
     */
    public static ConverterBuilder builder(UtilProvider provider) {
        return new ConverterBuilder(provider);
    }


    @Override
    public String toString() {
        return "Converter{" +
                "sourceArchive=" + sourceArchive +
                ", destinationArchive=" + destinationArchive +
                ", temporaryArchiveFolder=" + temporaryArchiveFolder +
                ", packer=" + packer +
                ", extractor=" + extractor +
                '}';
    }

    public record State(Options step,
                        Throwable exception) {
    }


    /**
     * Convertor options
     */
    public enum Options {
        /**
         * First converting step. Check environment and source file
         */
        START,
        /**
         * Perform extracting from source archive. Performs always
         */
        EXTRACTING,
        /**
         * Perform packing to target archive. Performs always
         */
        PACKING,
        /**
         * Perform the test source archive.
         */
        TEST_BEFORE,
        /**
         * Perform the test target archive
         */
        TEST_AFTER,
        /**
         * To compare the source and target archives by count of contained files
         */
        COMPARE,
        /**
         * Performs all steps
         */
        ALL
    }
}
