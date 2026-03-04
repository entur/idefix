package org.entur.ror.idefix.file;

import org.entur.ror.idefix.config.Config;
import org.entur.ror.idefix.gcs.GcsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

public class GcsFileService implements FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GcsFileService.class);

    private final GcsClient gcsClient;
    private final Config config;

    public GcsFileService(GcsClient gcsClient, Config config) {
        this.gcsClient = gcsClient;
        this.config = config;
    }

    @Override
    public Path getTimetableZip(Path tempDir) {
        Path destination = tempDir.resolve("timetable.zip");
        gcsClient.downloadFromGcs(config.timetableBucket(), config.timetablePath(), destination);
        return destination;
    }

    @Override
    public Path getRegistryZip(Path tempDir) {
        Path destination = tempDir.resolve("registry.zip");
        gcsClient.downloadFromGcs(config.registryBucket(), config.registryPath(), destination);
        return destination;
    }

    @Override
    public void publishOutput(Path outputZip) throws IOException {
        gcsClient.uploadToGcs(config.outputBucket(), config.outputPath(), outputZip);
        LOGGER.info("Output uploaded to gs://{}/{}", config.outputBucket(), config.outputPath());
    }
}
