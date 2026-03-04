package org.entur.ror.idefix.file;

import org.entur.ror.idefix.config.Config;
import org.entur.ror.idefix.gcs.GcsClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class GcsFileServiceTest {

    private static final Config CONFIG = new Config(
            "tt-bucket", "tt-path",
            "reg-bucket", "reg-path",
            "out-bucket", "out-path"
    );

    private final GcsClient gcs = mock(GcsClient.class);
    private final GcsFileService service = new GcsFileService(gcs, CONFIG);

    @TempDir
    Path tempDir;

    @Test
    void shouldDownloadTimetableZip() {
        Path result = service.getTimetableZip(tempDir);

        assertThat(result).isEqualTo(tempDir.resolve("timetable.zip"));
        verify(gcs).downloadFromGcs(eq("tt-bucket"), eq("tt-path"), eq(result));
    }

    @Test
    void shouldDownloadRegistryZip() {
        Path result = service.getRegistryZip(tempDir);

        assertThat(result).isEqualTo(tempDir.resolve("registry.zip"));
        verify(gcs).downloadFromGcs(eq("reg-bucket"), eq("reg-path"), eq(result));
    }

    @Test
    void shouldUploadOutput() throws IOException {
        Path outputZip = tempDir.resolve("output.zip");

        service.publishOutput(outputZip);

        verify(gcs).uploadToGcs(eq("out-bucket"), eq("out-path"), eq(outputZip));
    }
}
