package org.entur.ror.idefix.transform;

import org.entur.ror.idefix.file.FileService;
import org.entur.ror.idefix.xml.QuayRefTransformer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransformServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldRunTransformWithMockFileService() throws Exception {
        Path timetableZip = tempDir.resolve("timetable-test.zip");
        Path registryZip = tempDir.resolve("registry-test.zip");

        createTimetableTestZip(timetableZip);
        createRegistryTestZip(registryZip);

        Path publishedOutput = tempDir.resolve("published-output.zip");

        FileService fileService = new FileService() {
            @Override
            public Path getTimetableZip(Path dir) {
                return timetableZip;
            }

            @Override
            public Path getRegistryZip(Path dir) {
                return registryZip;
            }

            @Override
            public void publishOutput(Path outputZip) throws IOException {
                Files.copy(outputZip, publishedOutput);
            }
        };

        QuayRefTransformer.TransformResult result = new TransformService().run(fileService);

        assertThat(result).isNotNull();
        assertThat(result.matches()).isGreaterThanOrEqualTo(0);
        assertThat(result.misses()).isGreaterThanOrEqualTo(0);
        assertThat(publishedOutput).exists();
    }

    @Test
    void shouldThrowWhenFileServiceFails() {
        FileService failingService = new FileService() {
            @Override
            public Path getTimetableZip(Path dir) throws IOException {
                throw new IOException("download failed");
            }

            @Override
            public Path getRegistryZip(Path dir) {
                return Path.of("nonexistent.zip");
            }

            @Override
            public void publishOutput(Path outputZip) {
            }
        };

        assertThatThrownBy(() -> new TransformService().run(failingService))
                .isInstanceOf(RuntimeException.class);
    }

    private void createTimetableTestZip(Path zipPath) throws Exception {
        Path sharedDataXml = Path.of("src/test/resources/timetable/test_shared_data.xml");
        try (var zos = new java.util.zip.ZipOutputStream(Files.newOutputStream(zipPath))) {
            zos.putNextEntry(new ZipEntry("test_shared_data.xml"));
            Files.copy(sharedDataXml, zos);
            zos.closeEntry();
        }
    }

    private void createRegistryTestZip(Path zipPath) throws Exception {
        Path registryXml = Path.of("src/test/resources/registry/test_stop_places.xml");
        try (var zos = new java.util.zip.ZipOutputStream(Files.newOutputStream(zipPath))) {
            zos.putNextEntry(new ZipEntry("test_stop_places.xml"));
            Files.copy(registryXml, zos);
            zos.closeEntry();
        }
    }

}
