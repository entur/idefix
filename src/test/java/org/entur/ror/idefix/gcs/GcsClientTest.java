package org.entur.ror.idefix.gcs;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class GcsClientTest {

    private static final String BUCKET_NAME = "test-bucket";
    private final Storage storage = mock(Storage.class);
    private final GcsClient gcs = new GcsClient(storage);

    @Test
    void shouldDownloadFromGcs() {
        String path = "some/path.zip";
        Path destination = Path.of("/tmp/destination.zip");

        gcs.downloadFromGcs(BUCKET_NAME, path, destination);

        verify(storage).downloadTo(BlobId.of(BUCKET_NAME, path), destination);
    }

    @Test
    void shouldUploadToGcs() throws IOException {
        String path = "some/output.zip";
        Path source = Path.of("/tmp/source.zip");

        gcs.uploadToGcs(BUCKET_NAME, path, source);

        BlobInfo expectedBlobInfo = BlobInfo.newBuilder(BlobId.of(BUCKET_NAME, path)).build();
        verify(storage).createFrom(eq(expectedBlobInfo), eq(source));
    }
}
