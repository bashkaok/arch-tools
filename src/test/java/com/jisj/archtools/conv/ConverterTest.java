package com.jisj.archtools.conv;

import com.jisj.archtools.Type;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.jisj.archtools.Uils.clearFolder;
import static com.jisj.archtools.Utils.removeNotEmptyFolder;
import static java.lang.Integer.MAX_VALUE;
import static org.junit.jupiter.api.Assertions.*;

class ConverterTest {
    static Path resources = Path.of("src/test/resources");
    static Path sourceZip = Path.of("src/test/resources/ZIP archive.zip");
    static Path sourceRar = Path.of("src/test/resources/RAR archive.rar");
    static Path destinationFolder = Path.of("target/test-data");
    static Path temporaryFolder = Path.of("target/test-data/tmp");
    static UtilProvider provider = new NativeProvider();


    @BeforeAll
    static void setUp() throws IOException {
        clearFolder(temporaryFolder);
        if (Files.exists(destinationFolder.resolve("conv-test")))
            removeNotEmptyFolder(destinationFolder.resolve("conv-test"));
    }

    @AfterAll
    static void close() {
    }

    private static long getCountFiles(Path folder) throws IOException {
        try (var files = Files.walk(folder, MAX_VALUE)) {
            return files.count() - 1;
        }
    }

    @Test
    @Order(1)
    void test() {
        Converter converter = Converter.builder(new NativeProvider())
                .sourceArchive(sourceRar)
                .destinationFolder(destinationFolder)
                .targetFormat(Type.ZIP)
                .temporaryFolder(temporaryFolder)
                .options(Converter.Options.TEST_BEFORE)
                .build();
//        converter.setMessageListener(System.out::println);
//        assertTrue(converter.testSourceArchive());


    }


    @Test
    @Order(2)
    void unPack() throws IOException {
        Converter converter = Converter.builder(new NativeProvider())
                .sourceArchive(sourceRar)
                .destinationFolder(destinationFolder)
                .targetFormat(Type.ZIP)
                .temporaryFolder(temporaryFolder)
                .build();
//        converter.setMessageListener(System.out::println);
        assertTrue(converter.unPack());
        assertTrue(Files.exists(converter.getTemporaryArchiveFolder()));
        assertEquals(converter.getExtractor().getFileList(converter.getSourceArchive()).size(),
                getCountFiles(converter.getTemporaryArchiveFolder()));
    }

    @Test
    @Order(3)
    void pack() throws IOException {
        Converter converter = Converter.builder()
                .utilProvider(new NativeProvider())
                .sourceArchive(sourceRar)
                .destinationFolder(destinationFolder)
                .targetFormat(Type.ZIP)
                .temporaryFolder(temporaryFolder)
                .build();
//        converter.setProgressListener(System.out::println);
        assertTrue(converter.pack());
        assertEquals(converter.getExtractor().getFileList(converter.getSourceArchive()).size(),
                provider.getExtractor(Type.ZIP).orElseThrow().getFileList(converter.getDestinationArchive()).size());

    }

    @Test
    void convert() throws IOException {
        Converter converter = Converter.builder(new NativeProvider())
                .sourceArchive(sourceRar)
                .destinationFolder(destinationFolder.resolve("conv-test"))
                .targetFormat(Type.ZIP)
                .temporaryFolder(temporaryFolder)
//                .options(Converter.Options.TEST_BEFORE, Converter.Options.TEST_AFTER, Converter.Options.COMPARE)
                .build();
        converter.setStepMessageListener(step -> System.out.println("Step: " + step));
        converter.setProgressListener(count -> System.out.println("Progress=" + count + " of " + converter.getMaxProgressCount()));
//        converter.setMessageListener(System.out::println);
        converter.setStepMessageListener(System.out::println);
        assertTrue(converter.convert());
        System.out.println(converter.getState());
        converter.setMessageListener(null);
        converter.setProgressListener(null);
        //count the files in source and destination
        assertEquals(converter.getExtractor().getFileList(converter.getSourceArchive()).size(),
                provider.getExtractor(Type.ZIP).orElseThrow().getFileList(converter.getDestinationArchive()).size());


    }

}