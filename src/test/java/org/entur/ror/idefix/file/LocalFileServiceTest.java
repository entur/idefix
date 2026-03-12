package org.entur.ror.idefix.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class LocalFileServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldDeriveProviderFromFilename() {
        LocalFileService service = new LocalFileService(
                Path.of("/some/blekinge.zip"), Path.of("/reg.zip"), Path.of("/out.zip"));

        assertThat(service.getProviders()).containsExactly("blekinge");
    }

    @Test
    void shouldReturnTimetableZipDirectly() {
        Path timetableZip = Path.of("/some/timetable.zip");
        LocalFileService service = new LocalFileService(timetableZip, Path.of("/reg.zip"), Path.of("/out.zip"));

        Path result = service.getTimetableZip(tempDir, "anyprovider");

        assertThat(result).isEqualTo(timetableZip);
    }

    @Test
    void shouldReturnRegistryZipDirectly() {
        Path registryZip = Path.of("/some/registry.zip");
        LocalFileService service = new LocalFileService(Path.of("/tt.zip"), registryZip, Path.of("/out.zip"));

        Path result = service.getRegistryZip(tempDir);

        assertThat(result).isEqualTo(registryZip);
    }

    @Test
    void shouldCopyOutputToDestination() throws IOException {
        Path outputPath = tempDir.resolve("final-output.zip");
        LocalFileService service = new LocalFileService(Path.of("/tt.zip"), Path.of("/reg.zip"), outputPath);

        Path sourceZip = tempDir.resolve("source.zip");
        Files.writeString(sourceZip, "fake zip content");

        service.publishOutput(sourceZip, "anyprovider");

        assertThat(outputPath).exists();
        assertThat(Files.readString(outputPath)).isEqualTo("fake zip content");
    }
}
