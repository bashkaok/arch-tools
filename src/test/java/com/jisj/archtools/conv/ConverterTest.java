package com.jisj.archtools.conv;

import com.jisj.archtools.Type;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static com.jisj.archtools.Uils.clearFolder;
import static com.jisj.archtools.Utils.removeNotEmptyFolder;
import static java.lang.Integer.MAX_VALUE;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConverterTest {
    static Path resources = Path.of("src/test/resources");
    static Path source7Z = Path.of("src/test/resources/SEVEN archive.7z");
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
    void unPack() throws IOException {
        Converter converter = Converter.builder(new NativeProvider())
                .sourceArchive(sourceRar)
                .destinationFolder(destinationFolder)
                .targetFormat(Type.ZIP)
                .temporaryFolder(temporaryFolder)
                .build();
//        converter.setMessageListener(System.out::println);
        Files.deleteIfExists(converter.getDestinationArchive());
        converter.unPack();
        assertTrue(Files.exists(converter.getTemporaryArchiveFolder()));
        assertEquals(converter.getExtractor().getFileList(converter.getSourceArchive()).size(),
                getCountFiles(converter.getTemporaryArchiveFolder()));
    }

    @Test
    @Order(2)
    void pack() throws IOException {
        Converter converter = Converter.builder()
                .utilProvider(new NativeProvider())
                .sourceArchive(sourceRar)
                .destinationFolder(destinationFolder)
                .targetFormat(Type.ZIP)
                .temporaryFolder(temporaryFolder)
                .build();
//        converter.setProgressListener(System.out::println);
        converter.pack();
        System.out.println(converter.getState());
        assertEquals(converter.getExtractor().getFileList(converter.getSourceArchive()).size(),
                provider.getExtractor(Type.ZIP).orElseThrow().getFileList(converter.getDestinationArchive()).size());

    }

    @Test
    @Order(3)
    void convert_RAR2ZIP() throws IOException {
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

    @Test
    @Order(4)
    void convert_7Z2ZIP() throws IOException {
        Converter converter = Converter.builder(new NativeProvider())
                .sourceArchive(source7Z)
                .destinationFolder(destinationFolder.resolve("conv-test"))
                .targetFormat(Type.ZIP)
                .temporaryFolder(temporaryFolder)
//                .options(Converter.Options.TEST_BEFORE, Converter.Options.TEST_AFTER, Converter.Options.COMPARE)
                .options(Converter.Options.COMPARE)
                .build();
//        converter.setStepMessageListener(step -> System.out.println("Step: " + step));
//        converter.setProgressListener(count -> System.out.println("Progress=" + count + " of " + converter.getMaxProgressCount()));
        boolean result = converter.convert();
        assertEquals(new Converter.State(Converter.Options.ALL, null), converter.getState());
        assertTrue(result);

        converter.setMessageListener(null);
        converter.setProgressListener(null);
        //count the files in source and destination
        assertEquals(converter.getExtractor().getFileList(converter.getSourceArchive()).size(),
                provider.getExtractor(Type.ZIP).orElseThrow().getFileList(converter.getDestinationArchive()).size());
    }

    @Test
    @Order(5)
    void convert_START_step() throws IOException {
        //source archive is not exist
        Converter converter = Converter.builder(new NativeProvider())
                .sourceArchive(source7Z.resolve("not-exist.7z"))
                .destinationFolder(destinationFolder.resolve("conv-test"))
                .targetFormat(Type.ZIP)
                .temporaryFolder(temporaryFolder)
//                .options(Converter.Options.TEST_BEFORE, Converter.Options.TEST_AFTER, Converter.Options.COMPARE)
                .build();
        converter.convert();
        assertEquals(Converter.Options.START, converter.getState().step());
        assertTrue(converter.getState().exception().getMessage().startsWith("Source archive file not found"));

        //source archive is empty
        converter = Converter.builder(new NativeProvider())
                .sourceArchive(resources.resolve("Empty.7z"))
                .destinationFolder(destinationFolder.resolve("conv-test"))
                .targetFormat(Type.ZIP)
                .temporaryFolder(temporaryFolder)
//                .options(Converter.Options.TEST_BEFORE, Converter.Options.TEST_AFTER, Converter.Options.COMPARE)
                .build();
        converter.convert();
        assertEquals(Converter.Options.START, converter.getState().step());
        assertTrue(converter.getState().exception().getMessage().startsWith("No files to extract"));

        //target archive already exist
        converter = Converter.builder(new NativeProvider())
                .sourceArchive(source7Z)
                .destinationFolder(destinationFolder)
                .targetFormat(Type.ZIP)
                .temporaryFolder(temporaryFolder)
//                .options(Converter.Options.TEST_BEFORE, Converter.Options.TEST_AFTER, Converter.Options.COMPARE)
                .build();

        Files.copy(resources.resolve("SEVEN archive.zip"), destinationFolder
                .resolve("SEVEN archive.zip"), StandardCopyOption.REPLACE_EXISTING);

        converter.convert();
        assertEquals(Converter.Options.START, converter.getState().step());
        assertTrue(converter.getState().exception().getMessage().startsWith("Target archive file already exists"));
        System.out.println(converter.getOptions());

    }

    @Test
    @Order(6)
    void convert_COMPARE_step() throws IOException {
        Converter converter = Converter.builder(new NativeProvider())
                .sourceArchive(source7Z)
                .destinationFolder(destinationFolder.resolve("conv-test"))
                .targetFormat(Type.ZIP)
                .temporaryFolder(temporaryFolder)
//                .options(Converter.Options.TEST_BEFORE, Converter.Options.TEST_AFTER, Converter.Options.COMPARE)
                .build();
        Files.deleteIfExists(converter.getDestinationArchive());
//        converter.setStepMessageListener(step -> System.out.println("Step: " + step));
        converter.convert();
        assertEquals(new Converter.State(Converter.Options.ALL, null), converter.getState());
        //replace target archive
        Files.copy(resources.resolve("SEVEN archive.zip"), destinationFolder
                .resolve("conv-test")
                .resolve("SEVEN archive.zip"), StandardCopyOption.REPLACE_EXISTING);
        converter.compare();
        assertEquals(Converter.Options.COMPARE, converter.getState().step());
        assertTrue(converter.getState().exception().getMessage().startsWith("Source and target archives files count is differ"));
    }

    @Test
    @Order(7)
    void convert_TEST_step() throws IOException {
        Converter converter = Converter.builder(new NativeProvider())
                .sourceArchive(resources.resolve("RAR broken-archive.rar"))
                .destinationFolder(destinationFolder)
                .targetFormat(Type.ZIP)
                .temporaryFolder(temporaryFolder)
                .options(Converter.Options.TEST_BEFORE, Converter.Options.TEST_AFTER, Converter.Options.COMPARE)
                .build();
        Files.deleteIfExists(converter.getDestinationArchive());
//        converter.setStepMessageListener(step -> System.out.println("Step: " + step));
        converter.testSourceArchive();
        System.out.println(converter.getState());
        assertEquals(Converter.Options.TEST_BEFORE, converter.getState().step());
        assertTrue(converter.getState().exception().getMessage().startsWith("Unsupported command"));
    }
}