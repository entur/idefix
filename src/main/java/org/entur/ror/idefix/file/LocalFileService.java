package org.entur.ror.idefix.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class LocalFileService implements FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalFileService.class);

    private final Path timetableZip;
    private final Path registryZip;
    private final Path outputPath;

    public LocalFileService(Path timetableZip, Path registryZip, Path outputPath) {
        this.timetableZip = timetableZip;
        this.registryZip = registryZip;
        this.outputPath = outputPath;
    }

    @Override
    public Path getTimetableZip(Path tempDir) {
        LOGGER.info("LOCAL mode: using timetable ZIP {}", timetableZip);
        return timetableZip;
    }

    @Override
    public Path getRegistryZip(Path tempDir) {
        LOGGER.info("LOCAL mode: using registry ZIP {}", registryZip);
        return registryZip;
    }

    @Override
    public void publishOutput(Path outputZip) throws IOException {
        Files.copy(outputZip, outputPath, StandardCopyOption.REPLACE_EXISTING);
        LOGGER.info("Output written to {}", outputPath);
    }
}
