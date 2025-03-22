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

}