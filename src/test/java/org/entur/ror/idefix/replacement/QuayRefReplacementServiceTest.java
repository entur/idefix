package org.entur.ror.idefix.replacement;

import org.entur.ror.idefix.file.FileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuayRefReplacementServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldRunReplacementForSingleProvider() throws Exception {
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
            public void publishAggregatedOutput(Path aggregatedZip) throws IOException {
                Path dest = tempDir.resolve("published-aggregated.zip");
                Files.copy(aggregatedZip, dest);
                publishedOutputs.add(dest);
            }
        };

        Map<String, QuayRefReplacementResult> results = new QuayRefReplacementService().run(fileService);

        assertThat(results).hasSize(1);
        assertThat(results).containsKey("testprovider");
        QuayRefReplacementResult result = results.get("testprovider");
        assertThat(result.matches()).isGreaterThanOrEqualTo(0);
        assertThat(result.misses()).isGreaterThanOrEqualTo(0);
        assertThat(publishedOutputs).hasSize(1);
        assertThat(publishedOutputs.get(0)).exists();
    }

    @Test
    void shouldPublishAggregatedZipWithProviderPrefixedFiles() throws Exception {
        Path timetableZip = tempDir.resolve("timetable-test.zip");
        Path registryZip = tempDir.resolve("registry-test.zip");

        createTimetableTestZip(timetableZip);
        createRegistryTestZip(registryZip);

        Path publishedAggregated = tempDir.resolve("published-aggregated.zip");

        FileService fileService = new FileService() {
            @Override
            public List<String> getProviders() {
                return List.of("skane");
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
            public void publishAggregatedOutput(Path aggregatedZip) throws IOException {
                Files.copy(aggregatedZip, publishedAggregated);
            }
        };

        new QuayRefReplacementService().run(fileService);

        assertThat(publishedAggregated).exists();
        List<String> entryNames = readZipEntryNames(publishedAggregated);
        assertThat(entryNames).contains("_stops.xml");
        assertThat(entryNames).anyMatch(name -> name.startsWith("skane_"));
        assertThat(entryNames).noneMatch(name -> name.endsWith("_stops.xml") && !name.equals("_stops.xml"));
    }

    @Test
    void shouldRunReplacementForMultipleProviders() throws Exception {
        Path timetableZip = tempDir.resolve("timetable-test.zip");
        Path registryZip = tempDir.resolve("registry-test.zip");

        createTimetableTestZip(timetableZip);
        createRegistryTestZip(registryZip);

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
            public void publishAggregatedOutput(Path aggregatedZip) {
            }
        };

        Map<String, QuayRefReplacementResult> results = new QuayRefReplacementService().run(fileService);

        assertThat(results).hasSize(2);
        assertThat(results).containsKeys("providerA", "providerB");
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
            public void publishAggregatedOutput(Path aggregatedZip) {
            }
        };

        assertThatThrownBy(() -> new QuayRefReplacementService().run(failingService))
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
            zos.putNextEntry(new ZipEntry("registry_stops.xml"));
            zos.write("<stops/>".getBytes());
            zos.closeEntry();
        }
    }

    private List<String> readZipEntryNames(Path zipFile) throws IOException {
        List<String> names = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                names.add(entry.getName());
                zis.closeEntry();
            }
        }
        return names;
    }
}
