package com.jisj.archtools;

import java.nio.file.Path;

/**
 * Immutable record with nested archive path
 * @param rootPath {@code Path} with archive in OS file system
 * @param paths string paths to nest archive in archive
 * @see NestedZipPath
 */
public record NestedPath(Path rootPath, String...paths) {
}
