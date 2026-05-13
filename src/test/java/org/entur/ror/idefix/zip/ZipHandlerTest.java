package org.entur.ror.idefix.zip;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class ZipHandlerTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldExtractZip() throws IOException {
        Path zipFile = createTestZip("test.zip", Map.of(
                "file1.txt", "content1",
                "subdir/file2.txt", "content2"
        ));

        Path targetDir = tempDir.resolve("extracted");
        new ZipHandler().extractZip(zipFile, targetDir);

        assertThat(targetDir.resolve("file1.txt")).exists().hasContent("content1");
        assertThat(targetDir.resolve("subdir/file2.txt")).exists().hasContent("content2");
    }

    @Test
    void shouldRepackageZipWithReplacement() throws IOException {
        Path originalZip = createTestZip("original.zip", Map.of(
                "data/SE_shared_data.xml", "<original/>",
                "data/other.xml", "<other/>"
        ));

        Path replacementFile = tempDir.resolve("replacement.xml");
        Files.writeString(replacementFile, "<replaced/>");

        Path outputZip = tempDir.resolve("output.zip");
        new ZipHandler().repackageZip(originalZip, outputZip, Map.of("_shared_data.xml", replacementFile));

        Map<String, String> entries = readZipEntries(outputZip);
        assertThat(entries)
                .containsEntry("data/SE_shared_data.xml", "<replaced/>")
                .containsEntry("data/other.xml", "<other/>");
    }

    @Test
    void shouldPreserveEntriesWhenNoReplacements() throws IOException {
        Path originalZip = createTestZip("original.zip", Map.of(
                "file1.txt", "content1",
                "file2.txt", "content2"
        ));

        Path outputZip = tempDir.resolve("output.zip");
        new ZipHandler().repackageZip(originalZip, outputZip, Map.of());

        Map<String, String> entries = readZipEntries(outputZip);
        assertThat(entries)
                .hasSize(2)
                .containsEntry("file1.txt", "content1")
                .containsEntry("file2.txt", "content2");
    }

    @Test
    void shouldAssembleAggregatedZipWithPrefixedProviderFiles() throws IOException {
        Path stopsXml = tempDir.resolve("registry_stops.xml");
        Files.writeString(stopsXml, "<stops/>");

        Path skaneZip = createTestZip("skane.zip", Map.of(
                "SE_shared_data.xml", "<skane-shared/>",
                "SE_line_1.xml", "<skane-line/>"
        ));
        Path vtZip = createTestZip("vt.zip", Map.of(
                "_shared_data.xml", "<vt-shared-to-include/>",
                "_stops.xml", "<vt-stops-to-exclude/>",
                "line_1.xml", "<vt-line-to-include/>"
        ));

        Path aggregatedZip = tempDir.resolve("aggregated.zip");
        new ZipHandler().assembleAggregatedZip(stopsXml, Map.of("skane", skaneZip, "vt", vtZip), aggregatedZip);

        Map<String, String> entries = readZipEntries(aggregatedZip);
        assertThat(entries)
                .containsEntry("_stops.xml", "<stops/>")
                .containsEntry("skane_SE_shared_data.xml", "<skane-shared/>")
                .containsEntry("skane_SE_line_1.xml", "<skane-line/>")
                // gets double _ but that's ok so we get a common prefix for all the providers files.
                .containsEntry("vt__shared_data.xml", "<vt-shared-to-include/>")
                .containsEntry("vt_line_1.xml", "<vt-line-to-include/>")
                .doesNotContainKey("vt_stops.xml");
    }

    @Test
    void shouldExcludeProviderStopsXmlFromAggregatedZip() throws IOException {
        Path stopsXml = tempDir.resolve("registry_stops.xml");
        Files.writeString(stopsXml, "<stops/>");

        Path providerZip = createTestZip("provider.zip", Map.of(
                "provider_stops.xml", "<provider-stops/>",
                "provider_shared_data.xml", "<provider-shared/>"
        ));

        Path aggregatedZip = tempDir.resolve("aggregated.zip");
        new ZipHandler().assembleAggregatedZip(stopsXml, Map.of("provider", providerZip), aggregatedZip);

        Map<String, String> entries = readZipEntries(aggregatedZip);
        assertThat(entries)
                .containsKey("_stops.xml")
                .containsKey("provider_provider_shared_data.xml")
                .doesNotContainKey("provider_provider_stops.xml");
    }

    private Path createTestZip(String name, Map<String, String> entries) throws IOException {
        Path zipFile = tempDir.resolve(name);
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                zos.putNextEntry(new ZipEntry(entry.getKey()));
                zos.write(entry.getValue().getBytes());
                zos.closeEntry();
            }
        }
        return zipFile;
    }

    private Map<String, String> readZipEntries(Path zipFile) throws IOException {
        Map<String, String> result = new java.util.HashMap<>();
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                result.put(entry.getName(), new String(zis.readAllBytes()));
                zis.closeEntry();
            }
        }
        return result;
    }
}
