package com.jisj.archtools;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static java.lang.Integer.MAX_VALUE;
import static org.junit.jupiter.api.Assertions.*;

class ExtractorTest {
    private static final Path archiveRAR = Path.of("src/test/resources/You & Me Baby Poses for G9.rar");
    private static final Path destination = Path.of("target/test-data/tmp");

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @BeforeAll
    static void setUp() throws IOException {
        //clear target/test-data/tmp
        try (var files = Files.walk(destination, MAX_VALUE)) {
            files.filter(p -> !p.equals(destination))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @Test
    void extractToCmd_RAR() {
        ExtractUtil unRarUtil = new RarExtractUtil();
        assertEquals("\"C:\\Program Files\\WinRAR\\unrar.exe\" x -y \"D:\\Tools\\Java\\arch-tools\\src\\test\\resources\\You & Me Baby Poses for G9.rar\" \"D:\\Tools\\Java\\arch-tools\\target\\test-data\\tmp\"",
                unRarUtil.extractToDestinationCmd(archiveRAR, destination));
    }

    @Test
    void extractTo_RAR() throws IOException {
        Extractor unPacker = new Extractor(new RarExtractUtil());
        unPacker.extractTo(archiveRAR, destination);
        assertFalse(Files.exists(destination.resolve(archiveRAR.getFileName().toString() + ".log")));
        unPacker.setBreakTimeOutSec(0);
        assertThrowsExactly(TimeOutException.class, ()-> unPacker.extractTo(archiveRAR, destination));
        unPacker.setBreakTimeOutSec(10);

        unPacker.setLogFile(destination.resolve("test.log"));
        unPacker.setAppendLog(true);
        assertThrowsExactly(ArchiveException.class, () -> unPacker.extractTo(Path.of("src/test/resources/fake.rar"), destination));
        assertThrowsExactly(ArchiveException.class, () -> unPacker.extractTo(Path.of("src/test/resources/fake.rar"), destination));
        String str = Files.readString(destination.resolve("test.log"));
        int count = 0;
        int index = -1;
        String find = "is not RAR archive";
        while (true) {
            index = str.indexOf(find);
            if (index == -1) break;
            str = str.substring(index+1);
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    void getFileList_RAR() throws FileNotFoundException, ArchiveException {
        Extractor unPacker = new Extractor(new RarExtractUtil());
        assertEquals(264, unPacker.getFileList(archiveRAR).size());
    }

    @Test
    void process() {
        try {
            final ProcessBuilder p = new ProcessBuilder(new RarExtractUtil().getFileListCmd(archiveRAR));
            final Process proc = p.start();

            BufferedReader input =
                    new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader error =
                    new BufferedReader(new InputStreamReader(proc.getErrorStream()));


            String line = null;
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
            line = null;
            while ((line = error.readLine()) != null) {
                System.out.println(line);
            }
        }
        catch(Exception e) {e.printStackTrace();}
    }
}