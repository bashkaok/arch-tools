package com.jisj.archtools.impl;

import com.jisj.archtools.ArchiveException;
import com.jisj.archtools.Controller;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

import static com.jisj.archtools.Uils.clearFolder;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ZipFileSystemControllerTest {
    static Path testFolder = Path.of("target/test-data/zip-fs-test");
    static Path filesFolder = Path.of("src/test/resources/files-folder");

    @BeforeAll
    static void setUp() throws IOException {
        Files.createDirectories(testFolder);
        clearFolder(testFolder);
    }

    @Test
    void create() throws ArchiveException {
        ZipFileSystemController zfc = new ZipFileSystemController();
        Path newZip = testFolder.resolve("new-zip.zip");
        zfc.create(newZip);
        assertTrue(Files.exists(testFolder.resolve("new-zip.zip")));
    }

    @SuppressWarnings("resource")
    @Test
    void removeFiles() throws IOException {
        ZipFileSystemController zfc = new ZipFileSystemController();
        Path targetArchive = testFolder.resolve("for-removeFiles-test.zip");
        zfc.create(targetArchive);
        assertTrue(Files.exists(targetArchive));

        List<Path> files = Files.list(filesFolder).toList();
        zfc.addFiles(targetArchive, files,"/", Controller.CopyOptions.REPLACE_EXISTING);
        zfc.addFiles(targetArchive, Files.list(filesFolder.resolve("child")).toList(),
                "/child/", Controller.CopyOptions.REPLACE_EXISTING);

        assertEquals(7, zfc.getFileList(targetArchive).size());
        List<String> result = zfc.removeFiles(targetArchive, List.of("file1.txt", "file2.txt"));
        assertEquals(0, result.size());
        assertEquals(5, zfc.getFileList(targetArchive).size());
        result = zfc.removeFiles(targetArchive, List.of("not-exist.txt"));//FileNotFoundException
        assertEquals(1, result.size());
        result = zfc.removeFiles(targetArchive, List.of("&^%$#@*?")); //FileNotFoundException
        assertEquals(1, result.size());
        result = zfc.removeFiles(targetArchive, List.of("child")); //DirectoryNotEmptyException
        assertEquals(1, result.size());
        result = zfc.removeFiles(targetArchive, List.of("child/child_file.txt"));
        assertEquals(0, result.size());
        result = zfc.removeFiles(targetArchive, List.of("child"));
        assertEquals(0, result.size());
    }

    @SuppressWarnings("resource")
    @Test
    void addFiles() throws IOException {
        ZipFileSystemController zfc = new ZipFileSystemController();
        Path targetArchive = testFolder.resolve("for-addFiles-test.zip");
        zfc.create(targetArchive);
        assertTrue(Files.exists(targetArchive));

        zfc.addFiles(targetArchive, Files.list(filesFolder)
                        .filter(p-> List.of("file3.txt", "file4.txt", "file5.txt").contains(p.getFileName().toString()))
                        .toList(),
                "/", Controller.CopyOptions.REPLACE_EXISTING);

        assertFalse(zfc.getFileList(targetArchive).containsAll(List.of("file1.txt", "file2.txt")));
        assertEquals(3, zfc.getFileList(targetArchive).size());
        List<Path> result = zfc.addFiles(targetArchive, List.of(filesFolder.resolve("file1.txt"), filesFolder.resolve("file2.txt")),
                "/", Controller.CopyOptions.REPLACE_EXISTING);
        assertEquals(0, result.size());
        assertEquals(5, zfc.getFileList(targetArchive).size());
        //replace
        result = zfc.addFiles(targetArchive, List.of(filesFolder.resolve("file1.txt"), filesFolder.resolve("file2.txt")),
                "/", Controller.CopyOptions.REPLACE_EXISTING);
        assertEquals(0, result.size());
        assertEquals(5, zfc.getFileList(targetArchive).size());
        //skip same
        result = zfc.addFiles(targetArchive, List.of(filesFolder.resolve("file1.txt"), filesFolder.resolve("file2.txt")),
                "/", Controller.CopyOptions.OMIT_SAME);
        assertEquals(2, result.size());
        assertEquals(5, zfc.getFileList(targetArchive).size());
        result = zfc.addFiles(targetArchive, List.of(filesFolder.resolve("child/")),
                "/", Controller.CopyOptions.OMIT_SAME);
        assertEquals(0, result.size());
        result = zfc.addFiles(targetArchive, List.of(filesFolder.resolve("file1.txt"), filesFolder.resolve("file2.txt")),
                "/child", Controller.CopyOptions.OMIT_SAME);
        assertEquals(0, result.size());
        result = zfc.addFiles(targetArchive, List.of(filesFolder.resolve("file1.txt"), filesFolder.resolve("file2.txt")),
                "/child2", Controller.CopyOptions.OMIT_SAME);
        assertEquals(2, result.size());

        result = zfc.addFiles(targetArchive, List.of(filesFolder.resolve("/child/")),
                "", Controller.CopyOptions.REPLACE_EXISTING);
        assertEquals(1, result.size());

        assertSame(NoSuchFileException.class, assertThrowsExactly(ArchiveException.class,
                () -> zfc.addFiles(targetArchive.resolve("not-exist.zip"), List.of(filesFolder.resolve("file1.txt"), filesFolder.resolve("file2.txt")),
                "", Controller.CopyOptions.REPLACE_EXISTING)).getCause().getClass());

        assertTrue(assertThrowsExactly(ArchiveException.class, ()->zfc.addFiles(targetArchive, List.of(),
                "", Controller.CopyOptions.REPLACE_EXISTING)).getMessage().contains("Nothing to add"));
    }
}