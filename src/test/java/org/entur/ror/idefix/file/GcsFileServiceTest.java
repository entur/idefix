package org.entur.ror.idefix.file;

import org.entur.ror.idefix.config.Config;
import org.entur.ror.idefix.gcs.GcsClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class GcsFileServiceTest {

    private static final Config CONFIG = new Config(
            "timetable-bucket", List.of("provider1", "provider2"),
            "registry-bucket", "reg-path",
            "out-bucket"
    );

    private final GcsClient gcs = mock(GcsClient.class);
    private final GcsFileService service = new GcsFileService(gcs, CONFIG);

    @TempDir
    Path tempDir;

    @Test
    void shouldReturnConfiguredProviders() {
        assertThat(service.getProviders()).containsExactly("provider1", "provider2");
    }

    @Test
    void shouldDownloadTimetableZipForProvider() {
        Path result = service.getTimetableZip(tempDir, "provider1");

        assertThat(result).isEqualTo(tempDir.resolve("provider1.zip"));
        verify(gcs).downloadFromGcs(
                eq("timetable-bucket"),
                eq(CONFIG.timetablePrefix() + "provider1.zip"),
                eq(result));
    }

    @Test
    void shouldDownloadRegistryZip() {
        Path result = service.getRegistryZip(tempDir);

        assertThat(result).isEqualTo(tempDir.resolve("registry.zip"));
        verify(gcs).downloadFromGcs(eq("registry-bucket"), eq("reg-path"), eq(result));
    }

    @Test
    void shouldUploadOutputForProvider() throws IOException {
        Path outputZip = tempDir.resolve("output.zip");

        service.publishOutput(outputZip, "provider1");

        verify(gcs).uploadToGcs(
                eq("out-bucket"),
                eq(CONFIG.outputPrefix() + "provider1.zip"),
                eq(outputZip));
    }
}
