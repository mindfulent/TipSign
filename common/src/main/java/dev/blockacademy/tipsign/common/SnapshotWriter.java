package dev.blockacademy.tipsign.common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Atomic file write: writes to a .tmp file then renames to prevent partial reads.
 */
public final class SnapshotWriter {

    private SnapshotWriter() {}

    /**
     * Atomically writes JSON content to the target file.
     * Creates parent directories if needed.
     */
    public static void writeAtomically(Path targetFile, String jsonContent) throws IOException {
        Files.createDirectories(targetFile.getParent());

        Path tmpFile = targetFile.resolveSibling(targetFile.getFileName() + ".tmp");
        Files.writeString(tmpFile, jsonContent, StandardCharsets.UTF_8);
        Files.move(tmpFile, targetFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }
}
