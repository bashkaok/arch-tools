package com.jisj.archtools.conv;

import com.jisj.archtools.Type;

import java.nio.file.Path;


public class ConverterBuilder {
    private final Converter converter;
    private UtilProvider provider;

    ConverterBuilder() {
        converter = new Converter();
    }

    ConverterBuilder(UtilProvider provider) {
        this();
        this.provider = provider;
    }


    public ConverterBuilder utilProvider(UtilProvider provider) {
        this.provider = provider;
        return this;
    }

    /**
     * Sets source archive
     * @param fileName source archive name
     * @return ConverterBuilder
     */
    public ConverterBuilder sourceArchive(Path fileName) {
        converter.setSourceArchive(fileName);
        return this;
    }

    /**
     * Sets destination folder path
     * @param folderName destination folder path to new archive
     * @return ConverterBuilder
     */
    public ConverterBuilder destinationFolder(Path folderName) {
        converter.setDestinationFolder(folderName);
        return this;
    }

    /**
     * Sets target archive format
     * @param format type from enum {@link Type}
     * @return ConverterBuilder
     */
    public ConverterBuilder targetFormat(Type format) {
        converter.setTargetFormat(format);
        return this;
    }

    /**
     * Sets temporary folder
     * @param folderName temporary folder for extracting source archive
     * @return ConverterBuilder
     */
    public ConverterBuilder temporaryFolder(Path folderName) {
        converter.setTemporaryRootFolder(folderName);
        return this;
    }

    /**
     * Sets options
     * @param options options from {@link com.jisj.archtools.conv.Converter.Options Options}
     * @return {@code ConverterBuilder}
     */
    public ConverterBuilder options(Converter.Options... options) {
        converter.setOptions(options);
        return this;
    }

    public Converter build() {
        if (provider==null) {
            System.out.println("Archive util provider not set");
            throw new IllegalArgumentException("Archive util provider not set");
        }
        if (converter.getSourceArchive() == null)
            throw new IllegalArgumentException("Source archive path not set");
        if (converter.getTemporaryRootFolder() == null)
            throw new IllegalArgumentException("Temporary folder path not set");
        if (converter.getTargetFormat() == null)
            throw new IllegalArgumentException("Archive type not set");
        if (converter.getTargetFormat() == Type.UNKNOWN)
            throw new IllegalArgumentException("Unexpected archive type");

        converter.setExtractor(provider.getExtractor(Type.getType(converter.getSourceArchive()))
                .orElseThrow(()-> new IllegalStateException("Extractor utility not found for " + Type.getType(converter.getSourceArchive()))));

        converter.setPacker(provider.getPacker(converter.getTargetFormat())
                .orElseThrow(()-> new IllegalStateException("Packer utility not found for " + converter.getTargetFormat())));

        converter.build();

        return converter;
    }

}
