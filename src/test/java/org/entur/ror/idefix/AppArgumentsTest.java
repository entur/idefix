package org.entur.ror.idefix;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppArgumentsTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldReportAllMissingArguments() {
        assertThatThrownBy(() -> AppArguments.parse(new String[]{}))
                .isInstanceOf(AppArguments.InvalidArgumentsException.class)
                .hasMessageContaining("<timetables dir>")
                .hasMessageContaining("<registry>")
                .hasMessageContaining("<output dir>");
    }

    @Test
    void shouldReportMissingOutput() {
        assertThatThrownBy(() -> AppArguments.parse(new String[]{"a", "b"}))
                .hasMessage("Missing argument: <output dir>");
    }

    @Test
    void shouldRejectTooManyArguments() {
        assertThatThrownBy(() -> AppArguments.parse(new String[]{"a", "b", "c", "d"}))
                .hasMessageContaining("Too many arguments");
    }

    @Test
    void shouldRejectInvalidPaths() throws Exception {
        Path outFile = Files.writeString(tempDir.resolve("out.txt"), "");

        assertThatThrownBy(() -> AppArguments.parse(new String[]{
                tempDir.resolve("missing").toString(), "tiamat-export.xml", outFile.toString()}))
                .hasMessageContaining("<timetables dir> is not an existing directory")
                .hasMessageContaining("<registry> is not an existing file")
                .hasMessageContaining("<output dir> exists but is not a directory");
    }

    @Test
    void shouldRejectRegistryDirectory() {
        assertThatThrownBy(() -> AppArguments.parse(new String[]{
                tempDir.toString(), tempDir.toString(), tempDir.resolve("out").toString()}))
                .hasMessage("<registry> is not an existing file: " + tempDir);
    }

    @Test
    void shouldAcceptValidArguments() throws Exception {
        Path registry = Files.writeString(tempDir.resolve("registry.xml"), "<r/>");
        Path output = tempDir.resolve("out");

        AppArguments arguments = AppArguments.parse(new String[]{tempDir.toString(), registry.toString(), output.toString()});

        assertThat(arguments).isEqualTo(new AppArguments(tempDir, registry, output));
    }
}
