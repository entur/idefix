package org.entur.ror.idefix.config;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigTest {

    @Test
    void shouldConstructConfigWithProviderList() {
        Config config = new Config(
                "tt-bucket", List.of("blekinge", "btbuss", "gotland"),
                "reg-bucket", "reg-path",
                "out-bucket");

        assertThat(config.timetableProviders()).containsExactly("blekinge", "btbuss", "gotland");
    }

    @Test
    void shouldReturnTimetablePrefixWithTodaysDate() {
        Config config = new Config("b", List.of("p"), "r", "rp", "o");

        String expected = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "/timetable/";
        assertThat(config.timetablePrefix()).isEqualTo(expected);
    }

    @Test
    void shouldReturnOutputPrefixWithTodaysDate() {
        Config config = new Config("b", List.of("p"), "r", "rp", "o");

        String expected = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "/timetable/";
        assertThat(config.outputPrefix()).isEqualTo(expected);
    }
}
