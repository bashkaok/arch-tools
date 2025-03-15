package com.jisj.archtools;

import com.jisj.archtools.cmd.CmdExtractUtil;
import com.jisj.archtools.cmd.RarExtractCmd;
import com.jisj.archtools.cmd.ZipCmd;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.jisj.archtools.Uils.clearFolder;
import static org.junit.jupiter.api.Assertions.*;

class ExtractorNativeTest {
    private static final Path archiveRAR = Path.of("src/test/resources/RAR archive.rar"); //5 files
    private static final Path archiveZIP = Path.of("src/test/resources/ZIP archive.zip"); //4 files
    private static final Path archive7z = Path.of("src/test/resources/SEVEN archive.7z"); //3 files
    private static final Path destination = Path.of("target/test-data/tmp");

    @BeforeAll
    static void setUp() throws IOException {
        clearFolder(destination);
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
        ExtractorNative unPacker = new ExtractorNative(new RarExtractCmd());
        unPacker.extractTo(archiveRAR, destination);
        assertFalse(Files.exists(destination.resolve(archiveRAR.getFileName().toString() + ".log")));
        unPacker.setBreakTimeOutSec(0);

//        unPacker.setProgressListener(System.out::println);
//        unPacker.setMessageListener(System.out::println);
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
            str = str.substring(index + 1);
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    void getFileList_RAR() throws FileNotFoundException, ArchiveException {
        ExtractorNative unPacker = new ExtractorNative(new RarExtractCmd());
//        unPacker.setProgressListener(System.out::println);
        assertEquals(5, unPacker.getFileList(archiveRAR).size());
        assertEquals(0, unPacker.getFileList(archiveZIP).size());
        assertEquals(0, unPacker.getFileList(archive7z).size());
    }

    @Test
    void getFileList_ZIP_7Z() throws FileNotFoundException, ArchiveException {
        ExtractorNative unPacker = new ExtractorNative(new ZipCmd());
//        unPacker.setProgressListener(System.out::println);
//        unPacker.setMessageListener(System.out::println);
        assertEquals(5, unPacker.getFileList(archiveRAR).size());
        assertEquals(4, unPacker.getFileList(archiveZIP).size());
        assertEquals(3, unPacker.getFileList(archive7z).size());
    }

}