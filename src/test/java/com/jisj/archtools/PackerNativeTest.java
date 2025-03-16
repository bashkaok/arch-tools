package com.jisj.archtools;

import com.jisj.archtools.cmd.ZipCmd;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static com.jisj.archtools.Uils.clearFolder;
import static org.junit.jupiter.api.Assertions.*;

class PackerNativeTest {
    private static final Path resources = Path.of("src/test/resources");
    private static final Path testData = Path.of("target/test-data");
    private static final Path destination = testData.resolve("tmp");
    private static final Path results = destination.resolve("results");

    @BeforeAll
    static void setUp() throws IOException {
        clearFolder(testData);
    }

    @Test
    void packOfFolder() throws IOException {
        PackerNative packer = new PackerNative(new ZipCmd());
        assertThrowsExactly(ArchiveException.class, () -> packer.packOfFolder(destination.resolve("packer-test.zip"),
                destination.resolve("fake-folder")));
        assertThrowsExactly(IllegalArgumentException.class, ()-> packer.packOfFolder(destination.resolve("packer-test.zip"),
                resources.resolve("fake-folder.fld")));
        //folders overlap
        assertThrowsExactly(IllegalArgumentException.class, ()-> packer.packOfFolder(destination.resolve("test-folder")
                .resolve("packer-test.zip"), testData));
//        packer.setMessageListener(System.out::println);
        packer.packOfFolder(results.resolve("ZIP archive.zip"), resources.resolve("files-folder"));
        Extractor extractor = new ExtractorNative(new ZipCmd());
        assertEquals(5, extractor.getFileList(results.resolve("ZIP archive.zip")).size());
    }
}