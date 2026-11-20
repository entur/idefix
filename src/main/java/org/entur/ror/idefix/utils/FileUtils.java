package org.entur.ror.idefix.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class FileUtils {

    private FileUtils() {
    }

    public static List<Path> listXmlFiles(Path path) throws IOException {
        if (Files.isRegularFile(path)) {
            return List.of(path);
        }
        try (Stream<Path> files = Files.walk(path)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".xml"))
                    .sorted()
                    .toList();
        }
    }

    public static List<Path> listSubdirectories(Path dir) throws IOException {
        try (Stream<Path> entries = Files.list(dir)) {
            return entries.filter(Files::isDirectory).sorted().toList();
        }
    }
}
