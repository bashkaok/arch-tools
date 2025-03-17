package com.jisj.archtools.conv;

import com.jisj.archtools.*;
import com.jisj.archtools.cmd.RarExtractCmd;
import com.jisj.archtools.cmd.ZipCmd;
import com.jisj.archtools.impl.ExtractorNative;
import com.jisj.archtools.impl.PackerNative;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

/**
 * Class implements interface {@link UtilProvider}
 * <p>Default property file "native-provider.properties"
 * <pre>{@code
 * RAR_EXTRACTOR=C\:\\Program Files\\WinRAR\\unrar.exe
 * RAR_PACKER=
 * ZIP_EXTRACTOR=C\:\\Program Files\\7-Zip\\7z.exe
 * ZIP_PACKER=C\:\\Program Files\\7-Zip\\7z.exe}
 * </pre>
 * @implNote For .7z and .zip files use the same extractor/packer.<br>
 * The .rar packer is not implemented
 */
public class NativeProvider implements UtilProvider {
    private final Properties props = new Properties();

    public NativeProvider() {
        loadProperties();
    }

    private void loadProperties() {
        try (var is = ConverterBuilder.class.getClassLoader()
                .getResourceAsStream("native-provider.properties")) {
            if (is == null)
                throw new IllegalStateException("Properties not found in classpath: <native-provider.properties>");
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<Path> findPath(String key) {
        String path = props.getProperty(key);
        if (path == null || path.isEmpty()) return Optional.empty();
        return Optional.of(Path.of(path));
    }

    private Optional<Path> findPath(String... keys) {
        for (var key : keys) {
            String path = props.getProperty(key);
            if (path != null && !path.isEmpty()) return Optional.of(Path.of(path));
        }
        return Optional.empty();
    }


    @Override
    public Optional<Packer> getPacker(Type archiveType) {
        return switch (archiveType) {
            case RAR -> Optional.empty(); //RAR packer should be here
            case ZIP, S7Z -> findPath("ZIP_PACKER", "S7Z_PACKER")
                    .map(path -> new PackerNative(new ZipCmd(path)));
            case UNKNOWN -> Optional.empty();
        };

    }

    @Override
    public Optional<Extractor> getExtractor(Type archiveType) {
        return switch (archiveType) {
            case RAR -> findPath("RAR_EXTRACTOR")
                    .map(path -> new ExtractorNative(new RarExtractCmd(path)));
            case ZIP, S7Z -> findPath("ZIP_EXTRACTOR", "S7Z_EXTRACTOR")
                    .map(path -> new ExtractorNative(new ZipCmd(path)));
            case UNKNOWN -> Optional.empty();
        };
    }
}
