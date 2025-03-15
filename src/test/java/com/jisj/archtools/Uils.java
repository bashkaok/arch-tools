package com.jisj.archtools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static java.lang.Integer.MAX_VALUE;

public class Uils {
    public static void clearFolder(Path folder) throws IOException {
        //clear target/test-data/tmp
        try (var files = Files.walk(folder, MAX_VALUE)) {
            files.filter(p -> !p.equals(folder))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }

    }
}
