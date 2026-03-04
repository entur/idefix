package org.entur.ror.idefix.gcs;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

public class GcsClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(GcsClient.class);
    private final Storage storage;

    public GcsClient(Storage storage) {
        this.storage = storage;
    }

    public void downloadFromGcs(String bucket, String path, Path destination) {
        LOGGER.info("Downloading gs://{}/{} to {}", bucket, path, destination);
        storage.downloadTo(BlobId.of(bucket, path), destination);
    }

    public void uploadToGcs(String bucket, String path, Path source) throws IOException {
        LOGGER.info("Uploading {} to gs://{}/{}", source, bucket, path);
        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucket, path)).build();
        storage.createFrom(blobInfo, source);
    }
}
