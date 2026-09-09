package org.entur.ror.idefix.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class LocalFileServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldDeriveProviderFromFilename() throws IOException {
        createZipWithEntry(tempDir.resolve("blekinge.zip"));
        LocalFileService service = new LocalFileService(
                tempDir, Path.of("/reg.zip"), Path.of("/out.zip"));

        assertThat(service.getProviders()).containsExactly("blekinge");
    }

    @Test
    void shouldFilterOutProvidersWithEmptyZip() throws IOException {
        createEmptyZip(tempDir.resolve("empty.zip"));
        createZipWithEntry(tempDir.resolve("blekinge.zip"));
        LocalFileService service = new LocalFileService(
                tempDir, Path.of("/reg.zip"), Path.of("/out.zip"));

        assertThat(service.getProviders()).containsExactly("blekinge");
    }

    private void createZipWithEntry(Path zipPath) throws IOException {
        try (OutputStream fos = Files.newOutputStream(zipPath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            zos.putNextEntry(new ZipEntry("some-file.txt"));
            zos.write("content".getBytes());
            zos.closeEntry();
        }
    }

    private void createEmptyZip(Path zipPath) throws IOException {
        try (OutputStream fos = Files.newOutputStream(zipPath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            // no entries added
        }
    }

    @Test
    void shouldReturnTimetableZipDirectly() {
        Path timetablePath = Path.of("/some");
        LocalFileService service = new LocalFileService(timetablePath, Path.of("/reg.zip"), Path.of("/out.zip"));

        Path result = service.getTimetableZip(tempDir, "anyprovider");

        assertThat(result).isEqualTo(Path.of("/some/anyprovider.zip"));
    }

    @Test
    void shouldReturnRegistryZipDirectly() {
        Path registryZip = Path.of("/some/registry.zip");
        LocalFileService service = new LocalFileService(Path.of("/tt.zip"), registryZip, Path.of("/out.zip"));

        Path result = service.getRegistryZip(tempDir);

        assertThat(result).isEqualTo(registryZip);
    }

    @Test
    void shouldCopyAggregatedOutputToDestination() throws IOException {
        Path outputPath = tempDir.resolve("final-output.zip");
        LocalFileService service = new LocalFileService(Path.of("/tt.zip"), Path.of("/reg.zip"), outputPath);

        Path sourceZip = tempDir.resolve("source.zip");
        Files.writeString(sourceZip, "fake zip content");

        service.publishAggregatedOutput(sourceZip);

        assertThat(outputPath).exists();
        assertThat(Files.readString(outputPath)).isEqualTo("fake zip content");
    }
}
