package org.entur.ror.idefix.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipFile;

public record LocalFileService(Path timetablesPath, Path registryZip, Path outputPath) implements FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalFileService.class);

    @Override
    public List<String> getProviders() {
        try (var stream = Files.list(timetablesPath)) {
            return stream
                    .filter(path -> path.toString().endsWith(".zip"))
                    .filter(this::isNotEmptyZipFile)
                    .map(path -> path.getFileName().toString().replaceFirst("\\.zip$", ""))
                    .toList();
        } catch (NotDirectoryException e) {
            throw new RuntimeException("The specified timetables path is not a directory: " + timetablesPath, e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to list timetable ZIP files in " + timetablesPath, e);
        }
    }

    private boolean isNotEmptyZipFile(Path path) {
        try (ZipFile zipFile = new ZipFile(path.toFile())) {
            if (Collections.list(zipFile.entries()).isEmpty()) {
                LOGGER.warn("Skipping provider with empty ZIP file: {}", path);
                return false;
            }
            return true;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read ZIP file " + path, e);
        }
    }

    @Override
    public Path getTimetableZip(Path tempDir, String provider) {
        LOGGER.info("LOCAL mode: using timetable ZIP from path {}", timetablesPath);
        return Path.of(timetablesPath.toString(), provider + ".zip");
    }

    @Override
    public Path getRegistryZip(Path tempDir) {
        LOGGER.info("LOCAL mode: using registry ZIP {}", registryZip);
        return registryZip;
    }

    @Override
    public void publishAggregatedOutput(Path aggregatedZip) throws IOException {
        Files.copy(aggregatedZip, outputPath, StandardCopyOption.REPLACE_EXISTING);
        LOGGER.info("Aggregated output written to {}", outputPath);
    }
}
