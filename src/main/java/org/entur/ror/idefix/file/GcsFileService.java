package org.entur.ror.idefix.file;

import org.entur.ror.idefix.config.Config;
import org.entur.ror.idefix.gcs.GcsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class GcsFileService implements FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GcsFileService.class);

    private final GcsClient gcsClient;
    private final Config config;

    public GcsFileService(GcsClient gcsClient, Config config) {
        this.gcsClient = gcsClient;
        this.config = config;
    }

    @Override
    public List<String> getProviders() {
        return config.timetableProviders();
    }

    @Override
    public Path getTimetableZip(Path tempDir, String provider) {
        Path destination = tempDir.resolve(provider + ".zip");
        String objectPath = config.timetablePrefix() + provider + ".zip";
        gcsClient.downloadFromGcs(config.timetableBucket(), objectPath, destination);
        return destination;
    }

    @Override
    public Path getRegistryZip(Path tempDir) {
        Path destination = tempDir.resolve("registry.zip");
        gcsClient.downloadFromGcs(config.registryBucket(), config.registryPath(), destination);
        return destination;
    }

    @Override
    public void publishOutput(Path outputZip, String provider) throws IOException {
        String objectPath = config.outputPrefix() + provider + ".zip";
        gcsClient.uploadToGcs(config.outputBucket(), objectPath, outputZip);
        LOGGER.info("Output uploaded to gs://{}/{}", config.outputBucket(), objectPath);
    }
}
