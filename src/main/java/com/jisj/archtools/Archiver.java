package com.jisj.archtools;

import java.util.function.Consumer;

/**
 * Common interface for archives utils
 */
public interface Archiver {
    /**
     * Sets progress listener. Progress increases with each new string from output stream of {@code Process}
     * @param progressListener {@code Consumer<Long>}
     */
    void setProgressListener(Consumer<Long> progressListener);

    /**
     * Sets message listener. Listener gets each new string from output stream of {@code Process}
     * @param messageListener {@code Consumer<String>}
     */
    void setMessageListener(Consumer<String> messageListener);
}
