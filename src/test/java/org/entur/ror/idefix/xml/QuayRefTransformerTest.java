package org.entur.ror.idefix.xml;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

class QuayRefTransformerTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldReplaceMatchingQuayRefsAndCountMisses() throws Exception {
        Path input = Paths.get(getClass().getClassLoader().getResource("timetable/test_shared_data.xml").toURI());
        Path output = tempDir.resolve("output.xml");

        Map<String, String> lookupMap = Map.of(
                "014:9022014072021001", "SSR:Quay:1001",
                "050:9025050030510001", "SSR:Quay:1002"
        );

        QuayRefTransformer transformer = new QuayRefTransformer();
        QuayRefTransformer.TransformResult result = transformer.transform(input, output, lookupMap);

        assertThat(result.matches()).isEqualTo(2);
        assertThat(result.misses()).isEqualTo(1);

        String content = Files.readString(output);
        assertThat(content)
                .contains("SSR:Quay:1001")
                .contains("SSR:Quay:1002")
                .contains("SE:999:Quay:9999999999999999");
    }

    @Test
    void shouldPreserveXmlStructure() throws Exception {
        Path input = Paths.get(getClass().getClassLoader().getResource("timetable/test_shared_data.xml").toURI());
        Path output = tempDir.resolve("output.xml");

        Map<String, String> lookupMap = Map.of(
                "014:9022014072021001", "SSR:Quay:1001"
        );

        QuayRefTransformer transformer = new QuayRefTransformer();
        transformer.transform(input, output, lookupMap);

        String content = Files.readString(output);
        assertThat(content)
                .contains("PublicationDelivery")
                .contains("ScheduledStopPoint")
                .contains("QuayRef");
    }

    @Test
    void shouldHandleNoMatches() throws Exception {
        Path input = Paths.get(getClass().getClassLoader().getResource("timetable/test_shared_data.xml").toURI());
        Path output = tempDir.resolve("output.xml");

        Map<String, String> lookupMap = emptyMap();

        QuayRefTransformer transformer = new QuayRefTransformer();
        QuayRefTransformer.TransformResult result = transformer.transform(input, output, lookupMap);

        assertThat(result.matches()).isZero();
        assertThat(result.misses()).isEqualTo(3);
    }
}
