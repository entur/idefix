package org.entur.ror.idefix.file;

import org.entur.ror.idefix.config.Config;
import org.entur.ror.idefix.gcs.GcsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public record GcsFileService(GcsClient gcsClient, Config config) implements FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GcsFileService.class);

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
    public void publishAggregatedOutput(Path aggregatedZip) throws IOException {
        String datedPath = config.aggregatedDatedPath();
        gcsClient.uploadToGcs(config.outputBucket(), datedPath, aggregatedZip);
        LOGGER.info("Aggregated output uploaded to gs://{}/{}", config.outputBucket(), datedPath);

        String latestPath = config.latestAggregatedPath();
        gcsClient.copyInGcs(config.outputBucket(), datedPath, latestPath);
        LOGGER.info("Aggregated output copied to gs://{}/{}", config.outputBucket(), latestPath);
    }
}
