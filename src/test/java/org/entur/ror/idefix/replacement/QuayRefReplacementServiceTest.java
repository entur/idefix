package org.entur.ror.idefix.replacement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class QuayRefReplacementServiceTest {

    private static final Path SHARED_DATA = Path.of("src/test/resources/timetable/test_shared_data.xml");
    private static final Path REGISTRY = Path.of("src/test/resources/registry/test_stop_places.xml");

    @TempDir
    Path tempDir;

    @Test
    void shouldWriteOutputInSameLayoutAsInput() throws Exception {
        Path timetables = tempDir.resolve("timetables");
        Path provider = Files.createDirectories(timetables.resolve("skane"));
        Files.copy(SHARED_DATA, provider.resolve("test_shared_data.xml"));
        Files.writeString(provider.resolve("line_1.xml"), "<line/>");
        Files.writeString(provider.resolve("skane_stops.xml"), "<stops/>");
        Path output = tempDir.resolve("output");

        Map<String, QuayRefReplacementResult> results =
                new QuayRefReplacementService().run(timetables, REGISTRY, output);

        assertThat(results).containsOnlyKeys("skane");
        assertThat(results.get("skane").matches()).isGreaterThan(0);
        assertThat(fileNames(output)).containsExactly("skane");
        assertThat(fileNames(output.resolve("skane")))
                .containsExactlyInAnyOrder("test_shared_data.xml", "line_1.xml", "skane_stops.xml");
        assertThat(output.resolve("skane/line_1.xml")).hasContent("<line/>");
        assertThat(output.resolve("skane/skane_stops.xml")).hasContent("<stops/>");
    }

    @Test
    void shouldProcessMultipleProvidersAndSkipFailingAndEmpty() throws Exception {
        Path timetables = tempDir.resolve("timetables");
        for (String name : List.of("providerA", "providerB")) {
            Files.copy(SHARED_DATA, Files.createDirectories(timetables.resolve(name)).resolve("x_shared_data.xml"));
        }
        Files.writeString(Files.createDirectories(timetables.resolve("noShared")).resolve("a.xml"), "<a/>");
        Files.createDirectories(timetables.resolve("empty"));

        Map<String, QuayRefReplacementResult> results =
                new QuayRefReplacementService().run(timetables, REGISTRY, tempDir.resolve("out"));

        assertThat(results).containsOnlyKeys("providerA", "providerB");
    }

    private List<String> fileNames(Path dir) throws IOException {
        try (Stream<Path> files = Files.list(dir)) {
            return files.map(p -> p.getFileName().toString()).toList();
        }
    }
}
