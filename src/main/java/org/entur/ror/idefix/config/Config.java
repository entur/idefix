package org.entur.ror.idefix.config;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.util.Arrays.stream;

public record Config(
        String timetableBucket,
        List<String> timetableProviders,
        String registryBucket,
        String registryPath,
        String outputBucket
) {
    public static Config fromEnv() {
        String timetableBucket = fromEnv("TIMETABLE_BUCKET");
        List<String> timetableProviders = stream(fromEnv("TIMETABLE_PROVIDERS").split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        String registryBucket = fromEnv("REGISTRY_BUCKET");
        String registryPath = fromEnv("REGISTRY_PATH");
        String outputBucket = fromEnv("OUTPUT_BUCKET");
        return new Config(timetableBucket, timetableProviders, registryBucket, registryPath, outputBucket);
    }

    public String timetablePrefix() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "/timetable/";
    }

    public String outputPrefix() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "/timetable/";
    }

    private static String fromEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required environment variable " + name + " is not set");
        }
        return value;
    }
}
