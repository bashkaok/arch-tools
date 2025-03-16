package com.jisj.archtools.conv;

import com.jisj.archtools.Extractor;
import com.jisj.archtools.Packer;
import com.jisj.archtools.Type;

import java.util.Optional;

public interface UtilProvider {
    Optional<Packer> getPacker(Type archiveType);
    Optional<Extractor> getExtractor(Type archiveType);
}
