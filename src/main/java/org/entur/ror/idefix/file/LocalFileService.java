package org.entur.ror.idefix.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public record LocalFileService(Path timetableZip, Path registryZip, Path outputPath) implements FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalFileService.class);

    @Override
    public List<String> getProviders() {
        String filename = timetableZip.getFileName().toString();
        String provider = filename.endsWith(".zip")
                ? filename.substring(0, filename.length() - 4)
                : filename;
        return List.of(provider);
    }

    @Override
    public Path getTimetableZip(Path tempDir, String provider) {
        LOGGER.info("LOCAL mode: using timetable ZIP {}", timetableZip);
        return timetableZip;
    }

    @Override
    public Path getRegistryZip(Path tempDir) {
        LOGGER.info("LOCAL mode: using registry ZIP {}", registryZip);
        return registryZip;
    }

    @Override
    public void publishOutput(Path outputZip, String provider) throws IOException {
        Files.copy(outputZip, outputPath, StandardCopyOption.REPLACE_EXISTING);
        LOGGER.info("Output written to {}", outputPath);
    }
}
