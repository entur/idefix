package org.entur.ror.idefix.transform;

import org.entur.ror.idefix.file.FileService;
import org.entur.ror.idefix.xml.QuayRefTransformer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransformServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldRunTransformForSingleProvider() throws Exception {
        Path timetableZip = tempDir.resolve("timetable-test.zip");
        Path registryZip = tempDir.resolve("registry-test.zip");

        createTimetableTestZip(timetableZip);
        createRegistryTestZip(registryZip);

        List<Path> publishedOutputs = new ArrayList<>();

        FileService fileService = new FileService() {
            @Override
            public List<String> getProviders() {
                return List.of("testprovider");
            }

            @Override
            public Path getTimetableZip(Path dir, String provider) {
                return timetableZip;
            }

            @Override
            public Path getRegistryZip(Path dir) {
                return registryZip;
            }

            @Override
            public void publishOutput(Path outputZip, String provider) throws IOException {
                Path dest = tempDir.resolve("published-" + provider + ".zip");
                Files.copy(outputZip, dest);
                publishedOutputs.add(dest);
            }
        };

        Map<String, QuayRefTransformer.TransformResult> results = new TransformService().run(fileService);

        assertThat(results).hasSize(1);
        assertThat(results).containsKey("testprovider");
        QuayRefTransformer.TransformResult result = results.get("testprovider");
        assertThat(result.matches()).isGreaterThanOrEqualTo(0);
        assertThat(result.misses()).isGreaterThanOrEqualTo(0);
        assertThat(publishedOutputs).hasSize(1);
        assertThat(publishedOutputs.get(0)).exists();
    }

    @Test
    void shouldRunTransformForMultipleProviders() throws Exception {
        Path timetableZip = tempDir.resolve("timetable-test.zip");
        Path registryZip = tempDir.resolve("registry-test.zip");

        createTimetableTestZip(timetableZip);
        createRegistryTestZip(registryZip);

        List<String> publishedProviders = new ArrayList<>();

        FileService fileService = new FileService() {
            @Override
            public List<String> getProviders() {
                return List.of("providerA", "providerB");
            }

            @Override
            public Path getTimetableZip(Path dir, String provider) {
                return timetableZip;
            }

            @Override
            public Path getRegistryZip(Path dir) {
                return registryZip;
            }

            @Override
            public void publishOutput(Path outputZip, String provider) throws IOException {
                publishedProviders.add(provider);
                Path dest = tempDir.resolve("published-" + provider + ".zip");
                Files.copy(outputZip, dest);
            }
        };

        Map<String, QuayRefTransformer.TransformResult> results = new TransformService().run(fileService);

        assertThat(results).hasSize(2);
        assertThat(results).containsKeys("providerA", "providerB");
        assertThat(publishedProviders).containsExactly("providerA", "providerB");
    }

    @Test
    void shouldThrowWhenFileServiceFails() {
        FileService failingService = new FileService() {
            @Override
            public List<String> getProviders() {
                return List.of("failing");
            }

            @Override
            public Path getTimetableZip(Path dir, String provider) throws IOException {
                throw new IOException("download failed");
            }

            @Override
            public Path getRegistryZip(Path dir) {
                return Path.of("nonexistent.zip");
            }

            @Override
            public void publishOutput(Path outputZip, String provider) {
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
