package org.entur.ror.idefix.xml;

import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RegistryParserTest {

    @Test
    void shouldParseRegistryAndBuildLookupMap() throws URISyntaxException {
        Path registryDir = Paths.get(getClass().getClassLoader().getResource("registry").toURI());

        RegistryParser parser = new RegistryParser();
        Map<String, String> result = parser.parse(registryDir);

        assertThat(result)
                .isNotNull()
                .hasSize(3)
                .containsEntry("014:9022014072021001", "SSR:Quay:1001")
                .containsEntry("014:9022014072021002", "SSR:Quay:1001")
                .containsEntry("050:9025050030510001", "SSR:Quay:1002");
    }
}
