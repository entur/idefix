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
