package com.jisj.archtools;

import com.jisj.archtools.cmd.CmdExtractUtil;
import com.jisj.archtools.cmd.RarExtractCmd;
import com.jisj.archtools.cmd.ZipCmd;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static java.lang.Integer.MAX_VALUE;
import static org.junit.jupiter.api.Assertions.*;

class ExtractorImplTest {
    private static final Path archiveRAR = Path.of("src/test/resources/RAR archive.rar"); //264 files
    private static final Path archiveZIP = Path.of("src/test/resources/ZIP archive.zip"); //7 files
    private static final Path archive7z = Path.of("src/test/resources/SEVEN archive.7z"); //2 files
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
        CmdExtractUtil unRarUtil = new RarExtractCmd();
        assertEquals("\"C:\\Program Files\\WinRAR\\unrar.exe\" x -y \"D:\\Tools\\Java\\arch-tools\\src\\test\\resources\\RAR archive.rar\" \"D:\\Tools\\Java\\arch-tools\\target\\test-data\\tmp\"",
                unRarUtil.extractToDestinationCmd(archiveRAR, destination));
    }

    @Test
    void commands_ZIP_7Z() {
        String util = "C:/Program Files/7-Zip/7z.exe";
        CmdExtractUtil zipUtil = new ZipCmd();
        assertEquals(Path.of(util), zipUtil.getUtilPath());
        assertEquals("\"C:\\Program Files\\7-Zip\\7z.exe\" l -ba \"d:\\test.zip\"", zipUtil.getFileListCmd(Path.of("d:/test.zip")));

    }

    @Test
    void extractTo_RAR() throws IOException {
        ExtractorImpl unPacker = new ExtractorImpl(new RarExtractCmd());
        unPacker.extractTo(archiveRAR, destination);
        assertFalse(Files.exists(destination.resolve(archiveRAR.getFileName().toString() + ".log")));
        unPacker.setBreakTimeOutSec(0);

//        unPacker.setProgressListener(System.out::println);
        unPacker.extractTo(archiveRAR, destination);
        unPacker.setBreakTimeOutSec(10);

        unPacker.setLogFile(destination.resolve("test.log"));
        unPacker.setAppendLog(true);
        assertThrowsExactly(ArchiveException.class, () -> unPacker.extractTo(Path.of("src/test/resources/fake.rar"), destination));
        assertThrowsExactly(ArchiveException.class, () -> unPacker.extractTo(Path.of("src/test/resources/fake.rar"), destination));
        String str = Files.readString(destination.resolve("test.log"));
        int count = 0;
        int index;
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
        ExtractorImpl unPacker = new ExtractorImpl(new RarExtractCmd());
//        unPacker.setProgressListener(System.out::println);
        assertEquals(264, unPacker.getFileList(archiveRAR).size());
        assertEquals(0, unPacker.getFileList(archiveZIP).size());
        assertEquals(0, unPacker.getFileList(archive7z).size());
    }

    @Test
    void getFileList_ZIP_7Z() throws FileNotFoundException, ArchiveException {
        ExtractorImpl unPacker = new ExtractorImpl(new ZipCmd());
//        unPacker.setProgressListener(System.out::println);
        assertEquals(264, unPacker.getFileList(archiveRAR).size());
        assertEquals(7, unPacker.getFileList(archiveZIP).size());
        assertEquals(2, unPacker.getFileList(archive7z).size());
    }

}