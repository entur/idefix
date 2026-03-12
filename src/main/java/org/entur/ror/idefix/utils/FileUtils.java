package org.entur.ror.idefix.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class FileUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    private FileUtils() {
    }

    public static Path findSharedDataXml(Path dir) throws IOException {
        return Files.walk(dir)
                .filter(p -> p.getFileName().toString().endsWith("_shared_data.xml"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No _shared_data.xml found in timetable"));
    }

    public static void cleanUp(Path dir) {
        try {
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            LOGGER.warn("Failed to delete temp file: {}", p, e);
                        }
                    });
        } catch (IOException e) {
            LOGGER.warn("Failed to clean up temp directory: {}", dir, e);
        }
    }
}
