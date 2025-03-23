package com.jisj.archtools;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class NestedZipPathTest {
    private static final Path resources = Path.of("src/test/resources");

    @Test
    void getFileSystemPath() {
        try (var ap = NestedZipPath.newPath(resources.resolve("nested-archive.zip"), "file1.zip", "file2.zip", "file3.zip");
             var files = Files.list(ap.getPath())
        ) {
            assertEquals("file3.txt", files.findAny().orElseThrow().getFileName().toString());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void readFile() {
        try (var ap = NestedZipPath.newPath(resources.resolve("nested-archive.zip"), "file1.zip", "file2.zip", "file3.zip")
        ) {
            assertEquals("Text from file3.txt", Files.readString(ap.getPath().resolve("file3.txt")));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void readFile_newPath_with_NestedPath() {
        NestedPath path = new NestedPath(resources.resolve("nested-archive.zip"), "file1.zip", "file2.zip", "file3.zip");
        try (var ap = NestedZipPath.newPath(path)) {
            assertEquals("Text from file3.txt", Files.readString(ap.getPath().resolve("file3.txt")));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void ordinary_path() throws IOException {
        try (var nest = NestedZipPath.newPath(resources);
             var files = Files.list(nest.getPath())) {
            assertTrue(files.findAny().isPresent());
        }
    }

    @Test
    void file_name_in_path() throws IOException {
        try (var nest = NestedZipPath.newPath(resources.resolve("nested-archive.zip"), "file1.zip", "file2.zip", "file2.txt")) {
            assertEquals("Text from file2.txt", Files.readString(nest.getPath()));
        }
        NestedPath np = new NestedPath(resources.resolve("nested-archive.zip"), "file1.zip", "file2.zip", "file2.txt");
        try (var nest = NestedZipPath.newPath(np)) {
            assertEquals("Text from file2.txt", Files.readString(nest.getPath()));
        }
        assertEquals("file1.zip/file2.zip/file2.txt", np.ofNestFiles());

    }

}